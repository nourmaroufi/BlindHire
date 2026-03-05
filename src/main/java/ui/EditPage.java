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
        // Beautiful blue gradient background (matching AddUserPage)
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0F2027, #203A43, #2C5364);");

        // ── Top bar with close button ──
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(16, 20, 0, 20));
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2);" +
                        "-fx-background-radius: 50;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 8 12;" +
                        "-fx-border-color: rgba(255,255,255,0.3);" +
                        "-fx-border-radius: 50;"
        );

        // Hover effect for close button
        closeBtn.setOnMouseEntered(e ->
                closeBtn.setStyle(
                        "-fx-background-color: #FF6B6B;" +
                                "-fx-background-radius: 50;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 12;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-radius: 50;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.5), 10, 0, 0, 2);"
                )
        );
        closeBtn.setOnMouseExited(e ->
                closeBtn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.2);" +
                                "-fx-background-radius: 50;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 8 12;" +
                                "-fx-border-color: rgba(255,255,255,0.3);" +
                                "-fx-border-radius: 50;"
                )
        );
        closeBtn.setOnAction(e -> handleCancel());
        topBar.getChildren().add(closeBtn);

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(0, 40, 40, 40));

        // White card background for form (matching AddUserPage)
        contentBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);" +
                        "-fx-padding: 30 40 30 40;"
        );

        Text titleText = new Text("Edit Account");
        titleText.setFill(Color.web("#1E3C72"));
        titleText.setFont(Font.font("System", FontWeight.BOLD, 36));

        // Add a subtle blue underline effect
        Region underline = new Region();
        underline.setMaxWidth(80);
        underline.setMaxHeight(3);
        underline.setStyle("-fx-background-color: linear-gradient(to right, #00B4DB, #0083B0); -fx-background-radius: 5;");
        VBox titleContainer = new VBox(5, titleText, underline);
        titleContainer.setAlignment(Pos.CENTER);

        nomField = createTextField("Last Name", 380);
        prenomField = createTextField("First Name", 380);
        emailField = createTextField("Email Address", 380);

        mdpField = new PasswordField();
        mdpField.setPromptText("Password");
        mdpField.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 14 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 380;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 1;"
        );

        // Focus effect for password field
        mdpField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                mdpField.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-pref-width: 380;" +
                                "-fx-border-color: #00B4DB;" +
                                "-fx-border-radius: 15;" +
                                "-fx-border-width: 2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,180,219,0.2), 8, 0, 0, 0);"
                );
            } else {
                mdpField.setStyle(
                        "-fx-background-color: #f8fafc;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-pref-width: 380;" +
                                "-fx-border-color: #e2e8f0;" +
                                "-fx-border-radius: 15;" +
                                "-fx-border-width: 1;"
                );
            }
        });

        roleComboBox = new ComboBox<>();
        roleComboBox.setPromptText("Select Role");
        roleComboBox.getItems().addAll("admin", "recruteur", "client");
        roleComboBox.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 15;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: 380;" +
                        "-fx-padding: 5 0;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 1;"
        );

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setFont(Font.font("System", FontWeight.MEDIUM, 13));
        errorLabel.setStyle("-fx-padding: 5 0 0 5;");

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #00B4DB, #0083B0);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 14 60;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,180,219,0.4), 12, 0, 0, 4);"
        );

        // Hover effect for save button
        saveButton.setOnMouseEntered(e ->
                saveButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #0083B0, #00B4DB);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 60;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,180,219,0.6), 16, 0, 0, 6);" +
                                "-fx-scale-x: 1.02;" +
                                "-fx-scale-y: 1.02;"
                )
        );
        saveButton.setOnMouseExited(e ->
                saveButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, #00B4DB, #0083B0);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 60;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,180,219,0.4), 12, 0, 0, 4);"
                )
        );
        saveButton.setOnAction(e -> handleSave());

        Hyperlink cancelLink = new Hyperlink("Cancel");
        cancelLink.setStyle(
                "-fx-text-fill: #64748B;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: normal;" +
                        "-fx-border-color: transparent;"
        );
        cancelLink.setOnMouseEntered(e ->
                cancelLink.setStyle(
                        "-fx-text-fill: #0083B0;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-border-color: transparent;"
                )
        );
        cancelLink.setOnMouseExited(e ->
                cancelLink.setStyle(
                        "-fx-text-fill: #64748B;" +
                                "-fx-font-size: 14px;" +
                                "-fx-font-weight: normal;" +
                                "-fx-border-color: transparent;"
                )
        );
        cancelLink.setOnAction(e -> handleCancel());

        buttonBox.getChildren().addAll(saveButton, cancelLink);

        contentBox.getChildren().addAll(
                titleContainer,
                createFieldWithIcon("👤", nomField),
                createFieldWithIcon("👤", prenomField),
                createFieldWithIcon("✉️", emailField),
                createFieldWithIcon("🔒", mdpField),
                createFieldWithIcon("👔", roleComboBox),
                errorLabel,
                buttonBox
        );

        VBox wrapper = new VBox(topBar, contentBox);
        wrapper.setSpacing(10);
        root.getChildren().add(wrapper);

        // Center the content
        StackPane.setAlignment(wrapper, Pos.CENTER);
    }

    private TextField createTextField(String prompt, double width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 14 20;" +
                        "-fx-font-size: 14px;" +
                        "-fx-pref-width: " + width + ";" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-width: 1;"
        );

        // Focus effect
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-pref-width: " + width + ";" +
                                "-fx-border-color: #00B4DB;" +
                                "-fx-border-radius: 15;" +
                                "-fx-border-width: 2;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,180,219,0.2), 8, 0, 0, 0);"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: #f8fafc;" +
                                "-fx-background-radius: 15;" +
                                "-fx-padding: 14 20;" +
                                "-fx-font-size: 14px;" +
                                "-fx-pref-width: " + width + ";" +
                                "-fx-border-color: #e2e8f0;" +
                                "-fx-border-radius: 15;" +
                                "-fx-border-width: 1;"
                );
            }
        });

        return field;
    }

    private HBox createFieldWithIcon(String icon, Control field) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-min-width: 30px;" +
                        "-fx-alignment: center;"
        );

        container.getChildren().addAll(iconLabel, field);
        return container;
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
            errorLabel.setText("⚠️ Please fill in all fields");
            return;
        }
        if (!email.contains("@")) {
            errorLabel.setText("⚠️ Please enter a valid email");
            return;
        }

        try {
            if (!email.equals(user.getEmail()) && userService.emailExists(email)) {
                errorLabel.setText("⚠️ Email already exists");
                return;
            }

            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMdp(mdp);
            user.setRole(Role.valueOf(roleStr.toLowerCase()));   // "admin" | "recruteur" | "client"

            userService.updateUser(user);

            if (dashboardPage != null) dashboardPage.refreshTable();

            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();

        } catch (IllegalArgumentException e) {
            errorLabel.setText("⚠️ " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("⚠️ An error occurred. Please try again.");
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