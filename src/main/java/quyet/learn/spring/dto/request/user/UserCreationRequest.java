package quyet.learn.spring.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.validator.DobConstraint;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 5, message = "USERNAME_INVALID")
    private String username;

    @Size(min = 7, message = "INVALID_PASSWORD")
    private String password;
    private String firstName;
    private String lastName;

    @DobConstraint(min = 16,message = "INVALID_DOB")
    private LocalDate dob;

}
