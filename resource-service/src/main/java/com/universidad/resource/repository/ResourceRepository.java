package com.universidad.resource.repository;

import com.universidad.resource.model.EstadoRecurso;
import com.universidad.resource.model.Resource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByEstado(EstadoRecurso estado);
    boolean existsByNombreIgnoreCaseAndUbicacionIgnoreCase(String nombre, String ubicacion);
    boolean existsByNombreIgnoreCaseAndUbicacionIgnoreCaseAndIdNot(String nombre, String ubicacion, Long id);
}
