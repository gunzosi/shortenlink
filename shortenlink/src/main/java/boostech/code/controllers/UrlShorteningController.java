package boostech.code.controllers;

import boostech.code.exception.ResourceNotFoundException;
import boostech.code.models.UrlShortening;
import boostech.code.payload.request.UrlRequest;
import boostech.code.payload.request.UrlRequestUpdate;
import boostech.code.payload.response.ClickStats;
import boostech.code.payload.response.UrlResponse;
import boostech.code.payload.response.UrlUUID;
import boostech.code.service.ClickService;
import boostech.code.service.UrlShorteningService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UrlShorteningController {
    private final UrlShorteningService urlShorteningService;
    private final ClickService clickService;

    // LOG
    private static final Logger logger = LoggerFactory.getLogger(UrlShorteningController.class);

    @Autowired
    public UrlShorteningController(UrlShorteningService urlShorteningService, ClickService clickService) {
        this.urlShorteningService = urlShorteningService;
        this.clickService = clickService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<UrlResponse> getAllUrls() {
        UrlResponse response = urlShorteningService.getAllUrls();
        if (response.getData() == null || ((List<?>) response.getData()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlResponse("Error", "No URLs found", (List<?>) null));
        }
        return ResponseEntity.ok(response);
    }


    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@Valid @RequestBody UrlRequest urlRequest) {
        UrlResponse response = urlShorteningService.shortenUrl(urlRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{urlCode}")
    public ResponseEntity<UrlResponse> redirectUrl(@PathVariable String urlCode) {
        Optional<UrlShortening> longUrl = urlShorteningService.getLongUrlByCode(urlCode);
        return longUrl.map(urlShortening -> ResponseEntity.ok(new UrlResponse("Success", "Redirected", (List<?>) urlShortening, null)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UrlResponse("Error", "URL not found: " + urlCode, (List<?>) null)));
    }


    @PutMapping("/{urlCode}")
    public ResponseEntity<?> updateUrlCode(
            @PathVariable String urlCode,
            @Valid @RequestBody UrlRequestUpdate urlRequest) {
        try {
            UrlResponse response = urlShorteningService.updateUrlCode(urlCode, urlRequest);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlResponse("Error", ex.getMessage(), (List<?>) null));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UrlResponse("Error", "An unexpected error occurred", (List<?>) null));
        }
    }


    @DeleteMapping("/{urlCode}")
    public ResponseEntity<UrlResponse> deleteUrl(@PathVariable String urlCode) {
        urlShorteningService.deleteUrl(urlCode);
        return ResponseEntity.ok(new UrlResponse("Success", "URL and related click events deleted successfully", (List<?>) null));
    }



    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{urlCode}/stats")
    public ResponseEntity<ClickStats> getUrlStats(@PathVariable String urlCode) {
        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL not found: " + urlCode));

        ClickStats stats = clickService.getClickStats(urlShortening.getId().toString());
        return ResponseEntity.ok(stats);
    }

    // LAY THONG TIN CHO URL CO UUID de CHECK STATISTICS cho URL
    @GetMapping("/link/{urlUUID}/stats")
    public ResponseEntity<ClickStats> getUrlStatsByUUID(@PathVariable UUID urlUUID) {
        ClickStats stats = clickService.getClickStats(urlUUID);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/getUUID/{urlCode}")
    public ResponseEntity<UrlUUID> getUUID(@PathVariable String urlCode) {
        logger.info("Fetching UUID for urlCode: {}", urlCode);

        try {
            UUID urlUUID = urlShorteningService.getUUIDByUrlCode(urlCode);
            return ResponseEntity.ok(new UrlUUID("Success", urlUUID));
        } catch (ResourceNotFoundException ex) {
            logger.warn("URL Code isn't found: {}", urlCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlUUID("Error", UUID.fromString(ex.getMessage())));
        }
    }

}