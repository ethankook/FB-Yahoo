package com.example.fbyahoo.service;

import com.example.fbyahoo.config.YahooProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class YahooOAuthServiceTest {

    @Test
    void buildAuthorizeUrl_includesExpectedParams() {
        YahooProperties props = new YahooProperties();
        YahooProperties.OAuth oauth = new YahooProperties.OAuth();
        oauth.setBaseUrl("https://login.yahoo.com");
        oauth.setAuthorizeUrl("/oauth2/request_auth");
        oauth.setClientId("client");
        oauth.setRedirectUri("https://example.com/callback");
        props.setOauth(oauth);

        YahooOAuthService service = new YahooOAuthService(props, WebClient.builder().build());

        String url = service.buildAuthorizeUrl("state123");

        UriComponents components = UriComponentsBuilder.fromUriString(url).build(true);
        var query = components.getQueryParams();

        assertTrue(query.getOrDefault("client_id", List.of()).contains("client"));
        assertTrue(query.getOrDefault("redirect_uri", List.of()).contains("https://example.com/callback"));
        assertTrue(query.getOrDefault("response_type", List.of()).contains("code"));
        assertTrue(query.getOrDefault("state", List.of()).contains("state123"));
        assertTrue(query.getOrDefault("scope", List.of()).contains("openid"));
    }
}
