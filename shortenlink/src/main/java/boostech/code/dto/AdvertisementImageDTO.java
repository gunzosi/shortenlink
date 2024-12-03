package boostech.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AdvertisementImageDTO {
    private UUID id;
    private UUID advertisementId;
    private String imageFilename;
    private String imagePath;

    public AdvertisementImageDTO() {
    }
}
