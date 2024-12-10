package quyet.learn.spring.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.mapper.UserMapper;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.UserService;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRespository userRespository;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Users createUser(UserCreationRequest userRequest) {

        if (userRespository.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users user = userMapper.toUsers(userRequest);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        return userRespository.save(user);

    }

    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        Users user = userRespository.findById(userId).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        });
        userMapper.updateUsers(user, userUpdateRequest);
        return userMapper.toUserResponse(userRespository.save(user));
    }

    @Override
    public void deleteUser(String userId) {
        userRespository.deleteById(userId);
    }


    @Override
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(userRespository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @Override
    public List<Users> getAllUsers() {
        return userRespository.findAll();
    }
}
