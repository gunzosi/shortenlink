package boostech.code.payload.request;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestInfo {
    private String ipAddress;
    private UserAgent userAgent;
    private String referer;
    private String country;
    private String city;

    public RequestInfo() {
    }
}
