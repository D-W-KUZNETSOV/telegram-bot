package pro.sky.telegrambot.repozitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Query("SELECT nt FROM NotificationTask nt WHERE nt.sendTime = :currentTime")
    List<NotificationTask> findBySendTime(LocalDateTime currentTime);

    List<NotificationTask> findByScheduledTime(LocalDateTime now);

    List<NotificationTask> findBySendTimeBefore(LocalDateTime now);

}



