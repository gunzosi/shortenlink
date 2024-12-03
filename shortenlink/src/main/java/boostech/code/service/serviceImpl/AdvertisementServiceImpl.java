package boostech.code.service.serviceImpl;

import boostech.code.dto.AdvertisementDTO;
import boostech.code.models.Advertisement;
import boostech.code.models.AdvertisementImage;
import boostech.code.models.UrlShortening;
import boostech.code.payload.response.UrlAdvertisementRes;
import boostech.code.repository.AdvertisementImageRepository;
import boostech.code.repository.AdvertisementRepository;
import boostech.code.repository.UrlShorteningRepository;
import boostech.code.service.AdvertisementService;
import boostech.code.utils.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final UrlShorteningRepository urlShorteningRepository;
    private final FileUploadUtil fileUploadUtil;

    @Transactional
    @Override
    public AdvertisementDTO createAdvertisement(
            UUID urlId,
            String textContent,
            Integer countdownDuration,
            List<MultipartFile> images
    ) {
        UrlShortening url = urlShorteningRepository.findById(urlId)
                .orElseThrow(() -> new IllegalArgumentException("URL does not exist."));

        Advertisement advertisement = new Advertisement();
        advertisement.setId(UUID.randomUUID());
        advertisement.setUrl(url);
        advertisement.setTextContent(textContent != null ? textContent : "Default text content");
        advertisement.setCountdownDuration(countdownDuration != null ? countdownDuration : 5);
        advertisement.setCreatedAt(OffsetDateTime.now());

        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                try {
                    saveAdvertisementImage(savedAdvertisement, image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save advertisement image.", e);
                }
            }
        }

        return getAdvertisementDTO(savedAdvertisement);
    }



    private void saveAdvertisementImage(Advertisement advertisement, MultipartFile image) throws IOException {
        String uploadedFileName = fileUploadUtil.uploadFile(image, "advertisements");

        AdvertisementImage advertisementImage = new AdvertisementImage();
        advertisementImage.setId(UUID.randomUUID());
        advertisementImage.setAdvertisement(advertisement);
        advertisementImage.setImageFilename(image.getOriginalFilename());
        advertisementImage.setImagePath(uploadedFileName);
        advertisementImage.setCreatedAt(OffsetDateTime.now());

        advertisementImageRepository.save(advertisementImage);
    }

    @Transactional
    @Override
    public AdvertisementDTO updateAdvertisement(
            UUID advertisementId,
            String textContent,
            Integer countdownDuration,
            List<MultipartFile> newImages
    ) {
        Advertisement existingAdvertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement doesn't exist."));

        if (textContent != null && !textContent.trim().isEmpty()) {
            existingAdvertisement.setTextContent(textContent);
        }

        if (countdownDuration != null) {
            existingAdvertisement.setCountdownDuration(countdownDuration);
        }

        List<AdvertisementImage> oldImages = advertisementImageRepository.findByAdvertisement_Id(advertisementId);
        oldImages.forEach(img -> {
            try {
                fileUploadUtil.deleteFile(img.getImagePath(), "advertisements");
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete old images.", e);
            }
        });
        advertisementImageRepository.deleteAll(oldImages);

        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                try {
                    saveAdvertisementImage(existingAdvertisement, image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save new images.", e);
                }
            }
        }

        return getAdvertisementDTO(existingAdvertisement);
    }

    private AdvertisementDTO getAdvertisementDTO(Advertisement existingAdvertisement) {
        List<AdvertisementImage> advertisementImages = advertisementImageRepository.findByAdvertisement_Id(existingAdvertisement.getId());

        AdvertisementDTO advertisementDTO = new AdvertisementDTO();
        advertisementDTO.setId(existingAdvertisement.getId());
        advertisementDTO.setUrlId(existingAdvertisement.getUrl().getId());
        advertisementDTO.setTextContent(existingAdvertisement.getTextContent());
        advertisementDTO.setCountdownDuration(existingAdvertisement.getCountdownDuration());

        List<String> imagePaths = advertisementImages.stream()
                .map(AdvertisementImage::getImagePath)
                .collect(Collectors.toList());
        advertisementDTO.setImagePaths(imagePaths);

        return advertisementDTO;
    }


    @Transactional
    @Override
    public void deleteAdvertisement(UUID advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement does not exist."));

        List<AdvertisementImage> images = advertisementImageRepository.findByAdvertisement_Id(advertisementId);
        images.forEach(img -> {
            try {
                fileUploadUtil.deleteFile(img.getImagePath(), "advertisements");
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image.", e);
            }
        });
        advertisementImageRepository.deleteAll(images);

        advertisementRepository.delete(advertisement);
    }

    @Override
    public List<AdvertisementImage> getImagesByAdvertisementId(UUID advertisementId) {
        return advertisementImageRepository.findByAdvertisement_Id(advertisementId);
    }

    @Override
    public UrlAdvertisementRes getUrlAdvertisement(UUID urlId) {
        List<Object[]> results = advertisementRepository.findAdvertisementDetailsByUrlId(urlId);

        if (results.isEmpty()) {
            throw new IllegalArgumentException("The URL doesn't exist or has no advertisements.");
        }

        Object[] firstRow = results.getFirst();

        List<String> imageFilenames = results.stream()
                .map(row -> (String) row[6])
                .toList();

        List<String> imagePaths = results.stream()
                .map(row -> (String) row[7])
                .toList();

        return new UrlAdvertisementRes(
                (UUID) firstRow[0],
                (String) firstRow[1],
                (String) firstRow[2],               // shortUrl
                (String) firstRow[3],               // urlCode
                (String) firstRow[4],               // textContent
                (Integer) firstRow[5],              // countdownDuration
                imageFilenames,                     // image filenames
                imagePaths                          // image paths
        );
    }

}
