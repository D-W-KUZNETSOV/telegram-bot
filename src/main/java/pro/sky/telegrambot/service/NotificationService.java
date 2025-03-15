package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationService(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    public void processMessage(String message, Long chatId) {
        String regex = "(\\d{2}\\.\\d{2}\\.\\d{4})\\s*(\\d{2}:\\d{2})(\\s+)(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {
            String dateStr = matcher.group(1);
            String timeStr = matcher.group(2);
            String notificationText = matcher.group(4);


            String dateTimeStr = dateStr + " " + timeStr;
            LocalDateTime sendTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(chatId);
            notificationTask.setNotificationText(notificationText);
            notificationTask.setSendTime(sendTime);
            notificationTask.setScheduledTime(LocalDateTime.now());

            notificationTaskRepository.save(notificationTask);
            logger.info("Задача добавлена: {}", notificationTask);
        } else {
            logger.warn("Неверный формат сообщения: {}", message);
        }
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
