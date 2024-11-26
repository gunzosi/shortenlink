package boostech.code.repository;

import boostech.code.models.ClickEvent;
import boostech.code.models.UrlShortening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClickRepository extends JpaRepository<ClickEvent, UUID> {
    List<ClickEvent> findByUrlShorteningId(UUID urlId);

    @Query("SELECT c FROM ClickEvent c WHERE c.urlShortening.id = :urlId ORDER BY c.clickedAt DESC")
    List<ClickEvent> findRecentClicksByUrlId(UUID urlId);

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.urlShortening.id = :urlId")
    long countClicksByUrlId(UUID urlId);

    @Query("SELECT c.country, COUNT(c) FROM ClickEvent c WHERE c.urlShortening.id = :urlId GROUP BY c.country")
    List<Object[]> countClicksByCountry(UUID urlId);

    @Query("SELECT FUNCTION('DATE', c.clickedAt), COUNT(c) FROM ClickEvent c WHERE c.urlShortening.id = :urlId GROUP BY FUNCTION('DATE', c.clickedAt)")
    List<Object[]> countClicksByDay(UUID urlId);
    List<ClickEvent> findByUrlShortening_Id(UUID urlId);
}
