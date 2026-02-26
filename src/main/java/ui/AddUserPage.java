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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AddUserPage {

    private StackPane root;
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private PasswordField mdpField;
    private ComboBox<String> roleComboBox;
    private Label errorLabel;
    private DashboardPage dashboardPage;
    private userservice userService;

    public AddUserPage(DashboardPage dashboardPage) {
        this.dashboardPage = dashboardPage;
        this.userService = new userservice();
        createUI();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #A8E6F5;");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(40));

        Text titleText = new Text("Add New User");
        titleText.setFill(Color.web("#2C3E50"));
        titleText.setFont(Font.font("System", FontWeight.BOLD, 32));

        nomField    = createTextField("nom", 380);
        prenomField = createTextField("prenom", 380);
        emailField  = createTextField("email", 380);

        mdpField = new PasswordField();
        mdpField.setPromptText("password");
        mdpField.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 380;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 25;"
        );

        roleComboBox = new ComboBox<>();
        roleComboBox.setPromptText("role");
        roleComboBox.getItems().addAll("admin", "recruteur", "client");
        roleComboBox.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 25;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 380;"
        );

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font(12));

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add User");
        addButton.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 60;" +
                        "-fx-cursor: hand;"
        );
        addButton.setOnAction(e -> handleAdd());

        Hyperlink cancelLink = new Hyperlink("Cancel");
        cancelLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 14px;");
        cancelLink.setOnAction(e -> closeWindow());

        buttonBox.getChildren().addAll(addButton, cancelLink);

        contentBox.getChildren().addAll(
                titleText, nomField, prenomField, emailField,
                mdpField, roleComboBox, errorLabel, buttonBox
        );

        root.getChildren().add(contentBox);
    }

    private TextField createTextField(String prompt, double width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-background-color: #f0f0f0;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: " + width + ";" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 25;"
        );
        return field;
    }

    private void handleAdd() {
        String nom     = nomField.getText().trim();
        String prenom  = prenomField.getText().trim();
        String email   = emailField.getText().trim();
        String mdp     = mdpField.getText();
        String roleStr = roleComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty() || roleStr == null) {
            errorLabel.setText("Please fill in all fields");
            return;
        }
        if (!email.contains("@")) {
            errorLabel.setText("Please enter a valid email");
            return;
        }
        if (mdp.length() < 4) {
            errorLabel.setText("Password must be at least 4 characters");
            return;
        }

        try {
            Role role = Role.valueOf(roleStr);
            User newUser = new User(nom, prenom, email, mdp, role);
            userService.register(newUser);

            if (dashboardPage != null) dashboardPage.refreshTable();

            closeWindow();

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    public Parent getRoot() {
        return root;
    }
}