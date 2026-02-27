package Utils;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages "Remember Me" session persistence.
 *
 * Stores the logged-in user's ID in a local file:
 *   <user home>/.blindhire/session.properties
 *
 * On app start: call SessionManager.getSavedUserId() — if >= 0, auto-login.
 * On login with "remember me": call SessionManager.saveSession(userId).
 * On logout: call SessionManager.clearSession().
 */
public class SessionManager {

    private static final String APP_DIR      = System.getProperty("user.home") + File.separator + ".blindhire";
    private static final String SESSION_FILE = APP_DIR + File.separator + "session.properties";
    private static final String KEY_USER_ID  = "userId";

    // ── Save ──────────────────────────────────────────────────────────────────

    public static void saveSession(int userId) {
        try {
            Files.createDirectories(Paths.get(APP_DIR));
            Properties props = new Properties();
            props.setProperty(KEY_USER_ID, String.valueOf(userId));
            try (FileOutputStream fos = new FileOutputStream(SESSION_FILE)) {
                props.store(fos, "BlindHire session — do not edit");
            }
            System.out.println("[Session] Saved session for userId=" + userId);
        } catch (Exception e) {
            System.err.println("[Session] Could not save session: " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /** Returns the saved user ID, or -1 if no session exists. */
    public static int getSavedUserId() {
        File f = new File(SESSION_FILE);
        if (!f.exists()) return -1;
        try (FileInputStream fis = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(fis);
            String val = props.getProperty(KEY_USER_ID, "-1");
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            System.err.println("[Session] Could not read session: " + e.getMessage());
            return -1;
        }
    }

    // ── Clear ─────────────────────────────────────────────────────────────────

    public static void clearSession() {
        try {
            Files.deleteIfExists(Paths.get(SESSION_FILE));
            System.out.println("[Session] Session cleared.");
        } catch (Exception e) {
            System.err.println("[Session] Could not clear session: " + e.getMessage());
        }
    }

    public static boolean hasSession() {
        return getSavedUserId() >= 0;
    }
}