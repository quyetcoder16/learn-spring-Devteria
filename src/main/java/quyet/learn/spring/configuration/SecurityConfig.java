package quyet.learn.spring.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import quyet.learn.spring.enums.Role;

import javax.crypto.spec.SecretKeySpec;

@Configuration // Đánh dấu class này là class cấu hình Spring.
@EnableWebSecurity // Kích hoạt cấu hình bảo mật Spring Security.
@EnableMethodSecurity // Kích hoạt bảo mật phương thức, hỗ trợ @PreAuthorize và @PostAuthorize.
public class SecurityConfig {

    @Value("${jwt.singerKey}") // Lấy giá trị khóa bí mật từ file cấu hình.
    private String singerKey;

    // Các endpoint không yêu cầu xác thực.
    private final String[] PUBLIC_ENDPOINTS = {"/auth/token", "/auth/introspect", "/users"};

    /**
     * Cấu hình chuỗi filter bảo mật.
     *
     * @param httpSecurity đối tượng HttpSecurity để cấu hình bảo mật.
     * @return SecurityFilterChain đối tượng chuỗi filter bảo mật.
     * @throws Exception nếu xảy ra lỗi khi cấu hình.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(request ->
                        request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // Cho phép truy cập các endpoint công khai với POST.
                                // .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name()) // Chỉ admin được phép GET /users.
                                .anyRequest().authenticated() // Yêu cầu xác thực cho các endpoint khác.
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                        jwtConfigurer.decoder(jwtDecoder()) // Sử dụng jwtDecoder để giải mã JWT.
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter())) // Cấu hình ánh xạ quyền từ token.
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()) // Xử lý lỗi xác thực JWT.
                )
                .csrf(AbstractHttpConfigurer::disable); // Vô hiệu hóa CSRF (phù hợp với API REST).

        return httpSecurity.build(); // Trả về chuỗi filter bảo mật đã cấu hình.
    }

    /**
     * Cấu hình chuyển đổi thông tin xác thực từ JWT.
     *
     * @return JwtAuthenticationConverter đối tượng chuyển đổi xác thực JWT.
     */
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // Bỏ tiền tố mặc định "ROLE_" để khớp với dữ liệu trong JWT.

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter); // Sử dụng converter để ánh xạ quyền.

        return jwtAuthenticationConverter;
    }

    /**
     * Cấu hình giải mã JWT.
     *
     * @return JwtDecoder đối tượng giải mã JWT.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(singerKey.getBytes(), "HmacSHA256"); // Tạo khóa bí mật từ singerKey.
        return NimbusJwtDecoder.withSecretKey(secretKeySpec) // Sử dụng NimbusJwtDecoder với khóa bí mật.
                .macAlgorithm(MacAlgorithm.HS256) // Thuật toán giải mã HS256.
                .build();
    }

    /**
     * Cấu hình mã hóa mật khẩu bằng BCrypt.
     *
     * @return PasswordEncoder đối tượng mã hóa mật khẩu.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Mã hóa mật khẩu với độ mạnh 10.
    }
}
