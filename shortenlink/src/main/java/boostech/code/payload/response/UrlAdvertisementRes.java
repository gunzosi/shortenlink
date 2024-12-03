package boostech.code.payload.response;


import boostech.code.models.AdvertisementImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Data
@AllArgsConstructor
public class UrlAdvertisementRes {
    private UUID id;
    private String longUrl;
    private String shortUrl;
    private String urlCode;
    private String textContent;
    private int countdownDuration;
    private List<String> imageFilenames;
    private List<String> imagePaths;

    public UrlAdvertisementRes() {
    }
}

