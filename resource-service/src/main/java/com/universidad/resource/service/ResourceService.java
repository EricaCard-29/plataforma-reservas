package com.universidad.resource.service;

import com.universidad.resource.client.AuditClient;
import com.universidad.resource.dto.ResourceRequest;
import com.universidad.resource.exception.BadRequestException;
import com.universidad.resource.exception.ConflictException;
import com.universidad.resource.exception.NotFoundException;
import com.universidad.resource.model.EstadoRecurso;
import com.universidad.resource.model.Resource;
import com.universidad.resource.repository.ResourceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {

    private final ResourceRepository repo;
    private final AuditClient audit;

    public ResourceService(ResourceRepository repo, AuditClient audit) {
        this.repo = repo;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Resource> listar() {
        return repo.findAll();
    }

    /** Solo los recursos en estado DISPONIBLE pueden reservarse. */
    @Transactional(readOnly = true)
    public List<Resource> disponibles() {
        return repo.findByEstado(EstadoRecurso.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public Resource obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe el recurso con id " + id));
    }

    @Transactional
    public Resource crear(ResourceRequest r) {
        validarCapacidad(r.capacidad());
        if (repo.existsByNombreIgnoreCaseAndUbicacionIgnoreCase(r.nombre().trim(), r.ubicacion().trim())) {
            throw new ConflictException("Ya existe el recurso '" + r.nombre() + "' en " + r.ubicacion());
        }
        Resource guardado = repo.save(Resource.builder()
                .nombre(r.nombre().trim()).tipo(r.tipo()).ubicacion(r.ubicacion().trim())
                .capacidad(r.capacidad()).descripcion(r.descripcion())
                .estado(r.estado() == null ? EstadoRecurso.DISPONIBLE : r.estado())
                .build());
        audit.registrar("SISTEMA", "CREAR_RECURSO",
                "Se creó el recurso " + guardado.getNombre(), "EXITOSO", "recurso:" + guardado.getId());
        return guardado;
    }

    @Transactional
    public Resource actualizar(Long id, ResourceRequest r) {
        Resource recurso = obtener(id);
        validarCapacidad(r.capacidad());
        if (repo.existsByNombreIgnoreCaseAndUbicacionIgnoreCaseAndIdNot(r.nombre().trim(), r.ubicacion().trim(), id)) {
            throw new ConflictException("Ya existe otro recurso '" + r.nombre() + "' en " + r.ubicacion());
        }
        recurso.setNombre(r.nombre().trim());
        recurso.setTipo(r.tipo());
        recurso.setUbicacion(r.ubicacion().trim());
        recurso.setCapacidad(r.capacidad());
        recurso.setDescripcion(r.descripcion());
        if (r.estado() != null) {
            recurso.setEstado(r.estado());
        }
        Resource guardado = repo.save(recurso);
        audit.registrar("SISTEMA", "MODIFICAR_RECURSO",
                "Se modificó el recurso " + guardado.getNombre() + " (estado: " + guardado.getEstado() + ")",
                "EXITOSO", "recurso:" + id);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Resource recurso = obtener(id);
        repo.delete(recurso);
        audit.registrar("SISTEMA", "ELIMINAR_RECURSO",
                "Se eliminó el recurso " + recurso.getNombre(), "EXITOSO", "recurso:" + id);
    }

    private void validarCapacidad(Integer capacidad) {
        if (capacidad == null || capacidad < 0) {
            throw new BadRequestException("La capacidad no puede ser negativa");
        }
    }
}
