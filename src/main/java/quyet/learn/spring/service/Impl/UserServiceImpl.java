package quyet.learn.spring.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRespository userRespository;

    @Override
    public Users createUser(UserCreationRequest userRequest) {
        Users user = new Users();
        if(userRespository.existsByUsername(userRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setDateOfBirth(userRequest.getDob());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());

        return userRespository.save(user);

    }

    @Override
    public Users updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        Users user = getUser(userId);
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());
        user.setDateOfBirth(userUpdateRequest.getDob());
        user.setPassword(userUpdateRequest.getPassword());
        return userRespository.save(user);
    }

    @Override
    public Users deleteUser(String userId) {
        Users user = getUser(userId);
        userRespository.deleteById(userId);
        return user;
    }

    @Override
    public Users getUser(String userId) {
        return userRespository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found"));
    }

    @Override
    public List<Users> getAllUsers() {
        return userRespository.findAll();
    }
}
