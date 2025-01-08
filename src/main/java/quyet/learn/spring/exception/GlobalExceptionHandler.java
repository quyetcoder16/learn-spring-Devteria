package quyet.learn.spring.exception;

// Import các lớp và annotation cần thiết
import java.util.Map;
import java.util.Objects;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import quyet.learn.spring.dto.response.ApiResponse;

// Sử dụng @Slf4j để tự động tạo logger
@Slf4j
// Annotation @ControllerAdvice đánh dấu lớp xử lý ngoại lệ toàn cục
@ControllerAdvice
public class GlobalExceptionHandler {

    // Hằng số định nghĩa cho key "min" trong attribute của constraint
    private static final String MIN_ATTRIBUTE = "min";

    // Xử lý các ngoại lệ không xác định (Exception chung)
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingException(Exception exception) {
        // Log thông tin ngoại lệ
        System.out.println(exception.getMessage());

        // Tạo đối tượng phản hồi API
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorMsg());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode());

        // Trả về phản hồi với mã HTTP 500 (INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    // Xử lý ngoại lệ tùy chỉnh AppException
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        // Lấy mã lỗi từ ngoại lệ
        ErrorCode errorCode = exception.getErrorCode();

        // Tạo đối tượng phản hồi API với thông tin từ ErrorCode
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(errorCode.getErrorMsg());
        apiResponse.setCode(errorCode.getErrorCode());

        // Trả về phản hồi với mã HTTP tương ứng từ ErrorCode
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    // Xử lý ngoại lệ khi người dùng bị từ chối truy cập (AccessDeniedException)
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        // Sử dụng ErrorCode.UNAUTHORIZED để xác định lỗi truy cập
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // Tạo phản hồi API
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.builder()
                        .code(errorCode.getErrorCode())
                        .message(errorCode.getErrorMsg())
                        .build());
    }

    // Xử lý ngoại lệ khi tham số đầu vào không hợp lệ (MethodArgumentNotValidException)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // Lấy thông báo lỗi mặc định từ FieldError
        String enumKey = exception.getFieldError().getDefaultMessage();

        // Gán mã lỗi mặc định là ErrorCode.INVALID_KEY
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            // Kiểm tra nếu thông điệp tương ứng với một mã lỗi trong ErrorCode
            errorCode = ErrorCode.valueOf(enumKey);

            // Lấy thông tin chi tiết từ ConstraintViolation
            var constraintViolation =
                    exception.getBindingResult().getAllErrors().get(0).unwrap(ConstraintViolation.class);

            // Trích xuất các attributes của constraint (nếu có)
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString()); // Log thông tin attribute
        } catch (IllegalArgumentException e) {
            // Nếu không tìm thấy mã lỗi tương ứng, sử dụng ErrorCode.INVALID_KEY
        }

        // Tạo phản hồi API với thông điệp lỗi được xử lý (nếu có attributes)
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                        ? mapAttribute(errorCode.getErrorMsg(), attributes)
                        : errorCode.getErrorMsg());
        apiResponse.setCode(errorCode.getErrorCode());

        // Trả về phản hồi với mã HTTP 400 (BAD_REQUEST)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    // Phương thức thay thế các giá trị tham số trong thông điệp lỗi
    private String mapAttribute(String message, Map<String, Object> attributes) {
        // Lấy giá trị "min" từ attributes và thay thế vào thông điệp
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
