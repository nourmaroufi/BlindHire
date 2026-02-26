package ui;

import Model.Role;
import Model.User;
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

public class SignupPage {

    private StackPane root;

    // Basic fields
    private TextField    firstnameField;
    private TextField    lastnameField;
    private ComboBox<String> typeComboBox;
    private TextField    emailField;
    private PasswordField passwordField;

    // Client-only fields
    private TextArea  skillsArea;
    private TextField diplomasField;
    private TextField experienceField;
    private TextArea  bioArea;

    // The box that wraps client-only fields (shown/hidden dynamically)
    private VBox clientSection;

    private Label errorLabel;

    public SignupPage() {
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        root.getChildren().addAll(createBackgroundShapes(), createContent());
    }

    // ─── BACKGROUND ───────────────────────────────────────────────────────────

    private Pane createBackgroundShapes() {
        Pane pane = new Pane();

        Path wave = new Path();
        wave.setFill(Color.web("#A8E6F5"));
        wave.setLayoutX(0); wave.setLayoutY(410);
        wave.getElements().addAll(new MoveTo(0,0), new CubicCurveTo(150,-50,300,50,450,0),
                new LineTo(450,130), new LineTo(0,130), new ClosePath());

        Path right = new Path();
        right.setFill(Color.web("#6BA3BE"));
        right.setLayoutX(450); right.setLayoutY(0);
        right.getElements().addAll(new MoveTo(0,0), new LineTo(510,0), new LineTo(510,600),
                new CubicCurveTo(350,400,200,300,0,200), new ClosePath());

        Path overlay = new Path();
        overlay.setFill(Color.web("#87CEEB")); overlay.setOpacity(0.5);
        overlay.setLayoutX(400); overlay.setLayoutY(0);
        overlay.getElements().addAll(new MoveTo(0,0), new CubicCurveTo(50,100,100,200,150,300),
                new LineTo(560,300), new LineTo(560,0), new ClosePath());

        pane.getChildren().addAll(wave, right, overlay);
        return pane;
    }

    // ─── CONTENT ──────────────────────────────────────────────────────────────

