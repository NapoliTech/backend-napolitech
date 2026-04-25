package com.pizzaria.backendpizzaria.config;

import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mínimo 32 caracteres. Se vazio, gera chave efêmera (tokens invalidados ao reiniciar).
    @Value("${app.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (jwtSecret != null && jwtSecret.length() >= 32) {
            this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            log.info("JWT: usando secret configurado via JWT_SECRET");
        } else {
            // Chave efêmera — tokens expiram ao reiniciar o servidor
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.warn("JWT_SECRET ausente ou com menos de 32 chars — chave efêmera em uso. Configure JWT_SECRET em produção.");
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return Jwts.builder()
                .setSubject(email)
                .claim("id", usuario.getIdUsuario())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .claim("tipoUsuario", usuario.getTipoUsuario())
                .claim("cpf", usuario.getCpf())
                .claim("dataNasc", usuario.getDataNasc())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validarToken(String token, String nome) {
        final String usuarioExtraido = extractUsername(token);
        return (usuarioExtraido.equals(nome) && !isTokenExpired(token));
    }
}
