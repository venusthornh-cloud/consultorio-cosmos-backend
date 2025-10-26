package com.consultorio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String resendApiKey;

    @Value("${app.base-url}")
    private String baseUrl;

    public void enviarEmailBienvenidaYValidacion(String toEmail, String nombre, String apellido, String tokenValidacion) {
        String validacionUrl = baseUrl + "/api/usuarios/validar-email?token=" + tokenValidacion;

        System.out.println("📧 ===== ENVIANDO EMAIL CON RESEND API =====");
        System.out.println("👉 Para: " + nombre + " " + apellido);
        System.out.println("📨 Email: " + toEmail);
        System.out.println("🔗 URL: " + validacionUrl);

        String jsonBody = String.format("""
            {
                "from": "Consultorio Cosmos <consultorio.cosmos@gmail.com>",
                "to": ["%s"],
                "subject": "Consultorio Cosmos - Valida tu email",
                "html": "<h2>Hola %s %s</h2><p>¡Bienvenido/a al Consultorio Cosmos!</p><p>Para activar tu cuenta, haz clic aquí:</p><p><a href='%s' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Validar Email</a></p><p>O copia este enlace:<br>%s</p><p>Este enlace expira en 24 horas.</p><p>Atentamente,<br>Equipo del Consultorio Cosmos</p>"
            }
            """, toEmail, nombre, apellido, validacionUrl, validacionUrl);

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("✅ RESEND API RESPONSE: " + response.statusCode());
            System.out.println("📨 RESPONSE BODY: " + response.body());

            if (response.statusCode() == 200) {
                System.out.println("🎉 EMAIL ENVIADO EXITOSAMENTE");
            } else {
                System.out.println("❌ ERROR RESEND: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR ENVIANDO EMAIL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviarRecordatorioPassword(String toEmail, String nombre, String apellido, String password) {
        System.out.println("📧 Recordatorio enviado a: " + toEmail);
    }
}