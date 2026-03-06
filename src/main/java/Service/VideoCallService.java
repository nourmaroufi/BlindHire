package Service;

import Model.Interview;
import java.awt.Desktop;
import java.net.URI;

/**
 * Opens a Jitsi Meet video call for an interview.
 *
 * Room naming convention:
 *   blindhire-interview-{interviewId}
 *
 * Both recruiter ("RECRUITER") and candidate ("CANDIDATE") open the
 * exact same URL — Jitsi handles the multi-party room automatically.
 * No API key or account needed; meet.jit.si is free and open.
 */
public class VideoCallService {

    private static final String JITSI_BASE = "https://meet.jit.si/";

    /**
     * Opens the Jitsi Meet room for this interview in the system browser.
     *
     * @param interview  the Interview entity (needs getId())
     * @param role       "RECRUITER" or "CANDIDATE" — both open the same URL
     */
    public void openVideoCall(Interview interview, String role) {
        // Deterministic room name based on interview ID — same room for all parties
        String roomName = "blindhire-interview-" + interview.getId();
        String url = JITSI_BASE + roomName;

        // Optional: append display name as a URL fragment hint
        // Jitsi respects #config.startWithAudioMuted=true etc.
        // We keep it simple — just the room URL
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback: try xdg-open (Linux) or start (Windows)
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                }
            }
        } catch (Exception e) {
            System.err.println("[VideoCallService] Could not open browser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the Jitsi Meet URL for an interview without opening it.
     * Useful for copying to clipboard or displaying in the UI.
     */
    public String getCallUrl(Interview interview) {
        return JITSI_BASE + "blindhire-interview-" + interview.getId();
    }
}