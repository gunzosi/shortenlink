package boostech.code.service;

import boostech.code.dto.AdvertisementDTO;
import boostech.code.models.Advertisement;
import boostech.code.models.AdvertisementImage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public interface AdvertisementService {
    AdvertisementDTO createAdvertisement(UUID urlId,
                                         String textContent,
                                         Integer countdownDuration,
                                         List<MultipartFile> images);
    AdvertisementDTO updateAdvertisement(UUID urlId,
                                      String textContent,
                                      Integer countdownDuration,
                                      List<MultipartFile> images);
    void deleteAdvertisement(UUID urlId);
    List<AdvertisementImage> getImagesByAdvertisementId(UUID advertisementId);
}
