package com.example.fbyahoo.controller.api;

import com.example.fbyahoo.service.TokenService;
import com.example.fbyahoo.service.YahooOAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;
    private final YahooOAuthService yahooOAuthService;

    public AuthController(TokenService tokenService, YahooOAuthService yahooOAuthService) {
        this.tokenService = tokenService;
        this.yahooOAuthService = yahooOAuthService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> authStatus() {
        String loginUrl = yahooOAuthService.buildAuthorizeUrl("frontend");
        try {
            tokenService.getValidAccessToken();
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "loginUrl", loginUrl
            ));
        } catch (Exception e) {
            log.debug("Auth status check: not authenticated — {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "loginUrl", loginUrl
            ));
        }
    }
}
