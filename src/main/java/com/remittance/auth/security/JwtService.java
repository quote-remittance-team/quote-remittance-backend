package com.remittance.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey signingKey;

    /**
     * Initialize the signing key once (avoids decoding every time)
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtExpiration)))
                .signWith(signingKey) // no algorithm needed in 0.12+
                .compact();
    }

    /**
     * Extract email (subject)
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Validate token
     */
    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    /**
     * Check expiration
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    /**
     * Parse JWT and extract claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey) //
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
