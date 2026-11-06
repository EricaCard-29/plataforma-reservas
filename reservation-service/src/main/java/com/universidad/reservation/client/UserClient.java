package com.universidad.reservation.client;

import com.universidad.reservation.exception.ExternalServiceException;
import com.universidad.reservation.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Reservation-Service -> User-Service */
@Component
public class UserClient {

    private final RestClient client;

    public UserClient(@Value("${services.user-url}") String url) {
        this.client = RestClients.crear(url);
    }

    public UserDto obtener(Long id) {
        try {
            UserDto u = client.get().uri("/users/{id}", id).retrieve().body(UserDto.class);
            if (u == null) {
                throw new NotFoundException("El usuario con id " + id + " no existe");
            }
            return u;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("El usuario con id " + id + " no existe");
        } catch (RestClientException e) {
            throw new ExternalServiceException("No se pudo consultar el User-Service: " + e.getMessage(), e);
        }
    }
}
