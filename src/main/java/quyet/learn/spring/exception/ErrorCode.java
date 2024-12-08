package quyet.learn.spring.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized Exception error!"),
    INVALID_KEY(1001,"Invalid message key!"),
    USER_EXISTED(1002, "User Existed!"),
    USERNAME_INVALID(1003, "Username Must be at least 3 characters!"),
    INVALID_PASSWORD(1004, "Password Must be at least 6 characters!"),
    ;
    private int errorCode;
    private String errorMsg;

    ErrorCode(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
