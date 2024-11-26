package boostech.code.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetail {
    private String field;
    private String errorCode;
    private String message;

    public ErrorDetail() {
    }
}
