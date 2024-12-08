package quyet.learn.spring.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
}
