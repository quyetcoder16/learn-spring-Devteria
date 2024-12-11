package quyet.learn.spring.service;

import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.permission.PermissionRequest;
import quyet.learn.spring.dto.response.permission.PermissionResponse;

import java.util.List;

@Service
public interface PermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> findAll();
    void delete(String permission);
}
