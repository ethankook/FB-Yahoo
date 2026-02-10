package com.example.fbyahoo.service;

import com.example.fbyahoo.config.YahooProperties;
import com.example.fbyahoo.dto.YahooTokenResponse;
import com.example.fbyahoo.enums.OAuthFailureReason;
import com.example.fbyahoo.exception.OAuthFlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Base64;

@Service
public class YahooOAuthService {

    private final YahooProperties yahooProperties;
    private final WebClient oauthClient;

    private static final Logger log = LoggerFactory.getLogger(YahooOAuthService.class);


    public YahooOAuthService(
            YahooProperties yahooProperties,
            @Qualifier("yahooOauthClient") WebClient oauthClient
    ) {
        this.yahooProperties = yahooProperties;
        this.oauthClient = oauthClient;
    }


    public String buildAuthorizeUrl(String state) {

        String authorizeUrl = yahooProperties.getOauth().getBaseUrl() +
                            yahooProperties.getOauth().getAuthorizeUrl();

        return UriComponentsBuilder
                .fromUriString(authorizeUrl)
                .queryParam("client_id", yahooProperties.getOauth().getClientId())
                .queryParam("redirect_uri", yahooProperties.getOauth().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("scope", "openid")
                .build(true)
                .toUriString();
    }


    public YahooTokenResponse exchangeCodeForToken(String code){
        log.debug("Exchanging Yahoo authorization code for tokens");

        String basic = Base64.getEncoder().encodeToString((yahooProperties.getOauth().getClientId() + ":" + yahooProperties.getOauth().getClientSecret()).getBytes());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", yahooProperties.getOauth().getRedirectUri());

        return oauthClient.post()
                .uri(yahooProperties.getOauth().getTokenUrl())
                .header("Authorization", "Basic " + basic)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(errorBody -> {
                                    log.warn("Yahoo token exchange failed (status={})", response.statusCode());
                                    return new OAuthFlowException(
                                            OAuthFailureReason.TOKEN_EXCHANGE_ERROR,
                                            "Failed to exchange code for token: " + errorBody
                                    );
                                })
                )
                .bodyToMono(YahooTokenResponse.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .block();

    }

    public YahooTokenResponse refreshToken(String refreshToken){
        String basic = Base64.getEncoder().encodeToString((yahooProperties.getOauth().getClientId() + ":" + yahooProperties.getOauth().getClientSecret()).getBytes());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        return oauthClient.post()
                .uri(yahooProperties.getOauth().getTokenUrl())
                .header("Authorization", "Basic " + basic)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(errorBody -> {
                                    log.warn("Yahoo token refresh failed (status={})", response.statusCode().value());
                                    return new OAuthFlowException(
                                            OAuthFailureReason.REFRESH_FAILED,
                                            "Failed to refresh token: " + errorBody
                                    );
                                })
                )
                .bodyToMono(YahooTokenResponse.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .block();
    }


}
