package quyet.learn.spring.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import quyet.learn.spring.dto.request.role.RoleRequest;
import quyet.learn.spring.dto.response.ApiResponse;
import quyet.learn.spring.dto.response.role.RoleResponse;
import quyet.learn.spring.service.RoleService;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {

    RoleService roleService;

    @PostMapping("")
    ApiResponse<RoleResponse> createPermission(@RequestBody RoleRequest roleRequest) {
        return ApiResponse.<RoleResponse>builder()
                .data(roleService.createRole(roleRequest))
                .build();
    }

    @GetMapping("")
    ApiResponse<List<RoleResponse>> getAllRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .data(roleService.getAllRoles())
                .build();
    }

    @DeleteMapping("/{role}")
    ApiResponse<Void> deleteRole(@PathVariable("role") String role) {
        roleService.deleteRole(role);
        return ApiResponse.<Void>builder().build();
    }
}
