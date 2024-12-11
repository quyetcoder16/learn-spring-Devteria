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

@Configuration // Đánh dấu class này là class cấu hình trong Spring.
@EnableWebSecurity // Kích hoạt cấu hình bảo mật Spring Security.
@EnableMethodSecurity // config to enable method secirity PreAuthorize and PostAuthorize
public class SecurityConfig {

    @Value("${jwt.singerKey}") // Lấy giá trị khóa bí mật từ file cấu hình.
    private String singerKey;

    // Các endpoint không yêu cầu xác thực.
    private final String[] PUBLIC_ENDPOINTS = {"/auth/token", "/auth/introspect", "/users"};

    /**
     * Cấu hình chuỗi filter bảo mật.
     *
     * @param httpSecurity đối tượng HttpSecurity để cấu hình bảo mật.
     * @return SecurityFilterChain đối tượng chuỗi filter.
     * @throws Exception nếu xảy ra lỗi khi cấu hình.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Cấu hình xác thực các request HTTP.
        httpSecurity.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // Cho phép POST đến các endpoint công khai.
                        // .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name()) // Chỉ admin được phép GET /users.
                        .anyRequest().authenticated() // Các request khác yêu cầu xác thực.
        );

        // Cấu hình bảo mật dựa trên JWT.
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(jwtDecoder()) // Sử dụng jwtDecoder để giải mã token.
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())) // Sử dụng converter để ánh xạ quyền từ token.
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        );

        // Vô hiệu hóa bảo vệ CSRF (thích hợp cho API REST).
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build(); // Trả về chuỗi filter bảo mật đã cấu hình.
    }

    /**
     * Cấu hình chuyển đổi thông tin xác thực từ JWT.
     *
     * @return JwtAuthenticationConverter đối tượng chuyển đổi.
     */
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

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
        // Tạo khóa bí mật từ singerKey với thuật toán HmacSHA256.
        SecretKeySpec secretKeySpec = new SecretKeySpec(singerKey.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKeySpec) // Tạo JwtDecoder với khóa bí mật.
                .macAlgorithm(MacAlgorithm.HS256) // Sử dụng thuật toán HS256.
                .build();
    }

    /**
     * Cấu hình mã hóa mật khẩu bằng BCrypt.
     *
     * @return PasswordEncoder đối tượng mã hóa mật khẩu.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // Sử dụng BCrypt với strength = 10.
    }
}