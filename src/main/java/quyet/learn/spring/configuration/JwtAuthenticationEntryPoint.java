// Lớp này là một `AuthenticationEntryPoint` tùy chỉnh để xử lý các trường hợp xác thực JWT thất bại.
package quyet.learn.spring.configuration;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import quyet.learn.spring.dto.response.ApiResponse;
import quyet.learn.spring.exception.ErrorCode;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Phương thức này được gọi khi có lỗi xác thực xảy ra, ví dụ: JWT không hợp lệ hoặc thiếu JWT.
     *
     * @param request       Yêu cầu HTTP nhận được từ client.
     * @param response      Phản hồi HTTP được trả về cho client.
     * @param authException Ngoại lệ xác thực JWT, cung cấp thông tin về lỗi.
     */
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
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
