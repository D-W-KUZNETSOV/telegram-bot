package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repozitory.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessMessage_ValidMessage_SavesNotification() {
        String message = "25.12.2023 15:30 Уведомление";
        Long chatId = 123L;

        notificationService.processMessage(message, chatId);

        ArgumentCaptor<NotificationTask> taskCaptor = ArgumentCaptor.forClass(NotificationTask.class);
        verify(notificationTaskRepository).save(taskCaptor.capture());

        NotificationTask savedTask = taskCaptor.getValue();
        assertEquals(chatId, savedTask.getChatId());
        assertEquals("Уведомление", savedTask.getNotificationText());
        assertNotNull(savedTask.getSendTime());
    }

    @Test
    public void testProcessMessage_InvalidMessage_LogsWarning() {
        String invalidMessage = "Некорректное сообщение";
        Long chatId = 123L;

        notificationService.processMessage(invalidMessage, chatId);

        verify(notificationTaskRepository, never()).save(any());
    }

    @Test
    public void testSendNotifications_NoTasks_LogsInfo() {
        when(notificationTaskRepository.findBySendTimeBefore(any())).thenReturn(Collections.emptyList());

        notificationService.sendNotifications();

        verify(notificationTaskRepository, never()).delete(any());
    }

    @Test
    public void testSendNotifications_WithTasks_SendsNotifications() {
        NotificationTask task = new NotificationTask();
        task.setChatId(123L);
        task.setNotificationText("Уведомление");
        task.setSendTime(LocalDateTime.now().minusMinutes(1));

        when(notificationTaskRepository.findBySendTimeBefore(any())).thenReturn(List.of(task));
        SendResponse sendResponse = mock(SendResponse.class);
        when(sendResponse.isOk()).thenReturn(true);
        when(telegramBot.execute(any())).thenReturn(sendResponse);

        notificationService.sendNotifications();

        verify(telegramBot).execute(any());
        verify(notificationTaskRepository).delete(task);
    }

    @Test
    public void testSendNotification_SuccessfulSend_LogsInfo() {
        NotificationTask task = new NotificationTask();
        task.setChatId(123L);
        task.setNotificationText("Уведомление");

        SendResponse sendResponse = mock(SendResponse.class);
        when(sendResponse.isOk()).thenReturn(true);
        when(telegramBot.execute(any())).thenReturn(sendResponse);

        notificationService.sendNotification(task);


        verify(telegramBot).execute(any());
    }

    @Test
    public void testSendNotification_UnsuccessfulSend_LogsError() {
        NotificationTask task = new NotificationTask();
        task.setChatId(123L);
        task.setNotificationText("Уведомление");

        SendResponse sendResponse = mock(SendResponse.class);
        when(sendResponse.isOk()).thenReturn(false);
        when(sendResponse.errorCode()).thenReturn(400);
        when(sendResponse.description()).thenReturn("Ошибка отправки");
        when(telegramBot.execute(any())).thenReturn(sendResponse);

        notificationService.sendNotification(task);


        verify(telegramBot).execute(any());
    }

    @Test
    public void testSendNotification_ExceptionDuringSend_LogsError() {
        NotificationTask task = new NotificationTask();
        task.setChatId(123L);
        task.setNotificationText("Уведомление");

        when(telegramBot.execute(any())).thenThrow(new RuntimeException("Ошибка при отправке"));

        notificationService.sendNotification(task);


        verify(telegramBot).execute(any());
    }
}

