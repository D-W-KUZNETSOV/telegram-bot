package pro.sky.telegrambot.service;

public interface NotificationSender {
    void sendMessage(Long chatId, String text);
}
