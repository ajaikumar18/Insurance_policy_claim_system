package com.insurance.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@ipcms.local");
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_POLICYHOLDER"))
        );

        String token = tokenProvider.generateToken(authentication);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("test@ipcms.local", tokenProvider.getEmailFromJWT(token));
    }

    @Test
    void testValidateToken_InvalidSignature() {
        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGlwY21zLmxvY2FsIn0.invalidSignature";
        assertFalse(tokenProvider.validateToken(malformedToken));
    }
}
