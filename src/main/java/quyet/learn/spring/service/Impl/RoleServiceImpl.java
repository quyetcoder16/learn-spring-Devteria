package quyet.learn.spring.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.role.RoleRequest;
import quyet.learn.spring.dto.response.role.RoleResponse;
import quyet.learn.spring.mapper.RoleMapper;
import quyet.learn.spring.resporitory.PermissionRespository;
import quyet.learn.spring.resporitory.RoleRespository;
import quyet.learn.spring.service.RoleService;

import java.util.HashSet;
import java.util.List;


@Slf4j // Cung cấp logger để ghi lại log trong quá trình chạy.
@Service // Annotate class là một service trong Spring.
@RequiredArgsConstructor // Tự động tạo constructor với các field có `final`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Tự động thiết lập các field private và final.
public class RoleServiceImpl implements RoleService {
    RoleRespository roleRespository;
    PermissionRespository permissionRespository;
    RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleRequest roleRequest) {
        var role = roleMapper.toRole(roleRequest);
        var permission = permissionRespository.findAllById(roleRequest.getPermissions());
        role.setPermissions(new HashSet<>(permission));
        role = roleRespository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRespository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse).toList();
    }

    @Override
    public void deleteRole(String roleId) {
        roleRespository.deleteById(roleId);
    }
}
