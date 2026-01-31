package async_notification_engine.repository;

import async_notification_engine.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByUserId(String userId);
    List<NotificationLog> findByStatus(String status);
}
