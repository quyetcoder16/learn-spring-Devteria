package quyet.learn.spring.service.Impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.enums.Role;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.mapper.UserMapper;
import quyet.learn.spring.resporitory.RoleRespository;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.UserService;

@Service
@RequiredArgsConstructor // Tự động tạo constructor chứa các trường được đánh dấu là `final`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Đặt mặc định các trường là `private` và `final`.
@Slf4j // Kích hoạt tính năng ghi log.
public class UserServiceImpl implements UserService {

    // Khai báo các repository để thao tác với cơ sở dữ liệu.
    UserRespository userRespository;
    RoleRespository roleRespository;

    // Mapper dùng để chuyển đổi giữa các DTO và entity.
    UserMapper userMapper;

    // Đối tượng dùng để mã hóa mật khẩu.
    PasswordEncoder passwordEncoder;

    /**
     * Tạo một người dùng mới.
     *
     * @param userRequest thông tin người dùng từ yêu cầu.
     * @return Trả về thông tin người dùng vừa được tạo (UserResponse).
     */
    @Override
    public UserResponse createUser(UserCreationRequest userRequest) {
        log.info("Service : Creating user: ");

        // Chuyển đổi DTO thành đối tượng Users (entity).
        Users user = userMapper.toUsers(userRequest);

        // Mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu.
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Thiết lập vai trò mặc định là USER.
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        // user.setRoles(roles);
        try {
            user = userRespository.save(user);

        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        // Lưu đối tượng người dùng vào cơ sở dữ liệu và trả về phản hồi.
        return userMapper.toUserResponse(user);
    }

    /**
     * Cập nhật thông tin người dùng dựa trên ID.
     *
     * @param userId            ID người dùng cần cập nhật.
     * @param userUpdateRequest Thông tin cập nhật.
     * @return Trả về thông tin người dùng sau khi cập nhật (UserResponse).
     */
    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        // Tìm kiếm người dùng theo ID.
        Users user = userRespository.findById(userId).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Nếu không tìm thấy, ném ngoại lệ.
        });

        // Cập nhật các thông tin của người dùng từ yêu cầu.
        userMapper.updateUsers(user, userUpdateRequest);

        // Lấy danh sách vai trò từ cơ sở dữ liệu và gán cho người dùng.
        var roles = roleRespository.findAllById(userUpdateRequest.getListRoles());
        user.setRoles(new HashSet<>(roles));

        // Mã hóa mật khẩu mới trước khi lưu.
        user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));

        // Lưu thông tin người dùng đã cập nhật vào cơ sở dữ liệu.
        return userMapper.toUserResponse(userRespository.save(user));
    }

    /**
     * Xóa người dùng khỏi cơ sở dữ liệu.
     *
     * @param userId ID của người dùng cần xóa.
     */
    @Override
    public void deleteUser(String userId) {
        userRespository.deleteById(userId); // Xóa người dùng dựa trên ID.
    }

    /**
     * Lấy thông tin người dùng dựa trên ID.
     *
     * @param userId ID của người dùng.
     * @return Trả về thông tin người dùng (UserResponse).
     */
    @PostAuthorize("returnObject.username == authentication.name")
    // Chỉ trả về nếu username khớp với người dùng hiện đang đăng nhập.
    @Override
    public UserResponse getUser(String userId) {
        log.info("In method get user by Id"); // Ghi log khi gọi phương thức.
        return userMapper.toUserResponse(userRespository
                .findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))); // Nếu không tìm thấy, ném ngoại lệ.
    }

    /**
     * Lấy danh sách tất cả người dùng (chỉ dành cho admin hoặc người có quyền đặc
     * biệt).
     *
     * @return Trả về danh sách người dùng dưới dạng List<UserResponse>.
     */
    @Override
    // @PreAuthorize("hasRole('ADMIN')") // Chỉ cho phép admin truy cập vào phương
    // thức này.
    @PreAuthorize("hasAuthority('APPROVE_POST')") // Chỉ cho phép người dùng có quyền cụ thể gọi phương thức này.
    public List<UserResponse> getAllUsers() {
        log.info("In method get Users"); // Ghi log khi gọi phương thức.
        return userRespository.findAll().stream()
                .map(userMapper::toUserResponse) // Chuyển đổi từng đối tượng entity thành DTO.
                .toList();
    }

    /**
     * Lấy thông tin của người dùng hiện tại (người đang đăng nhập).
     *
     * @return Trả về thông tin của người dùng hiện tại dưới dạng UserResponse.
     */
    @Override
    public UserResponse getMyÌnfo() {
        var context = SecurityContextHolder.getContext(); // Lấy đối tượng SecurityContext hiện tại.
        System.out.println(context.getAuthentication().toString()); // Ghi thông tin xác thực ra console.
        String name = context.getAuthentication().getName(); // Lấy username của người dùng hiện tại.
        System.out.println("======> subject: " + name); // name = subject in token

        // Tìm người dùng trong cơ sở dữ liệu theo username.
        Users user = userRespository
                .findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Nếu không tìm thấy, ném ngoại lệ.

        // Trả về thông tin của người dùng.
        return userMapper.toUserResponse(user);
    }
}
