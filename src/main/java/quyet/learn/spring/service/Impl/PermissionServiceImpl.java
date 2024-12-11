package quyet.learn.spring.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.permission.PermissionRequest;
import quyet.learn.spring.dto.response.permission.PermissionResponse;
import quyet.learn.spring.entity.Permission;
import quyet.learn.spring.mapper.PermissionMapper;
import quyet.learn.spring.resporitory.PermissionRespository;
import quyet.learn.spring.service.PermissionService;

import java.util.List;

@Slf4j // Cung cấp logger để ghi lại log trong quá trình chạy.
@Service // Annotate class là một service trong Spring.
@RequiredArgsConstructor // Tự động tạo constructor với các field có `final`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Tự động thiết lập các field private và final.
public class PermissionServiceImpl implements PermissionService {

    PermissionRespository permissionRespository;
    PermissionMapper permissionMapper;

    @Override
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRespository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> findAll() {
        var permissions = permissionRespository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public void delete(String permission) {
        permissionRespository.deleteById(permission);
    }
}
