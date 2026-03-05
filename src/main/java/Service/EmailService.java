package Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String fromEmail = "naymahadtae@gmail.com";
    private final String appPassword = "vzod bswb opzz vijd";

    public void sendEmail(String toEmail, String subject, String messageText) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword.replace(" ", ""));
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            // ✅ Professional sender name
            message.setFrom(new InternetAddress(fromEmail, "BlindHire Recruitment"));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject, "UTF-8");

            // ✅ HTML Email Content
            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color:#f4f6f9; padding:20px;">
                    <div style="max-width:600px; margin:auto; background:white; padding:30px; border-radius:10px;">
                        
                        <h2 style="color:#4f46e5;">BlindHire</h2>
                        
                        <p>Dear Candidate,</p>
                        
                        <p>%s</p>
                        
                        <br>
                        <p style="color:#555;">
                            Best regards,<br>
                            <strong>BlindHire Recruitment Team</strong>
                        </p>
                        
                        <hr style="margin-top:30px;">
                        <p style="font-size:12px; color:#999;">
                            © 2026 BlindHire. All rights reserved.
                        </p>
                        
                    </div>
                </body>
                </html>
                """.formatted(messageText);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Email sent successfully to " + toEmail);

        } catch (Exception e) {  // ✅ catches UnsupportedEncodingException too
            e.printStackTrace();
        }
    }
}