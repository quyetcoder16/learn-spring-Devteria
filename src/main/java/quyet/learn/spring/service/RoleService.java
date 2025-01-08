package quyet.learn.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import quyet.learn.spring.dto.request.role.RoleRequest;
import quyet.learn.spring.dto.response.role.RoleResponse;

@Service
public interface RoleService {
    RoleResponse createRole(RoleRequest roleRequest);

    List<RoleResponse> getAllRoles();

    void deleteRole(String roleId);
}
