package boostech.code.utils;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum FileType {
    JPEG("image/jpeg", ".jpeg", ".jpg"),
    PNG("image/png", ".png"),
    GIF("image/gif", ".gif"),
    BMP("image/bmp", ".bmp"),
    WEBP("image/webp", ".webp");

    private final String mimeType;
    private final String[] extensions;

    FileType(String mimeType, String... extensions) {
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    public static boolean isValid(String fileName, String contentType) {
        if (fileName == null || contentType == null) return false;
        String fileExt = getFileExtension(fileName);

        return Arrays.stream(values())
                .anyMatch(fileType ->
                        Arrays.stream(fileType.getExtensions())
                                .anyMatch(ext -> ext.equalsIgnoreCase(fileExt)) &&
                                fileType.getMimeType().equalsIgnoreCase(contentType));
    }

    // Helper Function
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex).toLowerCase();
    }
}
