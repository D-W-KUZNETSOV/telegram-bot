package pro.sky.telegrambot.model;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import pro.sky.telegrambot.service.ReminderService;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private ReminderService reminderService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            Long chatId = update.getMessage().getChatId();

            if ("/start".equalsIgnoreCase(messageText)) {
                sendWelcomeMessage(chatId);
            } else if ("/help".equalsIgnoreCase(messageText)) {
                sendHelpMessage(chatId);
            } else if (isValidReminderFormat(messageText)) {
                handleReminder(chatId, messageText);
            } else {
                handleInvalidFormat(chatId, messageText);
            }
        }
    }

    private boolean isValidReminderFormat(String text) {

        return text.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2} .+");
    }

    private void sendWelcomeMessage(Long chatId) {
        sendMessage(chatId, MessageTexts.WELCOME_MESSAGE, true);
    }

    private void sendHelpMessage(Long chatId) {
        sendMessage(chatId, MessageTexts.HELP_MESSAGE, true);
    }


    private void handleReminder(Long chatId, String reminderText) {
        try {
            boolean saved = reminderService.processReminder(reminderText, chatId);
            if (saved) {
                sendMessage(chatId, "✅ *Напоминание успешно добавлено!*", true);
            } else {
                sendMessage(chatId, "❌ *Ошибка при сохранении напоминания.*\nПопробуйте еще раз.", true);
            }
        } catch (Exception e) {
            sendMessage(chatId, "⚠️ *Произошла ошибка.*\nУбедитесь, что дата и время корректны.", true);
        }
    }

    private void handleInvalidFormat(Long chatId, String userMessage) {

        if (userMessage.matches(".*\\d{1,2}[./]\\d{1,2}[./]\\d{2,4}.*") ||
                userMessage.matches(".*\\d{1,2}:\\d{2}.*")) {


            String suggestion = "🤔 *Кажется, вы хотите создать напоминание!*\n\n" +
                    "*Правильный формат:*\n" +
                    "`ДД.ММ.ГГГГ ЧЧ:MM Текст`\n\n" +
                    "*Пример:* `07.12.2024 14:30 Встреча`\n\n" +
                    "Используйте /help для подробной справки.";
            sendMessage(chatId, suggestion, true);
        } else {

            String response = "👋 *Я бот для создания напоминаний!*\n\n" +
                    "Чтобы создать напоминание, отправьте:\n" +
                    "`ДД.ММ.ГГГГ ЧЧ:MM Текст`\n\n" +
                    "Или используйте команды:\n" +
                    "/start - начать работу\n" +
                    "/help - подробная справка";
            sendMessage(chatId, response, true);
        }
    }

    private void sendMessage(Long chatId, String text, boolean enableMarkdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        if (enableMarkdown) {
            message.enableMarkdown(true);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}

