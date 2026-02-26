package ui;

import Model.Role;
import Model.User;
import Service.userservice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Optional;

public class DashboardPage {

    private BorderPane root;
    private TableView<User> usersTable;
    private Text recrutersCountLabel;
    private Text clientsCountLabel;
    private Text adminsCountLabel;
    private Button showAllBtn;
    private Button showAdminsBtn;
    private Button showRecrutersBtn;
    private Button showClientsBtn;
    private userservice userService;
    private ObservableList<User> usersList;

    public DashboardPage() {
        userService = new userservice();
        usersList = FXCollections.observableArrayList();
        createUI();
        showAllUsers();
        updateCounts();
    }

    private void createUI() {
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());
    }

    // ─── SIDEBAR ──────────────────────────────────────────────────────────────

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle("-fx-background-color: #1a2332; -fx-pref-width: 210;");

        VBox logoBox = new VBox(5);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(30, 20, 30, 20));
        Text logoText = new Text("BlindHire");
        logoText.setFill(Color.web("#4A9DB5"));
        logoText.setFont(Font.font("System", FontWeight.BOLD, 24));
        logoBox.getChildren().add(logoText);

        VBox menuBox = new VBox(0);
        VBox.setVgrow(menuBox, Priority.ALWAYS);

        Button dashboardBtn = createMenuButton("Dashboard", true);
        dashboardBtn.setOnAction(e -> showAllUsers());

        Button jobOffersBtn = createMenuButton("Job Offers", false);
        jobOffersBtn.setOnAction(e -> handleJobOffers());

        Button interviewsBtn = createMenuButton("Interviews", false);
        interviewsBtn.setOnAction(e -> handleInterviews());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createMenuButton("Logout", false);
        logoutBtn.setOnAction(e -> handleLogout());

        menuBox.getChildren().addAll(dashboardBtn, jobOffersBtn, interviewsBtn, spacer, logoutBtn);
        sidebar.getChildren().addAll(logoBox, menuBox);
        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + (active ? "#4A9DB5" : "transparent") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-alignment: center-left;" +
                        "-fx-padding: 15 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 0;" +
                        "-fx-pref-width: 210;"
        );
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    // ─── MAIN CONTENT ─────────────────────────────────────────────────────────

    private VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setStyle("-fx-background-color: #f5f5f5;");
        mainContent.setPadding(new Insets(30));

        Text headerText = new Text("Dashboard Overview");
        headerText.setFill(Color.web("#2C3E50"));
        headerText.setFont(Font.font("System", FontWeight.BOLD, 32));

        // ── Stats Cards (3 roles) ──
        HBox statsBox = new HBox(20);

        VBox adminsCard = createStatsCard("Admins");
        adminsCountLabel = (Text) adminsCard.getChildren().get(1);

        VBox recrutersCard = createStatsCard("Recruiters");
        recrutersCountLabel = (Text) recrutersCard.getChildren().get(1);

        VBox clientsCard = createStatsCard("Clients");
        clientsCountLabel = (Text) clientsCard.getChildren().get(1);

        statsBox.getChildren().addAll(adminsCard, recrutersCard, clientsCard);

        // ── Filter Buttons ──
        HBox filterBox = new HBox(15);

        showAllBtn = createFilterButton("All Users", true);
        showAllBtn.setOnAction(e -> showAllUsers());

        showAdminsBtn = createFilterButton("Show Admins", false);
        showAdminsBtn.setOnAction(e -> showAdmins());

        showRecrutersBtn = createFilterButton("Show Recruiters", false);
        showRecrutersBtn.setOnAction(e -> showRecruters());

        showClientsBtn = createFilterButton("Show Clients", false);
        showClientsBtn.setOnAction(e -> showClients());

        // ── Add User Button ──
        Button addUserBtn = new Button("+ Add User");
        addUserBtn.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );
        addUserBtn.setOnAction(e -> handleAddUser());

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterBox.getChildren().addAll(showAllBtn, showAdminsBtn, showRecrutersBtn, showClientsBtn, filterSpacer, addUserBtn);

        usersTable = createUsersTable();

        mainContent.getChildren().addAll(headerText, statsBox, filterBox, usersTable);
        return mainContent;
    }

    private VBox createStatsCard(String title) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: #d3d3d3;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 30;" +
                        "-fx-pref-width: 190;" +
                        "-fx-pref-height: 100;"
        );
        Text titleText = new Text(title);
        titleText.setFill(Color.web("#6c6c6c"));
        titleText.setFont(Font.font(16));

        Text countText = new Text("0");
        countText.setFill(Color.web("#6c6c6c"));
        countText.setFont(Font.font(14));

        card.getChildren().addAll(titleText, countText);
        return card;
    }

    private Button createFilterButton(String text, boolean active) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + (active ? "#4A9DB5" : "#6c6c6c") + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    // ─── TABLE ────────────────────────────────────────────────────────────────

    private TableView<User> createUsersTable() {
        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(100);

        TableColumn<User, String> prenomCol = new TableColumn<>("Prenom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomCol.setPrefWidth(100);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<User, String> mdpCol = new TableColumn<>("Password");
        mdpCol.setCellValueFactory(new PropertyValueFactory<>("mdp"));
        mdpCol.setPrefWidth(100);

        TableColumn<User, Role> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(90);

        TableColumn<User, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(70);
        editCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                btn.setStyle("-fx-background-color: #4A9DB5; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                btn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<User, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(80);
        deleteCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 10;");
                btn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, mdpCol, roleCol, editCol, deleteCol);
        return table;
    }

    // ─── DATA LOADING ─────────────────────────────────────────────────────────

    private void showAllUsers() {
        loadUsers(userService.getAllUsers());
        updateButtonStyles(showAllBtn);
        updateCounts();
    }

    private void showAdmins() {
        loadUsers(userService.getAdmins());
        updateButtonStyles(showAdminsBtn);
    }

    private void showRecruters() {
        loadUsers(userService.getRecruiters());
        updateButtonStyles(showRecrutersBtn);
    }

    private void showClients() {
        loadUsers(userService.getClients());
        updateButtonStyles(showClientsBtn);
    }

    private void loadUsers(java.util.List<User> users) {
        try {
            usersList.clear();
            usersList.addAll(users);
            usersTable.setItems(usersList);
        } catch (Exception e) {
            showError("Error loading users: " + e.getMessage());
        }
    }

    private void updateCounts() {
        try {
            adminsCountLabel.setText(userService.getAdminCount() + " admin(s)");
            recrutersCountLabel.setText(userService.getRecruiterCount() + " recruiter(s)");
            clientsCountLabel.setText(userService.getClientCount() + " client(s)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStyles(Button activeButton) {
        String inactive = "-fx-background-color: #6c6c6c; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;";
        String active   = "-fx-background-color: #4A9DB5; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 5; -fx-padding: 8 20; -fx-cursor: hand;";
        showAllBtn.setStyle(inactive);
        showAdminsBtn.setStyle(inactive);
        showRecrutersBtn.setStyle(inactive);
        showClientsBtn.setStyle(inactive);
        activeButton.setStyle(active);
    }

    // ─── HANDLERS ─────────────────────────────────────────────────────────────

    /** Open EditPage in a new window to edit an existing user. */
    private void handleEdit(User user) {
        EditPage editPage = new EditPage(user, this);
        Stage stage = new Stage();
        stage.setTitle("Edit User");
        stage.setScene(new Scene(editPage.getRoot(), 500, 480));
        stage.show();
    }

    /** Confirm then delete a user. */
    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getNom() + " " + user.getPrenom() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(user.getId());
                showAllUsers();
            } catch (Exception e) {
                showError("Error deleting user: " + e.getMessage());
            }
        }
    }

    /** Open AddUserPage in a new window to create a new user. */
    private void handleAddUser() {
        AddUserPage addPage = new AddUserPage(this);
        Stage stage = new Stage();
        stage.setTitle("Add New User");
        stage.setScene(new Scene(addPage.getRoot(), 500, 500));
        stage.show();
    }

    private void handleJobOffers() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Job Offers");
        alert.setContentText("Job Offers feature coming soon!");
        alert.showAndWait();
    }

    private void handleInterviews() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Interviews");
        alert.setContentText("Interviews feature coming soon!");
        alert.showAndWait();
    }

    private void handleLogout() {
        userService.setCurrentUser(null);
        BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshTable() {
        showAllUsers();
    }

    public Parent getRoot() {
        return root;
    }
}