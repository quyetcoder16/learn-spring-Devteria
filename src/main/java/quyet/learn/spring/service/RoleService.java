package quyet.learn.spring.service;

import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.role.RoleRequest;
import quyet.learn.spring.dto.response.role.RoleResponse;

import java.util.List;

@Service
public interface RoleService {
    RoleResponse createRole(RoleRequest roleRequest);

    List<RoleResponse> getAllRoles();

    void deleteRole(String roleId);
}
