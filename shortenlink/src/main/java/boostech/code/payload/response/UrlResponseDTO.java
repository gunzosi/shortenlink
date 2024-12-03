package boostech.code.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class UrlResponseDTO {
    private String shortUrl;
    private String longUrl;
    private boolean isPasswordProtected;

    public UrlResponseDTO() {
    }
}
