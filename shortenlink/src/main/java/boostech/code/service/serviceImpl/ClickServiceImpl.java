package boostech.code.service.serviceImpl;

import boostech.code.models.ClickEvent;
import boostech.code.models.UrlShortening;
import boostech.code.payload.request.RequestInfo;
import boostech.code.payload.response.ClickStats;
import boostech.code.repository.ClickRepository;
import boostech.code.repository.UrlShorteningRepository;
import boostech.code.service.ClickService;
import com.maxmind.geoip2.model.AbstractCityResponse;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClickServiceImpl implements ClickService {
    private final ClickRepository clickRepository;
    private final UrlShorteningRepository urlShorteningRepository;

    // Log
    private static final Logger logger = LoggerFactory.getLogger(ClickServiceImpl.class);

    @Autowired
    public ClickServiceImpl(ClickRepository clickRepository, UrlShorteningRepository urlShorteningRepository) {
        this.clickRepository = clickRepository;
        this.urlShorteningRepository = urlShorteningRepository;
    }

    @Override
    public void recordClick(UUID urlId, String ipAddress, UserAgent userAgent, String referer) {
        logger.info("Recording click for URL ID: {}", urlId);

        try {
            UrlShortening urlShortening = urlShorteningRepository.findById(urlId)
                    .orElseThrow(() -> new EntityNotFoundException("URL isn't found with ID: " + urlId));

            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setUrlShortening(urlShortening);
            clickEvent.setIpAddress(ipAddress);

            if (userAgent != null) {
                clickEvent.setUserAgent(userAgent.toString());
                if (userAgent.getBrowser() != null) {
                    clickEvent.setBrowser(userAgent.getBrowser().getName());
                }
                if (userAgent.getOperatingSystem() != null) {
                    clickEvent.setOs(userAgent.getOperatingSystem().getName());
                }
                clickEvent.setDeviceType(determineDeviceType(userAgent));
            }

            clickEvent.setReferer(referer);

            RequestInfo requestInfo = new RequestInfo();
            if (requestInfo.getCountry() != null) {
                clickEvent.setCountry(requestInfo.getCountry());
            }
            if (requestInfo.getCity() != null) {
                clickEvent.setCity(requestInfo.getCity());
            }

            clickRepository.save(clickEvent);
            logger.info("Click recorded successfully for URL ID: {}", urlId);

        } catch (Exception e) {
            logger.error("Error recording click for URL ID: {}", urlId, e);
            throw e;
        }
    }


    private String determineDeviceType(UserAgent userAgent) {
        if (userAgent == null || userAgent.getOperatingSystem() == null) {
            return "UNKNOWN";
        }

        OperatingSystem os = userAgent.getOperatingSystem();
        DeviceType deviceType = os.getDeviceType();

        logger.info("----------------- Device type: {}", deviceType);

        return switch (deviceType) {
            case COMPUTER -> "DESKTOP";
            case TABLET -> "TABLET";
            case MOBILE -> "MOBILE";
            case GAME_CONSOLE -> "GAME_CONSOLE";
            case DMR -> "DIGITAL_MEDIA_RECEIVER";
            case WEARABLE -> "WEARABLE";
            default -> "UNKNOWN";
        };
    }



    @Override
    public ClickStats getClickStats(String urlCode) {
        UrlShortening urlShortening = urlShorteningRepository.findByUrlCode(urlCode)
                .orElseThrow(() -> new EntityNotFoundException("URL not found with code: " + urlCode));

        UUID urlId = urlShortening.getId();

        ClickStats stats = new ClickStats();
        stats.setTotalClicks(clickRepository.countClicksByUrlId(urlId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> clicksByDay = clickRepository.countClicksByDay(urlId).stream()
                .collect(Collectors.toMap(
                        obj -> ((LocalDate) obj[0]).format(formatter),
                        obj -> (Long) obj[1]
                ));
        stats.setClicksByDay(clicksByDay);

        List<ClickEvent> recentClicks = clickRepository.findRecentClicksByUrlId(urlId);
        stats.setRecentClicks(recentClicks);

        return stats;
    }

    @Override
    public ClickStats getClickStats(UUID urlId) {
        UrlShortening urlShortening = urlShorteningRepository.findById(urlId)
                .orElseThrow(() -> new EntityNotFoundException("URL isn't found with ID: " + urlId));

        ClickStats stats = new ClickStats();
        List<ClickEvent> clickEvents = clickRepository.findByUrlShortening_Id(urlId);

        stats.setTotalClicks(clickEvents.size());

        Map<String, Long> clicksByDay = clickEvents.stream()
                .collect(Collectors
                        .groupingBy(clickEvent -> clickEvent
                                        .getClickedAt()
                                        .toLocalDate()
                                        .toString(),
                                Collectors.counting()));
        stats.setClicksByDay(clicksByDay);

        Map<String, Long> clicksByBrowser = clickEvents.stream()
                .collect(Collectors.groupingBy(ClickEvent::getBrowser,
                        Collectors.counting()));
        stats.setClicksByBrowser(clicksByBrowser);


        Map<String, Long> clicksByOS = clickEvents.stream()
                .collect(Collectors.groupingBy(ClickEvent::getOs,
                        Collectors.counting()));
        stats.setClicksByOS(clicksByOS);


        Map<String, Long> clicksByDevice = clickEvents.stream()
                .collect(Collectors.groupingBy(ClickEvent::getDeviceType,
                        Collectors.counting()));
        stats.setClicksByDevice(clicksByDevice);

        List<ClickEvent> recentClicks = clickEvents.stream()
                .limit(10)
                .collect(Collectors.toList());
        stats.setRecentClicks(recentClicks);

        return stats;
    }


}
