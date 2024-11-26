package boostech.code.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import boostech.code.payload.request.RequestInfo;

@Component
public class RequestInfoHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestInfoHandler.class);

    private DatabaseReader databaseReader;

    @Value("${geoip.database.path}")
    private String geoIpDatabasePath;

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    );

    @PostConstruct
    public void init() {
        try {
            File database = new File(geoIpDatabasePath);
            databaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            logger.error("Couldn't initialize GeoIP database", e);
        }
    }

    public RequestInfo extractRequestInfo(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        UserAgent userAgent = getUserAgent(request);
        String referer = getReferer(request);

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setIpAddress(ipAddress);
        requestInfo.setUserAgent(userAgent);
        requestInfo.setReferer(referer);

        try {
            if (databaseReader != null && ipAddress != null) {
                CityResponse response = databaseReader.city(InetAddress.getByName(ipAddress));
                requestInfo.setCountry(response.getCountry().getName());
                requestInfo.setCity(response.getCity().getName());
            }
        } catch (IOException | GeoIp2Exception e) {
            logger.warn("Couldn't determine location for IP: {}", ipAddress, e);
        }

        return requestInfo;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        return IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(ip -> ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
                .map(ip -> ip.split(",")[0].trim())
                .findFirst()
                .orElseGet(request::getRemoteAddr);
    }

    private UserAgent getUserAgent(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");
        if (userAgentString == null || userAgentString.isEmpty()) {
            logger.warn("User-Agent header is missing or empty");
            return UserAgent.parseUserAgentString("unknown");
        }
        return UserAgent.parseUserAgentString(userAgentString);
    }


    private String getReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return referer != null ? referer : "direct";
    }
}
