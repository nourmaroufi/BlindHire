package services;

import model.Interview;
import java.awt.Desktop;
import java.net.URI;

public class VideoCallService {

    public void openVideoCall(Interview interview, String participantRole) {
        try {
            String roomName = "BlindHire-Interview-" + interview.getId();
            String jitsiUrl = "https://meet.jit.si/" + roomName;

            // Open in system default browser instead of WebView
            Desktop.getDesktop().browse(new URI(jitsiUrl));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}