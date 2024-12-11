package quyet.learn.spring.service.Impl;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import quyet.learn.spring.dto.request.auth.AuthenticationRequest;
import quyet.learn.spring.dto.request.auth.IntrospectRequest;
import quyet.learn.spring.dto.response.auth.AuthenticationResponse;
import quyet.learn.spring.dto.response.auth.IntrospectResponse;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.AuthService;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

/**
 * AuthServiceImpl: Triển khai AuthService, chịu trách nhiệm xử lý xác thực người dùng và token JWT.
 */
@Slf4j // Tích hợp logger để ghi log trong quá trình xử lý.
@Service // Đánh dấu class là một service trong Spring.
@RequiredArgsConstructor // Tự động tạo constructor với các field có `final`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Đặt các field là private và final theo mặc định.
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRespository userRespository; // Repository để truy vấn thông tin người dùng từ cơ sở dữ liệu.

    @NonFinal // Không cần final vì giá trị được inject từ @Value.
    @Value("${jwt.singerKey}") // Lấy khóa ký JWT từ cấu hình.
    protected String SIGNER_KEY;

    /**
     * Xác thực thông tin người dùng dựa trên username và password.
     *
     * @param authenticationRequest Yêu cầu chứa thông tin xác thực.
     * @return AuthenticationResponse Phản hồi chứa token JWT và trạng thái xác thực.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        // Tìm người dùng theo username từ cơ sở dữ liệu.
        var user = userRespository.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Ném lỗi nếu không tìm thấy người dùng.
        });

        // Kiểm tra mật khẩu đã được mã hóa bằng BCrypt.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10); // Sử dụng BCrypt với độ mạnh 10.
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Ném lỗi nếu mật khẩu không khớp.
        }

        // Tạo token JWT khi xác thực thành công.
        var token = gennerateToken(user);

        // Trả về phản hồi chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    /**
     * Kiểm tra tính hợp lệ của token JWT.
     *
     * @param introspectRequest Yêu cầu chứa token cần kiểm tra.
     * @return IntrospectResponse Phản hồi trạng thái hợp lệ của token.
     * @throws JOSEException  Lỗi xử lý chữ ký JWT.
     * @throws ParseException Lỗi phân tích cú pháp token.
     */
    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken(); // Lấy token từ yêu cầu.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes()); // Tạo verifier để kiểm tra chữ ký.
        SignedJWT signedJWT = SignedJWT.parse(token); // Phân tích cú pháp token.

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn của token.

        // Kiểm tra chữ ký hợp lệ và token chưa hết hạn.
        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expiration.after(new Date())) // Token hợp lệ nếu có chữ ký đúng và chưa hết hạn.
                .build();
    }

    /**
     * Tạo token JWT cho người dùng.
     *
     * @param user Thông tin người dùng.
     * @return Chuỗi token JWT.
     */
    private String gennerateToken(Users user) {
        // Tạo header của token với thuật toán HS256.
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        // Tạo payload của token, bao gồm các claims.
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Subject là username của người dùng.
                .issuer("quyet.learn.spring") // Định danh của hệ thống phát hành token.
                .issueTime(new Date()) // Thời gian phát hành token.
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli() // Token hết hạn sau 1 giờ.
                ))
                .claim("userId", user.getId()) // Thêm thông tin bổ sung userId.
                .claim("scope", buildScope(user)) // Thêm thông tin quyền hạn (scope).
                .build();

        try {
            // Ký token với khóa bí mật.
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            signedJWT.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return signedJWT.serialize(); // Trả về token dưới dạng chuỗi.
        } catch (JOSEException e) {
            log.error("Can't create token", e); // Ghi log lỗi nếu xảy ra vấn đề khi ký.
            throw new RuntimeException(e); // Ném lỗi runtime nếu ký thất bại.
        }
    }

    /**
     * Xây dựng thông tin quyền hạn (scope) của người dùng.
     *
     * @param user Thông tin người dùng.
     * @return Chuỗi scope chứa các quyền và vai trò của người dùng.
     */
    private String buildScope(Users user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add("ROLE_" + role.getName()); // Thêm vai trò vào scope.
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        scopeJoiner.add(permission.getName()); // Thêm quyền vào scope.
                    });
                }
            });
        }
        return scopeJoiner.toString();
    }
}
