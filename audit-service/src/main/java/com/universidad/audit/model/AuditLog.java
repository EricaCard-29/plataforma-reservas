package com.universidad.audit.model;

import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    private String id;
    private String usuario;
    private String operacion;
    private String microservicio;
    private LocalDateTime fechaHora;
    private String descripcion;
    private String resultado;
    /** Identifica sobre qué se hizo la operación, por ejemplo "reserva:12" o "usuario:3". */
    private String referencia;
}
