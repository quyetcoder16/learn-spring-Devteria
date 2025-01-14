package quyet.learn.spring.service;

import java.util.List;

import org.springframework.stereotype.Service;

import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;

@Service
public interface UserService {
    public UserResponse createUser(UserCreationRequest user);

    public UserResponse updateUser(String userId, UserUpdateRequest user);

    public void deleteUser(String userId);

    public UserResponse getUser(String userId);

    public List<UserResponse> getAllUsers();

    public UserResponse getMyÌnfo();
}
