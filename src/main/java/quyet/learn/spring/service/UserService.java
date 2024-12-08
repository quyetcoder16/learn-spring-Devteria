package quyet.learn.spring.service;

import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;

import java.util.List;

@Service
public interface UserService {
    public Users createUser(UserCreationRequest user);
    public UserResponse updateUser(String userId,UserUpdateRequest user);
    public void deleteUser(String userId);
    public UserResponse getUser(String userId);
    public List<Users> getAllUsers();

}
