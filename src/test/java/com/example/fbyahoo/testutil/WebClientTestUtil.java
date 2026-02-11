package com.example.fbyahoo.testutil;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public final class WebClientTestUtil {

    private WebClientTestUtil() {}

    public static WebClient webClientForJson(String json) {
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
        ExchangeFunction exchangeFunction = request -> Mono.just(response);
        return WebClient.builder().exchangeFunction(exchangeFunction).build();
    }
}
