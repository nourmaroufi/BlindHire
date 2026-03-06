package ui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.math.BigDecimal;

public class QuizResultPopup {

    /** Callback invoked when user clicks "Get AI Analysis" (score < 50 only). */
    @FunctionalInterface
    public interface AiCallback {
        void run(java.math.BigDecimal percent, String skills);
    }

    public static void show(Stage owner,
                            String jobTitle,
                            int userId,
                            int jobOfferId,
                            BigDecimal percent,
                            int totalQuestions,
                            int correctAnswers,
                            boolean timesUp,
                            Runnable onClose) {
        show(owner, jobTitle, userId, jobOfferId, percent, totalQuestions,
                correctAnswers, timesUp, onClose, null);
    }

    public static void show(Stage owner,
                            String jobTitle,
                            int userId,
                            int jobOfferId,
                            BigDecimal percent,
                            int totalQuestions,
                            int correctAnswers,
                            boolean timesUp,
                            Runnable onClose,
                            AiCallback aiCallback) {

        Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) popup.initOwner(owner);

        // Semi-transparent full-screen backdrop
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(8,18,55,0.68);");
        backdrop.setOpacity(0);

        VBox card = buildCard(jobTitle, userId, jobOfferId, percent,
                totalQuestions, correctAnswers, timesUp, popup, onClose, aiCallback);
        card.setMaxWidth(500);
        card.setTranslateY(44);
        card.setOpacity(0);
        backdrop.getChildren().add(card);

        double w = owner != null ? owner.getWidth()  : 1280;
        double h = owner != null ? owner.getHeight() : 800;
        Scene scene = new Scene(backdrop, w, h);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        if (owner != null) { popup.setX(owner.getX()); popup.setY(owner.getY()); }
        popup.show();

