package async_notification_engine.controller;

import async_notification_engine.model.NotificationLog;
import async_notification_engine.repository.NotificationRepository;
import async_notification_engine.service.FcmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;

    public NotificationController(FcmService fcmService, NotificationRepository notificationRepository) {
        this.fcmService = fcmService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "notification-engine");
    }

    @PostMapping("/test")
    public Map<String, String> triggerTest(@RequestBody Map<String, String> payload) {
        logger.info("Received test notification request: {}", payload);
        
        NotificationLog log = new NotificationLog();
        String userId = payload.get("userId");
        String title = payload.get("title");
        String body = payload.get("body");
        String token = payload.get("deviceToken");
        
        log.setUserId(userId);
        log.setTitle(title);
        log.setMessage(body);
        log.setDeviceToken(token);
        log.setType("PUSH_TEST");

        try {
            if (token != null && !token.isEmpty()) {
                fcmService.sendPushNotification(token, title, body);
                log.setStatus("SENT");
                return Map.of("status", "success", "message", "Notification sent successfully");
            } else {
                log.setStatus("FAILED");
                log.setErrorDetails("Device token missing");
                return Map.of("status", "error", "message", "Device token missing");
            }
        } catch (Exception e) {
            logger.error("Error sending test notification: {}", e.getMessage());
            log.setStatus("FAILED");
            log.setErrorDetails(e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        } finally {
            notificationRepository.save(log);
        }
    }
}
