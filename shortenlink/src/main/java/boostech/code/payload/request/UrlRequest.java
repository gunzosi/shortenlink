package boostech.code.payload.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UrlRequest {
    @NotBlank(message = "Long URL is required")
    private String longUrl;
    private String urlCode;


    public UrlRequest() {
    }

    public UrlRequest(String longUrl) {
    }
}
