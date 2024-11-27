package boostech.code.controllers;

import boostech.code.component.AuthenticationFacade;
import boostech.code.dto.UserResponse;
import boostech.code.exception.ResourceNotFoundException;
import boostech.code.exception.UnauthorizedAccessException;
import boostech.code.models.UrlShortening;
import boostech.code.models.User;
import boostech.code.payload.request.UrlRequest;
import boostech.code.payload.request.UrlRequestUpdate;
import boostech.code.payload.response.ClickStats;
import boostech.code.payload.response.UrlResponse;
import boostech.code.payload.response.UrlUUID;
import boostech.code.repository.UrlShorteningRepository;
import boostech.code.service.ClickService;
import boostech.code.service.UrlShorteningService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
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
    private final AuthenticationFacade authenticationFacade;
    private final UrlShorteningRepository urlShorteningRepository;

    @Autowired
    public UrlShorteningController(UrlShorteningService urlShorteningService, ClickService clickService, AuthenticationFacade authenticationFacade, UrlShorteningRepository urlShorteningRepository) {
        this.urlShorteningService = urlShorteningService;
        this.clickService = clickService;
        this.authenticationFacade = authenticationFacade;
        this.urlShorteningRepository = urlShorteningRepository;
    }

    // LAY TAT CA URL cua USER DANG DANG NHAP (JWT)
    @GetMapping("/getAll")
    public ResponseEntity<UrlResponse> getAllUrls() {
        UrlResponse response = urlShorteningService.getAllUrls();
        List<?> data = (List<?>) response.getData();
        if (data == null || data.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlResponse(
                            "Error",
                            "No URLs found",
                            (List<?>) null));
        }
        return ResponseEntity.ok(response);
    }



    // Can phai LOGIN moi duojc su dung API nay
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@Valid @RequestBody UrlRequest urlRequest) {
        UrlResponse response = urlShorteningService.shortenUrl(urlRequest);
        return ResponseEntity.ok(response);
    }


    // LAY THONG TIN CHO URL CO UUID de CHECK STATISTICS cho URL - phai dang nhap moi duoc su dung
    @GetMapping("/{urlCode}")
    public ResponseEntity<UrlResponse> redirectUrl(@PathVariable String urlCode) {
        Optional<UrlShortening> longUrl = urlShorteningService.getLongUrlByCode(urlCode);
        return longUrl.map(urlShortening -> ResponseEntity.ok(new UrlResponse("Success", "Redirected", (List<?>) urlShortening, null)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UrlResponse("Error", "URL not found: " + urlCode, (List<?>) null)));
    }


    @PutMapping("edit/{oldUrlCode}")
    public ResponseEntity<UrlResponse> updateUrlCode(
            @PathVariable String oldUrlCode,
            @Valid @RequestBody UrlRequestUpdate urlRequestUpdate) {
        try {
            UrlResponse response = urlShorteningService.updateUrlCodeV2(oldUrlCode, urlRequestUpdate);
            return ResponseEntity.ok(response);

        } catch (UnauthorizedAccessException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UrlResponse("Error", ex.getMessage(), (List<?>) null));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlResponse("Error", ex.getMessage(), (List<?>) null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UrlResponse("Error",
                            "Error out of exception",
                            (List<?>) null));
        }
    }

    @GetMapping("/user/urls")
    public ResponseEntity<UrlResponse> getUserUrls() {
        UrlResponse response = urlShorteningService.getUserUrls();
        List<?> data = (List<?>) response.getData();
        if (data == null || data.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlResponse(
                            "Error",
                            "No URLs found for the current user",
                            (List<?>) null));
        }
        return ResponseEntity.ok(response);
    }


    // DELETE URL - phai dang nhap moi duoc su dung
    @DeleteMapping("/{urlCode}")
    public ResponseEntity<UrlResponse> deleteUrl(@PathVariable String urlCode) {
        urlShorteningService.deleteUrl(urlCode);
        return ResponseEntity.ok(new UrlResponse("Success", "URL and related click events deleted successfully", (List<?>) null));
    }

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        return ResponseEntity.ok().build();
    }

    // LAY THONG TIN CHO URL CO UUID de CHECK STATISTICS cho URL - phai dang nhap moi duoc su dung
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
            logger.warn("URL Code not found: {}", urlCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UrlUUID("Error", null));
        }
    }

    @GetMapping("/user/{userId}/urls")
    public ResponseEntity<UrlResponse> getUserUrlsById(@PathVariable Long userId) {
        UrlResponse response = urlShorteningService.getUserUrlsById(userId);

        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }


@GetMapping("/url/{urlCode}/owner")
    public ResponseEntity<?> getUrlOwner(@PathVariable String urlCode) {
        try {
            User owner = urlShorteningService.findUserByUrlCode(urlCode);
            return ResponseEntity.ok(new UserResponse(
                    owner.getId(),
                    owner.getUsername(),
                    owner.getEmail()
            ));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ProblemDetail.forStatusAndDetail(
                            HttpStatus.NOT_FOUND,
                            ex.getMessage()
                    ));
        }
    }
}