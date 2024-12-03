package boostech.code.controllers;

import boostech.code.models.UrlShortening;
import boostech.code.payload.request.RequestInfo;
import boostech.code.service.ClickService;
import boostech.code.service.UrlShorteningService;
import boostech.code.utils.RequestInfoHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// 100% free access from source, no JWT token needed
@RestController
@RequestMapping("/")
public class UrlRedirectController {

    private final UrlShorteningService urlShorteningService;
    private final ClickService clickService;
    private final RequestInfoHandler requestInfoHandler;

    @Autowired
    public UrlRedirectController(
            UrlShorteningService urlShorteningService,
            ClickService clickService,
            RequestInfoHandler requestInfoHandler) {
        this.urlShorteningService = urlShorteningService;
        this.clickService = clickService;
        this.requestInfoHandler = requestInfoHandler;
    }

    @GetMapping("/{urlCode}")
    public ResponseEntity<?> redirectUrl(
            @PathVariable String urlCode,
            @RequestHeader(value = "Password", required = false) String password, 
            HttpServletRequest request) {

        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL isn't found with code: " + urlCode));

        String longUrl = urlShortening.getLongUrl();

        // Get all request information
        RequestInfo requestInfo = requestInfoHandler.extractRequestInfo(request);

        // Record click with enhanced information
        clickService.recordClick(
                urlShortening.getId(),
                requestInfo.getIpAddress(),
                requestInfo.getUserAgent(),
                requestInfo.getReferer()
        );

        if (urlShortening.isPasswordProtected()) {
            // If a password isn't provided or incorrect, return 401 Unauthorized
            if (password == null || !password.equals(urlShortening.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("This URL is password-protected. Please provide a valid password.");
            }
        }

        // Redirect to the long URL
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
