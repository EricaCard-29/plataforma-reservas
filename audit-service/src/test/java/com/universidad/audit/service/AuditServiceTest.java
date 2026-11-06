package com.universidad.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.universidad.audit.dto.AuditRequest;
import com.universidad.audit.exception.NotFoundException;
import com.universidad.audit.model.AuditLog;
import com.universidad.audit.repository.AuditLogRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditLogRepository repo;
    AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(repo);
    }

    @Test
    void registraEventoConFechaAutomatica() {
        when(repo.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));
        AuditLog log = service.registrar(new AuditRequest("1", "CREAR_RESERVA", "reservation-service",
                "Reserva creada", "EXITOSO", "reserva:7"));
        assertNotNull(log.getFechaHora());
        assertEquals("CREAR_RESERVA", log.getOperacion());
        assertEquals("reserva:7", log.getReferencia());
    }

    @Test
    void lanzaNotFoundSiNoExiste() {
        when(repo.findById("zzz")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtener("zzz"));
    }
}
