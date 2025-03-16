package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.message.MessageProcessor;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;
    private final MessageProcessor messageProcessor;

    public NotificationService(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot, MessageProcessor messageProcessor) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
        this.messageProcessor = messageProcessor;
    }

    public void processMessage(String message, Long chatId) {
        messageProcessor.processMessage(message, chatId);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = notificationTaskRepository.findBySendTimeBefore(now);

        if (tasks.isEmpty()) {
            logger.info("Нет задач для отправки на текущее время: {}", now);
            return;
        }

        for (NotificationTask task : tasks) {
            sendNotification(task);
            notificationTaskRepository.delete(task);
        }
    }

    public void sendNotification(NotificationTask task) {
        SendMessage message = new SendMessage(task.getChatId(), task.getNotificationText());
        try {
            SendResponse response = telegramBot.execute(message);
            if (!response.isOk()) {
                logger.error("Ошибка при отправке сообщения: {} {}", response.errorCode(), response.description());
            } else {
                logger.info("Уведомление отправлено: {}", task.getNotificationText());
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке уведомления", e);
        }
    }
}

