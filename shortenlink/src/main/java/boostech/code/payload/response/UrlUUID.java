package boostech.code.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UrlUUID {
    private String message;
    private UUID urlUUID;

    public UrlUUID() {
    }
}
