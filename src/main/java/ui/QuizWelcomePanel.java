package ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * QuizWelcomePanel — shown when a candidate clicks an accepted notification.
 * Displays job details, quiz info, and a "Start Quiz" button.
 * On start: shows 3-2-1 countdown then fires onStart.runnable.
 */
public class QuizWelcomePanel extends StackPane {

    private Runnable onStart;
    private final ComboBox<String[]> langCombo = new ComboBox<>();

    /** Returns the language code selected by the user, e.g. "fr", "en" */
    public String getSelectedLanguage() {
        String[] sel = langCombo.getValue();
        return (sel != null) ? sel[0] : "en";
    }

    public QuizWelcomePanel(String jobTitle, String requiredSkills,
                            int questionCount, int timeLimitSeconds,
                            Runnable onStart, Runnable onBack) {
        this.onStart = onStart;

        // ── Background ────────────────────────────────────────────────────────
        setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#EEF6FF")),
                        new Stop(0.5, Color.web("#D6E8FF")),
                        new Stop(1, Color.web("#C3DBFF"))),
                CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().addAll(buildBubbles(), buildGrid(), buildContent(
                jobTitle, requiredSkills, questionCount, timeLimitSeconds, onBack));
    }

    // ── Animated floating bubbles ─────────────────────────────────────────────
    private Pane buildBubbles() {
        Pane p = new Pane();
        p.setPickOnBounds(false);
        double[][] specs = {
                {80,  150, 180, 0.06},
                {900, 80,  220, 0.05},
                {400, 500, 140, 0.07},
                {1100,450, 160, 0.04},
                {200, 380, 90,  0.08},
                {650, 200, 120, 0.05},
        };
        for (double[] s : specs) {
            Circle c = new Circle(s[2]);
            c.setFill(Color.web("#2563EB", s[3]));
            c.setTranslateX(s[0]);
            c.setTranslateY(s[1]);
            c.setEffect(new GaussianBlur(30));

            TranslateTransition tt = new TranslateTransition(
                    Duration.seconds(4 + Math.random() * 3), c);
            tt.setByY(-30 - Math.random() * 20);
            tt.setAutoReverse(true);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setDelay(Duration.seconds(Math.random() * 2));
            tt.play();
            p.getChildren().add(c);
        }
        return p;
    }

    // ── Subtle grid pattern overlay ───────────────────────────────────────────
    private Pane buildGrid() {
        Pane grid = new Pane();
        grid.setPickOnBounds(false);
        grid.setOpacity(0.06);
        for (int x = 0; x < 1400; x += 60) {
            Rectangle line = new Rectangle(x, 0, 1, 900);
            line.setFill(Color.WHITE);
            grid.getChildren().add(line);
        }
        for (int y = 0; y < 900; y += 60) {
            Rectangle line = new Rectangle(0, y, 1400, 1);
            line.setFill(Color.WHITE);
            grid.getChildren().add(line);
        }
        return grid;
    }

    // ── Main content card ─────────────────────────────────────────────────────
    private VBox buildContent(String jobTitle, String skills,
                              int qCount, int timeSecs, Runnable onBack) {
        VBox content = new VBox(0);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(680);
        StackPane.setAlignment(content, Pos.CENTER);

        // ── Back button top-left ──────────────────────────────────────────────
        StackPane backWrap = new StackPane();
        StackPane.setAlignment(backWrap, Pos.TOP_LEFT);
        Label btnBack = new Label("← Back");
        btnBack.setStyle(
                "-fx-text-fill: rgba(30,58,138,0.55); -fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 20 28;");
        btnBack.setOnMouseEntered(e -> btnBack.setStyle(
                "-fx-text-fill: #1e3a8a; -fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 20 28;"));
        btnBack.setOnMouseExited(e -> btnBack.setStyle(
                "-fx-text-fill: rgba(30,58,138,0.55); -fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 20 28;"));
        btnBack.setOnMouseClicked(e -> onBack.run());
        getChildren().add(btnBack);
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);

        // ── Top badge ─────────────────────────────────────────────────────────
        Label badge = new Label("🎉  APPLICATION ACCEPTED");
        badge.setStyle(
                "-fx-background-color: rgba(37,99,235,0.12);" +
                        "-fx-background-radius: 999; -fx-border-radius: 999;" +
                        "-fx-border-color: rgba(37,99,235,0.3); -fx-border-width: 1;" +
                        "-fx-text-fill: #1e3a8a; -fx-font-size: 11; -fx-font-weight: 700;" +
                        "-fx-font-family: 'Segoe UI'; -fx-padding: 6 18; -fx-letter-spacing: 1.5;");
        VBox.setMargin(badge, new Insets(0, 0, 24, 0));

        // ── Job title ─────────────────────────────────────────────────────────
        Label titleLbl = new Label(jobTitle != null ? jobTitle : "Quiz");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 44));
        titleLbl.setTextFill(Color.web("#1e3a8a"));
        titleLbl.setTextAlignment(TextAlignment.CENTER);
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(620);
        VBox.setMargin(titleLbl, new Insets(0, 0, 12, 0));

        // ── Subtitle ──────────────────────────────────────────────────────────
        Label sub = new Label("Your technical screening quiz is ready");
        sub.setFont(Font.font("Segoe UI", 16));
        sub.setTextFill(Color.web("#4B6FA8"));
        VBox.setMargin(sub, new Insets(0, 0, 36, 0));

        // ── Stats row ─────────────────────────────────────────────────────────
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
                statCard("⏱", timeSecs / 60 + " min", "Time Limit"),
                statCard("📝", String.valueOf(qCount > 0 ? qCount : "?"), "Questions"),
                statCard("🎯", "MCQ", "Format")
        );
        VBox.setMargin(stats, new Insets(0, 0, 28, 0));

        // ── Skills strip ──────────────────────────────────────────────────────
        if (skills != null && !skills.isBlank()) {
            VBox skillsBox = new VBox(8);
            skillsBox.setAlignment(Pos.CENTER);
            skillsBox.setStyle(
                    "-fx-background-color: rgba(37,99,235,0.06);" +
                            "-fx-background-radius: 14; -fx-padding: 14 20;");

            Label skillsTitle = new Label("REQUIRED SKILLS");
            skillsTitle.setStyle(
                    "-fx-text-fill: rgba(30,58,138,0.5); -fx-font-size: 10;" +
                            "-fx-font-weight: 700; -fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1.5;");

            HBox skillTags = new HBox(8);
            skillTags.setAlignment(Pos.CENTER);
            for (String skill : skills.split("[,;]")) {
                String s = skill.trim();
                if (!s.isEmpty()) skillTags.getChildren().add(skillTag(s));
            }

            skillsBox.getChildren().addAll(skillsTitle, skillTags);
            VBox.setMargin(skillsBox, new Insets(0, 0, 36, 0));
            content.getChildren().addAll(badge, titleLbl, sub, stats, skillsBox);
        } else {
            content.getChildren().addAll(badge, titleLbl, sub, stats);
        }

        // ── Language selector ─────────────────────────────────────────────────
        String[][] langs = {
                {"en","🇬🇧  English"}, {"fr","🇫🇷  Français"}, {"es","🇪🇸  Español"},
                {"de","🇩🇪  Deutsch"}, {"it","🇮🇹  Italiano"}, {"ar","🇸🇦  العربية"},
                {"pt","🇵🇹  Português"}, {"ru","🇷🇺  Русский"}, {"tr","🇹🇷  Türkçe"}
        };
        for (String[] l : langs) langCombo.getItems().add(l);
        langCombo.setValue(langs[0]); // default: English
        langCombo.setConverter(new javafx.util.StringConverter<String[]>() {
            @Override public String toString(String[] l) { return l != null ? l[1] : ""; }
            @Override public String[] fromString(String s) { return null; }
        });
        langCombo.setPrefWidth(220);
        langCombo.setStyle(
                "-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 999;" +
                        "-fx-border-radius: 999; -fx-border-color: rgba(37,99,235,0.25); -fx-border-width: 1.5;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13; -fx-font-weight: 600;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");

        VBox langBox = new VBox(6);
        langBox.setAlignment(Pos.CENTER);
        Label langLabel = new Label("QUIZ LANGUAGE");
        langLabel.setStyle(
                "-fx-text-fill: rgba(30,58,138,0.5); -fx-font-size: 10;" +
                        "-fx-font-weight: 700; -fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1.5;");
        langBox.getChildren().addAll(langLabel, langCombo);
        VBox.setMargin(langBox, new Insets(0, 0, 28, 0));
        content.getChildren().add(langBox);

        // ── Start button ──────────────────────────────────────────────────────
        Label btnStart = new Label("Start Quiz  →");
        btnStart.setStyle(startBtnStyle(false));
        btnStart.setPrefWidth(280);
        btnStart.setAlignment(Pos.CENTER);
        btnStart.setOnMouseEntered(e -> btnStart.setStyle(startBtnStyle(true)));
        btnStart.setOnMouseExited(e -> btnStart.setStyle(startBtnStyle(false)));
        btnStart.setOnMouseClicked(e -> startCountdown());

        // pulse animation on the button
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.6), btnStart);
        pulse.setFromX(1.0); pulse.setToX(1.03);
        pulse.setFromY(1.0); pulse.setToY(1.03);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        Label hint = new Label("All questions must be answered within the time limit");
        hint.setStyle(
                "-fx-text-fill: rgba(30,58,138,0.4); -fx-font-size: 11;" +
                        "-fx-font-family: 'Segoe UI'; -fx-padding: 10 0 0 0;");

        content.getChildren().addAll(btnStart, hint);

        // ── Fade-in entrance ──────────────────────────────────────────────────
        content.setOpacity(0);
        content.setTranslateY(30);
        FadeTransition ft = new FadeTransition(Duration.millis(600), content);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), content);
        tt.setToY(0);
        ParallelTransition entrance = new ParallelTransition(ft, tt);
        entrance.setDelay(Duration.millis(150));
        entrance.play();

        return content;
    }

    // ── 3-2-1 countdown overlay ───────────────────────────────────────────────
    private void startCountdown() {
        // Full-screen dark overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(8,16,50,0.92);");

        Label countLbl = new Label("3");
        countLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 160));
        countLbl.setTextFill(Color.WHITE);
        countLbl.setEffect(new DropShadow(60, Color.web("#4A9EFF", 0.8)));

        Label goLbl = new Label("GO!");
        goLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 110));
        goLbl.setTextFill(Color.web("#4A9EFF"));
        goLbl.setEffect(new DropShadow(60, Color.web("#4A9EFF", 0.9)));
        goLbl.setOpacity(0);

        overlay.getChildren().addAll(countLbl, goLbl);
        getChildren().add(overlay);

        // Animate each number
        Timeline tl = new Timeline();
        String[] nums = {"3", "2", "1"};
        String[] colors = {"#FFFFFF", "#7EC8FF", "#4A9EFF"};

        for (int i = 0; i < nums.length; i++) {
            final String num = nums[i];
            final String col = colors[i];
            final int idx = i;

            // Show number
            tl.getKeyFrames().add(new KeyFrame(Duration.seconds(i), ev -> {
                countLbl.setText(num);
                countLbl.setTextFill(Color.web(col));
                countLbl.setOpacity(1);
                countLbl.setScaleX(0.6); countLbl.setScaleY(0.6);

                ScaleTransition pop = new ScaleTransition(Duration.millis(300), countLbl);
                pop.setToX(1.0); pop.setToY(1.0);
                pop.play();
            }));

            // Fade out number
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 1000 + 700), ev -> {
                FadeTransition fade = new FadeTransition(Duration.millis(250), countLbl);
                fade.setToValue(0.2);
                fade.play();
            }));
        }

        // Show GO!
        tl.getKeyFrames().add(new KeyFrame(Duration.seconds(3), ev -> {
            countLbl.setOpacity(0);
            goLbl.setOpacity(1);
            goLbl.setScaleX(0.5); goLbl.setScaleY(0.5);

            ScaleTransition pop = new ScaleTransition(Duration.millis(300), goLbl);
            pop.setToX(1.1); pop.setToY(1.1);
            pop.setAutoReverse(true); pop.setCycleCount(2);
            pop.play();
        }));

        // Launch quiz
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(3700), ev -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e2 -> {
                getChildren().remove(overlay);
                onStart.run();
            });
            fadeOut.play();
        }));

        tl.play();
    }

    // ── Helper builders ───────────────────────────────────────────────────────
    private VBox statCard(String icon, String value, String label) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.8);" +
                        "-fx-background-radius: 16; -fx-border-radius: 16;" +
                        "-fx-border-color: rgba(37,99,235,0.15); -fx-border-width: 1;" +
                        "-fx-padding: 18 14;");
        card.setEffect(new DropShadow(12, Color.rgb(0, 0, 0, 0.25)));

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 24;");

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        valueLbl.setTextFill(Color.web("#1e3a8a"));

        Label labelLbl = new Label(label);
        labelLbl.setStyle(
                "-fx-text-fill: rgba(30,58,138,0.5); -fx-font-size: 11;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-weight: 600;");

        card.getChildren().addAll(iconLbl, valueLbl, labelLbl);
        return card;
    }

    private Label skillTag(String skill) {
        Label tag = new Label(skill);
        tag.setStyle(
                "-fx-background-color: rgba(37,99,235,0.1);" +
                        "-fx-background-radius: 999; -fx-border-radius: 999;" +
                        "-fx-border-color: rgba(37,99,235,0.25); -fx-border-width: 1;" +
                        "-fx-text-fill: #1e3a8a; -fx-font-size: 12; -fx-font-family: 'Segoe UI';" +
                        "-fx-font-weight: 600; -fx-padding: 5 14;");
        return tag;
    }

    private String startBtnStyle(boolean hover) {
        String bg = hover
                ? "linear-gradient(to bottom, #5AADFF, #2F6FE6)"
                : "linear-gradient(to bottom, #4A9EFF, #1a6ee0)";
        return "-fx-background-color: " + bg + ";" +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: 800;" +
                "-fx-font-size: 16; -fx-background-radius: 999; -fx-padding: 16 48;" +
                "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(74,158,255,0.5), 20, 0, 0, 4);";
    }
}