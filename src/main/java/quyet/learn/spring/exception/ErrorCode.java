package quyet.learn.spring.exception;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception error!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User Existed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User Not Existed!", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1004, "Username Must be at least 3 characters!", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Password Must be at least 6 characters!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated Error!",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "you do not have permission!",HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Invalid date of birth!",HttpStatus.BAD_REQUEST),
    ;
    private int errorCode;
    private String errorMsg;
    private HttpStatus httpStatus;

    ErrorCode(int errorCode, String errorMsg, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }


}
