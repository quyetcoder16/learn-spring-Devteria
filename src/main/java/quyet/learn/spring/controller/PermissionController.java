package quyet.learn.spring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import quyet.learn.spring.dto.request.permission.PermissionRequest;
import quyet.learn.spring.dto.response.ApiResponse;
import quyet.learn.spring.dto.response.permission.PermissionResponse;
import quyet.learn.spring.service.PermissionService;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    @PostMapping("")
    ApiResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest permissionRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .data(permissionService.create(permissionRequest))
                .build();
    }

    @GetMapping("")
    ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .data(permissionService.findAll())
                .build();
    }

    @DeleteMapping("/{permission}")
    ApiResponse<Void> deletePermission(@PathVariable("permission") String permission) {
        permissionService.delete(permission);
        return ApiResponse.<Void>builder().build();
    }
}
