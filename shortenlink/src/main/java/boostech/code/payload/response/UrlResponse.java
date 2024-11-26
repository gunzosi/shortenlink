package boostech.code.payload.response;

import boostech.code.models.UrlShortening;
import boostech.code.payload.response.ErrorDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UrlResponse {
    private String message;
    private String description;
    private String UUID;
    private String resource;
    private List<?> data;
    private List<ErrorDetail> errors = null;

    public UrlResponse() {
    }

    public UrlResponse(String message, String description, List<?> data) {
        this.message = message;
        this.description = description;
        this.data = data;
    }

    public UrlResponse(String message, String description, String resource) {
        this.message = message;
        this.description = description;
        this.resource = resource;
    }

    public UrlResponse(String message, String description, List<?> data, List<ErrorDetail> errors) {
        this.message = message;
        this.description = description;
        this.data = data;
        this.errors = errors;
    }
}
