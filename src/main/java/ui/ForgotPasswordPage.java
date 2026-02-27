package ui;

import Model.User;
import Service.userservice;
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

public class ForgotPasswordPage {

    private StackPane root;
    private userservice userService;

    // Step 1 — contact input
    private VBox      step1Box;
    private ToggleGroup contactToggle;
    private RadioButton emailRadio;
    private RadioButton phoneRadio;
    private TextField   contactField;
    private Label       step1Status;

    // Step 2 — code + new password
    private VBox         step2Box;
    private TextField    codeField;
    private PasswordField newPassField;
    private PasswordField confirmPassField;
    private Label        step2Status;

    private User   resetUser;   // the user we're resetting for
    private boolean usingPhone; // whether step1 used phone

    public ForgotPasswordPage() {
        this.userService = new userservice();
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.getChildren().addAll(createBackground(), createContent());
    }

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

    private VBox createContent() {
        VBox outer = new VBox();
        outer.setAlignment(Pos.CENTER);
        outer.setPadding(new Insets(50));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(480);
        card.setStyle(
                "-fx-background-color: rgba(168,230,245,0.6);" +
                        "-fx-background-radius: 30; -fx-padding: 44;"
        );

        Text icon  = new Text("🔒");
        icon.setFont(Font.font(48));

        Text title = new Text("Forgot Password");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setFill(Color.web("#2C3E50"));

        // ── Step 1: enter email or phone ──
        step1Box = buildStep1();

        // ── Step 2: enter code + new password (hidden initially) ──
        step2Box = buildStep2();
        step2Box.setVisible(false);
        step2Box.setManaged(false);

        Hyperlink backToLogin = new Hyperlink("← Back to Login");
        backToLogin.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 13px;");
        backToLogin.setOnAction(e -> BlindHireApp.loadScene(new LoginPage().getRoot(), 960, 540));

        card.getChildren().addAll(icon, title, step1Box, step2Box, backToLogin);
        outer.getChildren().add(card);
        return outer;
    }

    // ─── STEP 1 ───────────────────────────────────────────────────────────────

