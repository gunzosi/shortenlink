package boostech.code.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UrlProtected {
    String urlCode;
    String password;

    public UrlProtected() {
    }
}
