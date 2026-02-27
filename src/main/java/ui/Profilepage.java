package ui;

import Model.Role;
import Model.User;
import Service.userservice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Optional;

public class Profilepage {

    private BorderPane root;
    private User currentUser;
    private userservice userService;

    // Edit form — basic
    private TextField    nomField;
    private TextField    prenomField;
    private TextField    emailField;
    private PasswordField mdpField;

    // Edit form — client only
    private TextArea  skillsArea;
    private TextField diplomasField;
    private TextField experienceField;
    private TextArea  bioArea;
    private VBox      clientEditSection;

    // View labels
    private Text displayNom;
    private Text displayPrenom;
    private Text displayEmail;
    private Text displayRole;
    private Text displayUsername;
    // Client view rows (shown/hidden)
    private VBox clientViewSection;
    private Text displaySkills;
    private Text displayDiplomas;
    private Text displayExperience;
    private Text displayBio;

    private Label feedbackLabel;
    private VBox  viewBox;
    private VBox  editBox;

    public Profilepage(User user) {
        this.currentUser = user;
        this.userService = new userservice();
        userService.setCurrentUser(user);
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f5;");
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());
    }

    // ─── SIDEBAR ──────────────────────────────────────────────────────────────

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle("-fx-background-color: white; -fx-pref-width: 240;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),10,0,2,0);");

        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(24, 20, 24, 20));
        Rectangle logoIcon = new Rectangle(36, 36);
        logoIcon.setArcWidth(10); logoIcon.setArcHeight(10);
        logoIcon.setFill(Color.web("#4A6CF7"));
        Text logoText = new Text("BlindHire");
        logoText.setFont(Font.font("System", FontWeight.BOLD, 20));
        logoText.setFill(Color.web("#1a1a2e"));
        logoBox.getChildren().addAll(logoIcon, logoText);

        Separator sep1 = new Separator();

        VBox userBox = new VBox(8);
        userBox.setAlignment(Pos.CENTER);
        userBox.setPadding(new Insets(24, 20, 16, 20));
        Circle avatar = new Circle(40);
        avatar.setFill(Color.web("#4A6CF7"));
        Text nameText = new Text(currentUser.getDisplayName());
        nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameText.setFill(Color.web("#1a1a2e"));
        Text roleText = new Text(currentUser.getRole().name());
        roleText.setFont(Font.font("System", 12));
        roleText.setFill(Color.web("#888"));
        userBox.getChildren().addAll(avatar, nameText, roleText);

        Separator sep2 = new Separator();

        VBox menuBox = new VBox(4);
        menuBox.setPadding(new Insets(20, 12, 20, 12));
        Label menuLabel = new Label("MAIN MENU");
        menuLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-padding: 0 8 8 8;");

        Button homeBtn      = createNavButton("🏠  Home",       false);
        Button jobsBtn      = createNavButton("💼  Jobs",       false);
        Button companiesBtn = createNavButton("🏢  Companies",  false);
        Button profileBtn   = createNavButton("👤  My Profile", true);

        homeBtn.setOnAction(e      -> BlindHireApp.loadScene(new HomePage(currentUser).getRoot(), 960, 540));
        jobsBtn.setOnAction(e      -> showComingSoon("Jobs"));
        companiesBtn.setOnAction(e -> showComingSoon("Companies"));

        menuBox.getChildren().addAll(menuLabel, homeBtn, jobsBtn, companiesBtn, profileBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createNavButton("🚪  Logout", false);
        logoutBtn.setOnAction(e -> {
            Utils.SessionManager.clearSession();
            userService.setCurrentUser(null);
            BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
        });
        VBox bottomBox = new VBox(8);
        bottomBox.setPadding(new Insets(0, 12, 20, 12));
        bottomBox.getChildren().addAll(new Separator(), logoutBtn);

        sidebar.getChildren().addAll(logoBox, sep1, userBox, sep2, menuBox, spacer, bottomBox);
        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        if (active) {
            btn.setStyle("-fx-background-color: #eef0fe; -fx-text-fill: #4A6CF7;" +
                    "-fx-font-size: 14px; -fx-alignment: center-left;" +
                    "-fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            String n = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-size: 14px;" +
                    "-fx-alignment: center-left; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand;";
            String h = "-fx-background-color: #f5f5f5; -fx-text-fill: #333; -fx-font-size: 14px;" +
                    "-fx-alignment: center-left; -fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand;";
            btn.setStyle(n);
            btn.setOnMouseEntered(e -> btn.setStyle(h));
            btn.setOnMouseExited(e  -> btn.setStyle(n));
        }
        return btn;
    }

    // ─── MAIN CONTENT ─────────────────────────────────────────────────────────

    private ScrollPane createMainContent() {
        VBox content = new VBox(28);
        content.setPadding(new Insets(50, 60, 50, 60));
        content.setStyle("-fx-background-color: #f0f0f5;");

        Text pageTitle = new Text("My Profile");
        pageTitle.setFont(Font.font("System", FontWeight.BOLD, 32));
        pageTitle.setFill(Color.web("#1a1a2e"));

        // ── Card ──
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),12,0,0,4);");

        // Card header
        HBox cardHeader = new HBox(20);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(28, 30, 28, 30));
        cardHeader.setStyle("-fx-background-color: #4A6CF7; -fx-background-radius: 16 16 0 0;");

        Circle bigAvatar = new Circle(45);
        bigAvatar.setFill(Color.web("#ffffff", 0.25));
        bigAvatar.setStroke(Color.WHITE);
        bigAvatar.setStrokeWidth(2.5);
        Text initials = new Text(getInitials());
        initials.setFont(Font.font("System", FontWeight.BOLD, 22));
        initials.setFill(Color.WHITE);
        StackPane avatarStack = new StackPane(bigAvatar, initials);

        VBox headerInfo = new VBox(5);
        Text headerName = new Text(currentUser.getDisplayName());
        headerName.setFont(Font.font("System", FontWeight.BOLD, 22));
        headerName.setFill(Color.WHITE);
        Label roleBadge = new Label(currentUser.getRole().name().toUpperCase());
        roleBadge.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white;" +
                "-fx-background-radius: 20; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        headerInfo.getChildren().addAll(headerName, roleBadge);
        cardHeader.getChildren().addAll(avatarStack, headerInfo);

        // ── VIEW BOX ──
        viewBox = new VBox(0);
        viewBox.setPadding(new Insets(28, 30, 28, 30));

        displayNom      = new Text(currentUser.getNom());
        displayPrenom   = new Text(currentUser.getPrenom());
        displayEmail    = new Text(currentUser.getEmail());
        displayRole     = new Text(currentUser.getRole().name());
        displayUsername = new Text(currentUser.getDisplayName());

        viewBox.getChildren().addAll(
                infoRow("🎭  Username",    displayUsername), divider(),
                infoRow("👤  First Name",  displayNom),   divider(),
                infoRow("👤  Last Name",   displayPrenom), divider(),
                infoRow("✉️  Email",        displayEmail),  divider(),
                infoRow("🔖  Role",         displayRole)
        );

        // Client-only view section
        boolean isClient = currentUser.getRole() == Role.client;
        clientViewSection = new VBox(0);
        clientViewSection.setVisible(isClient);
        clientViewSection.setManaged(isClient);

        displaySkills     = new Text(orEmpty(currentUser.getSkills()));
        displayDiplomas   = new Text(orEmpty(currentUser.getDiplomas()));
        displayExperience = new Text(orEmpty(currentUser.getExperience()));
        displayBio        = new Text(orEmpty(currentUser.getBio()));

        clientViewSection.getChildren().addAll(
                divider(), sectionHeader("Professional Profile"),
                infoRow("🛠  Skills",      displaySkills),    divider(),
                infoRow("🎓  Diploma",     displayDiplomas),  divider(),
                infoRow("💼  Experience",  displayExperience), divider(),
                infoRow("📝  Bio",         displayBio)
        );
        viewBox.getChildren().add(clientViewSection);

        // ── EDIT BOX ──
        editBox = new VBox(16);
        editBox.setPadding(new Insets(28, 30, 28, 30));
        editBox.setVisible(false);
        editBox.setManaged(false);

        nomField        = editField("First Name",  currentUser.getNom());
        prenomField     = editField("Last Name",   currentUser.getPrenom());
        emailField      = editField("Email",       currentUser.getEmail());
        mdpField        = new PasswordField();
        mdpField.setPromptText("New password (leave blank to keep current)");
        mdpField.setStyle(inputStyle());

        feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font(12));
        feedbackLabel.setWrapText(true);

        editBox.getChildren().addAll(
                fieldGroup("👤  First Name", nomField),
                fieldGroup("👤  Last Name",  prenomField),
                fieldGroup("✉️  Email",       emailField),
                fieldGroup("🔒  New Password", mdpField),
                feedbackLabel
        );

        // Client edit section
        clientEditSection = new VBox(14);
        clientEditSection.setVisible(isClient);
        clientEditSection.setManaged(isClient);

        skillsArea      = editTextArea("Skills (e.g. Java, Python, Communication...)", 70, orEmpty(currentUser.getSkills()));
        diplomasField   = editField("Diploma (e.g. Bachelor in Computer Science)", orEmpty(currentUser.getDiplomas()));
        experienceField = editField("Experience (e.g. 3 years in software dev)", orEmpty(currentUser.getExperience()));
        bioArea         = editTextArea("Short bio...", 60, orEmpty(currentUser.getBio()));

        clientEditSection.getChildren().addAll(
                sectionHeader("Professional Profile"),
                fieldGroup("🛠  Skills",     skillsArea),
                fieldGroup("🎓  Diploma",    diplomasField),
                fieldGroup("💼  Experience", experienceField),
                fieldGroup("📝  Bio",        bioArea)
        );
        editBox.getChildren().add(clientEditSection);

        card.getChildren().addAll(cardHeader, viewBox, editBox);

        // ── ACTIONS ──
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn   = actionBtn("✏️  Edit Profile",   "#4A6CF7", "white");
        Button saveBtn   = actionBtn("💾  Save Changes",   "#27ae60", "white");
        Button cancelBtn = actionBtn("✕  Cancel",          "#888",    "white");
        Button deleteBtn = actionBtn("🗑️  Delete Account", "#e74c3c", "white");

        saveBtn.setVisible(false);   saveBtn.setManaged(false);
        cancelBtn.setVisible(false); cancelBtn.setManaged(false);

        editBtn.setOnAction(e -> {
            populateEditFields();
            setEditMode(true, editBtn, saveBtn, cancelBtn);
        });
        cancelBtn.setOnAction(e -> {
            feedbackLabel.setText("");
            setEditMode(false, editBtn, saveBtn, cancelBtn);
        });
        saveBtn.setOnAction(e -> {
            if (handleSave()) {
                refreshView(headerName);
                setEditMode(false, editBtn, saveBtn, cancelBtn);
            }
        });
        deleteBtn.setOnAction(e -> handleDelete());

        actions.getChildren().addAll(deleteBtn, editBtn, saveBtn, cancelBtn);
        content.getChildren().addAll(pageTitle, card, actions);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f0f0f5; -fx-background: #f0f0f5;");
        return scroll;
    }

    // ─── VIEW HELPERS ─────────────────────────────────────────────────────────

    private HBox infoRow(String label, Text value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 0, 14, 0));
        Text lbl = new Text(label);
        lbl.setFont(Font.font("System", 13));
        lbl.setFill(Color.web("#999"));
        value.setFont(Font.font("System", FontWeight.BOLD, 14));
        value.setFill(Color.web("#1a1a2e"));
        value.setWrappingWidth(340);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(lbl, spacer, value);
        return row;
    }

    private Separator divider() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #f0f0f5;");
        return s;
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        l.setStyle("-fx-text-fill: #4A6CF7; -fx-padding: 10 0 2 0;");
        return l;
    }

    // ─── EDIT HELPERS ─────────────────────────────────────────────────────────

    private TextField editField(String prompt, String value) {
        TextField f = new TextField(value);
        f.setPromptText(prompt);
        f.setStyle(inputStyle());
        return f;
    }

    private TextArea editTextArea(String prompt, double height, String value) {
        TextArea ta = new TextArea(value);
        ta.setPromptText(prompt);
        ta.setPrefHeight(height);
        ta.setWrapText(true);
        ta.setStyle("-fx-background-color: #f5f5f8; -fx-background-radius: 10;" +
                "-fx-padding: 10 14; -fx-font-size: 13px;" +
                "-fx-border-color: #e0e0e8; -fx-border-radius: 10; -fx-border-width: 1;");
        return ta;
    }

    private String inputStyle() {
        return "-fx-background-color: #f5f5f8; -fx-background-radius: 10;" +
                "-fx-padding: 12 16; -fx-font-size: 14px;" +
                "-fx-border-color: #e0e0e8; -fx-border-radius: 10; -fx-border-width: 1;";
    }

    private VBox fieldGroup(String label, Control field) {
        VBox g = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-weight: bold;");
        g.getChildren().addAll(lbl, field);
        return g;
    }

    private Button actionBtn(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                "-fx-background-radius: 10; -fx-padding: 11 22;" +
                "-fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        return b;
    }

    // ─── LOGIC ────────────────────────────────────────────────────────────────

    private void populateEditFields() {
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        mdpField.clear();
        feedbackLabel.setText("");
        if (currentUser.getRole() == Role.client) {
            skillsArea.setText(orEmpty(currentUser.getSkills()));
            diplomasField.setText(orEmpty(currentUser.getDiplomas()));
            experienceField.setText(orEmpty(currentUser.getExperience()));
            bioArea.setText(orEmpty(currentUser.getBio()));
        }
    }

    private void setEditMode(boolean on, Button editBtn, Button saveBtn, Button cancelBtn) {
        viewBox.setVisible(!on); viewBox.setManaged(!on);
        editBox.setVisible(on);  editBox.setManaged(on);
        editBtn.setVisible(!on); editBtn.setManaged(!on);
        saveBtn.setVisible(on);  saveBtn.setManaged(on);
        cancelBtn.setVisible(on); cancelBtn.setManaged(on);
    }

    private boolean handleSave() {
        String nom    = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email  = emailField.getText().trim();
        String mdp    = mdpField.getText();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            showFeedback("First name, last name and email are required.", true); return false;
        }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            showFeedback("First name must contain letters only.", true); return false;
        }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            showFeedback("Last name must contain letters only.", true); return false;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showFeedback("Please enter a valid email address.", true); return false;
        }
        if (!mdp.isEmpty()) {
            if (mdp.length() < 6) {
                showFeedback("Password must be at least 6 characters.", true); return false;
            }
            if (!mdp.matches(".*[a-zA-Z].*") || !mdp.matches(".*[0-9].*")) {
                showFeedback("Password must contain at least one letter and one number.", true); return false;
            }
        }
        if (!email.equals(currentUser.getEmail()) && new userservice().emailExists(email)) {
            showFeedback("This email is already used by another account.", true); return false;
        }

        try {
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setEmail(email);
            if (!mdp.isEmpty()) currentUser.setMdp(mdp);

            if (currentUser.getRole() == Role.client) {
                currentUser.setSkills(skillsArea.getText().trim());
                currentUser.setDiplomas(diplomasField.getText().trim());
                currentUser.setExperience(experienceField.getText().trim());
                currentUser.setBio(bioArea.getText().trim());
            }

            userService.updateUser(currentUser);
            showFeedback("Profile updated successfully!", false);
            return true;
        } catch (Exception e) {
            showFeedback("Error: " + e.getMessage(), true);
            e.printStackTrace();
            return false;
        }
    }

    private void refreshView(Text headerName) {
        displayNom.setText(currentUser.getNom());
        displayPrenom.setText(currentUser.getPrenom());
        displayEmail.setText(currentUser.getEmail());
        displayRole.setText(currentUser.getRole().name());
        displayUsername.setText(currentUser.getDisplayName());
        headerName.setText(currentUser.getNom() + " " + currentUser.getPrenom());
        if (currentUser.getRole() == Role.client) {
            displaySkills.setText(orEmpty(currentUser.getSkills()));
            displayDiplomas.setText(orEmpty(currentUser.getDiplomas()));
            displayExperience.setText(orEmpty(currentUser.getExperience()));
            displayBio.setText(orEmpty(currentUser.getBio()));
        }
    }

    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action is permanent and cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(currentUser.getId());
                userService.setCurrentUser(null);
                BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error deleting account: " + e.getMessage()).showAndWait();
            }
        }
    }

    private void showFeedback(String msg, boolean error) {
        feedbackLabel.setText(msg);
        feedbackLabel.setTextFill(error ? Color.web("#e74c3c") : Color.web("#27ae60"));
    }

    private void showComingSoon(String f) {
        new Alert(Alert.AlertType.INFORMATION, f + " coming soon!").showAndWait();
    }

    private String orEmpty(String s) { return s == null ? "" : s; }

    private String getInitials() {
        String n = currentUser.getNom(), p = currentUser.getPrenom();
        return (n.isEmpty() ? "?" : String.valueOf(n.charAt(0)).toUpperCase()) +
                (p.isEmpty() ? "" : String.valueOf(p.charAt(0)).toUpperCase());
    }

    public Parent getRoot() { return root; }
}