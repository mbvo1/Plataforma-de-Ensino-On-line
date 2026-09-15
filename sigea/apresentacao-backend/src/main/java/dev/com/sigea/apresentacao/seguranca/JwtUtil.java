package dev.com.sigea.apresentacao.seguranca;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String segredo;

    private static final long EXPIRACAO_MS = 2 * 60 * 60 * 1000; // 2 horas

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(segredo.getBytes());
    }

    public String gerarToken(String usuarioId, String email, String perfil) {
        return Jwts.builder()
                .subject(usuarioId)
                .claim("email", email)
                .claim("perfil", perfil)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACAO_MS))
                .signWith(getChave())
                .compact();
    }

    public Claims validarEExtrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}