package boostech.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AdvertisementDTO {
    private UUID id;
    private UUID urlId;
    private String textContent;
    private Integer countdownDuration;
    private List<String> imagePaths;

    public AdvertisementDTO() {
    }
}
