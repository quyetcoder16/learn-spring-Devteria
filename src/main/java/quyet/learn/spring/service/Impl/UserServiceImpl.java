package quyet.learn.spring.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.request.user.UserUpdateRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.enums.Role;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.mapper.UserMapper;
import quyet.learn.spring.resporitory.UserRespository;
import quyet.learn.spring.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Tự động tạo constructor chứa các trường `final` hoặc được đánh dấu `@NonNull`.
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// Tự động thiết lập quyền truy cập mặc định là private và final.
@Slf4j // Kích hoạt logger để ghi log.
public class UserServiceImpl implements UserService {

    // Repository để thao tác với cơ sở dữ liệu.
    UserRespository userRespository;

    // Mapper để chuyển đổi giữa các đối tượng DTO và entity.
    UserMapper userMapper;

    // Đối tượng mã hóa mật khẩu.
    PasswordEncoder passwordEncoder;

    /**
     * Tạo một người dùng mới.
     *
     * @param userRequest thông tin người dùng từ request.
     * @return Thông tin người dùng đã tạo dưới dạng UserResponse.
     */
    @Override
    public UserResponse createUser(UserCreationRequest userRequest) {

        // Kiểm tra nếu username đã tồn tại.
        if (userRespository.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED); // Ném ngoại lệ nếu người dùng đã tồn tại.
        }

        // Chuyển đổi request thành entity.
        Users user = userMapper.toUsers(userRequest);

        // Mã hóa mật khẩu trước khi lưu.
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // Thiết lập vai trò mặc định là USER.
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        // Lưu vào cơ sở dữ liệu và trả về đối tượng phản hồi.
        return userMapper.toUserResponse(userRespository.save(user));
    }

    /**
     * Cập nhật thông tin người dùng.
     *
     * @param userId            ID người dùng cần cập nhật.
     * @param userUpdateRequest thông tin cập nhật.
     * @return Thông tin người dùng sau khi cập nhật.
     */
    @Override
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest) {
        // Tìm người dùng theo ID.
        Users user = userRespository.findById(userId).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED); // Ném ngoại lệ nếu không tìm thấy người dùng.
        });

        // Cập nhật thông tin người dùng từ request.
        userMapper.updateUsers(user, userUpdateRequest);

        // Lưu thông tin người dùng đã cập nhật và trả về phản hồi.
        return userMapper.toUserResponse(userRespository.save(user));
    }

    /**
     * Xóa người dùng.
     *
     * @param userId ID của người dùng cần xóa.
     */
    @Override
    public void deleteUser(String userId) {
        userRespository.deleteById(userId); // Xóa người dùng khỏi cơ sở dữ liệu.
    }

    /**
     * Lấy thông tin người dùng theo ID.
     *
     * @param userId ID của người dùng.
     * @return Thông tin người dùng dưới dạng UserResponse.
     */
    @PostAuthorize("returnObject.username == authentication.name")
    // Chỉ cho phép trả về nếu username khớp với username của người dùng đăng nhập.
    @Override
    public UserResponse getUser(String userId) {
        log.info("In method get user by Id"); // Ghi log khi vào phương thức.
        return userMapper.toUserResponse(userRespository.findById(userId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED))); // Ném ngoại lệ nếu không tìm thấy người dùng.
    }

    /**
     * Lấy danh sách tất cả người dùng. Chỉ dành cho admin.
     *
     * @return Danh sách người dùng.
     */
    @PreAuthorize("hasRole('ADMIN')") // Chỉ cho phép admin truy cập vào phương thức này.
    @Override
    public List<UserResponse> getAllUsers() {
        log.info("In method get Users"); // Ghi log khi vào phương thức.
        return userRespository.findAll().stream()
                .map(userMapper::toUserResponse) // Chuyển đổi từng entity thành DTO.
                .toList();
    }

    /**
     * Lấy thông tin của người dùng hiện tại (đang đăng nhập).
     *
     * @return Thông tin người dùng.
     */
    @Override
    public UserResponse getMyÌnfo() {
        var context = SecurityContextHolder.getContext(); // Lấy SecurityContext hiện tại.
        System.out.println(context.getAuthentication().toString()); // Ghi thông tin xác thực ra console.
        String name = context.getAuthentication().getName(); // Lấy username của người dùng hiện tại.
        System.out.println("======> subject: " + name); // name = subject in token
        Users user = userRespository.findByUsername(name).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)); // Ném ngoại lệ nếu không tìm thấy người dùng.
        return userMapper.toUserResponse(user); // Trả về thông tin người dùng hiện tại.
    }
}
