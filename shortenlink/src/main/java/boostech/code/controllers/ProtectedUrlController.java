package boostech.code.controllers;

import boostech.code.payload.request.UrlProtected;
import boostech.code.payload.response.ApiResponse;
import boostech.code.payload.response.UrlResponseDTO;
import boostech.code.service.UrlShorteningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class ProtectedUrlController {
    @Autowired
    private UrlShorteningService urlShorteningService;

    @PostMapping("/protect-url")
    public ApiResponse<UrlResponseDTO> createPasswordProtectedShortUrl(@RequestBody UrlProtected urlRequestProtected) {
        return urlShorteningService.createPasswordProtectedShortUrl(urlRequestProtected);
    }

    @PostMapping("/validate-url")
    public ApiResponse<String> validatePasswordProtectedShortUrl(@RequestParam String urlCode, @RequestParam String password) {
        return urlShorteningService.validatePasswordProtectedShortUrl(urlCode, password);
    }

    @PutMapping("/update-url")
    public ApiResponse<UrlResponseDTO> updatePasswordProtectedShortUrl(@RequestParam String urlCode, @RequestParam String newPassword) {
        return urlShorteningService.updatePasswordProtectedShortUrl(urlCode, newPassword);
    }

    @DeleteMapping("/delete-url")
    public ApiResponse<UrlResponseDTO> deletePasswordProtectedShortUrl(@RequestParam String urlCode) {
        return urlShorteningService.deletePasswordProtectedShortUrl(urlCode);
    }
}
