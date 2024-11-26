package boostech.code.service.serviceImpl;

import boostech.code.component.AuthenticationFacade;
import boostech.code.exception.ResourceNotFoundException;
import boostech.code.models.UrlShortening;
import boostech.code.payload.request.UrlRequest;
import boostech.code.payload.request.UrlRequestUpdate;
import boostech.code.payload.response.UrlResponse;
import boostech.code.repository.UrlShorteningRepository;
import boostech.code.repository.UserRepository;
import boostech.code.service.UrlShorteningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class UrlShorteningServiceImpl implements UrlShorteningService {
    private final UrlShorteningRepository urlShorteningRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authenticationFacade;


    private final String urlControllerDevelop = "http://localhost:8080/api/v1/";
    private final String localUrl = "http://localhost:8080/";

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(UrlShorteningServiceImpl.class);

    @Autowired
    public UrlShorteningServiceImpl(UrlShorteningRepository urlShorteningRepository, UserRepository userRepository, AuthenticationFacade authenticationFacade) {
        this.urlShorteningRepository = urlShorteningRepository;
        this.userRepository = userRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public UrlResponse shortenUrl(UrlRequest urlRequest) {
        // Take the URL from the request

        String urlCode = generateUrlCode();

        while (urlShorteningRepository.existsByShortUrl(localUrl + urlCode)) {
            urlCode = generateUrlCode();
        }

        String urlController = urlControllerDevelop + urlCode;

        UrlShortening urlShortening = new UrlShortening();
        urlShortening.setLongUrl(urlRequest.getLongUrl());
        urlShortening.setShortUrl(localUrl + urlCode);
        urlShortening.setUrlCode(urlCode);
        urlShorteningRepository.save(urlShortening);

        return new UrlResponse("Success", urlShortening.getShortUrl(), urlController);
    }

    @Override
    @Transactional
    public UrlResponse updateUrlCode(String urlCode, UrlRequest urlRequest) {
        Optional<UrlShortening> urlShorteningOpt = urlShorteningRepository.findByShortUrl(localUrl + urlCode);

        if (urlShorteningOpt.isEmpty()) {
            return new UrlResponse("Error", "URL not found", (List<?>) null);
        }

        UrlShortening urlShortening = urlShorteningOpt.get();

        urlShortening.setLongUrl(urlRequest.getLongUrl());

        String newUrlCode = urlRequest.getUrlCode();


        while (urlShorteningRepository.existsByShortUrl(localUrl + newUrlCode)) {
            newUrlCode = generateUrlCode();
        }

        String newShortUrl = localUrl + newUrlCode;
        urlShortening.setUrlCode(newUrlCode);
        urlShortening.setShortUrl(newShortUrl);

        urlShorteningRepository.updateUrlCodeAndShortUrl(urlShortening.getId(), newUrlCode, newShortUrl);

        return new UrlResponse("Success", "URL has been updated", newShortUrl);
    }

    private String generateUrlCode() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().substring(0, 7);
    }

    @Override
    public UrlRequest redirectUrl(String urlCode) {
        Optional<UrlShortening> urlShortening = urlShorteningRepository
                .findByShortUrl(localUrl + urlCode);
        return urlShortening
                .map(shortening -> new UrlRequest(shortening.getLongUrl()))
                .orElse(null);
    }

    @Override
    public Optional<UrlShortening> getLongUrlByCode(String urlCode) {
        logger.info("Fetching URL of code: {}", urlCode);
        return urlShorteningRepository.findByShortUrl(localUrl + urlCode);
    }


    @Override
    public void deleteUrl(String urlCode) {
        urlShorteningRepository.findByShortUrl(localUrl + urlCode)
                .ifPresent(urlShorteningRepository::delete);

        System.out.println("URL deleted successfully");

        logger.info("URL deleted successfully -- CASCADE DELETE on ClickEvent");
    }

    @Override
    public boolean isUrlExists(String code) {
        return urlShorteningRepository.existsByShortUrl(localUrl + code);
    }


    @Override
    public UrlResponse getAllUrls() {
        List<UrlShortening> urlShortenings = urlShorteningRepository.findAll();

        if (urlShortenings.isEmpty()) {
            return new UrlResponse("Error", "No URLs found", (List<?>) null);
        }

        return new UrlResponse(
                "Success",
                "All URLs fetched successfully",
                urlShortenings
        );
    }


    @Override
    public Optional<UrlShortening> findByShortUrl(String shortUrl) {
        if (shortUrl == null || shortUrl.isEmpty()) {
            return Optional.empty();
        }
        return urlShorteningRepository.findByShortUrl(shortUrl);
    }

    @Override
    public boolean existsByShortUrl(String shortUrl) {
        if (shortUrl == null || shortUrl.isEmpty()) {
            return false;
        }

        return urlShorteningRepository.existsByShortUrl(shortUrl);
    }

    @Override
    public UrlResponse findIdByShortUrl(String shortUrl) {
        logger.info("Fetching ID for shortUrl: {}", shortUrl);

        Optional<UUID> urlUuid = urlShorteningRepository.findUuidByUrlCode(shortUrl);

        if (urlUuid.isEmpty()) {
            logger.error("A Short URL isn't found in a database: {}", shortUrl);
            return new UrlResponse("Error", "Short URL not found", (List<?>) null);
        }

        logger.info("URL found with UUID: {}", urlUuid.get());
        return new UrlResponse("Success", urlUuid.get().toString(), (List<?>) null);
    }

    @Override
    public UUID getUUIDByUrlCode(String urlCode) {
        return urlShorteningRepository.findUuidByUrlCode(urlCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL Code not found: " + urlCode));
    }

    // Update

    @Override
    public UrlResponse checkUrlCodeAvailability(String urlCode) {
        if (urlCode == null || urlCode.trim().isEmpty()) {
            return new UrlResponse(
                    "Error",
                    "URL code can't be empty",
                    (List<?>) null);
        }

        if (isValidUrlCode(urlCode)) {
            return new UrlResponse(
                    "Error",
                    "Invalid URL code format. Use only letters, numbers, and hyphens, length 3-50 characters",
                    (List<?>) null);
        }

        Optional<UrlShortening> existingUrl = urlShorteningRepository.findByUrlCode(urlCode);
        if (existingUrl.isPresent()) {
            return new UrlResponse(
                    "Error",
                    "Custom path already exists",
                    (List<?>) null);
        }

        return new UrlResponse(
                "Success",
                "Valid custom path, you can use it",
                (List<?>) null);
    }

    private boolean isValidUrlCode(String urlCode) {
        if (urlCode == null || urlCode.trim().isEmpty()) {
            return true;
        }
        return !urlCode.matches("^[a-zA-Z0-9-]{3,50}$");
    }


    @Override
    public UrlResponse updateUrlCode(String oldUrlCode, UrlRequestUpdate urlRequestUpdate) {
        UrlShortening urlShortening = urlShorteningRepository.findByUrlCode(oldUrlCode)
                .orElseThrow(() -> new ResourceNotFoundException("@Service - URL isn't found with code: " + oldUrlCode));

        String newUrlCode = urlRequestUpdate.getCustomUrlCode();

        Optional<UrlShortening> existingUrl = urlShorteningRepository.findByUrlCode(newUrlCode);
        if (existingUrl.isPresent() && !existingUrl.get().getId().equals(urlShortening.getId())) {
            return new UrlResponse("Error", "@Service - Custom URL code already exists", (List<?>) null);
        }

        if(isValidUrlCode(newUrlCode)){
            return new UrlResponse("Error", "@Service - Custom URL code can only contain letters, numbers, and hyphens", (List<?>) null);
        }

        urlShortening.setUrlCode(newUrlCode);
        urlShortening.setShortUrl(localUrl + newUrlCode);

        UrlShortening updatedUrl = urlShorteningRepository.save(urlShortening);

        return new UrlResponse("Success", "@Service - URL code updated successfully", Collections.singletonList(updatedUrl));
    }


}
