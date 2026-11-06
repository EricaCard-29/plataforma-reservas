package com.universidad.reservation.client;

import com.universidad.reservation.exception.ExternalServiceException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Reservation-Service -> Audit-Service.
 * Aquí la auditoría es OBLIGATORIA: "ninguna reserva debe quedar sin auditoría".
 * Si falla, se lanza una excepción y la reserva se revierte (rollback).
 */
@Component
public class AuditClient {

    private final RestClient client;

    public AuditClient(@Value("${services.audit-url}") String url) {
        this.client = RestClients.crear(url);
    }

    public void registrar(String usuario, String operacion, String descripcion, String resultado, String referencia) {
        try {
            client.post().uri("/audit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("usuario", usuario, "operacion", operacion, "microservicio", "reservation-service",
                            "descripcion", descripcion, "resultado", resultado, "referencia", referencia))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ExternalServiceException(
                    "No se pudo registrar la auditoría, por eso la operación fue revertida: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> historial(String referencia) {
        try {
            return client.get().uri("/audit/referencia/{ref}", referencia).retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (RestClientException e) {
            throw new ExternalServiceException("No se pudo consultar el Audit-Service: " + e.getMessage(), e);
        }
    }
}
