package pro.sky.telegrambot.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class MessageProcessor {
    private final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private final NotificationTaskRepository notificationTaskRepository;

    public MessageProcessor(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
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
}
