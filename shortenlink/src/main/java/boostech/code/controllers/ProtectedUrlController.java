package boostech.code.controllers;

import boostech.code.models.UrlShortening;
import boostech.code.payload.request.UrlProtected;
import boostech.code.payload.response.ApiResponse;
import boostech.code.payload.response.UrlResponseDTO;
import boostech.code.service.UrlShorteningService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
public class ProtectedUrlController {

    @Autowired
    private final UrlShorteningService urlShorteningService;

    @PostMapping("/protect-url")
    public ApiResponse<UrlResponseDTO> createPasswordProtectedShortUrl(@RequestBody UrlProtected urlRequestProtected) {
        return urlShorteningService.createPasswordProtectedShortUrl(urlRequestProtected);
    }

    @PostMapping("/validate-url")
    public ResponseEntity<?> validatePassword(@RequestBody Map<String, String> payload) {
        String urlCode = payload.get("urlCode");
        String password = payload.get("password");

        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL doesn't exist: " + urlCode));

        if (urlShortening.isPasswordProtected()) {
            if (password == null || !password.equals(urlShortening.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "Error", "message", "Wrong password!"));
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "Success",
                "message", "Authentication successful!",
                "data", urlShortening.getLongUrl()
        ));
    }

    @PutMapping("/update-url")
    public ApiResponse<UrlResponseDTO> updatePasswordProtectedShortUrl(@RequestParam String urlCode, @RequestParam String newPassword) {
        return urlShorteningService.updatePasswordProtectedShortUrl(urlCode, newPassword);
    }

    @DeleteMapping("/delete-url")
    public ApiResponse<UrlResponseDTO> deletePasswordProtectedShortUrl(@RequestParam String urlCode) {
        return urlShorteningService.deletePasswordProtectedShortUrl(urlCode);
    }

    @GetMapping("/is-protected/{urlCode}")
    public boolean isUrlProtected(@PathVariable String urlCode) {
        return urlShorteningService.isUrlProtected(urlCode);
    }
}
