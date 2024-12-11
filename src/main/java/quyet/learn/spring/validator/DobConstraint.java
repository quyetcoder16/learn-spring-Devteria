package quyet.learn.spring.validator;

// Định nghĩa annotation tùy chỉnh để ràng buộc (constraint) việc kiểm tra ngày sinh (dob - date of birth).
@java.lang.annotation.Target({ java.lang.annotation.ElementType.FIELD }) // Áp dụng cho các trường (field) của class.
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) // Annotation tồn tại ở thời điểm runtime để sử dụng cho xác thực.
@jakarta.validation.Constraint(validatedBy = { DobValidator.class }) // Xác định validator được sử dụng là DobValidator.
public @interface DobConstraint {

    /**
     * Thông báo lỗi mặc định nếu giá trị không hợp lệ.
     *
     * @return Thông báo lỗi.
     */
    java.lang.String message() default "Invalid date of birth";

    /**
     * Nhóm xác thực (groups), dùng để phân loại các ràng buộc. Mặc định không sử dụng.
     *
     * @return Nhóm xác thực.
     */
    java.lang.Class<?>[] groups() default {};

    /**
     * Metadata bổ sung cho ràng buộc, được sử dụng trong các ngữ cảnh đặc biệt.
     *
     * @return Mảng các payload.
     */
    java.lang.Class<? extends jakarta.validation.Payload>[] payload() default {};

    /**
     * Giá trị tối thiểu (min) cho số năm cần kiểm tra.
     *
     * @return Số năm tối thiểu.
     */
    int min() default 0; // Mặc định là 0, nhưng có thể cấu hình khi sử dụng annotation.
}
