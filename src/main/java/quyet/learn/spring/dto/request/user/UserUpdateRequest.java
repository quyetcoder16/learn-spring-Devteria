package quyet.learn.spring.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.validator.DobConstraint;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    private String password;
    private String firstName;
    private String lastName;
    @DobConstraint(min = 2,message = "INVALID_DOB")
    private LocalDate dob;
    private List<String> listRoles;

}
