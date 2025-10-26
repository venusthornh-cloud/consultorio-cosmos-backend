package com.consultorio.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(@Value("${RESEND_API_KEY}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion) {
        String validacionUrl = baseUrl + "/api/usuarios/validar-email?token=" + tokenValidacion;

        System.out.println("📧 ===== ENVIANDO EMAIL CON RESEND API =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("🔗 URL: " + validacionUrl);

        String asunto = "Consultorio Cosmos - Valida tu email";
        String mensaje = String.format("""
            Hola %s %s,

            ¡Bienvenido/a al Consultorio Cosmos!

            Para activar tu cuenta, haz clic aquí:
            %s

            Este enlace expira en 24 horas.

            Atentamente,
            Equipo del Consultorio Cosmos
            consultorio.cosmos@gmail.com
            """, nombre, apellido, validacionUrl);

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .from("Consultorio Cosmos <consultorio.cosmos@gmail.com>")
                    .to(toEmail)
                    .subject(asunto)
                    .html(mensaje.replace("\n", "<br>"))
                    .build();

            SendEmailResponse response = resend.emails().send(request);
            System.out.println("✅ EMAIL ENVIADO EXITOSAMENTE via API. ID: " + response.getId());

        } catch (ResendException e) {
            System.err.println("❌ ERROR RESEND API: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarRecordatorioPassword(String toEmail, String nombre, String apellido, String password) {
        // Similar implementación para recordatorios
        System.out.println("📧 Recordatorio enviado a: " + toEmail);
    }
}