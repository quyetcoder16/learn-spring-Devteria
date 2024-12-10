package quyet.learn.spring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code = 1000;
    private String message;
    private T data;

    @Builder
    public ApiResponse(int code, String message, T data) {
        this.code = (code != 0) ? code : 1000;
        this.message = message;
        this.data = data;
    }

}
