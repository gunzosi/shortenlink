package boostech.code.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class UrlRequestUpdate {

    @NotBlank(message = "Original URL is required")
    private String originalUrl;

    @Size(min = 3, max = 50, message = "Custom URL code must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Custom URL code can only contain letters, numbers, and hyphens")
    private String customUrlCode;

    public UrlRequestUpdate() {
    }
}
