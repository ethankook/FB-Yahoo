package com.example.fbyahoo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Yahoo OAuth client (token exchange / refresh).
     * Base URL should be something like https://api.login.yahoo.com
     */
    @Bean
    @Qualifier("yahooOauthClient")
    public WebClient yahooOauthClient(YahooProperties props, WebClient.Builder builder) {
        return builder
                .baseUrl(props.getOauth().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    /**
     * Yahoo Fantasy Sports API client.
     * Base URL should be something like https://fantasysports.yahooapis.com
     */
    @Bean
    @Qualifier("yahooFantasyClient")
    public WebClient yahooFantasyClient(YahooProperties props, WebClient.Builder builder) {
        return builder
                .baseUrl(props.getApi().getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
