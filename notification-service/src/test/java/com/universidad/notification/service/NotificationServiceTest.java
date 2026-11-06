package com.universidad.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.universidad.notification.dto.NotificationRequest;
import com.universidad.notification.model.EstadoNotificacion;
import com.universidad.notification.model.Notification;
import com.universidad.notification.model.TipoNotificacion;
import com.universidad.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository repo;
    NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repo);
    }

    @Test
    void generaNotificacionEnviadaConFecha() {
        when(repo.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));
        Notification n = service.enviar(new NotificationRequest(1L, "Reserva aprobada", "Tu reserva fue aprobada",
                TipoNotificacion.CONFIRMACION));
        assertEquals(EstadoNotificacion.ENVIADA, n.getEstado());
        assertNotNull(n.getFechaEnvio());
        assertEquals(TipoNotificacion.CONFIRMACION, n.getTipo());
    }

    @Test
    void consultaHistorialPorUsuario() {
        Notification n = Notification.builder().id(1L).userId(1L).asunto("a").mensaje("m").build();
        when(repo.findByUserIdOrderByFechaEnvioDesc(1L)).thenReturn(List.of(n));
        assertEquals(1, service.porUsuario(1L).size());
    }
}
