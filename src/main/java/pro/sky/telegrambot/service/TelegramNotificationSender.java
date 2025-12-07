package pro.sky.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pro.sky.telegrambot.model.TelegramBot;


@Service
public class TelegramNotificationSender implements NotificationSender {

    private final TelegramBot telegramBot;

    @Autowired
    public TelegramNotificationSender(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            telegramBot.execute(message);
            System.out.println("✅ Сообщение отправлено: " + text.substring(0, Math.min(text.length(), 50)) + "...");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки: " + e.getMessage());
        }
    }
}
