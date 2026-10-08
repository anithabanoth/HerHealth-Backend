package com.herhealth.herhealth.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    @Test
    void createsAndValidatesJwtToken() {
        JwtService jwtService = new JwtService("test-secret-key-that-is-at-least-32-characters");
        String token = jwtService.generateToken("user@example.com");

        assertNotNull(token);
        assertEquals("user@example.com", jwtService.getSubject(token));
        assertEquals(true, jwtService.isValid(token));
    }
}
