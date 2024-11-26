package boostech.code.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "clicks")
public class ClickEvent {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "url_id", nullable = false)
    @JsonIgnore
    private UrlShortening urlShortening;

    @Column(name = "clicked_at")
    private OffsetDateTime clickedAt = OffsetDateTime.now();

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = Integer.MAX_VALUE)
    private String userAgent;

    @Column(name = "referer", length = Integer.MAX_VALUE)
    private String referer;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 50)
    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Size(max = 50)
    @Column(name = "browser", length = 50)
    private String browser;

    @Size(max = 50)
    @Column(name = "os", length = 50)
    private String os;
}
