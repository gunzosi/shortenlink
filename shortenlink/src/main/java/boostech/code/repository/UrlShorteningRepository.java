package boostech.code.repository;

import boostech.code.models.UrlShortening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlShorteningRepository extends JpaRepository<UrlShortening, UUID> {
    Optional<UrlShortening> findByShortUrl(String shortUrl);
    Optional<UrlShortening> findByLongUrl(String longUrl);

    boolean existsByShortUrl(String shortUrl);
    boolean existsByLongUrl(String longUrl);

    @Modifying
    @Transactional
    @Query("UPDATE UrlShortening u SET u.shortUrl = :shortUrl WHERE u.id = :id")
    void updateShortUrl(UUID id, String shortUrl);

    @Modifying
    @Transactional
    @Query("UPDATE UrlShortening u SET u.longUrl = :longUrl WHERE u.id = :id")
    void updateLongUrl(UUID id, String longUrl);

    @Modifying
    @Transactional
    @Query("UPDATE UrlShortening u SET u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    void updateUpdatedAt(UUID id);

    @Modifying
    @Transactional
    void deleteByShortUrl(String shortUrl);

    @Modifying
    @Transactional
    void deleteAll();

    Optional<UrlShortening> findByUrlCode(String urlCode);

    @Modifying
    @Transactional
    @Query("UPDATE UrlShortening u SET u.urlCode = :urlCode, u.shortUrl = :shortUrl WHERE u.id = :id")
    void updateUrlCodeAndShortUrl(UUID id, String urlCode, String shortUrl);

    // GET UUID by shortUrl
    @Query("SELECT u.id FROM UrlShortening u WHERE u.shortUrl = :shortUrl")
    UUID findIdByShortUrl(String shortUrl);

    @Query("SELECT u.id FROM UrlShortening u WHERE u.urlCode = :urlCode")
    Optional<UUID> findUuidByUrlCode(@Param("urlCode") String urlCode);

    Optional<UrlShortening> findByUrlCodeAndUser_Id(String urlCode, Long userId);
    List<UrlShortening> findByUser_Id(Long userId);


}
