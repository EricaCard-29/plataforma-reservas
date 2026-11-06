package com.universidad.notification.repository;

import com.universidad.notification.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByFechaEnvioDesc(Long userId);
    List<Notification> findAllByOrderByFechaEnvioDesc();
}
