package boostech.code.repository;

import boostech.code.models.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    Advertisement findByUrl_Id(UUID urlId);
}
