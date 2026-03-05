package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Service.JobOfferService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import Controller.BackOffice.joboffer.addJobOfferController;
import Model.Role;
import Model.User;
import Service.userservice;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class JobOfferController implements Initializable {
    @FXML private StackPane contentArea;
    @FXML private javafx.scene.layout.AnchorPane root;

    @FXML private Label dashboardNav;
    @FXML private Label allJobsNav;
    @FXML private Label myJobsNav;

    @FXML private Button addJobButton;

    private JobOfferService jobService = new JobOfferService();
    private User currentUser;   // loaded from session in initialize()
    private ObservableList<JobOffer> allJobOffers = FXCollections.observableArrayList();
    private FilteredList<JobOffer> filteredJobOffers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ✅ Load current user from session
        currentUser = new userservice().getCurrentUser();

        // Inject ambient glow behind all content (index 0 = bottommost layer)
        if (root != null) root.getChildren().add(0, ambientLayer(root));

        highlightNav(dashboardNav);
        loadPage("/BackOffice/JobDashboard.fxml");
        loadJobOffers();
        setupFilters();
        setupNavigationHover();
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load page: " + fxml);
        }
    }

    private void setupNavigationHover() {
        Label[] navItems = {dashboardNav, allJobsNav, myJobsNav};
        for (Label item : navItems) {
            if (item != null) {
                item.setOnMouseEntered(e -> {
                    if (!item.getStyle().contains("#2563eb")) {
                        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #2563eb; -fx-cursor: hand;");
                    }
                });
                item.setOnMouseExited(e -> {
                    if (!item.getStyle().contains("#2563eb")) {
                        item.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-cursor: hand;");
                    }
                });
            }
        }
    }

    @FXML
    private void handleAllJobs() {
        highlightNav(allJobsNav);
        loadJobList("/BackOffice/AllJobsContent.fxml", JobOfferListController.PageMode.ALL_JOBS);
    }

    @FXML
    private void handleMyJobs() {
        highlightNav(myJobsNav);
        loadJobList("/BackOffice/MyJobsContent.fxml", JobOfferListController.PageMode.MY_JOBS);
    }

    private void loadJobList(String fxmlPath, JobOfferListController.PageMode mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

            JobOfferListController listController = loader.getController();
            listController.setCurrentUser(currentUser);
            listController.setPageMode(mode);
            listController.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDashboard() {
        highlightNav(dashboardNav);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/JobDashboard.fxml"));
            Parent dashView = loader.load();
            contentArea.getChildren().setAll(dashView);
            // No need to access totalJobsLabel here
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Load Error", "Could not load Dashboard.");
        }
    }


    @FXML
    private void handleProfile() {
        showInfo("Profile", "Profile page coming soon!");
    }

    @FXML
    private void handleAddJob() {
        // ✅ Only admin and recruteur can add jobs
        if (currentUser == null ||
                (currentUser.getRole() != Role.admin && currentUser.getRole() != Role.recruteur)) {
            showInfo("Access Denied", "Only admins and recruiters can add job offers.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/addjoboffer.fxml"));
            Parent addView = loader.load();

            addJobOfferController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);  // ✅ auto-fills recruiter from session

            Stage stage = new Stage();
            stage.setTitle("Add New Job Offer");
            stage.setScene(new Scene(addView));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            // ✅ Refresh the list after adding (if currently on AllJobs view)
            stage.setOnHidden(e -> handleMyJobs());  // refresh My Jobs after adding
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadJobOffers() {
        try {
            allJobOffers = FXCollections.observableArrayList(jobService.getJobOffers());
            filteredJobOffers = new FilteredList<>(allJobOffers, p -> true);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load job offers from database.");
        }
    }

    private void setupFilters() {
        // Initialize filters if needed
    }

    private String getJobLevel(JobOffer offer) {
        String[] levels = {"Entry Level", "Junior", "Mid Level", "Senior"};
        return levels[offer.getId() % levels.length];
    }

    private VBox createJobCard(JobOffer offer) {
        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,4); " +
                "-fx-border-color: #eef2ff; -fx-border-width: 1; -fx-border-radius: 16;");

        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                openJobOfferDetails(offer);
            }
        });

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15,0,0,6); " +
                        "-fx-border-color: #2563eb; -fx-border-width: 1; -fx-border-radius: 16; -fx-scale-x: 1.02; -fx-scale-y: 1.02;")
        );

        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10,0,0,4); " +
                        "-fx-border-color: #eef2ff; -fx-border-width: 1; -fx-border-radius: 16; -fx-scale-x: 1; -fx-scale-y: 1;")
        );

        Rectangle accent = new Rectangle(4, 30);
        String color = getAccentColor(offer.getId());
        accent.setFill(javafx.scene.paint.Color.web(color));
        accent.setArcWidth(4);
        accent.setArcHeight(4);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(accent, titleLabel);

        Label descriptionLabel = new Label(truncateDescription(offer.getDescription(), 100));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        HBox postedByBox = new HBox(5);
        postedByBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(3);
        dot.setFill(javafx.scene.paint.Color.web(color));

        Label postedByLabel = new Label("Posted by");
        postedByLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Label creatorLabel = new Label(getRecruiterName(offer.getRecruiterId()));
        creatorLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 12px;");

        postedByBox.getChildren().addAll(dot, postedByLabel, creatorLabel);

        FlowPane tagsContainer = new FlowPane(8, 8);
        tagsContainer.setStyle("-fx-padding: 5 0 0 0;");

        if (offer.getRequiredSkills() != null) {
            String[] skills = offer.getRequiredSkills().split(",");
            for (String skill : skills) {
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    Label tag = new Label(trimmedSkill);
                    tag.setStyle("-fx-background-color: #e0f2fe; -fx-padding: 4 10; -fx-background-radius: 20; " +
                            "-fx-font-size: 11px; -fx-text-fill: #0369a1;");
                    tagsContainer.getChildren().add(tag);
                }
            }
        }

        card.getChildren().addAll(titleBox, descriptionLabel, postedByBox, tagsContainer);
        return card;
    }

    private String getAccentColor(int id) {
        String[] colors = {"#2563eb", "#7e22ce", "#059669", "#d97706", "#dc2626", "#0891b2"};
        return colors[id % colors.length];
    }

    private String getRecruiterName(int recruiterId) {
        String[] names = {"Mark Lee", "Jung Jaehyun", "Kim Taeyeong", "Sarah Chen", "Mike Johnson", "Emily Brown"};
        return names[recruiterId % names.length];
    }

    private String getJobStatus(JobOffer offer) {
        if (offer.getId() % 3 == 0) {
            return "Active";
        } else if (offer.getId() % 3 == 1) {
            return "Pending";
        } else {
            return "Closed";
        }
    }

    private String truncateDescription(String description, int maxLength) {
        if (description == null || description.length() <= maxLength) {
            return description != null ? description : "";
        }
        return description.substring(0, maxLength) + "...";
    }

    private void openJobOfferDetails(JobOffer offer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/JobOfferDetails.fxml"));
            Parent detailsView = loader.load();

            JobOfferDetailsController controller = loader.getController();
            controller.setJobOffer(offer);

            Stage stage = new Stage();
            stage.setTitle("Job Offer Details - " + offer.getTitle());
            stage.setScene(new Scene(detailsView));
            stage.setMinWidth(720);
            stage.setMinHeight(580);
            stage.setWidth(880);
            stage.setHeight(780);
            stage.setResizable(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open job offer details.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void highlightNav(Label selected) {
        Label[] navItems = {dashboardNav, allJobsNav, myJobsNav};

        for (Label label : navItems) {
            if (label == selected) {
                label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-cursor: hand;");
            } else {
                label.setStyle("-fx-font-size: 14px; -fx-font-weight: normal; -fx-text-fill: #64748b; -fx-cursor: hand;");
            }
        }
    }



    // ── Ambient glow — identical pattern to LeaderboardPanel ─────────────────
    private javafx.scene.layout.Pane ambientLayer(javafx.scene.layout.Region host) {
        javafx.scene.layout.Pane p = new javafx.scene.layout.Pane();
        p.setMouseTransparent(true);
        p.setPickOnBounds(false);
        // Clip so glows never bleed outside this panel and never block sidebar buttons
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(host.widthProperty());
        clip.heightProperty().bind(host.heightProperty());
        p.setClip(clip);
        p.getChildren().addAll(
                glow(260, "#6366f1", 0.15,  60,  60),
                glow(220, "#06b6d4", 0.12, 900,  80),
                glow(180, "#10b981", 0.09, 180, 650)
        );
        return p;
    }

    private javafx.scene.shape.Circle glow(double r, String hex, double opacity, double x, double y) {
        javafx.scene.shape.Circle c = new javafx.scene.shape.Circle(r, javafx.scene.paint.Color.web(hex));
        c.setOpacity(opacity);
        c.setEffect(new javafx.scene.effect.GaussianBlur(90));
        c.setLayoutX(x);
        c.setLayoutY(y);
        return c;
    }
}