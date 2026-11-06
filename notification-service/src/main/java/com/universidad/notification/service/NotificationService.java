package com.universidad.notification.service;

import com.universidad.notification.dto.NotificationRequest;
import com.universidad.notification.model.EstadoNotificacion;
import com.universidad.notification.model.Notification;
import com.universidad.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Envía" y guarda notificaciones. El envío es simulado (se escribe en el log);
 * si quisieras correos reales aquí se conectaría un servicio SMTP.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Notification enviar(NotificationRequest r) {
        Notification n = Notification.builder()
                .userId(r.userId()).asunto(r.asunto()).mensaje(r.mensaje()).tipo(r.tipo())
                .fechaEnvio(LocalDateTime.now())
                .estado(EstadoNotificacion.ENVIADA)
                .build();
        Notification guardada = repo.save(n);
        log.info("Notificación {} enviada al usuario {}: {}", r.tipo(), r.userId(), r.asunto());
        return guardada;
    }

    @Transactional(readOnly = true)
    public List<Notification> listar() {
        return repo.findAllByOrderByFechaEnvioDesc();
    }

    @Transactional(readOnly = true)
    public List<Notification> porUsuario(Long userId) {
        return repo.findByUserIdOrderByFechaEnvioDesc(userId);
    }
}