        FadeTransition fd = new FadeTransition(Duration.millis(220), backdrop);
        fd.setToValue(1);
        FadeTransition cf = new FadeTransition(Duration.millis(320), card);
        cf.setToValue(1); cf.setDelay(Duration.millis(80));
        TranslateTransition ct = new TranslateTransition(Duration.millis(320), card);
        ct.setToY(0); ct.setDelay(Duration.millis(80));
        ct.setInterpolator(Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0));
        new ParallelTransition(fd, cf, ct).play();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private static VBox buildCard(String jobTitle, int userId, int jobOfferId,
                                  BigDecimal percent, int totalQ, int correct,
                                  boolean timesUp, Stage popup, Runnable onClose,
                                  AiCallback aiCallback) {

        double  pct    = (percent == null) ? 0 : percent.doubleValue();
        boolean passed = pct >= 50;
        String  color  = passed ? "#10B981" : "#EF4444";
        String  grad   = passed
                ? "linear-gradient(to right, #059669, #10B981)"
                : "linear-gradient(to right, #DC2626, #EF4444)";

        // Card — white rounded box, shadow on the card node itself
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-border-radius: 22;");
        card.setEffect(new DropShadow(36, 0, 6, Color.rgb(0, 0, 0, 0.26)));

        // ── Banner ────────────────────────────────────────────────────────────
        StackPane banner = new StackPane();
        banner.setStyle("-fx-background-color:" + grad + "; -fx-background-radius: 22 22 0 0;");
        banner.setPrefHeight(118);
        banner.setMaxHeight(118);

        // Clip banner so decorative circles don't spill
        Rectangle bannerClip = new Rectangle(500, 118);
        bannerClip.setArcWidth(44); bannerClip.setArcHeight(44);
        Pane deco = new Pane(); deco.setPickOnBounds(false);
        deco.setClip(bannerClip);
        Circle dc1 = new Circle(80); dc1.setFill(Color.WHITE); dc1.setOpacity(0.07);
        dc1.setTranslateX(420); dc1.setTranslateY(20);
        Circle dc2 = new Circle(50); dc2.setFill(Color.WHITE); dc2.setOpacity(0.06);
        dc2.setTranslateX(40);  dc2.setTranslateY(80);
        deco.getChildren().addAll(dc1, dc2);

        VBox bannerContent = new VBox(4);
        bannerContent.setAlignment(Pos.CENTER);
        // bottom padding leaves space for the ring that overlaps downward
        bannerContent.setPadding(new Insets(16, 20, 44, 20));

        Label emojiLbl = new Label(passed ? "🏆" : timesUp ? "⏰" : "📋");
        emojiLbl.setStyle("-fx-font-size:30;");
        Label titleLbl = new Label(passed ? "Quiz Passed!" : timesUp ? "Time's Up" : "Keep Practicing");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        titleLbl.setTextFill(Color.WHITE);
        Label jobLbl = new Label(jobTitle != null && !jobTitle.isBlank() ? jobTitle : "Technical Quiz");
        jobLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12; -fx-text-fill:rgba(255,255,255,0.82);");
        bannerContent.getChildren().addAll(emojiLbl, titleLbl, jobLbl);
        banner.getChildren().addAll(deco, bannerContent);

        // ── Score ring ────────────────────────────────────────────────────────
        // Sits in normal VBox flow, pulled UP with negative top margin to overlap banner
        StackPane ring = buildRing(pct, color);
        VBox.setMargin(ring, new Insets(-44, 0, 0, 0));

        // ── Percent label ─────────────────────────────────────────────────────
        Label pctLbl = new Label("0%");
        pctLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 42));
        pctLbl.setTextFill(Color.web(color));
        VBox.setMargin(pctLbl, new Insets(2, 0, 12, 0));

        // Animate count-up
        int target = (int) pct;
        if (target > 0) {
            Timeline cu = new Timeline();
            for (int i = 0; i <= target; i++) {
                final int v = i;
                cu.getKeyFrames().add(new KeyFrame(
                        Duration.millis(i * (1000.0 / target)),
                        e -> pctLbl.setText(v + "%")));
            }
            cu.setDelay(Duration.millis(460));
            cu.play();
        } else {
            pctLbl.setText("0%");
        }

        // ── Stat pills ────────────────────────────────────────────────────────
        HBox pills = new HBox(10);
        pills.setAlignment(Pos.CENTER);
        pills.setPadding(new Insets(0, 22, 0, 22));
        pills.getChildren().addAll(
                pill("✅", correct + "/" + totalQ,            "Correct",
                        "#10B981", "#F0FDF4", "#A7F3D0"),
                pill("❌", (totalQ - correct) + "/" + totalQ, "Wrong",
                        "#EF4444", "#FFF1F2", "#FECDD3"),
                pill("🎯", passed ? "PASS" : "FAIL",           "Result",
                        color,
                        passed ? "#F0FDF4" : "#FFF1F2",
                        passed ? "#A7F3D0" : "#FECDD3")
        );
        VBox.setMargin(pills, new Insets(0, 0, 4, 0));

        // ── Divider ───────────────────────────────────────────────────────────
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color:#F1F5F9;");
        VBox.setMargin(div, new Insets(14, 20, 10, 20));

        // ── Detail rows ───────────────────────────────────────────────────────
        VBox details = new VBox(7);
        details.setPadding(new Insets(0, 22, 0, 22));
        details.getChildren().addAll(
                detailRow("👤", "User ID",    String.valueOf(userId)),
                detailRow("💼", "Job Offer",  "#" + jobOfferId),
                detailRow("📊", "Score",      String.format("%.2f", pct) + " / 100"),
                detailRow("💾", "Saved",      "Recorded to database"),
                detailRowStatus("🏅", "Result", passed)
        );

        // ── Message ───────────────────────────────────────────────────────────
        Label msg = new Label(passed
                ? "🎉 Congratulations! Your result has been recorded. The recruiter will be in touch."
                : "Don't give up! Your result has been saved. Review the required skills and try again.");
        msg.setWrapText(true);
        msg.setTextAlignment(TextAlignment.CENTER);
        msg.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12; -fx-text-fill:#64748B; -fx-padding:0 26;");
        VBox.setMargin(msg, new Insets(10, 0, 0, 0));

        // ── Close button (declared here so both branches below can reference it) ──
        Label btnClose = new Label("Close");
        btnClose.setPrefWidth(210);
        btnClose.setAlignment(Pos.CENTER);
        btnClose.setStyle(
                "-fx-background-color:" + grad + ";" +
                        "-fx-text-fill:white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                        "-fx-font-size:14; -fx-background-radius:999; -fx-padding:13 0; -fx-cursor:hand;");
        btnClose.setEffect(new DropShadow(10, 0, 3, Color.web(color, 0.32)));
        btnClose.setOnMouseEntered(e -> btnClose.setScaleX(1.04));
        btnClose.setOnMouseExited(e  -> btnClose.setScaleX(1.00));
        btnClose.setOnMouseClicked(e -> {
            Scene sc = card.getScene();
            if (sc == null) { popup.close(); if (onClose != null) onClose.run(); return; }
            FadeTransition ft = new FadeTransition(Duration.millis(160), sc.getRoot());
            ft.setToValue(0);
            ft.setOnFinished(ev -> { popup.close(); if (onClose != null) onClose.run(); });
            ft.play();
        });
        VBox.setMargin(btnClose, new Insets(10, 22, 24, 22));

        // ── AI Analysis button (only shown when failed AND callback available) ──
        if (!passed && aiCallback != null) {
            final BigDecimal fPercent = percent;
            Label btnAi = new Label("✨  Show Recommendations");
            btnAi.setPrefWidth(310);
            btnAi.setAlignment(Pos.CENTER);
            btnAi.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
                            "-fx-text-fill: white; -fx-font-family:'Segoe UI'; -fx-font-weight: 800;" +
                            "-fx-font-size:13; -fx-background-radius:999; -fx-padding:12 0; -fx-cursor:hand;");
            btnAi.setEffect(new DropShadow(10, 0, 3, Color.web("#6366f1", 0.4)));
            btnAi.setOnMouseEntered(e -> btnAi.setScaleX(1.04));
            btnAi.setOnMouseExited(e  -> btnAi.setScaleX(1.00));
            btnAi.setOnMouseClicked(e -> {
                Scene sc = card.getScene();
                Runnable triggerAi = () -> aiCallback.run(fPercent, jobTitle);
                if (sc == null) { popup.close(); triggerAi.run(); return; }
                FadeTransition ft = new FadeTransition(Duration.millis(160), sc.getRoot());
                ft.setToValue(0);
                ft.setOnFinished(ev -> { popup.close(); triggerAi.run(); });
                ft.play();
            });
            VBox.setMargin(btnAi, new Insets(14, 22, 0, 22));
            card.getChildren().addAll(banner, ring, pctLbl, pills, div, details, msg, btnAi, btnClose);
        } else {
            card.getChildren().addAll(banner, ring, pctLbl, pills, div, details, msg, btnClose);
        }
        return card;
    }

    // ── Ring: Canvas-based so arc is always perfectly centred ───────────────
    private static StackPane buildRing(double pct, String color) {
        final int D  = 90;   // canvas size (= diameter)
        final int SW = 7;    // stroke width
        final double cx = D / 2.0;
        final double cy = D / 2.0;
        final double R  = (D - SW) / 2.0;   // radius to stroke centre

        Canvas canvas = new Canvas(D, D);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // ── Draw track (grey ring) ────────────────────────────────────────────
        gc.setStroke(javafx.scene.paint.Color.web("#E2E8F0"));
        gc.setLineWidth(SW);
        gc.strokeArc(cx - R, cy - R, R * 2, R * 2, 0, 360,
                javafx.scene.shape.ArcType.OPEN);

        // ── Draw white filled disc ────────────────────────────────────────────
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(cx - R + SW / 2.0, cy - R + SW / 2.0,
                (R - SW / 2.0) * 2, (R - SW / 2.0) * 2);

        // ── Animated progress arc (redrawn each frame) ────────────────────────
        double target = pct / 100.0 * 360.0;
        // We use a simple DoubleProperty as the animation target
        javafx.beans.property.DoubleProperty angleProp =
                new javafx.beans.property.SimpleDoubleProperty(0);
        angleProp.addListener((obs, oldV, newV) -> {
            gc.clearRect(0, 0, D, D);
            // track
            gc.setStroke(javafx.scene.paint.Color.web("#E2E8F0"));
            gc.setLineWidth(SW);
            gc.strokeArc(cx - R, cy - R, R * 2, R * 2, 0, 360,
                    javafx.scene.shape.ArcType.OPEN);
            // white fill
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(cx - R + SW / 2.0, cy - R + SW / 2.0,
                    (R - SW / 2.0) * 2, (R - SW / 2.0) * 2);
            // progress arc — JavaFX angles: 90° = top, counter-clockwise
            gc.setStroke(javafx.scene.paint.Color.web(color));
            gc.setLineWidth(SW);
            gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            gc.strokeArc(cx - R, cy - R, R * 2, R * 2,
                    90, -newV.doubleValue(),
                    javafx.scene.shape.ArcType.OPEN);
        });

        Timeline t = new Timeline(new KeyFrame(
                Duration.millis(1050),
                new KeyValue(angleProp, target,
                        Interpolator.SPLINE(0.25, 0.46, 0.45, 0.94))));
        t.setDelay(Duration.millis(420));
        t.play();

        StackPane sp = new StackPane(canvas);
        sp.setPrefSize(D, D);
        sp.setMaxSize(D, D);
        sp.setMinSize(D, D);
        sp.setEffect(new DropShadow(14, 0, 2, Color.rgb(0, 0, 0, 0.11)));
        return sp;
    }

    // ── Pill card ─────────────────────────────────────────────────────────────
    private static HBox pill(String icon, String val, String label,
                             String tc, String bg, String bc) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(124);
        box.setStyle(
                "-fx-background-color:" + bg + ";" +
                        "-fx-background-radius:13; -fx-border-radius:13;" +
                        "-fx-border-color:" + bc + "; -fx-border-width:1;" +
                        "-fx-padding:10 8;");
        Label i = new Label(icon); i.setStyle("-fx-font-size:17;");
        Label v = new Label(val);
        v.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 16));
        v.setTextFill(Color.web(tc));
        Label l = new Label(label);
        l.setStyle("-fx-font-size:10; -fx-font-weight:700; -fx-font-family:'Segoe UI';" +
                "-fx-text-fill:" + tc + "AA;");
        box.getChildren().addAll(i, v, l);
        HBox w = new HBox(box); w.setAlignment(Pos.CENTER);
        return w;
    }

    // ── Status detail row (colored badge) ────────────────────────────────────
    private static HBox detailRowStatus(String icon, String key, boolean passed) {
        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);
        r.setPadding(new Insets(7, 12, 7, 12));
        r.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:10;");
        Label ic = new Label(icon); ic.setStyle("-fx-font-size:13;");
        Label kl = new Label(key);
        kl.setStyle("-fx-font-size:12; -fx-text-fill:#94A3B8;" +
                "-fx-font-family:'Segoe UI'; -fx-font-weight:600;");
        kl.setPrefWidth(82);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        // Colored status badge
        String statusText = passed ? "✅  ACCEPTED" : "❌  REJECTED";
        String bgColor    = passed ? "#F0FDF4"    : "#FFF1F2";
        String bdColor    = passed ? "#A7F3D0"    : "#FECDD3";
        String txColor    = passed ? "#059669"    : "#DC2626";
        Label badge = new Label(statusText);
        badge.setStyle(
                "-fx-background-color:" + bgColor + ";" +
                        "-fx-background-radius:999; -fx-border-radius:999;" +
                        "-fx-border-color:" + bdColor + "; -fx-border-width:1;" +
                        "-fx-text-fill:" + txColor + "; -fx-font-size:12;" +
                        "-fx-font-weight:800; -fx-font-family:'Segoe UI'; -fx-padding:3 12;");
        r.getChildren().addAll(ic, kl, sp, badge);
        return r;
    }

    // ── Detail row ────────────────────────────────────────────────────────────
    private static HBox detailRow(String icon, String key, String val) {
        HBox r = new HBox(10);
        r.setAlignment(Pos.CENTER_LEFT);
        r.setPadding(new Insets(7, 12, 7, 12));
        r.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:10;");
        Label ic = new Label(icon); ic.setStyle("-fx-font-size:13;");
        Label kl = new Label(key);
        kl.setStyle("-fx-font-size:12; -fx-text-fill:#94A3B8;" +
                "-fx-font-family:'Segoe UI'; -fx-font-weight:600;");
        kl.setPrefWidth(82);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label vl = new Label(val);
        vl.setStyle("-fx-font-size:13; -fx-text-fill:#1E293B;" +
                "-fx-font-family:'Segoe UI'; -fx-font-weight:700;");
        r.getChildren().addAll(ic, kl, sp, vl);
        return r;
    }
}