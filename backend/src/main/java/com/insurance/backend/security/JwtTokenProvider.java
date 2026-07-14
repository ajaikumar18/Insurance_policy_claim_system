package com.insurance.backend.security;

import com.insurance.backend.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // A secure 256-bit key for HS256 signing
    private static final String SECRET_STRING = "YourSecureSuperSecretKeyForIPCMSJWTAuthenticationEnterpriseGradeSignaturesHere";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Expiration times in milliseconds
    private static final long STANDARD_EXPIRATION_MS = 8 * 60 * 60 * 1000L; // 8 hours
    private static final long UNDERWRITER_EXPIRATION_MS = 12 * 60 * 60 * 1000L; // 12 hours

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        Date now = new Date();

        // Determine user role and corresponding expiration time
        boolean isUnderwriter = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_UNDERWRITER") || role.equals("UNDERWRITER"));

        long expirationMs = isUnderwriter ? UNDERWRITER_EXPIRATION_MS : STANDARD_EXPIRATION_MS;
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log or handle exceptions
            return false;
        }
    }
}
