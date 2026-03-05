package ui;

import Model.Role;
import Model.User;
import Service.userservice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AddUserPage {

    private StackPane root;
    private TextField      nomField;
    private TextField      prenomField;
    private TextField      emailField;
    private PasswordField  mdpField;
    private ComboBox<String> roleComboBox;
    private Label          errorLabel;
    private DashboardPage  dashboardPage;
    private userservice    userService;

    // ── Design tokens (match leaderboard / DashboardPage dark theme) ─────────
    private static final String DARK_BG    = "#0f172a";
    private static final String CARD_BG    = "rgba(255,255,255,0.05)";
    private static final String INDIGO     = "#6366f1";
    private static final String CYAN       = "#06b6d4";
    private static final String GREEN      = "#10b981";
    private static final String ROSE       = "#f43f5e";

    public AddUserPage(DashboardPage dashboardPage) {
        this.dashboardPage = dashboardPage;
        this.userService   = new userservice();
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: " + DARK_BG + ";");

        // ── Ambient glow circles (clipped so they never escape the panel) ──
        Pane glows = buildGlows();

        // ── Top bar ──────────────────────────────────────────────────────────
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(16, 20, 0, 20));

        Button closeBtn = new Button("✕");
        String closeDef   = "-fx-background-color:rgba(255,255,255,0.08);-fx-background-radius:50;" +
                "-fx-text-fill:rgba(255,255,255,0.70);-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-cursor:hand;-fx-padding:6 12;-fx-border-color:rgba(255,255,255,0.15);-fx-border-radius:50;";
        String closeHover = "-fx-background-color:" + ROSE + ";-fx-background-radius:50;" +
                "-fx-text-fill:white;-fx-font-size:14px;-fx-font-weight:bold;" +
                "-fx-cursor:hand;-fx-padding:6 12;-fx-border-color:transparent;-fx-border-radius:50;";
        closeBtn.setStyle(closeDef);
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeHover));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle(closeDef));
        closeBtn.setOnAction(e -> closeWindow());
        topBar.getChildren().add(closeBtn);

        // ── Form card ────────────────────────────────────────────────────────
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 36, 32, 36));
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color:rgba(255,255,255,0.06);" +
                        "-fx-background-radius:24;" +
                        "-fx-border-color:rgba(99,102,241,0.22);" +
                        "-fx-border-radius:24;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.60),40,0,0,12);"
        );

        // Title area
        StackPane iconBubble = new StackPane();
        iconBubble.setPrefSize(52, 52); iconBubble.setMinSize(52, 52);
        iconBubble.setStyle("-fx-background-color:rgba(99,102,241,0.20);-fx-background-radius:16;" +
                "-fx-border-color:rgba(99,102,241,0.40);-fx-border-radius:16;-fx-border-width:1;");
        Label bubbleIcon = new Label("👤");
        bubbleIcon.setStyle("-fx-font-size:22px;");
        iconBubble.getChildren().add(bubbleIcon);

        Label title = new Label("Add New User");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        title.setTextFill(Color.WHITE);

        Label titleSub = new Label("Fill in the details to create a new account");
        titleSub.setFont(Font.font("Segoe UI", 12));
        titleSub.setTextFill(Color.web("rgba(255,255,255,0.38)"));

        VBox titleText = new VBox(4, title, titleSub);
        titleText.setAlignment(Pos.CENTER_LEFT);

        HBox titleRow = new HBox(14, iconBubble, titleText);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Divider
        Region divider = new Region();
        divider.setMaxWidth(Double.MAX_VALUE);
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color:rgba(255,255,255,0.08);");

        Rectangle underline = new Rectangle(40, 3);
        underline.setFill(Color.web(INDIGO));
        underline.setArcWidth(3); underline.setArcHeight(3);

        // Fields
        nomField     = darkField("Last Name");
        prenomField  = darkField("First Name");
        emailField   = darkField("Email Address");
        mdpField     = darkPasswordField("Password");

        roleComboBox = new ComboBox<>();
        roleComboBox.setPromptText("Select Role");
        roleComboBox.getItems().addAll("admin", "recruteur", "client");
        roleComboBox.setMaxWidth(Double.MAX_VALUE);
        roleComboBox.setStyle(
                "-fx-background-color:rgba(255,255,255,0.07);" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:rgba(255,255,255,0.15);" +
                        "-fx-border-radius:12;" +
                        "-fx-border-width:1;" +
                        "-fx-font-size:14px;" +
                        "-fx-padding:4 0;"
        );

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ROSE));
        errorLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));

        // Submit button — dark aesthetic, glowing indigo outline style
        Button addBtn = new Button("Create User");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        String addDef = "-fx-background-color:rgba(99,102,241,0.22);" +
                "-fx-text-fill:#c7d2fe;-fx-font-size:15px;-fx-font-weight:bold;" +
                "-fx-background-radius:14;-fx-padding:14 0;-fx-cursor:hand;" +
                "-fx-border-color:rgba(99,102,241,0.55);-fx-border-width:1;-fx-border-radius:14;";
        String addHov = "-fx-background-color:" + INDIGO + ";" +
                "-fx-text-fill:white;-fx-font-size:15px;-fx-font-weight:bold;" +
                "-fx-background-radius:14;-fx-padding:14 0;-fx-cursor:hand;" +
                "-fx-border-color:transparent;-fx-border-width:1;-fx-border-radius:14;" +
                "-fx-effect:dropshadow(gaussian,rgba(99,102,241,0.50),18,0,0,4);" +
                "-fx-scale-x:1.01;-fx-scale-y:1.01;";
        addBtn.setStyle(addDef);
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(addHov));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(addDef));
        addBtn.setOnAction(e -> handleAdd());

        Hyperlink cancelLink = new Hyperlink("Cancel");
        cancelLink.setStyle("-fx-text-fill:rgba(255,255,255,0.40);-fx-font-size:13px;-fx-border-color:transparent;");
        cancelLink.setOnMouseEntered(e -> cancelLink.setStyle("-fx-text-fill:" + CYAN + ";-fx-font-size:13px;-fx-border-color:transparent;"));
        cancelLink.setOnMouseExited(e  -> cancelLink.setStyle("-fx-text-fill:rgba(255,255,255,0.40);-fx-font-size:13px;-fx-border-color:transparent;"));
        cancelLink.setOnAction(e -> closeWindow());

        VBox buttons = new VBox(10, addBtn, cancelLink);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(
                titleRow,
                divider,
                fieldRow("👤", nomField),
                fieldRow("👤", prenomField),
                fieldRow("✉", emailField),
                fieldRow("🔒", mdpField),
                fieldRow("👔", roleComboBox),
                errorLabel,
                buttons
        );

        VBox outer = new VBox(topBar, card);
        outer.setSpacing(12);
        outer.setAlignment(Pos.CENTER);
        StackPane.setAlignment(outer, Pos.CENTER);

        root.getChildren().addAll(glows, outer);
    }

    // ── Ambient glow helper ───────────────────────────────────────────────────

    private Pane buildGlows() {
        Pane p = new Pane();
        p.setMouseTransparent(true);
        // Clip strictly to panel bounds so nothing bleeds outside
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        p.setClip(clip);

        Circle c1 = glow(200, "#6366f1", 0.18, 20, 30);
        Circle c2 = glow(160, "#06b6d4", 0.14, 380, 20);
        Circle c3 = glow(140, "#10b981", 0.10, 200, 620);
        p.getChildren().addAll(c1, c2, c3);
        return p;
    }

    private Circle glow(double r, String hex, double opacity, double x, double y) {
        Circle c = new Circle(r, Color.web(hex));
        c.setOpacity(opacity);
        c.setEffect(new GaussianBlur(80));
        c.setLayoutX(x); c.setLayoutY(y);
        return c;
    }

    // ── Field helpers ─────────────────────────────────────────────────────────

    private TextField darkField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        String def  = fieldStyle(false);
        String focus = fieldStyle(true);
        f.setStyle(def);
        f.focusedProperty().addListener((obs, o, n) -> f.setStyle(n ? focus : def));
        return f;
    }

    private PasswordField darkPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setMaxWidth(Double.MAX_VALUE);
        String def  = fieldStyle(false);
        String focus = fieldStyle(true);
        f.setStyle(def);
        f.focusedProperty().addListener((obs, o, n) -> f.setStyle(n ? focus : def));
        return f;
    }

    private String fieldStyle(boolean focused) {
        return focused
                ? "-fx-background-color:rgba(99,102,241,0.12);-fx-background-radius:12;" +
                "-fx-border-color:" + INDIGO + ";-fx-border-radius:12;-fx-border-width:1.5;" +
                "-fx-padding:12 16;-fx-font-size:14px;-fx-text-fill:white;-fx-prompt-text-fill:rgba(255,255,255,0.30);"
                : "-fx-background-color:rgba(255,255,255,0.07);-fx-background-radius:12;" +
                "-fx-border-color:rgba(255,255,255,0.14);-fx-border-radius:12;-fx-border-width:1;" +
                "-fx-padding:12 16;-fx-font-size:14px;-fx-text-fill:white;-fx-prompt-text-fill:rgba(255,255,255,0.30);";
    }

    private HBox fieldRow(String icon, Control field) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        // Visible icon in a small glowing pill
        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(36, 36); iconWrap.setMinSize(36, 36);
        iconWrap.setStyle("-fx-background-color:rgba(99,102,241,0.18);-fx-background-radius:10;");
        Label ico = new Label(icon);
        ico.setStyle("-fx-font-size:16px;");
        iconWrap.getChildren().add(ico);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(iconWrap, field);
        return row;
    }

    // ── Logic (unchanged) ─────────────────────────────────────────────────────

    private void handleAdd() {
        String nom     = nomField.getText().trim();
        String prenom  = prenomField.getText().trim();
        String email   = emailField.getText().trim();
        String mdp     = mdpField.getText();
        String roleStr = roleComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty() || roleStr == null) {
            errorLabel.setText("⚠️ Please fill in all fields");
            return;
        }
        if (!email.contains("@")) {
            errorLabel.setText("⚠️ Please enter a valid email");
            return;
        }
        if (mdp.length() < 4) {
            errorLabel.setText("⚠️ Password must be at least 4 characters");
            return;
        }
        try {
            Role role = Role.valueOf(roleStr.toLowerCase());
            User newUser = new User(nom, prenom, email, mdp, role);
            userService.register(newUser);
            if (dashboardPage != null) dashboardPage.refreshTable();
            closeWindow();
        } catch (IllegalArgumentException e) {
            errorLabel.setText("⚠️ " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("⚠️ An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    public Parent getRoot() { return root; }
}