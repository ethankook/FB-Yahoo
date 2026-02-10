package com.example.fbyahoo.service;


import com.example.fbyahoo.dto.YahooTokenResponse;
import com.example.fbyahoo.enums.OAuthFailureReason;
import com.example.fbyahoo.exception.OAuthFlowException;
import com.example.fbyahoo.model.OAuthToken;
import com.example.fbyahoo.repo.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final YahooOAuthService yahooOAuthService;

    private final static Logger log = LoggerFactory.getLogger(TokenService.class);

    public TokenService(TokenRepository tokenRepository, YahooOAuthService yahooOAuthService) {
        this.tokenRepository = tokenRepository;
        this.yahooOAuthService = yahooOAuthService;
    }

    public Optional<OAuthToken> getToken() {
        return tokenRepository.findTopByOrderByIdAsc();
    }


    private boolean isExpired(OAuthToken token) {
        return token.getExpiresAt().isBefore(Instant.now().plusSeconds(30));
    }

    private Instant computeExpiresAt(Long expiresIn) {
        return Instant.now().plusSeconds(expiresIn - 30);
    }

    @Transactional
    public String getValidAccessToken() {
        OAuthToken token = getToken().orElseThrow(() -> {
            log.info("No token found in database. Login needed");
            return new OAuthFlowException(OAuthFailureReason.TOKEN_MISSING, "No token found in database. Login needed");
        });

        if (!isExpired(token)) {
            log.debug("Using cached access token (expiresAt={})", token.getExpiresAt());
            return token.getAccessToken();
        }
        log.info("Access token expired/near expiry (expiresAt={}) — attempting refresh", token.getExpiresAt());
        try {
            YahooTokenResponse tokenResponse = yahooOAuthService.refreshToken(token.getRefreshToken());
            saveToken(tokenResponse);
            log.info("Token refresh succeeded — updated token stored in DB (newExpiresAt={})", computeExpiresAt(tokenResponse.expiresIn()));
            return tokenResponse.accessToken();
        } catch (Exception e) {
            log.warn("Token refresh failed — clearing stored tokens and requiring re-login", e);
            tokenRepository.deleteAll();
            throw new OAuthFlowException(OAuthFailureReason.REFRESH_FAILED, "Failed to refresh token. Login needed", e);
        }
    }

    @Transactional
    public void saveToken(YahooTokenResponse tokenResponse) {
        tokenRepository.deleteAll();
        log.debug("Cleared existing tokens to maintain single-row invariant");

        OAuthToken token = new OAuthToken();
        token.setAccessToken(tokenResponse.accessToken());

        if (tokenResponse.refreshToken() != null && !tokenResponse.refreshToken().isBlank()) {
            token.setRefreshToken(tokenResponse.refreshToken());
            log.debug("Received refresh token in response — storing refresh token");
        }
        else {
            log.warn("No refresh token provided in response — token refresh will fail");
        }
        token.setExpiresAt(computeExpiresAt(tokenResponse.expiresIn()));
        token.setTokenType(tokenResponse.tokenType());
        token.setScope(tokenResponse.scope());

        tokenRepository.save(token);
        log.debug("OAuth token stored in DB (expiresAt={}, tokenType={})", token.getExpiresAt(), token.getTokenType());
    }


}
