package quyet.learn.spring.dto.response.role;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;
import quyet.learn.spring.dto.response.permission.PermissionResponse;

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
