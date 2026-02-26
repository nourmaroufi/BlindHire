package ui;

import Model.User;
import Service.userservice;
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

public class LoginPage {

    private StackPane root;
    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;

    public LoginPage() {
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Background shapes
        Pane backgroundPane = createBackgroundShapes();

        // Content
        BorderPane contentPane = createContent();

        root.getChildren().addAll(backgroundPane, contentPane);
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

    private BorderPane createContent() {
        BorderPane borderPane = new BorderPane();

        // Top - Logo and Back button
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(20));

        VBox logo = createLogo();

        Button backButton = new Button("← back");
        backButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2C3E50;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> handleBack());

        topBox.getChildren().addAll(logo, backButton);
        borderPane.setTop(topBox);

        // Center - Form
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        VBox formCard = new VBox(20);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(450);
        formCard.setStyle(
                "-fx-background-color: rgba(168, 230, 245, 0.5);" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 40;"
        );

        Text titleText = new Text("Welcome Back");
        titleText.setFill(Color.web("#2C3E50"));
        titleText.setFont(Font.font("System", FontWeight.BOLD, 36));

        // Form fields
        emailField = new TextField();
        emailField.setPromptText("email");
        emailField.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 300;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 25;"
        );

        passwordField = new PasswordField();
        passwordField.setPromptText("password");
        passwordField.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 300;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 25;"
        );

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font(12));

        Button loginButton = new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #3E4A5E;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 80;" +
                        "-fx-cursor: hand;"
        );
        loginButton.setOnAction(e -> handleLogin());

        Hyperlink forgotPasswordLink = new Hyperlink("forget password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 13px;");

        formCard.getChildren().addAll(titleText, emailField, passwordField, errorLabel, loginButton, forgotPasswordLink);
        centerBox.getChildren().add(formCard);
        borderPane.setCenter(centerBox);

        return borderPane;
    }

    private VBox createLogo() {
        VBox logoBox = new VBox(3);
        logoBox.setAlignment(Pos.CENTER);

        SVGPath iconPath = new SVGPath();
        iconPath.setContent("M 20 10 L 30 10 L 30 20 L 25 20 L 25 30 L 35 30 L 35 20 L 30 20 L 30 10 C 30 5 25 0 20 0 C 15 0 10 5 10 10 L 10 20 L 5 20 L 5 30 L 15 30 L 15 20 L 10 20 L 10 10 L 20 10 Z");
        iconPath.setFill(Color.web("#4A9DB5"));

        Text brandText = new Text("BLINDHIRE");
        brandText.setFill(Color.web("#2C3E50"));
        brandText.setFont(Font.font("System", FontWeight.BOLD, 14));

        Text taglineText = new Text("RH AGENCY");
        taglineText.setFill(Color.web("#7F8C8D"));
        taglineText.setFont(Font.font(8));

        logoBox.getChildren().addAll(iconPath, brandText, taglineText);
        return logoBox;
    }

    private void handleBack() {
        WelcomePage welcomePage = new WelcomePage();
        BlindHireApp.loadScene(welcomePage.getRoot(), 960, 540);
    }

    private void navigateByRole(Model.User user) {
        switch (user.getRole()) {
            case admin:
                BlindHireApp.loadScene(new DashboardPage().getRoot(), 960, 540);
                break;
            case recruteur:
            case client:
            default:
                BlindHireApp.loadScene(new HomePage(user).getRoot(), 960, 540);
                break;
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            userservice userservice = new userservice();
            User user = userservice.authenticate(email, password);
            userservice.setCurrentUser(user);

            navigateByRole(user);
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    public Parent getRoot() {
        return root;
    }
}