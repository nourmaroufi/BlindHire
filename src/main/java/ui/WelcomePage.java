package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class WelcomePage {

    private StackPane root;

    public WelcomePage() {
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Background shapes
        Pane backgroundPane = createBackgroundShapes();

        // Content
        VBox contentBox = createContent();

        root.getChildren().addAll(backgroundPane, contentBox);
    }

    private Pane createBackgroundShapes() {
        Pane pane = new Pane();

        // Light blue wave bottom left
        Path wavePath = new Path();
        wavePath.setFill(Color.web("#A8E6F5"));
        wavePath.setLayoutX(0);
        wavePath.setLayoutY(410);
        wavePath.getElements().addAll(
                new MoveTo(0, 0),
                new CubicCurveTo(150, -50, 300, 50, 450, 0),
                new LineTo(450, 130),
                new LineTo(0, 130),
                new ClosePath()
        );

        // Top right blue shape
        Path rightShape = new Path();
        rightShape.setFill(Color.web("#6BA3BE"));
        rightShape.setLayoutX(450);
        rightShape.setLayoutY(0);
        rightShape.getElements().addAll(
                new MoveTo(0, 0),
                new LineTo(510, 0),
                new LineTo(510, 540),
                new CubicCurveTo(350, 400, 200, 300, 0, 200),
                new ClosePath()
        );

        // Light blue overlay
        Path overlayShape = new Path();
        overlayShape.setFill(Color.web("#87CEEB"));
        overlayShape.setOpacity(0.5);
        overlayShape.setLayoutX(400);
        overlayShape.setLayoutY(0);
        overlayShape.getElements().addAll(
                new MoveTo(0, 0),
                new CubicCurveTo(50, 100, 100, 200, 150, 300),
                new LineTo(560, 300),
                new LineTo(560, 0),
                new ClosePath()
        );

        pane.getChildren().addAll(wavePath, rightShape, overlayShape);
        return pane;
    }

    private VBox createContent() {
        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        contentBox.setPadding(new Insets(50));

        // Logo
        VBox logo = createLogo();

        // Main content
        VBox mainContent = new VBox(20);
        mainContent.setMaxWidth(500);

        Text titleText = new Text("Find your job better\nand faster");
        titleText.setFill(Color.web("#2C3E50"));
        titleText.setFont(Font.font("System", FontWeight.BOLD, 42));
        titleText.setWrappingWidth(450);

        Text subtitleText = new Text("Find your best job better and faster with Jobest");
        subtitleText.setFill(Color.web("#7F8C8D"));
        subtitleText.setFont(Font.font(16));

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button loginButton = new Button("login");
        styleButton(loginButton, "#3E4A5E");
        loginButton.setOnAction(e -> handleLogin());

        Button signupButton = new Button("sign up");
        styleButton(signupButton, "#4A9DB5");
        signupButton.setOnAction(e -> handleSignup());

        buttonBox.getChildren().addAll(loginButton, signupButton);
        mainContent.getChildren().addAll(titleText, subtitleText, buttonBox);

        contentBox.getChildren().addAll(logo, mainContent);
        return contentBox;
    }

    private VBox createLogo() {
        VBox logoBox = new VBox(5);
        logoBox.setAlignment(Pos.CENTER);

        // Simple logo representation
        SVGPath iconPath = new SVGPath();
        iconPath.setContent("M 20 10 L 30 10 L 30 20 L 25 20 L 25 30 L 35 30 L 35 20 L 30 20 L 30 10 C 30 5 25 0 20 0 C 15 0 10 5 10 10 L 10 20 L 5 20 L 5 30 L 15 30 L 15 20 L 10 20 L 10 10 L 20 10 Z");
        iconPath.setFill(Color.web("#4A9DB5"));
        iconPath.setScaleX(1.5);
        iconPath.setScaleY(1.5);

        Text brandText = new Text("BLINDHIRE");
        brandText.setFill(Color.web("#2C3E50"));
        brandText.setFont(Font.font("System", FontWeight.BOLD, 20));

        Text taglineText = new Text("RH AGENCY");
        taglineText.setFill(Color.web("#7F8C8D"));
        taglineText.setFont(Font.font(10));

        logoBox.getChildren().addAll(iconPath, brandText, taglineText);
        return logoBox;
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 40 12 40;" +
                        "-fx-cursor: hand;"
        );
    }

    private void handleLogin() {
        LoginPage loginPage = new LoginPage();
        BlindHireApp.loadScene(loginPage.getRoot(), 960, 540);
    }

    private void handleSignup() {
        SignupPage signupPage = new SignupPage();
        BlindHireApp.loadScene(signupPage.getRoot(), 960, 540);
    }

    public Parent getRoot() {
        return root;
    }
}
