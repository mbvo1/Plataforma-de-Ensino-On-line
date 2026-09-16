package dev.com.sigea.apresentacao.seguranca;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFiltro(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            String token = cabecalho.substring(7);
            try {
                Claims claims = jwtUtil.validarEExtrairClaims(token);
                String usuarioId = claims.getSubject();
                String perfil = claims.get("perfil", String.class);

                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
                var autenticacao = new UsernamePasswordAuthenticationToken(usuarioId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(autenticacao);

            } catch (Exception e) {
                // Token invalido ou expirado - segue sem autenticar,
                // o SecurityConfig decide se o endpoint exige autenticacao
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
