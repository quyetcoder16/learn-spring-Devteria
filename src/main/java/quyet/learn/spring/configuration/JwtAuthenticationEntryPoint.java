// Lớp này là một `AuthenticationEntryPoint` tùy chỉnh để xử lý các trường hợp xác thực JWT thất bại.
package quyet.learn.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper; // Thư viện để chuyển đổi đối tượng Java sang JSON.
import jakarta.servlet.ServletException; // Ngoại lệ của Java Servlet API.
import jakarta.servlet.http.HttpServletRequest; // Đối tượng đại diện cho yêu cầu HTTP.
import jakarta.servlet.http.HttpServletResponse; // Đối tượng đại diện cho phản hồi HTTP.
import org.springframework.http.MediaType; // Định nghĩa các kiểu media như JSON, XML.
import org.springframework.security.core.AuthenticationException; // Ngoại lệ khi xác thực thất bại.
import org.springframework.security.web.AuthenticationEntryPoint; // Giao diện xử lý xác thực thất bại.
import quyet.learn.spring.dto.response.ApiResponse; // Định nghĩa chuẩn của API response.
import quyet.learn.spring.exception.ErrorCode; // Liệt kê các mã lỗi tùy chỉnh của hệ thống.

import java.io.IOException; // Ngoại lệ xử lý I/O.

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Phương thức này được gọi khi có lỗi xác thực xảy ra, ví dụ: JWT không hợp lệ hoặc thiếu JWT.
     *
     * @param request       Yêu cầu HTTP nhận được từ client.
     * @param response      Phản hồi HTTP được trả về cho client.
     * @param authException Ngoại lệ xác thực JWT, cung cấp thông tin về lỗi.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Lấy mã lỗi tùy chỉnh cho trường hợp xác thực thất bại.
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        // Thiết lập mã trạng thái HTTP cho phản hồi.
        response.setStatus(errorCode.getHttpStatus().value());

        // Thiết lập kiểu nội dung của phản hồi là JSON.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Tạo một đối tượng phản hồi API chứa thông tin lỗi.
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getErrorCode()) // Mã lỗi.
                .data(errorCode.getErrorMsg()) // Thông báo lỗi.
                .build();

        // Sử dụng ObjectMapper để chuyển đối tượng phản hồi API thành chuỗi JSON.
        ObjectMapper objectMapper = new ObjectMapper();

        // Ghi chuỗi JSON vào phản hồi HTTP.
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // Đẩy dữ liệu phản hồi đến client.
        response.flushBuffer();
    }
}
