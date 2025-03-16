package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);


            if (update.message() != null) {
                Message message = update.message();
                String messageText = message.text();
                Chat chat = message.chat();
                long chatId = chat.id();


                if ("/start".equals(messageText)) {
                    sendWelcomeMessage(chatId);
                } else {

                    notificationService.processMessage(messageText, chatId);
                    sendConfirmationMessage(chatId);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeMessage = "Привет! Добро пожаловать в нашего бота!";
        SendMessage message = new SendMessage(chatId, welcomeMessage);
        telegramBot.execute(message);
    }

    private void sendConfirmationMessage(long chatId) {
        String confirmationMessage = "Ваше напоминание успешно добавлено!";
        SendMessage message = new SendMessage(chatId, confirmationMessage);
        telegramBot.execute(message);
    }
}