    private VBox buildStep1() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);

        Text sub = new Text("Choose how to receive your reset code:");
        sub.setFont(Font.font("System", 13));
        sub.setFill(Color.web("#555"));

        // Radio buttons
        contactToggle = new ToggleGroup();
        emailRadio = new RadioButton("Send code to my Email");
        phoneRadio = new RadioButton("Send code to my Phone");
        emailRadio.setToggleGroup(contactToggle);
        phoneRadio.setToggleGroup(contactToggle);
        emailRadio.setSelected(true);
        emailRadio.setStyle("-fx-font-size: 13px;");
        phoneRadio.setStyle("-fx-font-size: 13px;");

        HBox radioBox = new HBox(20, emailRadio, phoneRadio);
        radioBox.setAlignment(Pos.CENTER);

        contactField = new TextField();
        contactField.setPromptText("Enter your email address");
        contactField.setStyle(inputCss(360));

        // Update prompt when radio changes
        emailRadio.setOnAction(e -> contactField.setPromptText("Enter your email address"));
        phoneRadio.setOnAction(e -> contactField.setPromptText("Enter your phone (e.g. +21612345678)"));

        step1Status = new Label();
        step1Status.setFont(Font.font(12));
        step1Status.setWrapText(true);
        step1Status.setMaxWidth(360);

        Button sendBtn = new Button("Send Reset Code");
        sendBtn.setStyle(
                "-fx-background-color: #3E4A5E; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 25;" +
                        "-fx-padding: 11 50; -fx-cursor: hand;"
        );
        sendBtn.setOnAction(e -> handleSendCode());

        box.getChildren().addAll(sub, radioBox, contactField, step1Status, sendBtn);
        return box;
    }

    // ─── STEP 2 ───────────────────────────────────────────────────────────────

    private VBox buildStep2() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);

        Text sub = new Text("Enter the code you received and your new password:");
        sub.setFont(Font.font("System", 13));
        sub.setFill(Color.web("#555"));
        sub.setWrappingWidth(360);
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        codeField = new TextField();
        codeField.setPromptText("6-digit reset code");
        codeField.setStyle(inputCss(360) + "-fx-font-size: 20px; -fx-alignment: center;");
        codeField.textProperty().addListener((obs, o, nw) -> {
            if (!nw.matches("\\d*")) codeField.setText(nw.replaceAll("[^\\d]", ""));
            if (codeField.getText().length() > 6)
                codeField.setText(codeField.getText().substring(0, 6));
        });

        newPassField = new PasswordField();
        newPassField.setPromptText("New password (min 6 chars, letter + number)");
        newPassField.setStyle(inputCss(360));

        confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setStyle(inputCss(360));

        step2Status = new Label();
        step2Status.setFont(Font.font(12));
        step2Status.setWrapText(true);
        step2Status.setMaxWidth(360);

        Button resetBtn = new Button("Reset Password");
        resetBtn.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white;" +
                        "-fx-font-size: 14px; -fx-background-radius: 25;" +
                        "-fx-padding: 11 50; -fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> handleResetPassword());

        Hyperlink resendLink = new Hyperlink("Resend code");
        resendLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 12px;");
        resendLink.setOnAction(e -> {
            handleSendCode(); // re-trigger step 1 logic
            showStep2Status("A new code was sent.", false);
        });

        box.getChildren().addAll(sub, codeField, newPassField, confirmPassField, step2Status, resetBtn, resendLink);
        return box;
    }

    // ─── HANDLERS ─────────────────────────────────────────────────────────────

    private void handleSendCode() {
        String contact = contactField.getText().trim();
        usingPhone     = phoneRadio.isSelected();

        if (contact.isEmpty()) {
            showStep1Status(usingPhone ? "Please enter your phone number." : "Please enter your email.", true);
            return;
        }
        if (!usingPhone && !contact.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showStep1Status("Please enter a valid email address.", true); return;
        }
        if (usingPhone && !contact.matches("^\\+?[0-9]{8,15}$")) {
            showStep1Status("Please enter a valid phone number (e.g. +21612345678).", true); return;
        }

        try {
            resetUser = userService.initiatePasswordReset(contact, usingPhone);
            showStep1Status("✅ Code sent! Check your " + (usingPhone ? "phone." : "email."), false);

            // Switch to step 2
            step1Box.setVisible(false); step1Box.setManaged(false);
            step2Box.setVisible(true);  step2Box.setManaged(true);

        } catch (IllegalArgumentException e) {
            showStep1Status(e.getMessage(), true);
        } catch (Exception e) {
            showStep1Status("An error occurred. Please try again.", true);
            e.printStackTrace();
        }
    }

    private void handleResetPassword() {
        String code        = codeField.getText().trim();
        String newPass     = newPassField.getText();
        String confirmPass = confirmPassField.getText();

        if (code.length() != 6) {
            showStep2Status("Please enter the full 6-digit code.", true); return;
        }
        if (newPass.isEmpty()) {
            showStep2Status("Please enter a new password.", true); return;
        }
        if (!newPass.equals(confirmPass)) {
            showStep2Status("Passwords do not match.", true); return;
        }

        try {
            userService.resetPassword(resetUser, code, newPass);
            showStep2Status("✅ Password reset successfully! Redirecting to login...", false);

            new Thread(() -> {
                try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() ->
                        BlindHireApp.loadScene(new LoginPage().getRoot(), 960, 540));
            }).start();

        } catch (IllegalArgumentException e) {
            showStep2Status(e.getMessage(), true);
        } catch (Exception e) {
            showStep2Status("An error occurred. Please try again.", true);
            e.printStackTrace();
        }
    }

    private void showStep1Status(String msg, boolean err) {
        step1Status.setText(msg);
        step1Status.setTextFill(err ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    private void showStep2Status(String msg, boolean err) {
        step2Status.setText(msg);
        step2Status.setTextFill(err ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    private String inputCss(double width) {
        return "-fx-background-color: white; -fx-background-radius: 12;" +
                "-fx-padding: 12 18; -fx-font-size: 14px;" +
                "-fx-pref-width: " + width + ";";
    }

    public Parent getRoot() { return root; }
}