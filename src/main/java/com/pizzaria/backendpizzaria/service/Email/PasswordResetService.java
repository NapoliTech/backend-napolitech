package com.pizzaria.backendpizzaria.service.Email;

import com.pizzaria.backendpizzaria.domain.DTO.Login.Email;
import com.pizzaria.backendpizzaria.domain.PasswordResetToken;
import com.pizzaria.backendpizzaria.domain.Usuario;
import com.pizzaria.backendpizzaria.infra.exception.ValidationException;
import com.pizzaria.backendpizzaria.repository.PasswordResetTokenRepository;
import com.pizzaria.backendpizzaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private UsuarioRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public void sendResetLink(String email) {
        Optional<Usuario> optionalUser = userRepository.findByEmail(email.trim());

        if (optionalUser.isEmpty()) {
            throw new ValidationException("Nenhum usuário encontrado com este e-mail.");
        }

        Usuario user = optionalUser.get();

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsuario(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        if (resetToken.isExpired()){
            tokenRepository.delete(resetToken);
        }

        tokenRepository.save(resetToken);

        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        String message = """
                            Olá, %s!
                    
                            Para redefinir sua senha, clique no link abaixo:
                    
                            %s
                """.formatted(user.getNome(), resetUrl);
        String contentType = "text/html";
        Email emailSend = new Email(email, "Redefinição de senha", message, contentType);

        emailService.sendEmail(emailSend);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new ValidationException("Token expirado");
        }

        Usuario user = resetToken.getUsuario();
        user.setSenha(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);
    }
}