package quyet.learn.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import quyet.learn.spring.dto.request.role.RoleRequest;
import quyet.learn.spring.dto.response.permission.PermissionResponse;
import quyet.learn.spring.dto.response.role.RoleResponse;
import quyet.learn.spring.entity.Permission;
import quyet.learn.spring.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);


}
