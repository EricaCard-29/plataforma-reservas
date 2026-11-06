package com.universidad.resource.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Envía eventos al Audit-Service. Si la auditoría falla, solo se avisa en el log (no se cae la operación). */
@Component
public class AuditClient {

    private static final Logger log = LoggerFactory.getLogger(AuditClient.class);

    private final RestClient client;
    private final String microservicio;

    public AuditClient(@Value("${services.audit-url}") String url,
                       @Value("${spring.application.name}") String microservicio) {
        this.client = RestClient.builder().baseUrl(url).build();
        this.microservicio = microservicio;
    }

    public void registrar(String usuario, String operacion, String descripcion, String resultado, String referencia) {
        try {
            client.post().uri("/audit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("usuario", usuario, "operacion", operacion, "microservicio", microservicio,
                            "descripcion", descripcion, "resultado", resultado, "referencia", referencia))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.warn("No se pudo registrar la auditoría de {}: {}", operacion, e.getMessage());
        }
    }
}
