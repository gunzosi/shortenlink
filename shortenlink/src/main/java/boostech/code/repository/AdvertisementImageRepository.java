package boostech.code.repository;

import boostech.code.models.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, UUID> {
    List<AdvertisementImage> findByAdvertisement_Id(UUID advertisementId);
}

