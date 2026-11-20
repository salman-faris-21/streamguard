package com.example.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtEncoder jwtEncoder() {

        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(),
                "HmacSHA256"
        );

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );
    }

    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}