    private BorderPane createContent() {
        BorderPane bp = new BorderPane();

        // Top bar
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(20));
        Button backBtn = new Button("← back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2C3E50; -fx-font-size: 14px; -fx-cursor: hand;");
        backBtn.setOnAction(e -> BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540));
        topBox.getChildren().addAll(createLogo(), backBtn);
        bp.setTop(topBox);

        // Scrollable center
        ScrollPane scroll = new ScrollPane(createForm());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        bp.setCenter(scroll);

        return bp;
    }

    private VBox createForm() {
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(20, 40, 40, 40));

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(620);
        card.setStyle(
                "-fx-background-color: rgba(168,230,245,0.5);" +
                        "-fx-background-radius: 30; -fx-padding: 36;"
        );

        Text title = new Text("Create Account");
        title.setFill(Color.web("#2C3E50"));
        title.setFont(Font.font("System", FontWeight.BOLD, 34));

        // ── Row 1: firstname + lastname ──
        HBox row1 = new HBox(15);
        firstnameField = makeField("First name", 250);
        lastnameField  = makeField("Last name",  250);
        row1.getChildren().addAll(firstnameField, lastnameField);

        // ── Row 2: role + email ──
        HBox row2 = new HBox(15);

        typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("I am a...");
        // ── admin is intentionally excluded ──
        typeComboBox.getItems().addAll("recruteur", "client");
        typeComboBox.setStyle(
                "-fx-background-color: #f0f0f0; -fx-background-radius: 25;" +
                        "-fx-font-size: 14px; -fx-pref-width: 250;"
        );

        emailField = makeField("Email", 250);
        row2.getChildren().addAll(typeComboBox, emailField);

        // ── Password ──
        passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 chars, letter + number)");
        passwordField.setStyle(fieldCss(515));

        // ── Client-only section (hidden by default) ──
        clientSection = buildClientSection();
        clientSection.setVisible(false);
        clientSection.setManaged(false);

        // Show/hide client section when role changes
        typeComboBox.setOnAction(e -> {
            boolean isClient = "client".equals(typeComboBox.getValue());
            clientSection.setVisible(isClient);
            clientSection.setManaged(isClient);
            if (isClient) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), clientSection);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
            }
        });

        // ── Error label ──
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font(12));
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(500);

        // ── Signup button ──
        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(
                "-fx-background-color: #3E4A5E; -fx-text-fill: white;" +
                        "-fx-font-size: 16px; -fx-background-radius: 25;" +
                        "-fx-padding: 12 80; -fx-cursor: hand;"
        );
        signupBtn.setOnAction(e -> handleSignup());

        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 13px;");
        loginLink.setOnAction(e -> BlindHireApp.loadScene(new LoginPage().getRoot(), 960, 540));

        card.getChildren().addAll(title, row1, row2, passwordField, clientSection, errorLabel, signupBtn, loginLink);
        center.getChildren().add(card);
        return center;
    }

    // ─── CLIENT SECTION ───────────────────────────────────────────────────────

    private VBox buildClientSection() {
        VBox section = new VBox(14);
        section.setMaxWidth(515);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(0,0,0,0.1);");

        Label sectionTitle = new Label("📋  Your Professional Profile");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(Color.web("#2C3E50"));

        // Skills
        skillsArea = makeTextArea(
                "Your skills (e.g. Java, Python, Project Management, Communication...)", 80);

        // Diplomas
        diplomasField = makeField("Highest diploma (e.g. Bachelor in Computer Science)", 515);

        // Experience
        experienceField = makeField("Years of experience (e.g. 3 years in software development)", 515);

        // Bio
        bioArea = makeTextArea("Short bio — tell employers about yourself...", 70);

        section.getChildren().addAll(
                sep,
                sectionTitle,
                labeledField("🛠  Skills", skillsArea),
                labeledField("🎓  Diploma", diplomasField),
                labeledField("💼  Experience", experienceField),
                labeledField("📝  Bio", bioArea)
        );
        return section;
    }

    private VBox labeledField(String label, Control field) {
        VBox g = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #3E4A5E; -fx-font-size: 12px; -fx-font-weight: bold;");
        g.getChildren().addAll(lbl, field);
        return g;
    }

    // ─── VALIDATION & SUBMIT ──────────────────────────────────────────────────

    private void handleSignup() {
        String nom     = firstnameField.getText().trim();
        String prenom  = lastnameField.getText().trim();
        String typeStr = typeComboBox.getValue();
        String email   = emailField.getText().trim();
        String mdp     = passwordField.getText();

        // ── Basic required fields ──
        if (nom.isEmpty() || prenom.isEmpty() || typeStr == null || email.isEmpty() || mdp.isEmpty()) {
            showError("Please fill in all required fields."); return;
        }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            showError("First name must contain letters only."); return;
        }
        if (nom.length() < 2) {
            showError("First name must be at least 2 characters."); return;
        }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            showError("Last name must contain letters only."); return;
        }
        if (prenom.length() < 2) {
            showError("Last name must be at least 2 characters."); return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Please enter a valid email address."); return;
        }
        if (mdp.length() < 6) {
            showError("Password must be at least 6 characters."); return;
        }
        if (!mdp.matches(".*[a-zA-Z].*") || !mdp.matches(".*[0-9].*")) {
            showError("Password must contain at least one letter and one number."); return;
        }

        // ── Client-specific required fields ──
        String skills     = null;
        String diplomas   = null;
        String experience = null;
        String bio        = null;

        if ("client".equals(typeStr)) {
            skills     = skillsArea.getText().trim();
            diplomas   = diplomasField.getText().trim();
            experience = experienceField.getText().trim();
            bio        = bioArea.getText().trim();

            if (skills.isEmpty()) {
                showError("Please enter at least one skill."); return;
            }
            if (diplomas.isEmpty()) {
                showError("Please enter your diploma / education."); return;
            }
            if (experience.isEmpty()) {
                showError("Please describe your experience."); return;
            }
        }

        // ── Register ──
        try {
            Role role = Role.valueOf(typeStr);
            User newUser = new User(nom, prenom, email, mdp, role);
            newUser.setSkills(skills);
            newUser.setDiplomas(diplomas);
            newUser.setExperience(experience);
            newUser.setBio(bio);

            userservice svc = new userservice();
            User registered = svc.register(newUser);
            svc.setCurrentUser(registered);

            switch (registered.getRole()) {
                case admin:
                    BlindHireApp.loadScene(new DashboardPage().getRoot(), 960, 540);
                    break;
                case recruteur:
                case client:
                default:
                    BlindHireApp.loadScene(new HomePage(registered).getRoot(), 960, 540);
                    break;
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

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
                "-fx-background-color: #f0f0f0; -fx-background-radius: 12;" +
                        "-fx-padding: 10 16; -fx-font-size: 13px;" +
                        "-fx-border-color: transparent; -fx-border-radius: 12;" +
                        "-fx-pref-width: 515;"
        );
        return ta;
    }

    private String fieldCss(double width) {
        return "-fx-background-color: #f0f0f0; -fx-background-radius: 25;" +
                "-fx-padding: 12 20; -fx-font-size: 14px;" +
                "-fx-pref-width: " + width + ";" +
                "-fx-border-color: transparent; -fx-border-radius: 25;";
    }

    private void showError(String msg) { errorLabel.setText(msg); }

    private VBox createLogo() {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        SVGPath icon = new SVGPath();
        icon.setContent("M 20 10 L 30 10 L 30 20 L 25 20 L 25 30 L 35 30 L 35 20 L 30 20 L 30 10 C 30 5 25 0 20 0 C 15 0 10 5 10 10 L 10 20 L 5 20 L 5 30 L 15 30 L 15 20 L 10 20 L 10 10 L 20 10 Z");
        icon.setFill(Color.web("#4A9DB5"));
        Text brand = new Text("BLINDHIRE");
        brand.setFill(Color.web("#2C3E50"));
        brand.setFont(Font.font("System", FontWeight.BOLD, 14));
        Text tag = new Text("RH AGENCY");
        tag.setFill(Color.web("#7F8C8D"));
        tag.setFont(Font.font(8));
        box.getChildren().addAll(icon, brand, tag);
        return box;
    }

    public Parent getRoot() { return root; }
}