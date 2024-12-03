package boostech.code.utils;

import boostech.code.exception.FileUploadException;
import boostech.code.exception.FileValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class FileUploadUtil {
    @Value("${app.upload.dir}")
    private String baseUploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);

        String uploadDir = Paths.get(baseUploadDir, subDirectory).toString();
        createDirectoryIfNotExists(uploadDir);

        String cleanFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = generateUniqueFileName(cleanFileName);

        Path filePath = Paths.get(uploadDir, uniqueFileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", uniqueFileName);
            return uniqueFileName;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", uniqueFileName, e);
            throw new FileUploadException("Cannot save the file: " + uniqueFileName, e);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        return UUID.randomUUID() + "_" + originalFileName;
    }

    public void deleteFile(String fileName, String subDirectory) throws IOException {
        Path filePath = Paths.get(baseUploadDir, subDirectory, fileName);
        Files.deleteIfExists(filePath);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File can't be empty");
        }

        String cleanFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (cleanFileName.contains("..")) {
            throw new FileValidationException("Invalid filename: " + cleanFileName);
        }

        if (file.getSize() > maxFileSize) {
            throw new FileValidationException("File size exceeds maximum limit of " + maxFileSize / 1024 / 1024 + "MB");
        }

        String contentType = file.getContentType();
        if (!FileType.isValid(cleanFileName, contentType)) {
            throw new FileValidationException("Only image formats (JPEG, PNG, GIF, BMP, WEBP) are allowed");
        }
    }



    private void createDirectoryIfNotExists(String directoryPath) throws IOException {
        Path uploadPath = Paths.get(directoryPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }
}