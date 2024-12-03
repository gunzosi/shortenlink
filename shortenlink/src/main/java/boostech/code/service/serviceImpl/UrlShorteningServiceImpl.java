package boostech.code.service.serviceImpl;

import boostech.code.component.AuthenticationFacade;
import boostech.code.dto.UrlShorteningDTO;
import boostech.code.exception.ResourceNotFoundException;
import boostech.code.exception.UnauthorizedAccessException;
import boostech.code.models.UrlShortening;
import boostech.code.models.User;
import boostech.code.payload.request.UrlProtected;
import boostech.code.payload.request.UrlRequest;
import boostech.code.payload.request.UrlRequestUpdate;
import boostech.code.payload.response.ApiResponse;
import boostech.code.payload.response.UrlResponse;
import boostech.code.payload.response.UrlResponseDTO;
import boostech.code.repository.UrlShorteningRepository;
import boostech.code.repository.UserRepository;
import boostech.code.service.UrlShorteningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


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

        // Take the current user
        UserDetailsImpl userDetails = authenticationFacade.getCurrentUser();
        User currentUser = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String urlCode = generateUrlCode();
        while (urlShorteningRepository.existsByShortUrl(localUrl + urlCode)) {
            urlCode = generateUrlCode();
        }

        // Take the URL from the request
        String urlController = urlControllerDevelop + urlCode;

        UrlShortening urlShortening = new UrlShortening();
        urlShortening.setLongUrl(urlRequest.getLongUrl());
        urlShortening.setShortUrl(localUrl + urlCode);
        urlShortening.setUrlCode(urlCode);
        urlShortening.setUser(currentUser);

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
    @Transactional
    public void deleteUrl(String urlCode) {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(urlCode, currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL isn't found or not authorized"));

        urlShorteningRepository.delete(urlShortening);
        logger.info("URL deleted successfully for user: {}", currentUserId);
    }

    @Override
    public boolean isUrlExists(String code) {
        return urlShorteningRepository.existsByShortUrl(localUrl + code);
    }


    @Override
    public UrlResponse getAllUrls() {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        List<UrlShortening> urlShortenings = urlShorteningRepository.findByUser_Id(currentUserId);

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
    @Transactional
    public UrlResponse updateUrlCodeV2(String oldUrlCode, UrlRequestUpdate urlRequestUpdate) {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(oldUrlCode, currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL not found or not authorized"));

        String newUrlCode = urlRequestUpdate.getCustomUrlCode();


        Optional<UrlShortening> existingUrl = urlShorteningRepository.findByUrlCode(newUrlCode);
        if (existingUrl.isPresent() && !existingUrl.get().getId().equals(urlShortening.getId())) {
            return new UrlResponse("Error", "URL code is not exist", (List<?>) null);
        }


        urlShortening.setUrlCode(newUrlCode);
        urlShortening.setShortUrl(localUrl + newUrlCode);

        UrlShortening updatedUrl = urlShorteningRepository.save(urlShortening);

        return new UrlResponse(
                "Success",
                "Update URL successfully",
                Collections.singletonList(updatedUrl)
        );
    }

    @Override
    public ApiResponse<UrlResponseDTO> createPasswordProtectedShortUrl(UrlProtected urlRequestProtected) {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(urlRequestProtected.getUrlCode(), currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL isn't found or not authorized"));

        if (urlShortening.isPasswordProtected()) {
            return new ApiResponse<>("Error", "URL is already password protected");
        }

        urlShortening.setPassword(urlRequestProtected.getPassword());
        urlShortening.setPasswordProtected(true);
        urlShorteningRepository.save(urlShortening);

        UrlResponseDTO responseDTO = new UrlResponseDTO(
                urlShortening.getUrlCode(),
                urlShortening.getLongUrl(),
                urlShortening.isPasswordProtected()
        );

        return new ApiResponse<>("Success", "URL is now password protected", responseDTO);
    }

    @Override
    public ApiResponse<String> validatePasswordProtectedShortUrl(String urlCode, String password) {
        UrlShortening urlShortening = urlShorteningRepository.findByUrlCode(urlCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (urlShortening.isPasswordProtected()) {
            if (urlShortening.getPassword().equals(password)) {
                return new ApiResponse<>("Success", "Password is correct", urlShortening.getLongUrl());
            } else {
                return new ApiResponse<>("Error", "Password is incorrect", null);
            }
        } else {
            return new ApiResponse<>("Error", "URL isn't password protected", null);
        }
    }

    @Override
    public ApiResponse<UrlResponseDTO> updatePasswordProtectedShortUrl(String urlCode, String newPassword) {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(urlCode, currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL not found or not authorized"));

        if (!urlShortening.isPasswordProtected()) {
            return new ApiResponse<>("Error", "URL is not password-protected");
        }

        urlShortening.setPassword(newPassword);
        urlShorteningRepository.save(urlShortening);

        UrlResponseDTO responseDTO = new UrlResponseDTO(
                urlShortening.getUrlCode(),
                urlShortening.getLongUrl(),
                urlShortening.isPasswordProtected()
        );

        return new ApiResponse<>("Success", "Password updated successfully", responseDTO);
    }

    @Override
    public ApiResponse<UrlResponseDTO> deletePasswordProtectedShortUrl(String urlCode) {
        Long currentUserId = authenticationFacade.getCurrentUserId();

        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(urlCode, currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL not found or not authorized"));

        if (!urlShortening.isPasswordProtected()) {
            return new ApiResponse<>("Error", "URL is not password-protected");
        }

        urlShortening.setPasswordProtected(false);
        urlShortening.setPassword(null);
        urlShorteningRepository.save(urlShortening);

        UrlResponseDTO responseDTO = new UrlResponseDTO(
                urlShortening.getUrlCode(),
                urlShortening.getLongUrl(),
                urlShortening.isPasswordProtected()
        );

        return new ApiResponse<>("Success", "Password protection removed from URL", responseDTO);
    }



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

//    @Override
//    public UrlResponse getUserUrls() {
//        Long currentUserId = authenticationFacade.getCurrentUserId();
//        List<UrlShortening> userUrls = urlShorteningRepository.findByUser_Id(currentUserId);
//
//        if (userUrls.isEmpty()) {
//            return new UrlResponse(
//                    "Error",
//                    "No URLs found for the current user",
//                    (List<?>) null
//            );
//        }
//
//        return new UrlResponse(
//                "Success",
//                "User URLs retrieved successfully",
//                userUrls
//        );
//    }

    @Override
    public UrlResponse getUserUrls() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<UrlShortening> userUrls = urlShorteningRepository.findByUser(currentUser);

        List<UrlShorteningDTO> urlDTOs = userUrls.stream()
                .map(url -> new UrlShorteningDTO(
                        url.getId(),
                        url.getLongUrl(),
                        url.getShortUrl(),
                        url.getUrlCode(),
                        url.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new UrlResponse("Success", "User URLs retrieved", urlDTOs);
    }

    @Override
    public UrlResponse getUserUrlsById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Long currentUserId = authenticationFacade.getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            UserDetailsImpl currentUser = authenticationFacade.getCurrentUser();
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new UnauthorizedAccessException("You aren't authorized to view this user's URLs");
            }
        }

        List<UrlShortening> userUrls = urlShorteningRepository.findByUser_Id(userId);

        if (userUrls.isEmpty()) {
            return new UrlResponse(
                    "Error",
                    "No URLs found for the user",
                    (List<?>) null
            );
        }

        return new UrlResponse(
                "Success",
                "User URLs retrieved successfully",
                userUrls
        );
    }

    @Override
    public User findUserByUrlCode(String urlCode) {
        UrlShortening urlShortening = urlShorteningRepository.findByUrlCode(urlCode)
                .orElseThrow(() -> new ResourceNotFoundException("URL isn't found with code: " + urlCode));

        return urlShortening.getUser();
    }

    private boolean isValidUrlCode(String urlCode) {
        if (urlCode == null || urlCode.trim().isEmpty()) {
            return true;
        }
        return !urlCode.matches("^[a-zA-Z0-9-]{3,50}$");
    }


    @Override
    @Transactional
    public UrlResponse updateUrlCode(String urlCode, UrlRequestUpdate urlRequest) {

        Long currentUserId = authenticationFacade.getCurrentUserId();


        UrlShortening urlShortening = urlShorteningRepository.findByUrlCodeAndUser_Id(urlCode, currentUserId)
                .orElseThrow(() -> new UnauthorizedAccessException("URL not found or not authorized"));


        String newUrlCode = urlRequest.getCustomUrlCode();
        Optional<UrlShortening> existingUrl = urlShorteningRepository.findByUrlCode(newUrlCode);

        if (existingUrl.isPresent() && !existingUrl.get().getId().equals(urlShortening.getId())) {
            return new UrlResponse("Error", "Custom URL code already exists", (List<?>) null);
        }


        urlShortening.setUrlCode(newUrlCode);
        urlShortening.setShortUrl(localUrl + newUrlCode);

        UrlShortening updatedUrl = urlShorteningRepository.save(urlShortening);

        return new UrlResponse(
                "Success",
                "URL code updated successfully",
                Collections.singletonList(updatedUrl)
        );
    }


}
