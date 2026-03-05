package ui;

import Model.Role;
import Model.User;
import Service.userservice;
import Utils.ApiService;
import Utils.PdfReader;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
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
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;

public class SignupPage {

    private StackPane root;

    private TextField     firstnameField;
    private TextField     lastnameField;
    private ComboBox<String> typeComboBox;
    private TextField     emailField;
    private PasswordField passwordField;

    private TextArea  skillsArea;
    private TextField diplomasField;
    private TextField experienceField;
    private TextArea  bioArea;
    private VBox      clientSection;

    private ComboBox<String> countryCodeCombo;
    private TextField        phoneNumberField;
    private String           capturedFaceData   = null;
    private boolean          fingerprintEnabled = false;

    private Label errorLabel;
    private Label nameStatusLabel;
    private Label cvStatusLabel;

    private static final String BG_DEEP      = "#0f172a";
    private static final String ACCENT_CYAN  = "#06b6d4";
    private static final String ACCENT_TEAL  = "#0d9488";
    private static final String TEXT_PRIMARY = "#f1f5f9";
    private static final String TEXT_MUTED   = "#94a3b8";

    public SignupPage() {
        createUI();
    }

    // ── Exact same pattern as old: StackPane root, bg pane + BorderPane content ──
    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");
        root.getChildren().addAll(createBackground(), createContent());
        // No stage hacks — BlindHireApp.loadScene handles sizing
    }

    // ─── BACKGROUND — same Path shapes, dark palette ──────────────────────────

    private Pane createBackground() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        Path wave = new Path();
        wave.setFill(Color.web("#6366f1", 0.12));
        wave.setLayoutX(0); wave.setLayoutY(410);
        wave.getElements().addAll(new MoveTo(0, 0), new CubicCurveTo(150, -50, 300, 50, 450, 0),
                new LineTo(450, 130), new LineTo(0, 130), new ClosePath());

        Path right = new Path();
        right.setFill(Color.web("#0f2744", 0.88));
        right.setLayoutX(450); right.setLayoutY(0);
        right.getElements().addAll(new MoveTo(0, 0), new LineTo(510, 0), new LineTo(510, 600),
                new CubicCurveTo(350, 400, 200, 300, 0, 200), new ClosePath());

        Path overlay = new Path();
        overlay.setFill(Color.web("#06b6d4", 0.05));
        overlay.setLayoutX(400); overlay.setLayoutY(0);
        overlay.getElements().addAll(new MoveTo(0, 0), new CubicCurveTo(50, 100, 100, 200, 150, 300),
                new LineTo(560, 300), new LineTo(560, 0), new ClosePath());

        pane.getChildren().addAll(wave, right, overlay);
        return pane;
    }

    // ─── CONTENT — same BorderPane(top=HBox, center=ScrollPane) structure ─────

    private BorderPane createContent() {
        BorderPane bp = new BorderPane();

        // Top — logo + back button (same as old)
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(20));
        topBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← back");
        backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;" +
                        "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 8; -fx-border-width: 1;"
        );
        backBtn.setOnAction(e -> BlindHireApp.loadSceneFullscreen(new WelcomePage().getRoot()));

        topBox.getChildren().addAll(createLogo(), spacer, backBtn);
        bp.setTop(topBox);

        // Center — scrollable form (same as old)
        ScrollPane scroll = new ScrollPane(buildForm());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        bp.setCenter(scroll);

        return bp;
    }

    // ─── FORM — same VBox(center) + card + all fields ─────────────────────────

    private VBox buildForm() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20, 40, 40, 40));

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(640);
        card.setStyle(
                "-fx-background-color: rgba(30,41,59,0.82);" +
                        "-fx-background-radius: 28;" +
                        "-fx-padding: 36;" +
                        "-fx-border-color: rgba(99,102,241,0.22);" +
                        "-fx-border-radius: 28; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 40, 0, 0, 8);"
        );

        Text title = new Text("Create Account");
        title.setFill(Color.web(TEXT_PRIMARY));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));

        // ── Name row + Generate button (same as old) ──
        VBox nameGroup = new VBox(6);
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        firstnameField = makeField("First name", 190);
        lastnameField  = makeField("Last name",  190);

        Button genNameBtn = new Button("🎲 Blind Name");
        genNameBtn.setStyle(
                "-fx-background-color: rgba(6,182,212,0.18); -fx-text-fill: #67e8f9;" +
                        "-fx-background-radius: 20; -fx-padding: 10 14;" +
                        "-fx-font-size: 12px; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(6,182,212,0.3); -fx-border-radius: 20; -fx-border-width: 1;"
        );
        genNameBtn.setOnAction(e -> handleGenerateName(genNameBtn));
        nameRow.getChildren().addAll(firstnameField, lastnameField, genNameBtn);

        nameStatusLabel = new Label("Click 🎲 to auto-generate a blind/anonymous name");
        nameStatusLabel.setFont(Font.font("Segoe UI", 11));
        nameStatusLabel.setTextFill(Color.web(TEXT_MUTED));
        nameGroup.getChildren().addAll(nameRow, nameStatusLabel);

        // ── Role + Email row (same as old) ──
        HBox row2 = new HBox(15);
        typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("I am a...");
        typeComboBox.getItems().addAll("recruteur", "client");
        typeComboBox.setStyle(fieldCss(210));
        emailField = makeField("Email", 250);
        row2.getChildren().addAll(typeComboBox, emailField);

        // ── Password ──
        passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 chars, letter + number)");
        passwordField.setStyle(fieldCss(515));

        // ── Phone row (same as old) ──
        HBox phoneRow = new HBox(10);
        phoneRow.setAlignment(Pos.CENTER_LEFT);

        countryCodeCombo = new ComboBox<>();
        countryCodeCombo.getItems().addAll(
                "🇹🇳 +216 Tunisia",     "🇩🇿 +213 Algeria",     "🇲🇦 +212 Morocco",
                "🇱🇾 +218 Libya",       "🇪🇬 +20 Egypt",        "🇫🇷 +33 France",
                "🇩🇪 +49 Germany",      "🇬🇧 +44 UK",           "🇺🇸 +1 USA",
                "🇨🇦 +1 Canada",        "🇸🇦 +966 Saudi Arabia","🇦🇪 +971 UAE",
                "🇶🇦 +974 Qatar",       "🇰🇼 +965 Kuwait",      "🇧🇭 +973 Bahrain",
                "🇴🇲 +968 Oman",        "🇯🇴 +962 Jordan",      "🇱🇧 +961 Lebanon",
                "🇸🇾 +963 Syria",       "🇮🇶 +964 Iraq",        "🇾🇪 +967 Yemen",
                "🇸🇩 +249 Sudan",       "🇸🇴 +252 Somalia",     "🇩🇯 +253 Djibouti",
                "🇲🇷 +222 Mauritania",  "🇸🇳 +221 Senegal",     "🇨🇮 +225 Côte d'Ivoire",
                "🇳🇬 +234 Nigeria",     "🇬🇭 +233 Ghana",       "🇰🇪 +254 Kenya",
                "🇿🇦 +27 South Africa", "🇪🇸 +34 Spain",        "🇮🇹 +39 Italy",
                "🇵🇹 +351 Portugal",    "🇧🇪 +32 Belgium",      "🇨🇭 +41 Switzerland",
                "🇳🇱 +31 Netherlands",  "🇸🇪 +46 Sweden",       "🇳🇴 +47 Norway",
                "🇩🇰 +45 Denmark",      "🇵🇱 +48 Poland",       "🇹🇷 +90 Turkey",
                "🇷🇺 +7 Russia",        "🇺🇦 +380 Ukraine",     "🇨🇳 +86 China",
                "🇯🇵 +81 Japan",        "🇰🇷 +82 South Korea",  "🇮🇳 +91 India",
                "🇧🇷 +55 Brazil",       "🇦🇷 +54 Argentina",    "🇲🇽 +52 Mexico",
                "🇦🇺 +61 Australia",    "🇳🇿 +64 New Zealand"
        );
        countryCodeCombo.setValue("🇹🇳 +216 Tunisia");
        countryCodeCombo.setStyle(fieldCss(230));
        phoneNumberField = makeField("Phone number", 265);
        phoneRow.getChildren().addAll(countryCodeCombo, phoneNumberField);

        // ── Biometrics section (same as old) ──
        VBox faceGroup = new VBox(8);
        Label bioTitle = new Label("🔐  Biometric Login (optional)");
        bioTitle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Button faceBtn = new Button("📷  Register Face");
        faceBtn.setStyle(
                "-fx-background-color: rgba(30,41,59,0.9); -fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-background-radius: 20; -fx-padding: 10 16;" +
                        "-fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 20; -fx-border-width: 1;"
        );
        Label faceStatusLabel = new Label("Not registered");
        faceStatusLabel.setFont(Font.font("Segoe UI", 11));
        faceStatusLabel.setTextFill(Color.web(TEXT_MUTED));
        VBox faceCol = new VBox(5, faceBtn, faceStatusLabel);
        faceCol.setAlignment(Pos.CENTER);

        faceBtn.setOnAction(e -> {
            FaceCaptureDialog dialog = new FaceCaptureDialog();
            dialog.showAndWait();
            capturedFaceData = dialog.getCapturedFace();
            if (capturedFaceData != null) {
                faceStatusLabel.setText("✅ Registered");
                faceStatusLabel.setTextFill(Color.web("#10b981"));
                faceBtn.setText("📷  Re-capture Face");
            } else {
                faceStatusLabel.setText("Cancelled");
                faceStatusLabel.setTextFill(Color.web(TEXT_MUTED));
            }
        });

        Service.FingerprintService.Result fpAvail = Service.FingerprintService.checkAvailability();
        boolean fpAvailable = fpAvail != Service.FingerprintService.Result.NOT_AVAILABLE
                && fpAvail != Service.FingerprintService.Result.ERROR;

        Button fpBtn = new Button("🖐  Register Fingerprint");
        fpBtn.setStyle(
                "-fx-background-color: rgba(30,41,59,0.9); -fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-background-radius: 20; -fx-padding: 10 16;" +
                        "-fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 20; -fx-border-width: 1;"
        );
        Label fpStatusLabel = new Label(
                fpAvail == Service.FingerprintService.Result.NOT_CONFIGURED
                        ? "⚠ Windows Hello not set up"
                        : (fpAvailable ? "Not registered" : "Not available on this PC")
        );
        fpStatusLabel.setFont(Font.font("Segoe UI", 11));
        fpStatusLabel.setTextFill(fpAvailable ? Color.web(TEXT_MUTED) : Color.web("#475569"));
        fpBtn.setDisable(!fpAvailable);
        VBox fpCol = new VBox(5, fpBtn, fpStatusLabel);
        fpCol.setAlignment(Pos.CENTER);

        fpBtn.setOnAction(e -> {
            fpBtn.setDisable(true);
            fpStatusLabel.setText("⏳ Waiting for Windows Hello...");
            fpStatusLabel.setTextFill(Color.web(ACCENT_CYAN));
            new Thread(() -> {
                Service.FingerprintService.Result result =
                        Service.FingerprintService.verify("BlindHire — Register your fingerprint");
                Platform.runLater(() -> {
                    fpBtn.setDisable(false);
                    if (result == Service.FingerprintService.Result.SUCCESS) {
                        fingerprintEnabled = true;
                        fpStatusLabel.setText("✅ Registered");
                        fpStatusLabel.setTextFill(Color.web("#10b981"));
                        fpBtn.setText("🖐  Re-verify");
                    } else {
                        fingerprintEnabled = false;
                        fpStatusLabel.setText(Service.FingerprintService.getResultMessage(result));
                        fpStatusLabel.setTextFill(Color.web("#f43f5e"));
                    }
                });
            }, "fp-register-thread").start();
        });

        HBox bioRow = new HBox(30, faceCol, fpCol);
        bioRow.setAlignment(Pos.CENTER_LEFT);
        Label faceHint = new Label("💡 Register one or both to enable biometric login later");
        faceHint.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        faceGroup.getChildren().addAll(bioTitle, bioRow, faceHint);

        // ── Client section (same as old: hidden until "client" selected) ──
        clientSection = buildClientSection();
        clientSection.setVisible(false);
        clientSection.setManaged(false);

        typeComboBox.setOnAction(e -> {
            boolean isClient = "client".equals(typeComboBox.getValue());
            clientSection.setVisible(isClient);
            clientSection.setManaged(isClient);
            if (isClient) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), clientSection);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
            }
        });

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#f43f5e"));
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(530);

        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2);" +
                        "-fx-text-fill: white; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 25; -fx-padding: 12 80; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.4), 14, 0, 0, 3);"
        );
        signupBtn.setOnAction(e -> handleSignup());

        Hyperlink loginLink = new Hyperlink("Already have an account? Sign In");
        loginLink.setStyle("-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 13px; -fx-border-color: transparent; -fx-font-family: 'Segoe UI';");
        loginLink.setOnAction(e -> BlindHireApp.loadScene(new LoginPage().getRoot(), 960, 540));

        card.getChildren().addAll(title, nameGroup, row2, passwordField, phoneRow,
                faceGroup, clientSection, errorLabel, signupBtn, loginLink);
        center.getChildren().add(card);
        return center;
    }

    // ─── CLIENT SECTION (same structure as old) ───────────────────────────────

    private VBox buildClientSection() {
        VBox section = new VBox(14);
        section.setMaxWidth(530);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(148,163,184,0.15);");

        Label sectionTitle = new Label("📋  Your Professional Profile");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(Color.web(TEXT_PRIMARY));

        VBox cvGroup = new VBox(6);
        Label cvLabel = new Label("📄  Upload your CV (PDF)");
        cvLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        HBox cvRow = new HBox(12);
        cvRow.setAlignment(Pos.CENTER_LEFT);

        Button uploadBtn = new Button("📂 Choose CV file");
        uploadBtn.setStyle(
                "-fx-background-color: rgba(30,41,59,0.9); -fx-text-fill: " + TEXT_MUTED + ";" +
                        "-fx-background-radius: 20; -fx-padding: 10 18;" +
                        "-fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 20; -fx-border-width: 1;"
        );
        cvStatusLabel = new Label("No file chosen");
        cvStatusLabel.setFont(Font.font("Segoe UI", 12));
        cvStatusLabel.setTextFill(Color.web(TEXT_MUTED));

        uploadBtn.setOnAction(e -> handleCvUpload(uploadBtn));
        cvRow.getChildren().addAll(uploadBtn, cvStatusLabel);
        cvGroup.getChildren().addAll(cvLabel, cvRow);

        skillsArea      = makeTextArea("Skills (auto-filled from CV, or type manually)...", 70);
        diplomasField   = makeField("Diploma / Education", 515);
        experienceField = makeField("Years & domain of experience", 515);
        bioArea         = makeTextArea("Short bio (optional)...", 60);

        section.getChildren().addAll(
                sep, sectionTitle, cvGroup,
                labeledField("🛠  Skills *",     skillsArea),
                labeledField("🎓  Diploma *",    diplomasField),
                labeledField("💼  Experience *", experienceField),
                labeledField("📝  Bio",           bioArea)
        );
        return section;
    }

    // ─── HANDLERS (unchanged from old) ───────────────────────────────────────

    private void handleGenerateName(Button btn) {
        btn.setDisable(true); btn.setText("...");
        nameStatusLabel.setText("Fetching random name...");
        nameStatusLabel.setTextFill(Color.web(TEXT_MUTED));
        Thread t = new Thread(() -> {
            String[] name = ApiService.fetchRandomName();
            Platform.runLater(() -> {
                btn.setDisable(false); btn.setText("🎲 Blind Name");
                if (name != null) {
                    firstnameField.setText(name[0]); lastnameField.setText(name[1]);
                    nameStatusLabel.setText("✅ Blind name generated — your real identity stays private!");
                    nameStatusLabel.setTextFill(Color.web("#10b981"));
                } else {
                    nameStatusLabel.setText("⚠ Could not reach randomuser.me — check your connection.");
                    nameStatusLabel.setTextFill(Color.web("#f43f5e"));
                }
            });
        });
        t.setDaemon(true); t.start();
    }

    private void handleCvUpload(Button uploadBtn) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select your CV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;
        cvStatusLabel.setText("📖 Reading CV..."); cvStatusLabel.setTextFill(Color.web(ACCENT_CYAN));
        uploadBtn.setDisable(true);
        Thread t = new Thread(() -> {
            String pdfText = PdfReader.extractText(file);
            if (pdfText == null || pdfText.isEmpty()) {
                Platform.runLater(() -> { uploadBtn.setDisable(false);
                    cvStatusLabel.setText("❌ Could not read PDF.");
                    cvStatusLabel.setTextFill(Color.web("#f43f5e")); });
                return;
            }
            Platform.runLater(() -> cvStatusLabel.setText("🤖 Analyzing CV with Groq AI..."));
            ApiService.CvData data = ApiService.extractCvData(pdfText);
            Platform.runLater(() -> {
                uploadBtn.setDisable(false);
                if (data == null) { cvStatusLabel.setText("❌ Groq extraction failed. Fill fields manually.");
                    cvStatusLabel.setTextFill(Color.web("#f43f5e")); return; }
                if (data.skills     != null && !data.skills.isEmpty())     skillsArea.setText(data.skills);
                if (data.diplomas   != null && !data.diplomas.isEmpty())   diplomasField.setText(data.diplomas);
                if (data.experience != null && !data.experience.isEmpty()) experienceField.setText(data.experience);
                if (data.bio        != null && !data.bio.isEmpty())        bioArea.setText(data.bio);
                cvStatusLabel.setText("✅ CV analyzed! Fields auto-filled — review and edit if needed.");
                cvStatusLabel.setTextFill(Color.web("#10b981"));
            });
        });
        t.setDaemon(true); t.start();
    }

    private void handleSignup() {
        String nom = firstnameField.getText().trim(), prenom = lastnameField.getText().trim();
        String typeStr = typeComboBox.getValue(), email = emailField.getText().trim(), mdp = passwordField.getText();

        String dialCode = "", comboVal = countryCodeCombo.getValue();
        if (comboVal != null) {
            int p = comboVal.indexOf('+'), s = comboVal.indexOf(' ', p);
            if (p != -1 && s != -1) dialCode = comboVal.substring(p, s);
        }
        String phoneNum = phoneNumberField.getText().trim();
        String phone = phoneNum.isEmpty() ? null : (dialCode + phoneNum);

        if (nom.isEmpty() || prenom.isEmpty() || typeStr == null || email.isEmpty() || mdp.isEmpty())
        { showError("Please fill in all required fields."); return; }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) { showError("First name must contain letters only."); return; }
        if (nom.length() < 2) { showError("First name must be at least 2 characters."); return; }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) { showError("Last name must contain letters only."); return; }
        if (prenom.length() < 2) { showError("Last name must be at least 2 characters."); return; }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
        { showError("Please enter a valid email address."); return; }
        if (mdp.length() < 6) { showError("Password must be at least 6 characters."); return; }
        if (!mdp.matches(".*[a-zA-Z].*") || !mdp.matches(".*[0-9].*"))
        { showError("Password must contain at least one letter and one number."); return; }
        if (!phoneNum.isEmpty() && !phoneNum.matches("[0-9]{5,15}"))
        { showError("Phone number must contain digits only (e.g. 12345678)."); return; }

        String skills = null, diplomas = null, experience = null, bio = null;
        if ("client".equals(typeStr)) {
            skills = skillsArea.getText().trim(); diplomas = diplomasField.getText().trim();
            experience = experienceField.getText().trim(); bio = bioArea.getText().trim();
            if (skills.isEmpty())     { showError("Please enter at least one skill."); return; }
            if (diplomas.isEmpty())   { showError("Please enter your diploma."); return; }
            if (experience.isEmpty()) { showError("Please describe your experience."); return; }
        }

        try {
            Role role = Role.valueOf(typeStr);
            User newUser = new User(nom, prenom, email, mdp, role);
            newUser.setSkills(skills); newUser.setDiplomas(diplomas);
            newUser.setExperience(experience); newUser.setBio(bio);
            newUser.setPhone(phone); newUser.setFaceData(capturedFaceData);
            userservice svc = new userservice();
            User registered = svc.register(newUser);
            svc.setCurrentUser(registered);
            BlindHireApp.loadScene(new VerificationPage(registered).getRoot(), 960, 540);
        } catch (IllegalArgumentException e) { showError(e.getMessage());
        } catch (Exception e) { showError("An error occurred. Please try again."); e.printStackTrace(); }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private TextField makeField(String prompt, double width) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(fieldCss(width));
        return f;
    }

    private TextArea makeTextArea(String prompt, double height) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefHeight(height);
        ta.setWrapText(true);
        ta.setStyle(
                "-fx-background-color: rgba(15,23,42,0.7);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 16; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                        "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-prompt-text-fill: #475569;" +
                        "-fx-border-color: rgba(148,163,184,0.18); -fx-border-radius: 12; -fx-pref-width: 515;"
        );
        return ta;
    }

    private VBox labeledField(String label, javafx.scene.control.Control field) {
        VBox g = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        g.getChildren().addAll(lbl, field);
        return g;
    }

    private String fieldCss(double width) {
        return "-fx-background-color: rgba(15,23,42,0.7);" +
                "-fx-background-radius: 25;" +
                "-fx-padding: 12 20; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';" +
                "-fx-text-fill: " + TEXT_PRIMARY + "; -fx-prompt-text-fill: #475569;" +
                "-fx-pref-width: " + width + ";" +
                "-fx-border-color: rgba(148,163,184,0.18); -fx-border-radius: 25;";
    }

    private VBox createLogo() {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER_LEFT);
        ImageView logoImg = new ImageView();
        try {
            Image img = new Image(getClass().getResourceAsStream("/images/blindhire_logo.png"));
            logoImg.setImage(img);
        } catch (Exception ignored) {}
        logoImg.setFitWidth(32); logoImg.setFitHeight(32);
        logoImg.setPreserveRatio(true); logoImg.setSmooth(true);
        logoImg.setEffect(new DropShadow(12, Color.web(ACCENT_CYAN, 0.45)));
        Text brand = new Text("BLINDHIRE");
        brand.setFill(Color.web(TEXT_PRIMARY));
        brand.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 14));
        Text tag = new Text("RH AGENCY");
        tag.setFill(Color.web(TEXT_MUTED));
        tag.setFont(Font.font("Segoe UI", 8));
        box.getChildren().addAll(logoImg, brand, tag);
        return box;
    }

    private void showError(String msg) { errorLabel.setText(msg); }

    public Parent getRoot() { return root; }
}