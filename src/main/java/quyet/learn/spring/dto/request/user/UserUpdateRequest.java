package quyet.learn.spring.dto.request.user;

import java.time.LocalDate;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.validator.DobConstraint;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    private String password;
    private String firstName;
    private String lastName;

    @DobConstraint(min = 2, message = "INVALID_DOB")
    private LocalDate dob;

    private List<String> listRoles;
}
