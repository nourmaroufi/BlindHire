package Service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Sends verification codes via:
 *  - Gmail SMTP  (email)
 *  - Twilio      (SMS)
 *
 * ── GMAIL SETUP ───────────────────────────────────────────────────────────────
 *  1. Go to myaccount.google.com/security
 *  2. Enable 2-Step Verification
 *  3. Search "App passwords" → Select app: Mail → Generate
 *  4. Copy the 16-character password (spaces don't matter)
 *  5. Paste your Gmail address into GMAIL_ADDRESS
 *  6. Paste the app password into GMAIL_APP_PASSWORD
 *
 * ── MAVEN DEPENDENCY (JavaMail) ───────────────────────────────────────────────
 *  Add to pom.xml:
 *    <dependency>
 *      <groupId>com.sun.mail</groupId>
 *      <artifactId>jakarta.mail</artifactId>
 *      <version>2.0.1</version>
 *    </dependency>
 *
 * ── TWILIO SETUP ─────────────────────────────────────────────────────────────
 *  1. Go to https://www.twilio.com/try-twilio → Sign up free
 *  2. Get Account SID, Auth Token, and a free phone number from Console
 *  3. Paste all 3 values below
 *  Note: Trial account can only SMS verified numbers — verify yours in console
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class NotificationService {

    // ── Gmail SMTP ────────────────────────────────────────────────────────────
    private static final String GMAIL_ADDRESS      = "jedchihi100@gmail.com";
    private static final String GMAIL_APP_PASSWORD = "agjx xeho zrck nhcb";

    // ── Twilio SMS ────────────────────────────────────────────────────────────
    private static final String TWILIO_ACCOUNT_SID = "ACd7784cedc764c0264de4c988a294f022";
    private static final String TWILIO_AUTH_TOKEN  = "d717a778a5e3e2ab934d78ccdc239782";
    private static final String TWILIO_FROM_PHONE  = "+13527815271";

    // =========================================================================
    //  CODE GENERATION
    // =========================================================================

    /** Generates a random 6-digit verification code. */
    public static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // =========================================================================
    //  EMAIL  —  Gmail SMTP via JavaMail
    // =========================================================================

    /**
     * Sends a verification code by email using Gmail SMTP.
     *
     * @param toEmail   recipient email address
     * @param toName    recipient display name
     * @param code      6-digit code to include
     * @param isReset   true = password reset email, false = signup verification
     * @return true if sent successfully
     */
    public static boolean sendEmailCode(String toEmail, String toName, String code, boolean isReset) {
        if (GMAIL_ADDRESS.equals("YOUR_GMAIL@gmail.com") || GMAIL_APP_PASSWORD.equals("YOUR_16_CHAR_APP_PASSWORD")) {
            System.err.println("[Gmail] Credentials not set — skipping email.");
            return false;
        }

        // Gmail SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.ssl.trust",       "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Strip spaces from app password (Google sometimes shows it with spaces)
                return new PasswordAuthentication(
                        GMAIL_ADDRESS,
                        GMAIL_APP_PASSWORD.replace(" ", "")
                );
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(GMAIL_ADDRESS, "BlindHire"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(isReset
                    ? "BlindHire — Password Reset Code"
                    : "BlindHire — Verify Your Account");

            // HTML body
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(
                    isReset ? buildResetHtml(toName, code) : buildVerifyHtml(toName, code),
                    "text/html; charset=utf-8"
            );

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            msg.setContent(multipart);

            Transport.send(msg);
            System.out.println("[Gmail] Email sent to " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[Gmail] Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================================
    //  SMS  —  Twilio
    // =========================================================================

    /**
     * Sends a verification code by SMS using Twilio.
     *
     * @param toPhone  recipient phone in E.164 format (e.g. +21612345678)
     * @param code     6-digit code to send
     * @param isReset  true = password reset, false = signup verification
     * @return true if sent successfully
     */
    public static boolean sendSmsCode(String toPhone, String code, boolean isReset) {
        if (TWILIO_ACCOUNT_SID.equals("YOUR_TWILIO_ACCOUNT_SID_HERE")) {
            System.err.println("[Twilio] Credentials not set — skipping SMS.");
            return false;
        }

        String message = isReset
                ? "BlindHire: Your password reset code is " + code + ". Valid for 10 minutes."
                : "BlindHire: Your verification code is " + code + ". Valid for 10 minutes.";

        try {
            String params = "To="   + urlEncode(toPhone)
                    + "&From=" + urlEncode(TWILIO_FROM_PHONE)
                    + "&Body=" + urlEncode(message);

            URL url = new URL("https://api.twilio.com/2010-04-01/Accounts/"
                    + TWILIO_ACCOUNT_SID + "/Messages.json");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String auth = java.util.Base64.getEncoder().encodeToString(
                    (TWILIO_ACCOUNT_SID + ":" + TWILIO_AUTH_TOKEN).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status == 200 || status == 201) {
                System.out.println("[Twilio] SMS sent to " + toPhone);
                return true;
            } else {
                System.err.println("[Twilio] HTTP " + status + ": " + readStream(conn.getErrorStream()));
                return false;
            }
        } catch (Exception e) {
            System.err.println("[Twilio] " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    //  EMAIL TEMPLATES
    // =========================================================================

    private static String buildVerifyHtml(String name, String code) {
        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:0 auto;padding:30px;'>"
                + "<h2 style='color:#4A6CF7;'>Welcome to BlindHire!</h2>"
                + "<p>Hi <b>" + name + "</b>,</p>"
                + "<p>Please verify your account using the code below:</p>"
                + "<div style='background:#f0f0f5;border-radius:10px;padding:20px;"
                + "text-align:center;font-size:36px;font-weight:bold;"
                + "letter-spacing:8px;color:#1a1a2e;margin:20px 0;'>"
                + code
                + "</div>"
                + "<p style='color:#888;font-size:13px;'>This code expires in <b>10 minutes</b>.</p>"
                + "<p style='color:#888;font-size:12px;'>If you did not create this account, ignore this email.</p>"
                + "</div>";
    }

    private static String buildResetHtml(String name, String code) {
        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:0 auto;padding:30px;'>"
                + "<h2 style='color:#e74c3c;'>Password Reset</h2>"
                + "<p>Hi <b>" + name + "</b>,</p>"
                + "<p>Use the code below to reset your BlindHire password:</p>"
                + "<div style='background:#f0f0f5;border-radius:10px;padding:20px;"
                + "text-align:center;font-size:36px;font-weight:bold;"
                + "letter-spacing:8px;color:#1a1a2e;margin:20px 0;'>"
                + code
                + "</div>"
                + "<p style='color:#888;font-size:13px;'>This code expires in <b>10 minutes</b>.</p>"
                + "<p style='color:#888;font-size:12px;'>If you did not request a reset, ignore this email.</p>"
                + "</div>";
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    private static String urlEncode(String s) throws Exception {
        return java.net.URLEncoder.encode(s, "UTF-8");
    }
}