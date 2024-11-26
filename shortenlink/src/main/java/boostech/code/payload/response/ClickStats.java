package boostech.code.payload.response;

import boostech.code.models.ClickEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
//@Builder
public class ClickStats {
    private long totalClicks;
    private Map<String, Long> clicksByDay;
    private Map<String, Long> clicksByCountry;
    private Map<String, Long> clicksByBrowser;
    private Map<String, Long> clicksByOS;
    private Map<String, Long> clicksByDevice;
    private List<ClickEvent> recentClicks;

    public ClickStats() {
    }
}
