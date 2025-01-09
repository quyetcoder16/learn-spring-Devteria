package quyet.learn.spring.service.Impl;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
import quyet.learn.spring.dto.request.auth.AuthenticationRequest;
import quyet.learn.spring.dto.request.auth.IntrospectRequest;
import quyet.learn.spring.dto.request.auth.LogoutRequest;
import quyet.learn.spring.dto.request.auth.RefreshRequest;
import quyet.learn.spring.dto.response.auth.AuthenticationResponse;
import quyet.learn.spring.dto.response.auth.IntrospectResponse;
import quyet.learn.spring.entity.InvalidatedToken;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.resporitory.InvalidatedTokenRepository;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.AuthService;

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

    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal // Không cần final vì giá trị được inject từ @Value.
    @Value("${jwt.singerKey}") // Lấy khóa ký JWT từ cấu hình.
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected Long REFRESHABLE_DURATION;

    /**
     * Xác thực thông tin người dùng dựa trên username và password.
     *
     * @param authenticationRequest Yêu cầu chứa thông tin xác thực.
     * @return AuthenticationResponse Phản hồi chứa token JWT và trạng thái xác thực.
     */
    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        log.info("signer key: {}", SIGNER_KEY);
        // Tìm người dùng theo username từ cơ sở dữ liệu.
        var user = userRespository
                .findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> {
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
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
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

        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    /**
     * Phương thức logout thực hiện đăng xuất và vô hiệu hóa token.
     *
     * @param logoutRequest Yêu cầu đăng xuất.
     * @throws ParseException Nếu token không thể phân tích cú pháp.
     * @throws JOSEException  Nếu token không thể kiểm tra chữ ký.
     */
    @Override
    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        try {
            // Xác thực và lấy thông tin từ token.
            var signedToken = verifyToken(logoutRequest.getToken(), true);
            String jit = signedToken.getJWTClaimsSet().getJWTID(); // Lấy ID của token.
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn.

            // Tạo đối tượng InvalidatedToken để lưu token đã vô hiệu hóa.
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            // Lưu token vào cơ sở dữ liệu.
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token already expired"); // Ghi log nếu token đã hết hạn.
        }
    }

    /**
     * Phương thức refreshToken tạo token mới dựa trên token hiện tại.
     *
     * @param refreshRequest Yêu cầu làm mới token.
     * @return Đối tượng AuthenticationResponse chứa token mới.
     */
    @Override
    public AuthenticationResponse refreshToken(RefreshRequest refreshRequest) throws ParseException, JOSEException {
        // Xác thực token hiện tại và kiểm tra quyền làm mới.
        var signedJWT = verifyToken(refreshRequest.getToken(), true);

        // Lấy thông tin ID và thời gian hết hạn từ token.
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Kiểm tra nếu token đã hết hạn thì không thêm vào InvalidatedToken
        if (expiryTime.after(new Date())) {
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);
        }

        // Lấy thông tin người dùng từ token.
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user =
                userRespository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Tạo token mới.
        var token = gennerateToken(user);

        // Trả về phản hồi với token mới.
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    /**
     * Phương thức verifyToken kiểm tra tính hợp lệ của token JWT.
     *
     * @param token     Token cần kiểm tra.
     * @param isRefresh Đánh dấu token được dùng để làm mới.
     * @return Đối tượng SignedJWT sau khi xác minh.
     */
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        // Tạo verifier để kiểm tra chữ ký token.
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Xác định thời gian hết hạn của token dựa trên isRefresh.
        Date expiration = isRefresh
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime()
                        .toInstant()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // Kiểm tra chữ ký và hạn token.
        var verified = signedJWT.verify(verifier);
        if (!(verified && expiration.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra token có bị vô hiệu hóa hay không.
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return signedJWT;
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
                        Instant.now()
                                .plus(VALID_DURATION, ChronoUnit.SECONDS)
                                .toEpochMilli() // Token hết hạn sau 1 giờ.
                        ))
                .jwtID(UUID.randomUUID().toString())
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
