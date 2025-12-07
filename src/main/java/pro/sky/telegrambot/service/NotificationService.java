package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationTaskRepository notificationTaskRepository;
    private final ReminderService reminderService;
    private final NotificationSender notificationSender;

    @Autowired
    public NotificationService(
            NotificationTaskRepository notificationTaskRepository,
            ReminderService reminderService,
            NotificationSender notificationSender
    ) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.reminderService = reminderService;
        this.notificationSender = notificationSender;
    }

    public void processMessage(String message, Long chatId) {
        reminderService.processReminder(message, chatId);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationTask> tasks = notificationTaskRepository.findBySendTimeBefore(now);

        if (tasks.isEmpty()) {
            logger.info("Нет задач для отправки на текущее время: {}", now);
            return;
        }

        for (NotificationTask task : tasks) {
            try {

                notificationSender.sendMessage(task.getChatId(), "⏰ Напоминание: " + task.getNotificationText());
                logger.info("✅ Отправлено: {}", task.getNotificationText());


                notificationTaskRepository.delete(task);

            } catch (Exception e) {
                logger.error("❌ Ошибка отправки: {}", task.getNotificationText(), e);
            }
        }
    }
}

