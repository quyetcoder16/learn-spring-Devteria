package quyet.learn.spring.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.dto.response.role.RoleResponse;
import quyet.learn.spring.entity.Role;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    Set<RoleResponse> roles;
}
