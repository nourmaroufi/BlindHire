package ui;

import Service.FaceService;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Modal webcam window — REGISTER or AUTH mode.
 *
 * FIX: AnimationTimer is now created and started on the JavaFX thread
 *      (via Platform.runLater) instead of from the camera background thread.
 *      Previously the timer was started on the wrong thread, which could cause
 *      it to never fire on Windows.
 */
public class FaceCaptureDialog {

    public enum Mode { REGISTER, AUTH }

    private Stage         stage;
    private FaceService   faceService;
    private ImageView     cameraView;
    private Label         statusLabel;
    private AnimationTimer frameTimer;
    private Button        actionBtn;   // kept as field so handleAction can re-enable it

    private String  capturedFaceBase64 = null;
    private boolean authSuccess        = false;

    private final Mode   mode;
    private final String storedFace;

    public FaceCaptureDialog(Mode mode, String storedFace) {
        this.mode       = mode;
        this.storedFace = storedFace;
        this.faceService = new FaceService();
    }

    /** Convenience constructor for REGISTER mode */
    public FaceCaptureDialog() { this(Mode.REGISTER, null); }

    // =========================================================================
    //  SHOW
    // =========================================================================

    public void showAndWait() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(mode == Mode.REGISTER ? "Register Your Face" : "Face Login");
        stage.setOnCloseRequest(e -> shutdown());
        stage.setScene(new Scene(buildUI(), 620, 560));
        stage.setResizable(false);

        // Start camera on a background thread, then start the frame timer on JavaFX thread
        new Thread(() -> {
            try {
                faceService.startCamera();
                // ✅ FIX: Platform.runLater ensures AnimationTimer is created on the FX thread
                Platform.runLater(this::startFrameTimer);
            } catch (Exception e) {
                Platform.runLater(() ->
                        setStatus("❌ Could not open camera: " + e.getMessage(), true));
            }
        }, "camera-start-thread").start();

        stage.showAndWait();
    }

    // =========================================================================
    //  UI
    // =========================================================================

    private VBox buildUI() {
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Text title = new Text(mode == Mode.REGISTER ? "📸  Register Your Face" : "🔍  Face Login");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setFill(Color.WHITE);

        Text instruction = new Text(mode == Mode.REGISTER
                ? "Position your face inside the green box, then click Capture."
                : "Look at the camera and click Verify Face.");
        instruction.setFont(Font.font("System", 13));
        instruction.setFill(Color.web("#aaa"));
        instruction.setWrappingWidth(560);
        instruction.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Tips label — helps users get the camera working
        Text tips = new Text(
                mode == Mode.REGISTER
                        ? "💡 Tips: good lighting, 50–80 cm from screen, face the camera directly"
                        : "💡 Tips: same lighting as when you registered, face forward"
        );
        tips.setFont(Font.font("System", 11));
        tips.setFill(Color.web("#888"));
        tips.setWrappingWidth(560);
        tips.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Camera feed
        cameraView = new ImageView();
        cameraView.setFitWidth(480);
        cameraView.setFitHeight(360);
        cameraView.setPreserveRatio(true);

        StackPane cameraPane = new StackPane(cameraView);
        cameraPane.setStyle(
                "-fx-background-color: #0d0d1a;" +
                        "-fx-border-color: #4A6CF7;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        cameraPane.setMaxWidth(490);
        cameraPane.setMaxHeight(370);

        statusLabel = new Label("⏳ Starting camera...");
        statusLabel.setTextFill(Color.web("#aaa"));
        statusLabel.setFont(Font.font(12));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(560);

        // Buttons
        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        actionBtn = new Button(mode == Mode.REGISTER ? "📸  Capture Face" : "✓  Verify Face");
        actionBtn.setStyle(
                "-fx-background-color: #4A6CF7; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 25;" +
                        "-fx-padding: 11 36; -fx-cursor: hand; -fx-font-weight: bold;"
        );

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #444; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-background-radius: 25;" +
                        "-fx-padding: 11 28; -fx-cursor: hand;"
        );

        actionBtn.setOnAction(e -> handleAction());
        cancelBtn.setOnAction(e -> shutdown());
        buttons.getChildren().addAll(actionBtn, cancelBtn);

        root.getChildren().addAll(title, instruction, tips, cameraPane, statusLabel, buttons);
        return root;
    }

    // =========================================================================
    //  CAMERA LOOP  — must be called on JavaFX thread
    // =========================================================================

    private void startFrameTimer() {
        frameTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // Throttle to ~30 fps to avoid overwhelming the UI thread
                if (now - lastUpdate < 33_000_000L) return;
                lastUpdate = now;

                if (!faceService.isRunning()) return;

                // Grab frame on a separate thread to avoid blocking JavaFX
                new Thread(() -> {
                    javafx.scene.image.Image img = faceService.grabFrameAsImage();
                    if (img != null) {
                        Platform.runLater(() -> {
                            cameraView.setImage(img);
                            // Update status once camera is confirmed live
                            String current = statusLabel.getText();
                            if (current.startsWith("⏳") || current.startsWith("Camera")) {
                                setStatus("✅ Camera ready — " +
                                        (mode == Mode.REGISTER
                                                ? "align your face and click Capture."
                                                : "look at the camera and click Verify Face."), false);
                            }
                        });
                    }
                }).start();
            }
        };
        frameTimer.start();
    }

    // =========================================================================
    //  ACTION
    // =========================================================================

    private void handleAction() {
        actionBtn.setDisable(true);
        setStatus(mode == Mode.REGISTER ? "📸 Capturing — hold still..." : "🔍 Comparing faces...", false);

        new Thread(() -> {
            try {
                if (mode == Mode.REGISTER) {
                    capturedFaceBase64 = faceService.captureFaceSnapshot();
                    Platform.runLater(() -> {
                        setStatus("✅ Face captured successfully!", false);
                        actionBtn.setText("✅ Captured");
                        // Close after short delay
                        new Thread(() -> {
                            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                            Platform.runLater(this::shutdown);
                        }).start();
                    });
                } else {
                    authSuccess = faceService.compareFace(storedFace);
                    Platform.runLater(() -> {
                        if (authSuccess) {
                            setStatus("✅ Face recognized! Logging in...", false);
                            new Thread(() -> {
                                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                                Platform.runLater(this::shutdown);
                            }).start();
                        } else {
                            setStatus("❌ Face not recognized. Please try again.", true);
                            actionBtn.setDisable(false);
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // Show the detailed error message from FaceService (includes tips)
                    setStatus("❌ " + e.getMessage(), true);
                    actionBtn.setDisable(false);
                });
            }
        }, "face-action-thread").start();
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void shutdown() {
        if (frameTimer != null) frameTimer.stop();
        faceService.stopCamera();
        if (stage != null && stage.isShowing()) stage.close();
    }

    private void setStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setTextFill(isError ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    public String  getCapturedFace()  { return capturedFaceBase64; }
    public boolean isAuthSuccess()    { return authSuccess; }
}