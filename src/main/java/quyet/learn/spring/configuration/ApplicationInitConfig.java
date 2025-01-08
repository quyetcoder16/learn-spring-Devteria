package quyet.learn.spring.configuration;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.enums.Role;
import quyet.learn.spring.resporitory.UserRespository;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRespository userRespository) {
        return args -> {
            if (userRespository.findByUsername("admin").isEmpty()) {
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());
                Users user = Users.builder()
                        .username("admin")
                        // .roles(roles)
                        .password(passwordEncoder.encode("admin"))
                        .build();

                userRespository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
