package com.pizzaria.backendpizzaria.config;

import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(jwtSecret)) {
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres");
            }
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } else {
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            log.warn("JWT_SECRET não configurado; tokens serão invalidados ao reiniciar a aplicação.");
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
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
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
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
