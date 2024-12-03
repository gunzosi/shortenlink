package boostech.code.repository;

import boostech.code.models.Advertisement;
import boostech.code.payload.response.UrlAdvertisementRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import boostech.code.payload.response.UrlAdvertisementRes;

import java.util.List;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    Advertisement findByUrl_Id(UUID urlId);

    @Query(value = """
        SELECT us.id, us.long_url, us.short_url, us.url_code,\s
               a.text_content, a.countdown_duration,\s
               ai.image_filename, ai.image_path
        FROM url_shortening us
        JOIN advertisements a ON us.id = a.url_id
        JOIN advertisement_images ai ON a.id = ai.advertisement_id
        WHERE us.id = :urlId
      \s""", nativeQuery = true)
    List<Object[]> findAdvertisementDetailsByUrlId(@Param("urlId") UUID urlId);

}
