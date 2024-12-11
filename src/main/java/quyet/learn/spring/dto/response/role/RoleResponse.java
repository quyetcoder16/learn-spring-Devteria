package quyet.learn.spring.dto.response.role;

import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.dto.response.permission.PermissionResponse;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}
