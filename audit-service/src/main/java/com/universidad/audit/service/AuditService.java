package com.universidad.audit.service;

import com.universidad.audit.dto.AuditRequest;
import com.universidad.audit.exception.NotFoundException;
import com.universidad.audit.model.AuditLog;
import com.universidad.audit.repository.AuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/** Solo se puede registrar y consultar: el historial nunca se modifica ni se borra. */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public AuditLog registrar(AuditRequest r) {
        return repo.save(AuditLog.builder()
                .usuario(r.usuario()).operacion(r.operacion()).microservicio(r.microservicio())
                .descripcion(r.descripcion()).resultado(r.resultado()).referencia(r.referencia())
                .fechaHora(LocalDateTime.now())   // fecha y hora automáticas
                .build());
    }

    public List<AuditLog> listar() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "fechaHora"));
    }

    public AuditLog obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe el registro de auditoría " + id));
    }

    public List<AuditLog> porUsuario(String userId) {
        return repo.findByUsuarioOrderByFechaHoraDesc(userId);
    }

    public List<AuditLog> porReferencia(String referencia) {
        return repo.findByReferenciaOrderByFechaHoraDesc(referencia);
    }
}
