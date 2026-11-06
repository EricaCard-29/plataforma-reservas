package com.universidad.resource.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.universidad.resource.client.AuditClient;
import com.universidad.resource.dto.ResourceRequest;
import com.universidad.resource.exception.BadRequestException;
import com.universidad.resource.exception.ConflictException;
import com.universidad.resource.model.EstadoRecurso;
import com.universidad.resource.model.Resource;
import com.universidad.resource.model.TipoRecurso;
import com.universidad.resource.repository.ResourceRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock ResourceRepository repo;
    @Mock AuditClient audit;
    ResourceService service;

    @BeforeEach
    void setUp() {
        service = new ResourceService(repo, audit);
    }

    private ResourceRequest request(int capacidad) {
        return new ResourceRequest("Laboratorio 1", TipoRecurso.LABORATORIO, "Bloque A", capacidad, "Lab de redes", null);
    }

    @Test
    void creaRecursoDisponiblePorDefecto() {
        when(repo.save(any(Resource.class))).thenAnswer(i -> {
            Resource r = i.getArgument(0);
            r.setId(10L);
            return r;
        });
        Resource creado = service.crear(request(30));
        assertEquals(10L, creado.getId());
        assertEquals(EstadoRecurso.DISPONIBLE, creado.getEstado());
    }

    @Test
    void rechazaCapacidadNegativa() {
        assertThrows(BadRequestException.class, () -> service.crear(request(-5)));
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaRecursoDuplicado() {
        when(repo.existsByNombreIgnoreCaseAndUbicacionIgnoreCase("Laboratorio 1", "Bloque A")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.crear(request(30)));
    }

    @Test
    void listaSoloRecursosDisponibles() {
        Resource r = Resource.builder().id(1L).nombre("Lab").estado(EstadoRecurso.DISPONIBLE).build();
        when(repo.findByEstado(EstadoRecurso.DISPONIBLE)).thenReturn(List.of(r));
        assertEquals(1, service.disponibles().size());
    }
}
