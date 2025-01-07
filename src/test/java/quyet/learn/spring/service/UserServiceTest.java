package quyet.learn.spring.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.entity.Users;
import quyet.learn.spring.exception.AppException;
import quyet.learn.spring.exception.ErrorCode;
import quyet.learn.spring.resporitory.UserRespository;

import java.time.LocalDate;

// Chú thích: @SpringBootTest để chạy ngữ cảnh Spring trong môi trường kiểm thử
@SpringBootTest
@TestPropertySource("/test.properties")
public class UserServiceTest {

    @Autowired
    private UserService userService; // Inject UserService để kiểm thử

    @MockBean
    private UserRespository userRespository; // Tạo một mock của UserRespository để kiểm soát hành vi trong test

    private UserCreationRequest userCreationRequest; // Đối tượng yêu cầu (request) mẫu
    private UserResponse userResponse; // Đối tượng phản hồi (response) mẫu
    private Users user; // Đối tượng `Users` mẫu
    private LocalDate dob; // Ngày sinh mẫu

    // Phương thức được chạy trước mỗi test case để khởi tạo dữ liệu mẫu
    @BeforeEach
    void initData() {
        dob = LocalDate.of(1990, 1, 1); // Ngày sinh giả lập

        // Tạo đối tượng yêu cầu (UserCreationRequest) với dữ liệu hợp lệ
        userCreationRequest = UserCreationRequest.builder()
                .username("admin123")
                .password("adminjkhgjk")
                .firstName("quyet")
                .lastName("Doe")
                .dob(dob)
                .build();

        // Tạo đối tượng phản hồi (UserResponse) giả lập
        userResponse = UserResponse.builder()
                .id("6203deb0")
                .username("admin123")
                .firstName("quyet")
                .lastName("Doe")
                .dateOfBirth(dob)
                .build();

        // Tạo đối tượng người dùng (Users) giả lập
        user = Users.builder()
                .id("6203deb0")
                .username("admin123")
                .firstName("quyet")
                .lastName("Doe")
                .dateOfBirth(dob)
                .build();
    }

    // Test case: kiểm tra tạo người dùng với dữ liệu hợp lệ
    @Test
    void createUser_validRequest_success() {
        // given: giả lập hành vi của repository
        Mockito.when(userRespository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(false); // Người dùng chưa tồn tại
        Mockito.when(userRespository.save(ArgumentMatchers.any())).thenReturn(user); // Lưu người dùng trả về đối tượng `user`

        // when: gọi phương thức cần kiểm tra
        var result = userService.createUser(userCreationRequest);

        // then: kiểm tra kết quả trả về
        Assertions.assertEquals(userResponse.getId(), result.getId()); // Kiểm tra ID trả về
        Assertions.assertEquals(userResponse.getUsername(), result.getUsername()); // Kiểm tra username trả về
    }

    // Test case: kiểm tra lỗi khi tạo người dùng với username đã tồn tại
    @Test
    void createUser_userExisted_fail() {
        // given: giả lập rằng username đã tồn tại trong cơ sở dữ liệu
        Mockito.when(userRespository.existsByUsername(ArgumentMatchers.anyString())).thenReturn(true);

        // when, then: gọi phương thức và kiểm tra ngoại lệ được ném ra
        var exception = Assertions.assertThrowsExactly(AppException.class, () -> userService.createUser(userCreationRequest));

        // Kiểm tra thông báo lỗi và mã lỗi
        Assertions.assertEquals(exception.getMessage(), ErrorCode.USER_EXISTED.getErrorMsg()); // Kiểm tra thông báo lỗi
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ErrorCode.USER_EXISTED.getErrorCode()); // Kiểm tra mã lỗi
    }
}
