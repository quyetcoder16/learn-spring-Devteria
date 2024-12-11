package quyet.learn.spring.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
/**
 * Validator để kiểm tra ngày sinh (dob - date of birth) có đáp ứng yêu cầu về tuổi tối thiểu hay không.
 */
public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {

    private int min; // Giá trị tuổi tối thiểu được cấu hình từ annotation @DobConstraint.

    /**
     * Phương thức khởi tạo validator, lấy thông tin cấu hình từ annotation.
     *
     * @param constraintAnnotation annotation chứa thông tin cấu hình (ví dụ: giá trị min).
     */
    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min(); // Gán giá trị min từ annotation.
    }

    /**
     * Phương thức kiểm tra giá trị ngày sinh có hợp lệ hay không.
     *
     * @param value                     Giá trị ngày sinh cần kiểm tra.
     * @param constraintValidatorContext Bối cảnh của quá trình xác thực (dùng để cấu hình thông báo lỗi nếu cần).
     * @return true nếu giá trị hợp lệ, false nếu không.
     */
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(value)) {
            // Trả về true nếu giá trị là null, tránh lỗi xác thực không cần thiết.
            return true;
        }

        // Tính khoảng cách (theo năm) giữa ngày sinh và ngày hiện tại.
        long years = ChronoUnit.YEARS.between(value, LocalDate.now());

        // Kiểm tra nếu số năm lớn hơn hoặc bằng giá trị tối thiểu thì hợp lệ.
        return years >= min;
    }
}
