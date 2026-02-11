package com.example.fbyahoo.service;

import com.example.fbyahoo.dto.YahooTokenResponse;
import com.example.fbyahoo.enums.OAuthFailureReason;
import com.example.fbyahoo.exception.OAuthFlowException;
import com.example.fbyahoo.model.OAuthToken;
import com.example.fbyahoo.repo.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    TokenRepository tokenRepository;

    @Mock
    YahooOAuthService yahooOAuthService;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(tokenRepository, yahooOAuthService);
    }

    @Test
    void getValidAccessToken_throwsWhenMissingToken() {
        when(tokenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());

        OAuthFlowException ex = assertThrows(OAuthFlowException.class, tokenService::getValidAccessToken);

        assertEquals(OAuthFailureReason.TOKEN_MISSING, ex.getReason());
        verifyNoInteractions(yahooOAuthService);
    }

    @Test
    void getValidAccessToken_returnsCachedWhenNotExpired() {
        OAuthToken token = new OAuthToken();
        token.setAccessToken("cached");
        token.setRefreshToken("refresh");
        token.setExpiresAt(Instant.now().plusSeconds(120));

        when(tokenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(token));

        String accessToken = tokenService.getValidAccessToken();

        assertEquals("cached", accessToken);
        verifyNoInteractions(yahooOAuthService);
        verify(tokenRepository, never()).deleteAll();
    }

    @Test
    void getValidAccessToken_refreshesWhenExpired() {
        OAuthToken token = new OAuthToken();
        token.setAccessToken("old");
        token.setRefreshToken("refresh");
        token.setExpiresAt(Instant.now().minusSeconds(1));

        when(tokenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(token));

        YahooTokenResponse response = new YahooTokenResponse(
                "newAccess",
                "newRefresh",
                3600L,
                "Bearer",
                "scope"
        );

        when(yahooOAuthService.refreshToken("refresh")).thenReturn(response);

        String accessToken = tokenService.getValidAccessToken();

        assertEquals("newAccess", accessToken);
        verify(yahooOAuthService).refreshToken("refresh");
        verify(tokenRepository).deleteAll();
        verify(tokenRepository).save(any(OAuthToken.class));
    }

    @Test
    void getValidAccessToken_clearsTokensOnRefreshFailure() {
        OAuthToken token = new OAuthToken();
        token.setAccessToken("old");
        token.setRefreshToken("refresh");
        token.setExpiresAt(Instant.now().minusSeconds(1));

        when(tokenRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(token));
        when(yahooOAuthService.refreshToken("refresh")).thenThrow(new RuntimeException("boom"));

        OAuthFlowException ex = assertThrows(OAuthFlowException.class, tokenService::getValidAccessToken);

        assertEquals(OAuthFailureReason.REFRESH_FAILED, ex.getReason());
        verify(tokenRepository).deleteAll();
        verify(tokenRepository, never()).save(any());
    }
}
