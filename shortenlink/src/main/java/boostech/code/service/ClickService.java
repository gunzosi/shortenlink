package boostech.code.service;

import boostech.code.models.UrlShortening;
import boostech.code.payload.request.RequestInfo;
import boostech.code.payload.response.ClickStats;
import eu.bitwalker.useragentutils.UserAgent;

import java.util.UUID;

public interface ClickService {
    void recordClick(UUID urlId, String ipAddress, UserAgent userAgent, String referer);
    ClickStats getClickStats(String urlCode);
    ClickStats getClickStats(UUID urlId);
    void logClick(UrlShortening urlShortening, RequestInfo requestInfo);
}
