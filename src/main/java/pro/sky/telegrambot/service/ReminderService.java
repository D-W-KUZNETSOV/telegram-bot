package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReminderService {

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    public boolean processReminder(String message, Long chatId) {
        try {
            String regex = "(\\d{2}\\.\\d{2}\\.\\d{4})\\s*(\\d{2}:\\d{2})(\\s+)(.+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                String dateStr = matcher.group(1);
                String timeStr = matcher.group(2);
                String notificationText = matcher.group(4);

                String dateTimeStr = dateStr + " " + timeStr;
                LocalDateTime sendTime = LocalDateTime.parse(dateTimeStr,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                NotificationTask notificationTask = new NotificationTask();
                notificationTask.setChatId(chatId);
                notificationTask.setNotificationText(notificationText);
                notificationTask.setSendTime(sendTime);
                notificationTask.setCreatedAt(LocalDateTime.now());

                notificationTaskRepository.save(notificationTask);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки напоминания: " + e.getMessage());
        }
        return false;
    }
}
