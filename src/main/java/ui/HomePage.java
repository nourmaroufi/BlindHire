package ui;

import Model.User;
import Service.userservice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class HomePage {

    private BorderPane root;
    private User currentUser;
    private userservice userService;

    public HomePage(User user) {
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
        sidebar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-pref-width: 240;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 2, 0);"
        );

        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(24, 20, 24, 20));

        Rectangle logoIcon = new Rectangle(36, 36);
        logoIcon.setArcWidth(10);
        logoIcon.setArcHeight(10);
        logoIcon.setFill(Color.web("#4A6CF7"));

        Text logoText = new Text("BlindHire");
        logoText.setFont(Font.font("System", FontWeight.BOLD, 20));
        logoText.setFill(Color.web("#1a1a2e"));
        logoBox.getChildren().addAll(logoIcon, logoText);

        Separator sep1 = new Separator();

        // User avatar + info
        VBox userBox = new VBox(8);
        userBox.setAlignment(Pos.CENTER);
        userBox.setPadding(new Insets(24, 20, 16, 20));

        Circle avatar = new Circle(40);
        avatar.setFill(Color.web("#4A6CF7"));

        String displayName = currentUser.getDisplayName();
        String displayRole = currentUser.getRole().name();

        Text nameText = new Text(displayName);
        nameText.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameText.setFill(Color.web("#1a1a2e"));

        Text roleText = new Text(displayRole);
        roleText.setFont(Font.font("System", 12));
        roleText.setFill(Color.web("#888"));

        userBox.getChildren().addAll(avatar, nameText, roleText);

        Separator sep2 = new Separator();

        // Navigation menu
        VBox menuBox = new VBox(4);
        menuBox.setPadding(new Insets(20, 12, 20, 12));

        Label menuLabel = new Label("MAIN MENU");
        menuLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px; -fx-padding: 0 8 8 8;");

        Button homeBtn      = createNavButton("🏠  Home", true);
        Button jobsBtn      = createNavButton("💼  Jobs", false);
        Button companiesBtn = createNavButton("🏢  Companies", false);
        Button profileBtn   = createNavButton("👤  My Profile", false);
        Button dashboardBtn = createNavButton("📊  Dashboard", false);

        jobsBtn.setOnAction(e      -> showComingSoon("Jobs"));
        companiesBtn.setOnAction(e -> showComingSoon("Companies"));
        profileBtn.setOnAction(e   -> BlindHireApp.loadScene(new Profilepage(currentUser).getRoot(), 960, 540));
        dashboardBtn.setOnAction(e -> showComingSoon("Dashboard"));

        menuBox.getChildren().addAll(menuLabel, homeBtn, jobsBtn, companiesBtn, profileBtn, dashboardBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout
        Separator sep3 = new Separator();
        sep3.setPadding(new Insets(0, 12, 0, 12));

        Button logoutBtn = createNavButton("🚪  Logout", false);
        logoutBtn.setOnAction(e -> {
            Utils.SessionManager.clearSession();
            userService.setCurrentUser(null);
            BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
        });

        VBox bottomBox = new VBox(8);
        bottomBox.setPadding(new Insets(0, 12, 20, 12));
        bottomBox.getChildren().addAll(sep3, logoutBtn);

        sidebar.getChildren().addAll(logoBox, sep1, userBox, sep2, menuBox, spacer, bottomBox);
        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        if (active) {
            btn.setStyle(
                    "-fx-background-color: #eef0fe; -fx-text-fill: #4A6CF7;" +
                            "-fx-font-size: 14px; -fx-alignment: center-left;" +
                            "-fx-background-radius: 10; -fx-padding: 12 16;" +
                            "-fx-cursor: hand; -fx-font-weight: bold;"
            );
        } else {
            String normal = "-fx-background-color: transparent; -fx-text-fill: #555;" +
                    "-fx-font-size: 14px; -fx-alignment: center-left;" +
                    "-fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand;";
            String hover  = "-fx-background-color: #f5f5f5; -fx-text-fill: #333;" +
                    "-fx-font-size: 14px; -fx-alignment: center-left;" +
                    "-fx-background-radius: 10; -fx-padding: 12 16; -fx-cursor: hand;";
            btn.setStyle(normal);
            btn.setOnMouseEntered(e -> btn.setStyle(hover));
            btn.setOnMouseExited(e  -> btn.setStyle(normal));
        }
        return btn;
    }

    // ─── MAIN CONTENT ─────────────────────────────────────────────────────────

    private StackPane createMainContent() {
        StackPane wrapper = new StackPane();
        wrapper.setStyle("-fx-background-color: #f0f0f5;");
        wrapper.setPadding(new Insets(60, 60, 60, 60));

        HBox content = new HBox(60);
        content.setAlignment(Pos.CENTER);

        // Left: hero text
        VBox leftBox = new VBox(22);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        Text heroTitle = new Text("Find Your Dream Job ✦");
        heroTitle.setFont(Font.font("System", FontWeight.BOLD, 36));
        heroTitle.setFill(Color.web("#1a1a2e"));
        heroTitle.setWrappingWidth(400);

        Text heroSub = new Text("Connect with top companies and opportunities\nthat match your skills and aspirations.");
        heroSub.setFont(Font.font("System", 15));
        heroSub.setFill(Color.web("#666"));

        Button browseBtn = new Button("Browse All Jobs →");
        browseBtn.setStyle(
                "-fx-background-color: #4A6CF7; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-background-radius: 25;" +
                        "-fx-padding: 14 32; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        browseBtn.setOnAction(e -> showComingSoon("Jobs"));
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(
                "-fx-background-color: #3a5ce4; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-background-radius: 25;" +
                        "-fx-padding: 14 32; -fx-cursor: hand; -fx-font-weight: bold;"
        ));
        browseBtn.setOnMouseExited(e -> browseBtn.setStyle(
                "-fx-background-color: #4A6CF7; -fx-text-fill: white;" +
                        "-fx-font-size: 15px; -fx-background-radius: 25;" +
                        "-fx-padding: 14 32; -fx-cursor: hand; -fx-font-weight: bold;"
        ));

        leftBox.getChildren().addAll(heroTitle, heroSub, browseBtn);

        // Right: decorative illustration
        StackPane rightBox = new StackPane();
        rightBox.setMinSize(270, 270);
        rightBox.setMaxSize(270, 270);

        Circle bgCircle = new Circle(135);
        bgCircle.setFill(Color.web("#e0e0f0"));

        VBox illustration = new VBox(14);
        illustration.setAlignment(Pos.CENTER);
        illustration.setMaxWidth(190);

        Text rocketEmoji = new Text("🚀");
        rocketEmoji.setFont(Font.font(52));

        illustration.getChildren().addAll(
                rocketEmoji,
                makeCard(), makeCard(), makeCard()
        );

        rightBox.getChildren().addAll(bgCircle, illustration);

        content.getChildren().addAll(leftBox, rightBox);
        wrapper.getChildren().add(content);
        return wrapper;
    }

    private Rectangle makeCard() {
        Rectangle r = new Rectangle(175, 20);
        r.setArcWidth(8);
        r.setArcHeight(8);
        r.setFill(Color.WHITE);
        r.setOpacity(0.9);
        return r;
    }

    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(feature);
        alert.setContentText(feature + " feature coming soon!");
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }
}