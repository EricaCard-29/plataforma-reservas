package com.universidad.reservation.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Reservation-Service -> Notification-Service. Si falla, la reserva sigue valiendo (solo se avisa en el log). */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient client;

    public NotificationClient(@Value("${services.notification-url}") String url) {
        this.client = RestClients.crear(url);
    }

    public void enviar(Long userId, String tipo, String asunto, String mensaje) {
        try {
            client.post().uri("/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("userId", userId, "tipo", tipo, "asunto", asunto, "mensaje", mensaje))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificación {} al usuario {}: {}", tipo, userId, e.getMessage());
        }
    }
}
