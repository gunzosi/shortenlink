package boostech.code.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "url_shortening")
public class UrlShortening {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "long_url", nullable = false, length = Integer.MAX_VALUE)
    private String longUrl;

    @Column(name = "short_url", nullable = false, length = Integer.MAX_VALUE)
    private String shortUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "url_code", nullable = false, length = Integer.MAX_VALUE)
    private String urlCode;

    @Column(name = "password", nullable = true, length = Integer.MAX_VALUE)
    private String password;

    @Column(name = "is_password_protected", nullable = false)
    private boolean isPasswordProtected;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public String randomUniqueUrlCode() {
        int codeLength = (int) (Math.random() * 3) + 6;
        return UUID.randomUUID().toString().replace("-", "").substring(0, codeLength);
    }

    public void setPasswordProtected(boolean isPasswordProtected) {
        this.isPasswordProtected = isPasswordProtected;
    }

    public boolean isPasswordProtected() {
        return isPasswordProtected;
    }
}
