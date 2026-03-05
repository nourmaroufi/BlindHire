package ui;

import Model.User;
import Service.userservice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LoginPage {

    private StackPane root;
    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;

    private static final String BG_DEEP      = "#0f172a";
    private static final String ACCENT_CYAN  = "#06b6d4";
    private static final String TEXT_PRIMARY = "#f1f5f9";
    private static final String TEXT_MUTED   = "#94a3b8";

    public LoginPage() {
        createUI();
    }

    // ── Exact same pattern as old: StackPane + bg pane + BorderPane content ──
    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");

        Pane backgroundPane = createBackgroundShapes();
        BorderPane contentPane = createContent();

        root.getChildren().addAll(backgroundPane, contentPane);
        // No stage hacks — BlindHireApp.loadScene handles sizing
    }

    // ── Same Path shapes, dark palette ──
    private Pane createBackgroundShapes() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        Path wavePath = new Path();
        wavePath.setFill(Color.web("#6366f1", 0.15));
        wavePath.setLayoutX(0);
        wavePath.setLayoutY(410);
        wavePath.getElements().addAll(
                new MoveTo(0, 0),
                new CubicCurveTo(150, -50, 300, 50, 450, 0),
                new LineTo(450, 130), new LineTo(0, 130), new ClosePath()
        );

        Path rightShape = new Path();
        rightShape.setFill(Color.web("#0f2744", 0.92));
        rightShape.setLayoutX(450);
        rightShape.setLayoutY(0);
        rightShape.getElements().addAll(
                new MoveTo(0, 0), new LineTo(510, 0), new LineTo(510, 540),
                new CubicCurveTo(350, 400, 200, 300, 0, 200), new ClosePath()
        );

        Path overlayShape = new Path();
        overlayShape.setFill(Color.web("#06b6d4", 0.06));
        overlayShape.setLayoutX(400);
        overlayShape.setLayoutY(0);
        overlayShape.getElements().addAll(
                new MoveTo(0, 0),
                new CubicCurveTo(50, 100, 100, 200, 150, 300),
                new LineTo(560, 300), new LineTo(560, 0), new ClosePath()
        );

        pane.getChildren().addAll(wavePath, rightShape, overlayShape);
        return pane;
    }

    // ── Same BorderPane structure: top = HBox(logo + back), center = card ──
    private BorderPane createContent() {
        BorderPane borderPane = new BorderPane();

        // Top — logo left, back button right
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.CENTER_LEFT);

        VBox logo = createLogo();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backButton = new Button("← back");
        backButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-font-size: 13px; -fx-background-radius: 8;" +
                        "-fx-padding: 6 14; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 8; -fx-border-width: 1;"
        );
        backButton.setOnAction(e -> BlindHireApp.loadSceneFullscreen(new WelcomePage().getRoot()));

        topBox.getChildren().addAll(logo, spacer, backButton);
        borderPane.setTop(topBox);

        // Center — card centered in VBox, same as old
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        VBox formCard = new VBox(16);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(420);
        formCard.setStyle(
                "-fx-background-color: rgba(30,41,59,0.80);" +
                        "-fx-background-radius: 28;" +
                        "-fx-padding: 40;" +
                        "-fx-border-color: rgba(99,102,241,0.25);" +
                        "-fx-border-radius: 28; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 40, 0, 0, 10);"
        );

        Text titleText = new Text("Welcome Back");
        titleText.setFill(Color.web(TEXT_PRIMARY));
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleText.setEffect(new DropShadow(10, Color.web("#06b6d4", 0.2)));

        // Fields — same structure as old
        emailField = new TextField();
        emailField.setPromptText("email");
        styleInput(emailField);

        passwordField = new PasswordField();
        passwordField.setPromptText("password");
        styleInput(passwordField);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#f43f5e"));
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(340);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + "; -fx-font-family: 'Segoe UI';");

        Button loginButton = new Button("Sign In");
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2);" +
                        "-fx-text-fill: white; -fx-font-size: 15px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 25; -fx-padding: 12 80; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.4), 14, 0, 0, 3);"
        );
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #22d3ee, #06b6d4);" +
                        "-fx-text-fill: white; -fx-font-size: 15px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 25; -fx-padding: 12 80; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.6), 18, 0, 0, 5);"
        ));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2);" +
                        "-fx-text-fill: white; -fx-font-size: 15px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 25; -fx-padding: 12 80; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.4), 14, 0, 0, 3);"
        ));
        loginButton.setOnAction(e -> handleLogin(rememberMe.isSelected()));

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setStyle("-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 12px; -fx-border-color: transparent; -fx-font-family: 'Segoe UI';");
        forgotPasswordLink.setOnAction(e -> BlindHireApp.loadScene(new ForgotPasswordPage().getRoot(), 960, 540));

        // Divider — same as old
        Label orLabel = new Label("── or ──");
        orLabel.setStyle("-fx-text-fill: rgba(148,163,184,0.5); -fx-font-size: 12px;");

        Button faceLoginBtn = new Button("📷  Login with Face Recognition");
        faceLoginBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.12);" +
                        "-fx-text-fill: #a5b4fc; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 25; -fx-padding: 12 40; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(99,102,241,0.3); -fx-border-radius: 25; -fx-border-width: 1;"
        );
        faceLoginBtn.setOnMouseEntered(e -> faceLoginBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.22);" +
                        "-fx-text-fill: #c7d2fe; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 25; -fx-padding: 12 40; -fx-cursor: hand;" +
                        "-fx-border-color: #6366f1; -fx-border-radius: 25; -fx-border-width: 1;"
        ));
        faceLoginBtn.setOnMouseExited(e -> faceLoginBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.12);" +
                        "-fx-text-fill: #a5b4fc; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                        "-fx-background-radius: 25; -fx-padding: 12 40; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(99,102,241,0.3); -fx-border-radius: 25; -fx-border-width: 1;"
        ));
        faceLoginBtn.setOnAction(e -> handleFaceLogin());

        Hyperlink signupLink = new Hyperlink("New here? Create an account");
        signupLink.setStyle("-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 12px; -fx-border-color: transparent; -fx-font-family: 'Segoe UI';");
        signupLink.setOnAction(e -> BlindHireApp.loadScene(new SignupPage().getRoot(), 960, 540));

        formCard.getChildren().addAll(titleText, emailField, passwordField,
                errorLabel, rememberMe, loginButton, forgotPasswordLink, orLabel, faceLoginBtn, signupLink);
        centerBox.getChildren().add(formCard);
        borderPane.setCenter(centerBox);

        return borderPane;
    }

    // ── Same logo structure as old ──
    private VBox createLogo() {
        VBox logoBox = new VBox(3);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        ImageView logoImg = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/blindhire_logo.png"));
            logoImg.setImage(img);
        } catch (Exception ignored) {}
        logoImg.setFitWidth(32);
        logoImg.setFitHeight(32);
        logoImg.setPreserveRatio(true);
        logoImg.setSmooth(true);
        logoImg.setEffect(new DropShadow(12, Color.web(ACCENT_CYAN, 0.45)));

        Text brandText = new Text("BLINDHIRE");
        brandText.setFill(Color.web(TEXT_PRIMARY));
        brandText.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 14));

        Text taglineText = new Text("RH AGENCY");
        taglineText.setFill(Color.web(TEXT_MUTED));
        taglineText.setFont(Font.font("Segoe UI", 8));

        logoBox.getChildren().addAll(logoImg, brandText, taglineText);
        return logoBox;
    }

    private void styleInput(TextField f) {
        String base = "-fx-background-color: rgba(15,23,42,0.7);" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: rgba(148,163,184,0.18); -fx-border-radius: 25; -fx-border-width: 1;" +
                "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-prompt-text-fill: #475569;" +
                "-fx-padding: 12 20; -fx-font-size: 14px; -fx-pref-width: 320; -fx-font-family: 'Segoe UI';";
        String focus = "-fx-background-color: rgba(15,23,42,0.7);" +
                "-fx-background-radius: 25;" +
                "-fx-border-color: rgba(6,182,212,0.5); -fx-border-radius: 25; -fx-border-width: 1;" +
                "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-prompt-text-fill: #475569;" +
                "-fx-padding: 12 20; -fx-font-size: 14px; -fx-pref-width: 320; -fx-font-family: 'Segoe UI';";
        f.setStyle(base);
        f.setOnMouseEntered(e -> f.setStyle(focus));
        f.setOnMouseExited(e -> f.setStyle(base));
    }

    private void handleBack() {
        BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
    }

    private void navigateByRole(User user) {
        switch (user.getRole()) {
            case admin: case recruteur:
                BlindHireApp.loadSceneFullscreen(new DashboardPage().getRoot()); break;
            default:
                BlindHireApp.loadSceneFullscreen(new HomePage(user).getRoot()); break;
        }
    }

    private void handleFaceLogin() {
        String email = emailField.getText().trim();
        userservice svc = new userservice();
        if (!email.isEmpty()) {
            User user = svc.getUserByEmail(email);
            if (user == null) { errorLabel.setText("No account found with this email."); return; }
            if (user.getFaceData() == null || user.getFaceData().isEmpty()) {
                errorLabel.setText("No face registered. Use password login."); return; }
            if (!user.isVerified()) { errorLabel.setText("Account not verified."); return; }
            FaceCaptureDialog dialog = new FaceCaptureDialog(FaceCaptureDialog.Mode.AUTH, user.getFaceData());
            dialog.showAndWait();
            if (dialog.isAuthSuccess()) { svc.setCurrentUser(user); navigateByRole(user); }
            else errorLabel.setText("Face not recognized. Try again or use password.");
        } else {
            errorLabel.setText("Please enter your email first, then click Login with Face.");
        }
    }

    private void handleLogin(boolean rememberMe) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) { errorLabel.setText("Please fill in all fields."); return; }
        try {
            userservice svc = new userservice();
            User user = svc.authenticate(email, password);
            svc.setCurrentUser(user);
            if (rememberMe) Utils.SessionManager.saveSession(user.getId());
            navigateByRole(user);
        } catch (IllegalArgumentException e) {
            if ("UNVERIFIED".equals(e.getMessage())) {
                try {
                    userservice svc = new userservice();
                    User unverified = svc.getUserByEmail(email);
                    svc.setCurrentUser(unverified);
                    svc.resendVerificationCode(unverified);
                    BlindHireApp.loadScene(new VerificationPage(unverified).getRoot(), 960, 540);
                } catch (Exception ex) { errorLabel.setText("Account not verified. Check your email."); }
            } else { errorLabel.setText(e.getMessage()); }
        } catch (Exception e) {
            errorLabel.setText("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    public Parent getRoot() { return root; }
}