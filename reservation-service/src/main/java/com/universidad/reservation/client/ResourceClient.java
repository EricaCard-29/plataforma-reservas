package com.universidad.reservation.client;

import com.universidad.reservation.exception.ExternalServiceException;
import com.universidad.reservation.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Reservation-Service -> Resource-Service */
@Component
public class ResourceClient {

    private final RestClient client;

    public ResourceClient(@Value("${services.resource-url}") String url) {
        this.client = RestClients.crear(url);
    }

    public ResourceDto obtener(Long id) {
        try {
            ResourceDto r = client.get().uri("/resources/{id}", id).retrieve().body(ResourceDto.class);
            if (r == null) {
                throw new NotFoundException("El recurso con id " + id + " no existe");
            }
            return r;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("El recurso con id " + id + " no existe");
        } catch (RestClientException e) {
            throw new ExternalServiceException("No se pudo consultar el Resource-Service: " + e.getMessage(), e);
        }
    }
}
