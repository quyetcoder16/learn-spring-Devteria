package quyet.learn.spring.mapper;

import org.mapstruct.Mapper;
import quyet.learn.spring.dto.request.permission.PermissionRequest;
import quyet.learn.spring.dto.response.permission.PermissionResponse;
import quyet.learn.spring.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);

    PermissionResponse toPermissionResponse(Permission permission);


}
