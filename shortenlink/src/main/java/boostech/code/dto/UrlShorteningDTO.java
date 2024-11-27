package boostech.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UrlShorteningDTO {
    private UUID id;
    private String longUrl;
    private String shortUrl;
    private String urlCode;
    private OffsetDateTime createdAt;

    public UrlShorteningDTO() {
    }
}
