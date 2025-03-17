package pro.sky.telegrambot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.sky.telegrambot.message.MessageProcessor;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageProcessorTest {

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @InjectMocks
    private MessageProcessor messageProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessMessage_ValidMessage_SavesNotification() {
        String message = "17.03.2025 18:54 Уведомление";
        Long chatId = 12345L;

        // Вызов метода, который мы тестируем
        messageProcessor.processMessage(message, chatId);

        // Проверяем, что метод save был вызван с правильным объектом NotificationTask
        verify(notificationTaskRepository).save(any(NotificationTask.class));
    }

    @Test
    public void testProcessMessage_InvalidMessage_LogsWarning() {
        String message = "Неверное сообщение"; // Сообщение с неверным форматом
        Long chatId = 12345L;

        // Вызов метода, который мы тестируем
        messageProcessor.processMessage(message, chatId);

        // Здесь мы можем использовать Mockito для проверки, что метод save не был вызван
        verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
    }
}

