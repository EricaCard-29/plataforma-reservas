package com.universidad.reservation.client;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** Fábrica de RestClient con tiempos de espera, para que un servicio caído no cuelgue al resto. */
final class RestClients {

    private RestClients() {}

    static RestClient crear(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
