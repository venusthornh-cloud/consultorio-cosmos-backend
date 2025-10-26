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

        // LOGS DETALLADOS - ESTO SE VERÁ EN RENDER
        System.out.println("📧 ===== INICIANDO ENVÍO DE EMAIL =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email destino: " + toEmail);
        System.out.println("📤 Email origen: " + fromEmail);
        System.out.println("🔗 URL de validación: " + validacionUrl);
        System.out.println("🔑 Token: " + tokenValidacion);

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

        try {
            enviarEmailSimple(toEmail, asunto, mensaje);
            System.out.println("✅ ===== EMAIL ENVIADO EXITOSAMENTE =====");
        } catch (Exception e) {
            System.out.println("❌ ===== ERROR ENVIANDO EMAIL =====");
            System.out.println("💥 Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Email de recordatorio de contraseña
     */
    public void enviarRecordatorioPassword(String toEmail, String nombre, String apellido, String password) {
        System.out.println("📧 Enviando recordatorio a: " + toEmail);

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
            System.out.println("🔄 Preparando envío de email a: " + toEmail);

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom(fromEmail);
            email.setTo(toEmail);
            email.setSubject(asunto);
            email.setText(mensaje);

            System.out.println("📤 Intentando enviar a través de SMTP...");
            mailSender.send(email);

            System.out.println("🎉 Email enviado correctamente a: " + toEmail);

        } catch (Exception e) {
            System.out.println("💥 ERROR CRÍTICO ENVIANDO EMAIL:");
            System.out.println("📧 Destino: " + toEmail);
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace(); // Esto mostrará el stack trace completo en Render
            // No lanzamos excepción para no interrumpir el flujo
        }
    }
}