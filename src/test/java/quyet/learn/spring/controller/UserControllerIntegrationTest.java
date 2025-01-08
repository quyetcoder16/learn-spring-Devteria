package quyet.learn.spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import quyet.learn.spring.dto.request.user.UserCreationRequest;
import quyet.learn.spring.dto.response.UserResponse;

import java.time.LocalDate;

/**
 * Tích hợp kiểm thử cho UserController sử dụng Testcontainers.
 */
@SpringBootTest // Chạy toàn bộ ngữ cảnh ứng dụng Spring để kiểm thử tích hợp.
@Slf4j // Sử dụng log để hỗ trợ ghi nhật ký trong quá trình kiểm thử.
@AutoConfigureMockMvc // Tự động cấu hình MockMvc để mô phỏng các yêu cầu HTTP.
@Testcontainers // Kích hoạt hỗ trợ Testcontainers.
public class UserControllerIntegrationTest {

    // Khởi tạo Testcontainer MySQL.
    @Container
    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:latest");

    @Autowired
    private MockMvc mockMvc; // Công cụ MockMvc dùng để mô phỏng yêu cầu và phản hồi HTTP.

    // Cấu hình động các thuộc tính kết nối cơ sở dữ liệu cho Testcontainer.
    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl); // Đặt URL JDBC.
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName); // Đặt driver class.
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername); // Đặt tên người dùng.
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword); // Đặt mật khẩu.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update"); // Cấu hình Hibernate để tự động cập nhật schema.
    }

    private UserCreationRequest userCreationRequest; // Đối tượng yêu cầu (request) mẫu để kiểm thử.
    private UserResponse userResponse; // Đối tượng phản hồi (response) mẫu để kiểm thử.
    private LocalDate dob; // Ngày sinh mẫu.

    /**
     * Phương thức khởi tạo dữ liệu mẫu trước mỗi test case.
     */
    @BeforeEach
    void initData() {
        dob = LocalDate.of(1990, 1, 1); // Thiết lập ngày sinh giả lập.

        // Tạo đối tượng yêu cầu UserCreationRequest với dữ liệu hợp lệ.
        userCreationRequest = UserCreationRequest.builder()
                .username("admin123")
                .password("adminjkhgjk")
                .firstName("quyet")
                .lastName("Doe")
                .dob(dob)
                .build();

        // Tạo đối tượng phản hồi UserResponse giả lập để kiểm tra tính đúng đắn.
        userResponse = UserResponse.builder()
                .id("6203deb0")
                .username("admin123")
                .firstName("quyet")
                .lastName("Doe")
                .dateOfBirth(dob)
                .build();
    }

    /**
     * Test case kiểm tra việc tạo người dùng với dữ liệu hợp lệ.
     */
    @Test
    public void createUser_validRequest_success() throws Exception {
        log.info("hello test"); // Ghi nhật ký để kiểm tra quá trình thực hiện test.

        // Chuyển đổi đối tượng Java thành chuỗi JSON để gửi trong yêu cầu HTTP.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Đăng ký module để hỗ trợ định dạng ngày tháng.
        String content = objectMapper.writeValueAsString(userCreationRequest); // Chuyển request mẫu thành JSON.

        // Gửi yêu cầu POST tới endpoint `/users` và kiểm tra kết quả trả về.
        var response = mockMvc.perform(MockMvcRequestBuilders.post("/users") // Thực hiện HTTP POST request.
                        .contentType(MediaType.APPLICATION_JSON_VALUE) // Đặt Content-Type là JSON.
                        .content(content)) // Gửi nội dung JSON trong body của request.
                .andExpect(MockMvcResultMatchers.status().isOk()) // Kiểm tra HTTP status code là 200 OK.
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1000)) // Kiểm tra trường "code" trong JSON response.
                .andExpect(MockMvcResultMatchers.jsonPath("data.username").value(userResponse.getUsername())) // Kiểm tra username.
                .andExpect(MockMvcResultMatchers.jsonPath("data.firstName").value(userResponse.getFirstName())) // Kiểm tra firstName.
                .andExpect(MockMvcResultMatchers.jsonPath("data.lastName").value(userResponse.getLastName())); // Kiểm tra lastName.

        // Ghi log nội dung phản hồi từ phía server.
        log.info("result: {}", response.andReturn().getResponse().getContentAsString());
    }
}
