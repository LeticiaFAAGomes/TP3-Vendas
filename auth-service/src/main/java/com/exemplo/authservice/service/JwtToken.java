package com.exemplo.authservice.service;

import com.exemplo.authservice.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtToken {

    private final SecretKey secretKey;

    // Access Token: 15 minutos
    private final long ACCESS_EXPIRATION = 15 * 60 * 1000;

    // Refresh Token: 7 dias
    private final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000;

    public JwtToken(@Value("${jwt.secret}") String segredo) {
        secretKey = Keys.hmacShaKeyFor(
                segredo.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String gerarAccessToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("tipo", "access")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + ACCESS_EXPIRATION
                ))
                .signWith(secretKey)
                .compact();
    }

    public String gerarRefreshToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("tipo", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + REFRESH_EXPIRATION
                ))
                .signWith(secretKey)
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean ehRefreshToken(String token) {
        String tipo = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tipo", String.class);

        return "refresh".equals(tipo);
    }
}