package boostech.code.controllers;

import boostech.code.models.UrlShortening;
import boostech.code.payload.request.RequestInfo;
import boostech.code.service.ClickService;
import boostech.code.service.UrlShorteningService;
import boostech.code.payload.request.RequestInfo;
import boostech.code.utils.RequestInfoHandler;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// 100% free access from source , dont need JWT token
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
    public ResponseEntity<Void> redirectUrl(
            @PathVariable String urlCode,
            HttpServletRequest request) {

        UrlShortening urlShortening = urlShorteningService.getLongUrlByCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("Don't find the URL with: " + urlCode));

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

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }


}