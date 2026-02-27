package ui;

import Model.User;
import Service.NotificationService;
import Service.userservice;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class VerificationPage {

    private StackPane   root;
    private User        user;
    private userservice userService;

    // Step 1 — choose channel
    private VBox        step1Box;
    private RadioButton emailRadio;
    private RadioButton phoneRadio;
    private Label       step1Status;

    // Step 2 — enter code
    private VBox      step2Box;
    private TextField codeField;
    private Label     step2Status;
    private Label     sentToLabel;

    private boolean usedPhone = false;

    public VerificationPage(User user) {
        this.user        = user;
        this.userService = new userservice();
        userService.setCurrentUser(user);
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.getChildren().addAll(createBackground(), createContent());
    }

    // ─── BACKGROUND ───────────────────────────────────────────────────────────

    private Pane createBackground() {
        Pane pane = new Pane();
        Path wave = new Path();
        wave.setFill(Color.web("#A8E6F5")); wave.setLayoutX(0); wave.setLayoutY(410);
        wave.getElements().addAll(new MoveTo(0,0), new CubicCurveTo(150,-50,300,50,450,0),
                new LineTo(450,130), new LineTo(0,130), new ClosePath());
        Path right = new Path();
        right.setFill(Color.web("#6BA3BE")); right.setLayoutX(450); right.setLayoutY(0);
        right.getElements().addAll(new MoveTo(0,0), new LineTo(510,0), new LineTo(510,540),
                new CubicCurveTo(350,400,200,300,0,200), new ClosePath());
        pane.getChildren().addAll(wave, right);
        return pane;
    }

    // ─── CONTENT ──────────────────────────────────────────────────────────────

    private VBox createContent() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(60));

        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(480);
        card.setStyle(
                "-fx-background-color: rgba(168,230,245,0.6);" +
                        "-fx-background-radius: 30; -fx-padding: 44;"
        );

        Text icon  = new Text("🔐");
        icon.setFont(Font.font(52));

        Text title = new Text("Verify Your Account");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setFill(Color.web("#2C3E50"));

        // Build both steps
        step1Box = buildStep1();
        step2Box = buildStep2();
        step2Box.setVisible(false);
        step2Box.setManaged(false);

        card.getChildren().addAll(icon, title, step1Box, step2Box);
        outer.getChildren().add(card);
        return outer;
    }

    // ─── STEP 1 : choose email or phone ───────────────────────────────────────

    private VBox buildStep1() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);

        Text sub = new Text("How would you like to receive your verification code?");
        sub.setFont(Font.font("System", 13));
        sub.setFill(Color.web("#444"));
        sub.setWrappingWidth(360);
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // ── Choice cards ──
        boolean hasPhone = user.getPhone() != null && !user.getPhone().isEmpty();

        ToggleGroup group = new ToggleGroup();
        emailRadio = new RadioButton("📧  Email\n" + user.getEmail());
        emailRadio.setToggleGroup(group);
        emailRadio.setSelected(true);
        emailRadio.setWrapText(true);
        styleChoiceCard(emailRadio, true);

        phoneRadio = new RadioButton("📱  SMS\n" + (hasPhone ? maskPhone(user.getPhone()) : "No phone registered"));
        phoneRadio.setToggleGroup(group);
        phoneRadio.setDisable(!hasPhone);
        phoneRadio.setWrapText(true);
        styleChoiceCard(phoneRadio, false);

        // Highlight selected card
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            styleChoiceCard(emailRadio, emailRadio.isSelected());
            styleChoiceCard(phoneRadio, phoneRadio.isSelected());
        });

        HBox choices = new HBox(14, emailRadio, phoneRadio);
        choices.setAlignment(Pos.CENTER);

        step1Status = new Label();
        step1Status.setFont(Font.font(12));
        step1Status.setWrapText(true);
        step1Status.setMaxWidth(360);

        Button sendBtn = new Button("Send Code");
        sendBtn.setStyle(
                "-fx-background-color: #3E4A5E; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 25;" +
                        "-fx-padding: 12 60; -fx-cursor: hand;"
        );
        sendBtn.setOnAction(e -> handleSendCode());

        box.getChildren().addAll(sub, choices, step1Status, sendBtn);
        return box;
    }

    // ─── STEP 2 : enter the code ──────────────────────────────────────────────

    private VBox buildStep2() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);

        sentToLabel = new Label();
        sentToLabel.setFont(Font.font("System", 13));
        sentToLabel.setTextFill(Color.web("#444"));
        sentToLabel.setWrapText(true);
        sentToLabel.setMaxWidth(360);
        sentToLabel.setAlignment(Pos.CENTER);

        codeField = new TextField();
        codeField.setPromptText("Enter 6-digit code");
        codeField.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 14 20;" +
                        "-fx-font-size: 24px;" +
                        "-fx-pref-width: 240;" +
                        "-fx-alignment: center;"
        );
        // Digits only, max 6
        codeField.textProperty().addListener((obs, o, nw) -> {
            if (!nw.matches("\\d*")) codeField.setText(nw.replaceAll("[^\\d]", ""));
            if (codeField.getText().length() > 6)
                codeField.setText(codeField.getText().substring(0, 6));
        });
        codeField.setOnAction(e -> handleVerify());

        step2Status = new Label();
        step2Status.setFont(Font.font(12));
        step2Status.setWrapText(true);
        step2Status.setMaxWidth(360);

        Button verifyBtn = new Button("Verify Account ✓");
        verifyBtn.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 25;" +
                        "-fx-padding: 12 50; -fx-cursor: hand;"
        );
        verifyBtn.setOnAction(e -> handleVerify());

        HBox bottomRow = new HBox(16);
        bottomRow.setAlignment(Pos.CENTER);

        Button changeMethodBtn = new Button("← Change method");
        changeMethodBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4A9DB5;" +
                        "-fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: #4A9DB5;" +
                        "-fx-border-radius: 20; -fx-padding: 6 14;"
        );
        changeMethodBtn.setOnAction(e -> switchToStep1());

        Hyperlink resendLink = new Hyperlink("Resend code");
        resendLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 12px;");
        resendLink.setOnAction(e -> handleResend());

        bottomRow.getChildren().addAll(changeMethodBtn, resendLink);
        box.getChildren().addAll(sentToLabel, codeField, step2Status, verifyBtn, bottomRow);
        return box;
    }

    // ─── HANDLERS ─────────────────────────────────────────────────────────────

    private void handleSendCode() {
        usedPhone = phoneRadio.isSelected();

        // Generate and store a fresh code
        String code = NotificationService.generateCode();
        try {
            // Update DB
            new DAO.userDAO().updateVerificationCode(user.getId(), code);
            user.setVerificationCode(code);
        } catch (Exception e) {
            showStep1Status("Database error. Please try again.", true);
            return;
        }

        boolean sent;
        if (usedPhone) {
            sent = NotificationService.sendSmsCode(user.getPhone(), code, false);
        } else {
            sent = NotificationService.sendEmailCode(
                    user.getEmail(),
                    user.getNom() + " " + user.getPrenom(),
                    code, false
            );
        }

        if (!sent) {
            showStep1Status("Failed to send code. Check your credentials in NotificationService.java.", true);
            return;
        }

        // Update sent-to label and transition to step 2
        sentToLabel.setText("A 6-digit code was sent to: "
                + (usedPhone ? maskPhone(user.getPhone()) : user.getEmail()));

        switchToStep2();
    }

    private void handleVerify() {
        String code = codeField.getText().trim();
        if (code.length() != 6) {
            showStep2Status("Please enter the full 6-digit code.", true); return;
        }
        try {
            userService.verifyAccount(user, code);
            showStep2Status("✅ Account verified!", false);

            new Thread(() -> {
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    switch (user.getRole()) {
                        case admin:
                            BlindHireApp.loadScene(new DashboardPage().getRoot(), 960, 540); break;
                        default:
                            BlindHireApp.loadScene(new HomePage(user).getRoot(), 960, 540); break;
                    }
                });
            }).start();

        } catch (IllegalArgumentException e) {
            showStep2Status(e.getMessage(), true);
        }
    }

    private void handleResend() {
        // Re-send using the same channel chosen in step 1
        String code = NotificationService.generateCode();
        try {
            new DAO.userDAO().updateVerificationCode(user.getId(), code);
            user.setVerificationCode(code);
        } catch (Exception e) {
            showStep2Status("Database error. Please try again.", true); return;
        }

        boolean sent = usedPhone
                ? NotificationService.sendSmsCode(user.getPhone(), code, false)
                : NotificationService.sendEmailCode(
                user.getEmail(),
                user.getNom() + " " + user.getPrenom(),
                code, false);

        showStep2Status(sent
                ? "✅ New code sent to " + (usedPhone ? maskPhone(user.getPhone()) : user.getEmail()) + "."
                : "❌ Failed to resend. Try changing method.", !sent);
    }

    // ─── TRANSITIONS ──────────────────────────────────────────────────────────

    private void switchToStep2() {
        FadeTransition out = new FadeTransition(Duration.millis(200), step1Box);
        out.setFromValue(1); out.setToValue(0);
        out.setOnFinished(e -> {
            step1Box.setVisible(false); step1Box.setManaged(false);
            step2Box.setVisible(true);  step2Box.setManaged(true);
            step2Box.setOpacity(0);
            FadeTransition in = new FadeTransition(Duration.millis(300), step2Box);
            in.setFromValue(0); in.setToValue(1); in.play();
            codeField.requestFocus();
        });
        out.play();
    }

    private void switchToStep1() {
        codeField.clear();
        step2Status.setText("");
        FadeTransition out = new FadeTransition(Duration.millis(200), step2Box);
        out.setFromValue(1); out.setToValue(0);
        out.setOnFinished(e -> {
            step2Box.setVisible(false); step2Box.setManaged(false);
            step1Box.setVisible(true);  step1Box.setManaged(true);
            step1Box.setOpacity(0);
            FadeTransition in = new FadeTransition(Duration.millis(300), step1Box);
            in.setFromValue(0); in.setToValue(1); in.play();
        });
        out.play();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void styleChoiceCard(RadioButton rb, boolean selected) {
        rb.setStyle(
                "-fx-background-color: " + (selected ? "white" : "#f0f0f5") + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + (selected ? "#4A6CF7" : "#ddd") + ";" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 16 20;" +
                        "-fx-font-size: 13px;" +
                        "-fx-pref-width: 160;" +
                        "-fx-text-fill: " + (selected ? "#1a1a2e" : "#666") + ";" +
                        "-fx-cursor: hand;"
        );
    }

    private void showStep1Status(String msg, boolean err) {
        step1Status.setText(msg);
        step1Status.setTextFill(err ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    private void showStep2Status(String msg, boolean err) {
        step2Status.setText(msg);
        step2Status.setTextFill(err ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) return phone;
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    public Parent getRoot() { return root; }
}