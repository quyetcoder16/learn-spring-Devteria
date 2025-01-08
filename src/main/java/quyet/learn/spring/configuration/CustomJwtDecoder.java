package quyet.learn.spring.configuration;

import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;

import quyet.learn.spring.dto.request.auth.IntrospectRequest;
import quyet.learn.spring.service.AuthService;

/**
 * CustomJwtDecoder là một lớp tùy chỉnh dùng để giải mã (decode) và xác minh token JWT.
 * Nó được cài đặt giao diện JwtDecoder của Spring Security.
 */
@Component
public class CustomJwtDecoder implements JwtDecoder {

    // Key dùng để ký JWT, được cấu hình trong tệp `application.properties`.
    @Value("${jwt.singerKey}")
    private String signerKey;

    // Dịch vụ xác thực được sử dụng để kiểm tra tính hợp lệ của token với máy chủ từ xa.
    @Autowired
    private AuthService authService;

    // Decoder NimbusJwtDecoder được sử dụng để thực hiện việc decode token sau khi xác minh.
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    /**
     * Phương thức chính để giải mã và xác minh token JWT.
     *
     * @param token JWT token cần decode.
     * @return Đối tượng Jwt nếu token hợp lệ.
     * @throws JwtException nếu token không hợp lệ hoặc không thể decode.
     */
    @Override
    public Jwt decode(String token) throws JwtException {
        // Bước 1: Kiểm tra token với máy chủ xác thực từ xa
        try {
            var response = authService.introspect(IntrospectRequest.builder()
                    .token(token) // Gửi token đến endpoint introspect để kiểm tra tính hợp lệ
                    .build());

            // Nếu máy chủ trả về không hợp lệ, ném ngoại lệ JwtException
            if (!response.isValid()) throw new JwtException("Token invalid");
        } catch (JOSEException | ParseException e) {
            // Xử lý các lỗi liên quan đến JWT hoặc parse dữ liệu
            throw new JwtException(e.getMessage());
        }

        // Bước 2: Khởi tạo NimbusJwtDecoder nếu chưa được khởi tạo
        if (Objects.isNull(nimbusJwtDecoder)) {
            // Tạo SecretKeySpec từ khóa ký JWT và thuật toán HS256
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS256");

            // Khởi tạo NimbusJwtDecoder với secret key và thuật toán ký HS256
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256) // Xác định thuật toán MAC là HS256
                    .build();
        }

        // Bước 3: Giải mã token bằng NimbusJwtDecoder
        return nimbusJwtDecoder.decode(token);
    }
}
