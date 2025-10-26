package com.consultorio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Email de bienvenida y validación para nuevos registros
     */
    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion) {
        String validacionUrl = baseUrl + "/api/usuarios/validar-email?token=" + tokenValidacion;

        String asunto = "Bienvenido/a al Consultorio Cosmos - Valida tu email";
        String mensaje = String.format("""
            Hola %s %s,

            ¡Bienvenido/a al Consultorio Cosmos!

            Para activar tu cuenta, por favor valida tu email haciendo clic en este enlace:

            %s

            Este enlace expirará en 24 horas.

            Si no te registraste, ignora este mensaje.

            Atentamente,
            Equipo del Consultorio Cosmos
            """, nombre, apellido, validacionUrl);

        enviarEmailSimple(toEmail, asunto, mensaje);
    }

    /**
     * Email de recordatorio de contraseña
     */
    public void enviarRecordatorioPassword(String toEmail, String nombre, String apellido, String password) {
        String asunto = "Consultorio Cosmos - Recordatorio de Contraseña";
        String mensaje = String.format("""
            Hola %s %s,

            Tu contraseña actual es: %s

            Por seguridad, te recomendamos cambiarla después de ingresar.

            Si no solicitaste esto, contacta con la administración.

            Atentamente,
            Equipo del Consultorio Cosmos
            """, nombre, apellido, password);

        enviarEmailSimple(toEmail, asunto, mensaje);
    }

    /**
     * Método auxiliar para enviar emails
     */
    private void enviarEmailSimple(String toEmail, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(toEmail);
            email.setSubject(asunto);
            email.setText(mensaje);

            mailSender.send(email);

            System.out.println("✅ Email enviado a: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Error enviando email a " + toEmail + ": " + e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo
        }
    }
}