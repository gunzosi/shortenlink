package boostech.code.controllers;

import boostech.code.dto.AdvertisementDTO;
import boostech.code.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @PostMapping("/{urlId}")
    public ResponseEntity<?> createAdvertisement(
            @PathVariable UUID urlId,
            @RequestParam(required = false) String textContent,
            @RequestParam(required = false, defaultValue = "5") Integer countdownDuration,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        try {
            AdvertisementDTO newAdvertisement = advertisementService.createAdvertisement(
                    urlId, textContent, countdownDuration, images);
            return ResponseEntity.ok(newAdvertisement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{advertisementId}")
    public ResponseEntity<?> updateAdvertisement(
            @PathVariable UUID advertisementId,
            @RequestParam(required = false) String textContent,
            @RequestParam(required = false, defaultValue = "5") Integer countdownDuration,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        try {
            AdvertisementDTO updatedAdvertisement = advertisementService.updateAdvertisement(
                    advertisementId, textContent, countdownDuration, images);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{advertisementId}")
    public ResponseEntity<?> deleteAdvertisement(@PathVariable UUID advertisementId) {
        try {
            advertisementService.deleteAdvertisement(advertisementId);
            return ResponseEntity.ok().body("Advertisement deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{advertisementId}/images")
    public ResponseEntity<?> getImagesByAdvertisementId(@PathVariable UUID advertisementId) {
        try {
            return ResponseEntity.ok(advertisementService.getImagesByAdvertisementId(advertisementId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
