package ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class WelcomePage {

    private StackPane root;

    private static final String BG_DEEP       = "#080f1e";
    private static final String ACCENT_CYAN   = "#06b6d4";
    private static final String ACCENT_INDIGO = "#6366f1";
    private static final String ACCENT_TEAL   = "#0d9488";
    private static final String TEXT_PRIMARY  = "#f1f5f9";
    private static final String TEXT_MUTED    = "#94a3b8";

    public WelcomePage() { createUI(); }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");

        // Responsive background that fills whatever window size is used
        Pane bgPane = createBackground();
        bgPane.prefWidthProperty().bind(root.widthProperty());
        bgPane.prefHeightProperty().bind(root.heightProperty());

        // Two-column layout: LEFT = content, RIGHT = decorative card
        HBox mainLayout = new HBox(0);
        mainLayout.setAlignment(Pos.CENTER);

        // ── LEFT PANEL ────────────────────────────────────────────────────────
        VBox leftPanel = new VBox(0);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(60, 60, 60, 80));
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxWidth(680);
        leftPanel.setMinWidth(400);

        VBox logoBox = createLogo();
        VBox.setMargin(logoBox, new Insets(0, 0, 52, 0));

        VBox headline = new VBox(6,
                styledText("Hire smarter,", TEXT_PRIMARY, 58),
                styledGlowText("hire blindly.", ACCENT_CYAN, 58)
        );
        VBox.setMargin(headline, new Insets(0, 0, 22, 0));

        Text subtitle = new Text("Remove unconscious bias from recruitment.\nFocus on skills, not identity.");
        subtitle.setFill(Color.web(TEXT_MUTED));
        subtitle.setFont(Font.font("Segoe UI", 17));
        subtitle.setLineSpacing(6);
        VBox.setMargin(subtitle, new Insets(0, 0, 32, 0));

        HBox pills = new HBox(12,
                makePill("✦  Anonymous profiles", ACCENT_INDIGO),
                makePill("✦  AI-matching",        ACCENT_TEAL),
                makePill("✦  Face login",          "#f59e0b")
        );
        pills.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(pills, new Insets(0, 0, 44, 0));

        HBox buttonBox = new HBox(18);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        Button loginBtn  = new Button("Sign In");
        Button signupBtn = new Button("Create Account");
        styleButtonPrimary(loginBtn);
        styleButtonOutline(signupBtn);
        loginBtn.setOnAction(e  -> BlindHireApp.loadSceneFullscreen(new LoginPage().getRoot()));
        signupBtn.setOnAction(e -> BlindHireApp.loadSceneFullscreen(new SignupPage().getRoot()));
        buttonBox.getChildren().addAll(loginBtn, signupBtn);

        leftPanel.getChildren().addAll(logoBox, headline, subtitle, pills, buttonBox);

        // ── RIGHT PANEL ───────────────────────────────────────────────────────
        StackPane rightPanel = new StackPane();
        rightPanel.setAlignment(Pos.CENTER);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        VBox floatingCard = buildFloatingCard();
        rightPanel.getChildren().add(floatingCard);

        mainLayout.getChildren().addAll(leftPanel, rightPanel);
        root.getChildren().addAll(bgPane, mainLayout);

        // Staggered entrance animations
        animateSlideUp(logoBox,    0);
        animateSlideUp(headline,   90);
        animateSlideUp(subtitle,  180);
        animateSlideUp(pills,     260);
        animateSlideUp(buttonBox, 340);
        animateSlideIn(floatingCard, 200);
    }

    // ── RESPONSIVE BACKGROUND ─────────────────────────────────────────────────

    private Pane createBackground() {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);

        // Glowing orbs bound to relative positions — scales with window
        Circle orb1 = new Circle(360);
        orb1.setFill(Color.web("#0ea5e9", 0.07));
        orb1.setEffect(new GaussianBlur(90));
        orb1.layoutXProperty().bind(pane.widthProperty().multiply(0.76));
        orb1.layoutYProperty().bind(pane.heightProperty().multiply(0.20));

        Circle orb2 = new Circle(240);
        orb2.setFill(Color.web("#6366f1", 0.09));
        orb2.setEffect(new GaussianBlur(70));
        orb2.layoutXProperty().bind(pane.widthProperty().multiply(0.16));
        orb2.layoutYProperty().bind(pane.heightProperty().multiply(0.84));

        Circle orb3 = new Circle(140);
        orb3.setFill(Color.web("#06b6d4", 0.055));
        orb3.setEffect(new GaussianBlur(50));
        orb3.layoutXProperty().bind(pane.widthProperty().multiply(0.50));
        orb3.layoutYProperty().bind(pane.heightProperty().multiply(0.50));

        // Subtle grid — lines positioned proportionally
        for (int i = 1; i < 7; i++) {
            Line h = new Line(); h.setStartX(0);
            h.endXProperty().bind(pane.widthProperty());
            double f = i / 7.0;
            h.startYProperty().bind(pane.heightProperty().multiply(f));
            h.endYProperty().bind(pane.heightProperty().multiply(f));
            h.setStroke(Color.web("#ffffff", 0.016)); h.setStrokeWidth(1);
            pane.getChildren().add(h);
        }
        for (int i = 1; i < 9; i++) {
            Line v = new Line(); v.setStartY(0);
            v.endYProperty().bind(pane.heightProperty());
            double f = i / 9.0;
            v.startXProperty().bind(pane.widthProperty().multiply(f));
            v.endXProperty().bind(pane.widthProperty().multiply(f));
            v.setStroke(Color.web("#ffffff", 0.016)); v.setStrokeWidth(1);
            pane.getChildren().add(v);
        }

        pane.getChildren().addAll(orb1, orb2, orb3);
        return pane;
    }

    // ── FLOATING CARD ─────────────────────────────────────────────────────────

    private VBox buildFloatingCard() {
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(390);
        card.setPrefWidth(390);
        card.setPadding(new Insets(42, 46, 42, 46));
        card.setStyle(
                "-fx-background-color: rgba(15,23,42,0.75);" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: rgba(99,102,241,0.22);" +
                        "-fx-border-radius: 28; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.18), 50, 0, 0, 18);"
        );

        Text cardTitle = new Text("Why BlindHire?");
        cardTitle.setFill(Color.WHITE);
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        card.getChildren().addAll(
                cardTitle,
                featureRow("🎯", "Bias-free screening",     "Evaluate candidates purely on skills and merit."),
                featureRow("🤖", "AI-powered matching",      "Ranks applicants against your exact requirements."),
                featureRow("🔒", "Privacy by design",        "Personal info hidden until you decide to reveal it."),
                featureRow("📊", "Real-time analytics",      "Track your pipeline and diversity metrics live.")
        );

        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(10, 18, 10, 18));
        chip.setStyle(
                "-fx-background-color: rgba(6,182,212,0.10);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(6,182,212,0.3); -fx-border-radius: 999; -fx-border-width: 1;"
        );
        Text chipTxt = new Text("✦  Join 2,400+ companies hiring smarter");
        chipTxt.setFill(Color.web(ACCENT_CYAN));
        chipTxt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        chip.getChildren().add(chipTxt);
        card.getChildren().add(chip);
        return card;
    }

    private HBox featureRow(String emoji, String title, String desc) {
        StackPane icon = new StackPane();
        icon.setMinSize(42, 42); icon.setMaxSize(42, 42);
        icon.setStyle(
                "-fx-background-color: rgba(99,102,241,0.15);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: rgba(99,102,241,0.25); -fx-border-radius: 999; -fx-border-width: 1;"
        );
        Text em = new Text(emoji); em.setFont(Font.font(18));
        icon.getChildren().add(em);

        VBox txt = new VBox(3);
        Text t1 = new Text(title);
        t1.setFill(Color.web(TEXT_PRIMARY));
        t1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        Text t2 = new Text(desc);
        t2.setFill(Color.web(TEXT_MUTED));
        t2.setFont(Font.font("Segoe UI", 12));
        t2.setWrappingWidth(255);
        txt.getChildren().addAll(t1, t2);

        HBox row = new HBox(14, icon, txt);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── LOGO ──────────────────────────────────────────────────────────────────

    private VBox createLogo() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView logoImg = new ImageView();
        try {
            logoImg.setImage(new Image(getClass().getResourceAsStream("/images/blindhire_logo.png")));
        } catch (Exception ignored) {}
        logoImg.setFitWidth(58); logoImg.setFitHeight(58);
        logoImg.setPreserveRatio(true); logoImg.setSmooth(true);
        logoImg.setEffect(new DropShadow(24, Color.web(ACCENT_CYAN, 0.65)));

        VBox labels = new VBox(2);
        Text brand = new Text("BLINDHIRE");
        brand.setFill(Color.web(TEXT_PRIMARY));
        brand.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        Text tagline = new Text("RH AGENCY");
        tagline.setFill(Color.web(TEXT_MUTED));
        tagline.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        labels.getChildren().addAll(brand, tagline);

        row.getChildren().addAll(logoImg, labels);
        return new VBox(row);
    }

    // ── ANIMATIONS ────────────────────────────────────────────────────────────

    private void animateSlideUp(javafx.scene.Node n, int delayMs) {
        n.setOpacity(0); n.setTranslateY(30);
        FadeTransition ft = new FadeTransition(Duration.millis(520), n);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delayMs));
        TranslateTransition tt = new TranslateTransition(Duration.millis(520), n);
        tt.setFromY(30); tt.setToY(0); tt.setDelay(Duration.millis(delayMs));
        new ParallelTransition(ft, tt).play();
    }

    private void animateSlideIn(javafx.scene.Node n, int delayMs) {
        n.setOpacity(0); n.setTranslateX(50);
        FadeTransition ft = new FadeTransition(Duration.millis(620), n);
        ft.setFromValue(0); ft.setToValue(1); ft.setDelay(Duration.millis(delayMs));
        TranslateTransition tt = new TranslateTransition(Duration.millis(620), n);
        tt.setFromX(50); tt.setToX(0); tt.setDelay(Duration.millis(delayMs));
        new ParallelTransition(ft, tt).play();
    }

    // ── STYLE HELPERS ─────────────────────────────────────────────────────────

    private Text styledText(String s, String color, double size) {
        Text t = new Text(s);
        t.setFill(Color.web(color));
        t.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, size));
        return t;
    }

    private Text styledGlowText(String s, String color, double size) {
        Text t = new Text(s);
        t.setFill(Color.web(color));
        t.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, size));
        t.setEffect(new DropShadow(30, Color.web(color, 0.48)));
        return t;
    }

    private HBox makePill(String text, String color) {
        HBox pill = new HBox();
        pill.setAlignment(Pos.CENTER);
        pill.setPadding(new Insets(7, 16, 7, 16));
        pill.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: " + color + "55;" +
                        "-fx-border-radius: 999; -fx-border-width: 1;"
        );
        Text t = new Text(text);
        t.setFill(Color.web(color));
        t.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        pill.getChildren().add(t);
        return pill;
    }

    private void styleButtonPrimary(Button btn) {
        String base =
                "-fx-background-color: linear-gradient(to right, #06b6d4, #0891b2);" +
                        "-fx-text-fill: white; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 30; -fx-padding: 14 50; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.45), 20, 0, 0, 5);";
        String hover =
                "-fx-background-color: linear-gradient(to right, #22d3ee, #06b6d4);" +
                        "-fx-text-fill: white; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 30; -fx-padding: 14 50; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.65), 26, 0, 0, 7);";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
    }

    private void styleButtonOutline(Button btn) {
        String base =
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 30; -fx-padding: 14 50; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(148,163,184,0.3); -fx-border-radius: 30; -fx-border-width: 1.5;";
        String hover =
                "-fx-background-color: rgba(99,102,241,0.12);" +
                        "-fx-text-fill: #c7d2fe; -fx-font-size: 16px;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-background-radius: 30; -fx-padding: 14 50; -fx-cursor: hand;" +
                        "-fx-border-color: #6366f1; -fx-border-radius: 30; -fx-border-width: 1.5;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
    }

    public Parent getRoot() { return root; }
}