package ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;

/**
 * SplashScreen  (exactly 2 seconds)
 *
 * 0.00s  radial glow pulses in              (200ms)
 * 0.20s  logo image + group scales in       (500ms cubic ease-out)
 * 0.70s  "BLINDHIRE" text fades in          (250ms)
 * 0.95s  "RH AGENCY" tagline fades in       (200ms)
 * 1.15s  loading bar fills left to right    (350ms)
 * 1.50s  brief hold                         (200ms)
 * 1.70s  whole scene zooms out + fades      (300ms)
 * 2.00s  onFinished fires  →  navigate
 *
 * Logo loaded from:  /images/blindhire_logo.png  (classpath resource)
 */
public class SplashScreen {

    private final StackPane root;
    private final Runnable  onFinished;

    public SplashScreen(Runnable onFinished) {
        this.onFinished = onFinished;
        root = new StackPane();
        build();
    }

    private void build() {
        root.setStyle("-fx-background-color: #0D1B2A;");

        // Soft radial glow
        Region glow = new Region();
        glow.setPrefSize(400, 400);
        glow.setMaxSize(400, 400);
        glow.setStyle(
                "-fx-background-color: radial-gradient(center 50% 50%, radius 50%," +
                        "#4A9DB535 0%, transparent 100%);" +
                        "-fx-background-radius: 999;");
        glow.setOpacity(0);

        // Logo group
        VBox logoGroup = new VBox(18);
        logoGroup.setAlignment(Pos.CENTER);
        logoGroup.setOpacity(0);
        logoGroup.setScaleX(0.38);
        logoGroup.setScaleY(0.38);

        // Logo image
        ImageView logoImg = new ImageView();
        try {
            Image img = new Image(
                    SplashScreen.class.getResourceAsStream("/images/blindhire_logo.png"));
            logoImg.setImage(img);
        } catch (Exception ignored) { /* glow still shows if image is missing */ }
        logoImg.setFitWidth(120);
        logoImg.setFitHeight(120);
        logoImg.setPreserveRatio(true);
        logoImg.setSmooth(true);
        javafx.scene.effect.DropShadow imgGlow =
                new javafx.scene.effect.DropShadow(28, Color.web("#4A9DB5", 0.65));
        logoImg.setEffect(imgGlow);

        // Brand name
        Text brandText = new Text("BLINDHIRE");
        brandText.setFill(Color.WHITE);
        brandText.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 40));
        brandText.setOpacity(0);
        brandText.setEffect(new javafx.scene.effect.DropShadow(
                16, Color.web("#4A9DB5", 0.45)));

        // Tagline
        Text tagline = new Text("RH AGENCY");
        tagline.setFill(Color.web("#7F8C8D"));
        tagline.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        tagline.setOpacity(0);

        // Loading bar
        double barW = 200;
        Region barTrack = new Region();
        barTrack.setPrefSize(barW, 3);
        barTrack.setMaxSize(barW, 3);
        barTrack.setStyle("-fx-background-color: #ffffff1a; -fx-background-radius: 999;");

        Region barFill = new Region();
        barFill.setPrefHeight(3);
        barFill.setMaxHeight(3);
        barFill.setPrefWidth(0);
        barFill.setStyle("-fx-background-color: linear-gradient(to right, #4A9DB5, #87CEEB);" +
                "-fx-background-radius: 999;");
        barFill.setOpacity(0);

        StackPane barStack = new StackPane(barTrack, barFill);
        barStack.setMaxWidth(barW);
        StackPane.setAlignment(barFill, Pos.CENTER_LEFT);
        barStack.setOpacity(0);

        logoGroup.getChildren().addAll(logoImg, brandText, tagline, barStack);
        root.getChildren().addAll(glow, logoGroup);

        playSequence(glow, logoGroup, brandText, tagline, barStack, barFill, barW);
    }

    private void playSequence(Region glow, VBox logoGroup,
                              Text brandText, Text tagline,
                              StackPane barStack, Region barFill, double barW) {

        // Phase 1: glow in — 200ms
        FadeTransition glowFade = new FadeTransition(Duration.millis(200), glow);
        glowFade.setFromValue(0); glowFade.setToValue(1);

        // Phase 2: logo scale + fade in — 500ms
        FadeTransition logoFade = new FadeTransition(Duration.millis(500), logoGroup);
        logoFade.setFromValue(0); logoFade.setToValue(1);
        ScaleTransition logoScale = new ScaleTransition(Duration.millis(500), logoGroup);
        logoScale.setFromX(0.38); logoScale.setToX(1.0);
        logoScale.setFromY(0.38); logoScale.setToY(1.0);
        logoScale.setInterpolator(Interpolator.SPLINE(0.22, 0.61, 0.36, 1.0)); // cubic ease-out
        ParallelTransition phase2 = new ParallelTransition(logoFade, logoScale);

        // Phase 3: brand name — 250ms
        FadeTransition brandFade = new FadeTransition(Duration.millis(250), brandText);
        brandFade.setFromValue(0); brandFade.setToValue(1);

        // Phase 4: tagline — 200ms
        FadeTransition taglineFade = new FadeTransition(Duration.millis(200), tagline);
        taglineFade.setFromValue(0); taglineFade.setToValue(1);

        // Phase 5: loading bar — 350ms
        FadeTransition barAppear = new FadeTransition(Duration.millis(80), barStack);
        barAppear.setFromValue(0); barAppear.setToValue(1);
        Timeline barAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(barFill.prefWidthProperty(), 0),
                        new KeyValue(barFill.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(350),
                        new KeyValue(barFill.prefWidthProperty(), barW, Interpolator.EASE_BOTH),
                        new KeyValue(barFill.opacityProperty(), 1))
        );
        ParallelTransition phase5 = new ParallelTransition(barAppear, barAnim);

        // Hold — 2200ms  (extends total from 2000ms to 4000ms)
        PauseTransition hold = new PauseTransition(Duration.millis(2000));

        // Phase 6: zoom-out + fade — 300ms
        // Logo zooms away while the whole root fades to black
        ScaleTransition zoomOut = new ScaleTransition(Duration.millis(300), logoGroup);
        zoomOut.setToX(2.2); zoomOut.setToY(2.2);
        zoomOut.setInterpolator(Interpolator.EASE_IN);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        ParallelTransition phase6 = new ParallelTransition(zoomOut, fadeOut);

        // Total: 200 + 500 + 250 + 200 + 350 + 2200 + 300 = 4000ms
        SequentialTransition seq = new SequentialTransition(
                glowFade, phase2, brandFade, taglineFade, phase5, hold, phase6);
        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }

    public Parent getRoot() { return root; }
}