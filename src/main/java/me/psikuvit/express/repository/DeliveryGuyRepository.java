package me.psikuvit.express.repository;

import me.psikuvit.express.model.DeliveryGuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryGuyRepository extends JpaRepository<DeliveryGuy, Long> {
    List<DeliveryGuy> findByAvailable(Boolean available);
    Optional<DeliveryGuy> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
