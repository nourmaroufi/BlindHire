package ui;

import Model.Role;
import Model.User;
import Service.userservice;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EditPage {

    private StackPane root;
    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private PasswordField mdpField;
    private ComboBox<String> roleComboBox;
    private Label errorLabel;
    private User user;
    private DashboardPage dashboardPage;
    private userservice userService;

    public EditPage(User user, DashboardPage dashboardPage) {
        this.user = user;
        this.dashboardPage = dashboardPage;
        this.userService = new userservice();
        createUI();
        populateFields();
    }

    private void createUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #A8E6F5;");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new javafx.geometry.Insets(40));

        Text titleText = new Text("Edit Account");
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

        // ── Fixed: values match the Role enum (admin, recruteur, client) ──
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

        Button saveButton = new Button("Save Edit");
        saveButton.setStyle(
                "-fx-background-color: #3E4A5E;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-radius: 25;" +
                        "-fx-padding: 12 60;" +
                        "-fx-cursor: hand;"
        );
        saveButton.setOnAction(e -> handleSave());

        Hyperlink cancelLink = new Hyperlink("Cancel");
        cancelLink.setStyle("-fx-text-fill: #4A9DB5; -fx-font-size: 14px;");
        cancelLink.setOnAction(e -> handleCancel());

        buttonBox.getChildren().addAll(saveButton, cancelLink);

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

    private void populateFields() {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        mdpField.setText(user.getMdp());
        // role.name() returns "admin" / "recruteur" / "client" — matches ComboBox items
        roleComboBox.setValue(user.getRole().name());
    }

    private void handleSave() {
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

        try {
            if (!email.equals(user.getEmail()) && userService.emailExists(email)) {
                errorLabel.setText("Email already exists");
                return;
            }

            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMdp(mdp);
            user.setRole(Role.valueOf(roleStr));   // "admin" | "recruteur" | "client"

            userService.updateUser(user);

            if (dashboardPage != null) dashboardPage.refreshTable();

            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("An error occurred. Please try again.");
            e.printStackTrace();
        }
    }

    private void handleCancel() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    public Parent getRoot() {
        return root;
    }
}