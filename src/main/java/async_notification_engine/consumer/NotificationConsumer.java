package async_notification_engine.consumer;

import async_notification_engine.model.NotificationLog;
import async_notification_engine.repository.NotificationRepository;
import async_notification_engine.service.FcmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationConsumer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationConsumer(FcmService fcmService, NotificationRepository notificationRepository) {
        this.fcmService = fcmService;
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "${app.notification.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String messageContent) {
        logger.info("Consumed notification event: {}", messageContent);
        
        NotificationLog log = new NotificationLog();
        try {
            Map<String, String> payload = objectMapper.readValue(messageContent, Map.class);
            String userId = payload.get("userId");
            String title = payload.get("title");
            String body = payload.get("body");
            String token = payload.get("deviceToken");
            
            log.setUserId(userId);
            log.setTitle(title);
            log.setMessage(body);
            log.setDeviceToken(token);
            log.setType("PUSH");

            if (token != null && !token.isEmpty()) {
                fcmService.sendPushNotification(token, title, body);
                log.setStatus("SENT");
            } else {
                logger.warn("No device token found for user: {}", userId);
                log.setStatus("FAILED");
                log.setErrorDetails("Device token missing");
            }
        } catch (Exception e) {
            logger.error("Error processing notification: {}", e.getMessage());
            log.setStatus("FAILED");
            log.setErrorDetails(e.getMessage());
        } finally {
            notificationRepository.save(log);
        }
    }
}
