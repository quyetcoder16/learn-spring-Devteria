package quyet.learn.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import quyet.learn.spring.dto.request.permission.PermissionRequest;
import quyet.learn.spring.dto.response.permission.PermissionResponse;

@Service
public interface PermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> findAll();

    void delete(String permission);
}
