package quyet.learn.spring.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    Users createUser(@RequestBody @Valid UserCreationRequest request) {
        return userService.createUser(request);
    }


    @GetMapping
    List<Users> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    Users getUser(@PathVariable("userId") String userId) {
        return userService.getUser(userId);
    }

    @PutMapping("/{userId}")
    Users updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return "User has been deleted";
    }
}
