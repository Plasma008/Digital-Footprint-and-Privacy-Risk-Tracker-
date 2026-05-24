package privacychecker.repository;

import privacychecker.model.BrowserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrowserSessionRepository extends JpaRepository<BrowserSessionEntity, Long> {
    List<BrowserSessionEntity> findByUsernameOrderByScannedAtDesc(String username);
}
