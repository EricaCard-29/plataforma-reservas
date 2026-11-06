package com.universidad.audit.repository;

import com.universidad.audit.model.AuditLog;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUsuarioOrderByFechaHoraDesc(String usuario);
    List<AuditLog> findByReferenciaOrderByFechaHoraDesc(String referencia);
}
