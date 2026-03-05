package Controller.FrontOffice;

import Model.Candidature;
import Model.Notification;
import Model.User;
import Service.CandidatureService;
import Service.NotificationCService;
import Service.userservice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import Utils.NavigationManager;


public class MyApplicationsController {

    @FXML private VBox   applicationsContainer;
    @FXML private Label  countLabel;
    @FXML private Button notifBellBtn;
    @FXML private Label  notifBadge;
    @FXML private Button filterAll;
    @FXML private Button filterPending;
    @FXML private Button filterAccepted;
    @FXML private Button filterRejected;

    private BorderPane homeBorderPane;
    private final CandidatureService  candidatureService  = new CandidatureService();
    private final NotificationCService notificationService = new NotificationCService();

    private List<Candidature> allCandidatures;
    private String activeFilter = null;

    // Popup state
    private Popup  notifPopup;
    private boolean popupOpen = false;

    public void setHomeBorderPane(BorderPane borderPane) {
        this.homeBorderPane = borderPane;
    }

    @FXML
    public void initialize() {
        loadApplications();
        refreshNotifBadge();
    }

    // ── LOAD ──────────────────────────────────────────────────────────────────

    private void loadApplications() {
        User currentUser = new userservice().getCurrentUser();
        if (currentUser == null) { countLabel.setText("Not logged in"); return; }
        try {
            allCandidatures = candidatureService.getCandidaturesByUserId(currentUser.getId());
            updateFilterButtonStyles();
            renderCards();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── NOTIFICATIONS ────────────────────────────────────────────────────────

    private void refreshNotifBadge() {
        User user = new userservice().getCurrentUser();
        if (user == null || notifBadge == null) return;
        try {
            int unread = notificationService.countUnread(user.getId());
            if (unread > 0) {
                notifBadge.setText(String.valueOf(unread));
                notifBadge.setVisible(true);
                notifBadge.setManaged(true);
            } else {
                notifBadge.setVisible(false);
                notifBadge.setManaged(false);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleToggleNotifications() {
        if (popupOpen && notifPopup != null) {
            notifPopup.hide();
            popupOpen = false;
            return;
        }
        openNotificationsPopup();
    }

    private void openNotificationsPopup() {
        User user = new userservice().getCurrentUser();
        if (user == null) return;

        List<Notification> notifications;
        try {
            notifications = notificationService.getByUserId(user.getId());
            notificationService.markAllRead(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // ── Build popup content ───────────────────────────────────────────────
        VBox container = new VBox(0);
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20,0,0,6);"
        );
        container.setPrefWidth(360);
        container.setMaxHeight(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 16 16 0 0; -fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label("🔔 Notifications");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLbl = new Label(notifications.size() + " total");
        countLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        header.getChildren().addAll(title, spacer, countLbl);

        // Scrollable list
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scroll.setMaxHeight(360);

        VBox listBox = new VBox(0);

        if (notifications.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 30;");
            empty.setMaxWidth(Double.MAX_VALUE);
            empty.setAlignment(Pos.CENTER);
            listBox.getChildren().add(empty);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                VBox row = buildNotifRow(n);
                // subtle separator between items
                if (i < notifications.size() - 1) {
                    Rectangle sep = new Rectangle();
                    sep.setHeight(1);
                    sep.setFill(Color.web("#f1f5f9"));
                    sep.widthProperty().bind(listBox.widthProperty());
                    listBox.getChildren().addAll(row, sep);
                } else {
                    listBox.getChildren().add(row);
                }
            }
        }

        scroll.setContent(listBox);
        container.getChildren().addAll(header, scroll);

        // ── Show popup anchored below the bell ───────────────────────────────
        notifPopup = new Popup();
        notifPopup.setAutoHide(true);
        notifPopup.setAutoFix(true);
        notifPopup.getContent().add(container);
        notifPopup.setOnHidden(e -> popupOpen = false);

        javafx.geometry.Bounds bounds = notifBellBtn.localToScreen(notifBellBtn.getBoundsInLocal());
        double x = bounds.getMaxX() - 360;
        double y = bounds.getMaxY() + 8;
        notifPopup.show(notifBellBtn.getScene().getWindow(), x, y);
        popupOpen = true;

        // Refresh badge (now all read)
        refreshNotifBadge();
    }

    private VBox buildNotifRow(Notification n) {
        VBox row = new VBox(4);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setCursor(javafx.scene.Cursor.HAND);

        // Unread highlight
        String bgNormal = n.isRead() ? "white" : "#f0f4ff";
        row.setStyle("-fx-background-color: " + bgNormal + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fafc;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color: " + bgNormal + ";"));

        HBox topLine = new HBox(8);
        topLine.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(n.getIcon());
        icon.setStyle("-fx-font-size: 18px;");

        Label titleLbl = new Label(n.getTitle());
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(270);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        topLine.getChildren().addAll(icon, titleLbl);

        Label msgLbl = new Label(n.getMessage());
        msgLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        msgLbl.setWrapText(true);

        Label timeLbl = new Label(n.getFormattedTime());
        timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        row.getChildren().addAll(topLine, msgLbl, timeLbl);

        // Click action
        row.setOnMouseClicked(e -> handleNotificationClick(n));

        return row;
    }

    private void handleNotificationClick(Notification n) {
        if (notifPopup != null) notifPopup.hide();

        if ("accepted".equalsIgnoreCase(n.getType())) {
            openQuizPanel(n.getJobOfferId());
        }
    }

    /**
     * Embeds TakeQuizPanel inside the parent BorderPane center,
     * pre-filling the userId for the accepted candidate.
     */
    private void openQuizPanel(int jobOfferId) {
        if (homeBorderPane == null) return;

        ui.TakeQuizPanel quizPanel = new ui.TakeQuizPanel();

        // Pre-fill logged-in user id
        Model.User user = new Service.userservice().getCurrentUser();
        if (user != null) {
            quizPanel.userIdField.setText(String.valueOf(user.getId()));
        }

        // Back button returns to My Applications
        quizPanel.btnBack.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/FrontOffice/MyApplications.fxml"));
                javafx.scene.Parent myApps = loader.load();
                MyApplicationsController ctrl = loader.getController();
                ctrl.setHomeBorderPane(homeBorderPane);
                homeBorderPane.setCenter(myApps);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        homeBorderPane.setCenter(quizPanel);
    }

    // ── FILTER HANDLERS ───────────────────────────────────────────────────────

    @FXML private void handleFilterAll()      { setFilter(null); }
    @FXML private void handleFilterPending()  { setFilter("pending"); }
    @FXML private void handleFilterAccepted() { setFilter("accepted"); }
    @FXML private void handleFilterRejected() { setFilter("rejected"); }

    private void setFilter(String status) {
        activeFilter = status;
        updateFilterButtonStyles();
        renderCards();
    }

    private void updateFilterButtonStyles() {
        if (filterAll == null) return; // FXML might not have filter buttons yet
        filterAll.setStyle(inactiveStyle("#0fafdd", "rgba(15,175,221,0.08)"));
        filterPending.setStyle(inactiveStyle("#d97706", "#fef3c7"));
        filterAccepted.setStyle(inactiveStyle("#059669", "#d1fae5"));
        filterRejected.setStyle(inactiveStyle("#dc2626", "#fee2e2"));

        if (activeFilter == null)
            filterAll.setStyle(activeStyle("#0fafdd"));
        else switch (activeFilter) {
            case "pending"  -> filterPending.setStyle(activeStyle("#d97706"));
            case "accepted" -> filterAccepted.setStyle(activeStyle("#059669"));
            case "rejected" -> filterRejected.setStyle(activeStyle("#dc2626"));
        }
    }

    private String inactiveStyle(String textColor, String bgColor) {
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 7 20; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-opacity: 0.65;";
    }

    private String activeStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 7 20; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-opacity: 1.0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8,0,0,2);";
    }

    // ── RENDER ────────────────────────────────────────────────────────────────

    private void renderCards() {
        applicationsContainer.getChildren().clear();

        List<Candidature> visible = (activeFilter == null) ? allCandidatures :
                allCandidatures.stream()
                        .filter(c -> activeFilter.equalsIgnoreCase(c.getStatus()))
                        .collect(Collectors.toList());

        if (visible.isEmpty()) {
            String msg = activeFilter == null
                    ? "You have not submitted any applications yet."
                    : "No " + activeFilter + " applications.";
            Label empty = new Label(msg);
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b; -fx-padding: 30;");
            applicationsContainer.getChildren().add(empty);
        } else {
            for (Candidature c : visible)
                applicationsContainer.getChildren().add(createApplicationCard(c));
        }

        String countText = (activeFilter == null)
                ? allCandidatures.size() + " Applications"
                : visible.size() + " / " + allCandidatures.size() + " Applications";
        countLabel.setText(countText);
    }

    private VBox createApplicationCard(Candidature c) {
        VBox card = new VBox(15);
        String normal = "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,2);";
        String hover  = "-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #0fafdd; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(15,175,221,0.15), 15,0,0,5);";
        card.setStyle(normal);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(normal));

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label jobIcon  = new Label("📋");
        jobIcon.setStyle("-fx-font-size: 20px;");
        String jobTitle;
        try {
            jobTitle = candidatureService.getJobTitleById(c.getJobOfferId());
        } catch (SQLException ex) {
            jobTitle = "Job #" + c.getJobOfferId();
        }
        Label jobLabel = new Label(jobTitle);
        jobLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(c.getStatus());
        statusLabel.setStyle("-fx-padding: 5 15; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 13px;" + getStatusStyle(c.getStatus()));

        topRow.getChildren().addAll(jobIcon, jobLabel, spacer, statusLabel);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.setStyle("-fx-padding: 10 0 5 0;");

        HBox salaryBox = makeDetailBox("💰", "Expected Salary: " + c.getExpectedSalary(), "-fx-text-fill: #475569; -fx-font-size: 14px;");
        HBox dateBox   = makeDetailBox("📅", "Applied: " + c.getApplicationDate(),        "-fx-text-fill: #64748b; -fx-font-size: 13px;");
        grid.add(salaryBox, 0, 0);
        grid.add(dateBox,   1, 0);

        Separator sep = new Separator();

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 5 0 0 0;");

        Button updateBtn = createActionButton("✏️ Update", "#0fafdd");
        updateBtn.setOnAction(e -> handleUpdateApplication(c));

        Button cancelBtn = createActionButton("🗑️ Cancel", "#dc2626");
        cancelBtn.setOnAction(e -> handleCancelApplication(c));

        actions.getChildren().addAll(updateBtn, cancelBtn);
        card.getChildren().addAll(topRow, grid, sep, actions);
        return card;
    }

    private HBox makeDetailBox(String icon, String text, String textStyle) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px;");
        Label textLbl = new Label(text);
        textLbl.setStyle(textStyle);
        HBox box = new HBox(8, iconLbl, textLbl);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        String n = "-fx-background-color: white; -fx-text-fill: " + color + "; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 8 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String h = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 8 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e -> btn.setStyle(n));
        return btn;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        return switch (status.toLowerCase()) {
            case "pending"  -> "-fx-background-color: #fef3c7; -fx-text-fill: #d97706;";
            case "accepted" -> "-fx-background-color: #d1fae5; -fx-text-fill: #059669;";
            case "rejected" -> "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;";
            default         -> "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;";
        };
    }

    private void handleUpdateApplication(Candidature c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/UpdateCandidature.fxml"));
            Parent root = loader.load();
            UpdateCandidatureController ctrl = loader.getController();
            ctrl.setCandidature(c);
            Stage stage = new Stage();
            stage.setTitle("Update Application");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadApplications();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCancelApplication(Candidature c) {
        try {
            candidatureService.deleteCandidature(c.getId());
            loadApplications();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }
}