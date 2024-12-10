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
 * AuthServiceImpl: Implementation của AuthService, chịu trách nhiệm xử lý xác thực và kiểm tra token JWT.
 */
@Slf4j // Cung cấp logger để ghi lại log trong quá trình chạy.
@Service // Annotate class là một service trong Spring.
@RequiredArgsConstructor // Tự động tạo constructor với các field có `final`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Tự động thiết lập các field private và final.
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRespository userRespository; // Repository để truy xuất dữ liệu người dùng từ database.

    @NonFinal // Biến này không cần phải final vì được inject từ @Value.
    @Value("${jwt.singerKey}") // Lấy giá trị từ file cấu hình application.properties hoặc application.yml.
    protected String SIGNER_KEY;

    /**
     * Phương thức xử lý xác thực người dùng.
     *
     * @param authenticationRequest yêu cầu chứa thông tin username và password.
     * @return AuthenticationResponse chứa token JWT và trạng thái xác thực.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        // Lấy thông tin người dùng từ database.
        var user = userRespository.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Nếu không tìm thấy user, ném lỗi.
        });

        // So sánh mật khẩu được mã hóa.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10); // Sử dụng BCrypt với strength 10.
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Nếu sai mật khẩu, ném lỗi.
        }

        // Tạo token JWT sau khi xác thực thành công.
        var token = gennerateToken(user);

        // Trả về phản hồi chứa token và trạng thái xác thực.
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    /**
     * Phương thức kiểm tra tính hợp lệ của token JWT.
     *
     * @param introspectRequest yêu cầu chứa token JWT.
     * @return IntrospectResponse chứa trạng thái hợp lệ của token.
     * @throws JOSEException  nếu lỗi trong việc xử lý JWT.
     * @throws ParseException nếu lỗi trong việc phân tích token.
     */
    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken(); // Lấy token từ yêu cầu.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes()); // Tạo verifier với SIGNER_KEY.
        SignedJWT signedJWT = SignedJWT.parse(token); // Phân tích token thành đối tượng SignedJWT.

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn của token.

        // Kiểm tra tính hợp lệ của chữ ký và thời gian hết hạn.
        var verified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expiration.after(new Date())) // Token hợp lệ nếu có chữ ký đúng và chưa hết hạn.
                .build();
    }

    /**
     * Phương thức tạo token JWT.
     *
     * @param user nguoi dung.
     * @return chuỗi token JWT.
     */
    private String gennerateToken(Users user) {
        // Tạo header của token với thuật toán HS256.
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        // Tạo payload của token, bao gồm các claims.
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Đặt subject là username.
                .issuer("quyet.learn.spring") // Đặt issuer là hệ thống hiện tại.
                .issueTime(new Date()) // Thời gian phát hành token.
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli() // Token hết hạn sau 1 giờ.
                ))
                .claim("userId", user.getId()) // Thêm thông tin bổ sung userId.
                .claim("scope",buildScope(user))
                .build();

        // Chuyển payload sang đối tượng JWSObject.
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            // Ký token với SIGNER_KEY.
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize(); // Trả về token ở dạng chuỗi.
        } catch (JOSEException e) {
            log.error("Can't create token", e); // Ghi log lỗi nếu xảy ra lỗi.
            throw new RuntimeException(e); // Ném RuntimeException.
        }
    }

    private String buildScope(Users user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add(role);
            });
        }
        return scopeJoiner.toString();
    }
}