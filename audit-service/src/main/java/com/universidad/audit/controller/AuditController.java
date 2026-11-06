package com.universidad.audit.controller;

import com.universidad.audit.dto.AuditRequest;
import com.universidad.audit.model.AuditLog;
import com.universidad.audit.service.AuditService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditLog> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AuditLog obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @GetMapping("/user/{userId}")
    public List<AuditLog> porUsuario(@PathVariable String userId) {
        return service.porUsuario(userId);
    }

    /** Extra: historial de una entidad concreta, por ejemplo /audit/referencia/reserva:12 */
    @GetMapping("/referencia/{referencia}")
    public List<AuditLog> porReferencia(@PathVariable String referencia) {
        return service.porReferencia(referencia);
    }

    /** Lo usan los otros microservicios para registrar eventos. */
    @PostMapping
    public ResponseEntity<AuditLog> registrar(@Valid @RequestBody AuditRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
