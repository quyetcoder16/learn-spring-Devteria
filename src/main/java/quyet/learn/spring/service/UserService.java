package quyet.learn.spring.service;

import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.entity.Users;

import java.util.List;

@Service
public interface UserService {
    public Users createUser(UserCreationRequest user);
    public Users updateUser(String userId,UserUpdateRequest user);
    public Users deleteUser(String userId);
    public Users getUser(String userId);
    public List<Users> getAllUsers();

}
