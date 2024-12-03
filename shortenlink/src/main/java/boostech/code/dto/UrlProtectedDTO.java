package boostech.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class UrlProtectedDTO {
    private String urlCode;
    private String password;
    private boolean isPasswordProtected;

    public UrlProtectedDTO() {
    }
}
