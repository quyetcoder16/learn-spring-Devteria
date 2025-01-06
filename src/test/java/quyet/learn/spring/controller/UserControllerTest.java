package quyet.learn.spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.response.UserResponse;
import quyet.learn.spring.service.UserService;

import java.time.LocalDate;

// Chú thích: @SpringBootTest dùng để chạy toàn bộ ngữ cảnh Spring trong môi trường kiểm thử
@SpringBootTest

// Chú thích: @Slf4j để ghi log trong quá trình kiểm thử
@Slf4j

// Chú thích: @AutoConfigureMockMvc tự động cấu hình MockMvc để mô phỏng các yêu cầu HTTP
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc; // MockMvc được tự động tiêm để thực hiện các yêu cầu HTTP giả lập

    @MockBean
    private UserService userService; // Tạo một mock của UserService để kiểm soát hành vi trong test

    private UserCreationRequest userCreationRequest; // Đối tượng yêu cầu (request) mẫu để kiểm thử
    private UserResponse userResponse; // Đối tượng phản hồi (response) mẫu để kiểm thử
    private LocalDate dob; // Ngày sinh mẫu

    @Autowired
    private MockMvc mockMvc; // Thêm một instance khác của MockMvc (không cần thiết ở đây)

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

        // Tạo đối tượng phản hồi (UserResponse) giả lập mà UserService sẽ trả về
        userResponse = UserResponse.builder()
                .id("6203deb0")
                .username("admin123")
                .firstName("quyet")
                .lastName("Doe")
                .dateOfBirth(dob)
                .build();
    }

    // Test case kiểm tra việc tạo người dùng với dữ liệu hợp lệ
    @Test
    public void createUser_validRequest_success() throws Exception {
        log.info("hello test"); // Ghi log để biết rằng test này được chạy

        // Chuyển đổi đối tượng Java thành chuỗi JSON để gửi qua HTTP request
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Đăng ký module để hỗ trợ định dạng ngày tháng
        String content = objectMapper.writeValueAsString(userCreationRequest); // JSON hóa request mẫu

        // Giả lập hành vi của UserService khi gọi phương thức createUser
        Mockito.when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

        // Gửi yêu cầu POST tới endpoint `/users` và kiểm tra kết quả trả về
        mockMvc.perform(MockMvcRequestBuilders.post("/users") // Gửi HTTP POST request
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // Đặt Content-Type là JSON
                        .content(content)) // Gửi nội dung JSON trong body
                .andExpect(MockMvcResultMatchers.status().isOk()) // Kiểm tra HTTP status code là 200 OK
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000)) // Kiểm tra trường "code" trong JSON response
                .andExpect(MockMvcResultMatchers.jsonPath("data.id").value("6203deb0")); // Kiểm tra "id" của dữ liệu trả về
    }

    // Test case kiểm tra tạo người dùng với dữ liệu không hợp lệ
    @Test
    public void createUser_usernameInvalid_fail() throws Exception {
        log.info("hello test"); // Ghi log để xác nhận test đang được chạy

        // Cập nhật username trong request mẫu thành dữ liệu không hợp lệ (quá ngắn)
        userCreationRequest.setUsername("ad");

        // Chuyển đổi request thành JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Đăng ký module hỗ trợ ngày tháng
        String content = objectMapper.writeValueAsString(userCreationRequest); // JSON hóa request

        // Gửi yêu cầu POST với username không hợp lệ và kiểm tra kết quả trả về
        mockMvc.perform(MockMvcRequestBuilders.post("/users") // Gửi HTTP POST request
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // Đặt Content-Type là JSON
                        .content(content)) // Gửi nội dung JSON trong body
                .andExpect(MockMvcResultMatchers.status().isBadRequest()) // Kiểm tra HTTP status code là 400 Bad Request
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1004)) // Kiểm tra trường "code" trong JSON response
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Username Must be at least 5 characters!")); // Kiểm tra thông báo lỗi
    }
}
