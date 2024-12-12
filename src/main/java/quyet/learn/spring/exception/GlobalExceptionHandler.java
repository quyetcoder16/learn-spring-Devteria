package quyet.learn.spring.exception;

// Import các lớp và annotation cần thiết

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import quyet.learn.spring.dto.response.ApiResponse;

import java.util.Map;
import java.util.Objects;

// Annotation để đánh dấu lớp xử lý ngoại lệ toàn cục
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    // Xử lý ngoại lệ chung (mọi loại ngoại lệ không xác định)
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingException(Exception exception) {
        // Tạo đối tượng phản hồi API
        System.out.println(exception.getMessage());
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorMsg());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode());

        // Trả về phản hồi với mã HTTP 500 (INTERNAL_SERVER_ERROR)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiResponse);
    }

    // Xử lý ngoại lệ tùy chỉnh `AppException`
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        // Lấy mã lỗi từ ngoại lệ
        ErrorCode errorCode = exception.getErrorCode();

        // Tạo phản hồi API
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(errorCode.getErrorMsg());
        apiResponse.setCode(errorCode.getErrorCode());

        // Trả về phản hồi với mã HTTP 400 (BAD_REQUEST)
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.builder()
                        .code(errorCode.getErrorCode())
                        .message(errorCode.getErrorMsg())
                        .build());
    }

    // Xử lý ngoại lệ khi tham số không hợp lệ
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // Lấy thông điệp mặc định từ lỗi
        String enumKey = exception.getFieldError().getDefaultMessage();

        // Gán mã lỗi mặc định là `INVALID_KEY`
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        Map<String, Object> attributes = null;
        try {
            // Nếu thông điệp phù hợp với một mã trong `ErrorCode`, sử dụng mã đó
            errorCode = ErrorCode.valueOf(enumKey);

            var constrainViolation = exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constrainViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString());
        } catch (IllegalArgumentException e) {
            // Nếu không tìm thấy mã tương ứng, sử dụng mã mặc định
        }

        // Tạo phản hồi API
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(
                Objects.nonNull(attributes) ? mapAttribute(errorCode.getErrorMsg(), attributes)
                        : errorCode.getErrorMsg());
        apiResponse.setCode(errorCode.getErrorCode());

        // Trả về phản hồi với mã HTTP 400 (BAD_REQUEST)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
