package boostech.code.controllers;

import boostech.code.models.UrlShortening;
import boostech.code.service.ClickService;
import boostech.code.service.UrlShorteningService;
import boostech.code.utils.RequestInfoHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/")
public class UrlRedirectController {

    private final UrlShorteningService urlShorteningService;
    private final ClickService clickService;
    private final RequestInfoHandler requestInfo;

    @Autowired
    public UrlRedirectController(
            UrlShorteningService urlShorteningService,
            ClickService clickService,
            RequestInfoHandler requestInfoHandler) {
        this.urlShorteningService = urlShorteningService;
        this.clickService = clickService;
        this.requestInfo = requestInfoHandler;
    }

    private final String URL_FRONTEND = "http://localhost:3000";

    @GetMapping("/{urlCode}")
    public void redirectUrl(
            @PathVariable String urlCode,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL don't find any: " + urlCode));

        clickService.recordClick(
                urlShortening.getId(),
                requestInfo.getClientIpAddress(request),
                requestInfo.getUserAgent(request),
                requestInfo.getReferer(request)
        );

        if (urlShortening.isPasswordProtected()) {
            response.sendRedirect(URL_FRONTEND + "/password-form?urlCode=" + urlCode);
            return;
        }

        response.sendRedirect(urlShortening.getLongUrl());
    }

    @PostMapping("/{urlCode}")
    public ResponseEntity<?> redirectWithPassword(
            @PathVariable String urlCode,
            @RequestBody Map<String, String> payload,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String password = payload.get("password");

        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL don't find any:" + urlCode));

        if (!urlShortening.isPasswordProtected()) {
            response.sendRedirect(urlShortening.getLongUrl());
            return ResponseEntity.ok().build();
        }

        if (!urlShortening.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong password!");
        }

        clickService.recordClick(
                urlShortening.getId(),
                requestInfo.getClientIpAddress(request),
                requestInfo.getUserAgent(request),
                requestInfo.getReferer(request)
        );

        return ResponseEntity.ok().body(Map.of("url", urlShortening.getLongUrl()));
    }
}
