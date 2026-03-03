package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Service.JobOfferService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class JobOfferListController {

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;


    @FXML private MenuButton timeFilter;
    @FXML private MenuButton typeFilter;
    private String selectedTime = "All";
    private String selectedType = "All";
    private JobOfferService jobService = new JobOfferService();
    private ObservableList<JobOffer> allJobOffers = FXCollections.observableArrayList();
    private FilteredList<JobOffer> filteredJobOffers;

    private final String[] accentColors = {
            "#2563eb", "#7c3aed", "#db2777", "#ea580c", "#059669", "#0284c7",
            "#9333ea", "#e11d48", "#65a30d", "#0d9488", "#4f46e5", "#b45309"
    };

    @FXML
    public void initialize() {
        loadJobOffers();
        setupSearch();
        if (cardsContainer != null) {
            cardsContainer.setAlignment(Pos.CENTER);
        }
        setupFilters();
    }
    private void setupFilters() {

        addMenuItem(timeFilter, "All", () -> selectedTime = "All");
        addMenuItem(timeFilter, "Today", () -> selectedTime = "Today");
        addMenuItem(timeFilter, "Last 7 days", () -> selectedTime = "Week");
        addMenuItem(timeFilter, "Last 30 days", () -> selectedTime = "Month");


        addMenuItem(typeFilter, "All", () -> selectedType = "All");
        addMenuItem(typeFilter, "Full-time", () -> selectedType = "Full-time");
        addMenuItem(typeFilter, "Part-time", () -> selectedType = "Part-time");
        addMenuItem(typeFilter, "Remote", () -> selectedType = "Remote");
    }
    private void addMenuItem(MenuButton menu, String text, Runnable action) {

        MenuItem item = new MenuItem(text);

        item.setOnAction(e -> {
            menu.setText(text + " ▼");
            action.run();
            applyFilters();
        });

        menu.getItems().add(item);
    }
    private void applyFilters() {

        String searchText = searchField.getText() == null
                ? ""
                : searchField.getText().toLowerCase();

        filteredJobOffers.setPredicate(job -> {

            // ---------- SEARCH ----------
            boolean matchesSearch =
                    searchText.isEmpty()
                            || job.getTitle().toLowerCase().contains(searchText)
                            || job.getDescription().toLowerCase().contains(searchText)
                            || (job.getRequiredSkills() != null &&
                            job.getRequiredSkills().toLowerCase().contains(searchText));

            // ---------- TYPE ----------
            boolean matchesType =
                    selectedType.equals("All")
                            || (job.getType() != null &&
                            job.getType().equalsIgnoreCase(selectedType));



            // ---------- TIME ----------
            boolean matchesTime = true;

            if (!selectedTime.equals("All") && job.getPostingDate() != null) {

                LocalDate today = LocalDate.now();

                switch (selectedTime) {
                    case "Today" ->
                            matchesTime = job.getPostingDate().isEqual(today);

                    case "Week" ->
                            matchesTime = job.getPostingDate()
                                    .isAfter(today.minusDays(7));

                    case "Month" ->
                            matchesTime = job.getPostingDate()
                                    .isAfter(today.minusDays(30));
                }
            }

            return matchesSearch && matchesType && matchesTime;
        });

        displayJobCards(FXCollections.observableArrayList(filteredJobOffers));
    }

    private void loadJobOffers() {
        try {
            allJobOffers = FXCollections.observableArrayList(jobService.getJobOffers());
            filteredJobOffers = new FilteredList<>(allJobOffers, p -> true);
            displayJobCards(filteredJobOffers);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not load job offers from database.");
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredJobOffers.setPredicate(job -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String filter = newVal.toLowerCase();
                    return job.getTitle().toLowerCase().contains(filter) ||
                            job.getDescription().toLowerCase().contains(filter) ||
                            (job.getRequiredSkills() != null && job.getRequiredSkills().toLowerCase().contains(filter));
                });
                displayJobCards(FXCollections.observableArrayList(filteredJobOffers));
            });
        }
    }

    private void displayJobCards(ObservableList<JobOffer> offers) {
        cardsContainer.getChildren().clear();
        if (offers.isEmpty()) {
            showEmptyState();
            return;
        }
        for (JobOffer offer : offers) {
            VBox card = createJobCard(offer);
            cardsContainer.getChildren().add(card);
        }

        int remainder = offers.size() % 3;
        if (remainder != 0) {
            for (int i = 0; i < 3 - remainder; i++) {
                VBox placeholder = new VBox();
                placeholder.setPrefWidth(280);
                placeholder.setVisible(false);
                cardsContainer.getChildren().add(placeholder);
            }
        }
    }

    private VBox createJobCard(JobOffer offer) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 16;");

        String accentColor = accentColors[offer.getId() % accentColors.length];

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.15), 15, 0, 0, 8); " +
                    "-fx-border-color: " + accentColor + "; -fx-border-width: 2; -fx-border-radius: 16; " +
                    "-fx-scale-x: 1.02; -fx-scale-y: 1.02; -fx-transition: all 0.2s ease;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                    "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 16; " +
                    "-fx-scale-x: 1; -fx-scale-y: 1;");
        });

        Rectangle accent = new Rectangle(4, 40);
        accent.setFill(Color.web(accentColor));
        accent.setArcWidth(4);
        accent.setArcHeight(4);
        accent.setEffect(new javafx.scene.effect.DropShadow(5, Color.web(accentColor + "80")));

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);

        HBox titleBox = new HBox(12, accent, titleLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox recruiterBox = new HBox(6);
        recruiterBox.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(4);
        dot.setFill(Color.web(accentColor));

        Label recruiterLabel = new Label("Recruiter ID: " + offer.getRecruiterId());
        recruiterLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px; -fx-font-weight: 500;");

        recruiterBox.getChildren().addAll(dot, recruiterLabel);

        String description = offer.getDescription();
        if (description.length() > 100) description = description.substring(0, 97) + "...";
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-line-spacing: 2;");

        FlowPane skillsContainer = new FlowPane(8, 8);
        skillsContainer.setStyle("-fx-padding: 10 0 5 0;");

        if (offer.getRequiredSkills() != null) {
            String[] skills = offer.getRequiredSkills().split(",");
            int skillCount = 0;
            for (String skill : skills) {
                if (skillCount >= 3) {
                    Label moreTag = new Label("+" + (skills.length - 3) + " more");
                    moreTag.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 4 12; -fx-background-radius: 20; " +
                            "-fx-font-size: 11px; -fx-text-fill: #475569; -fx-font-weight: 500;");
                    skillsContainer.getChildren().add(moreTag);
                    break;
                }

                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    Label skillTag = new Label(trimmedSkill);
                    skillTag.setStyle("-fx-background-color: " + accentColor + "15; -fx-padding: 4 12; -fx-background-radius: 20; " +
                            "-fx-font-size: 11px; -fx-text-fill: " + accentColor + "; -fx-font-weight: 500;");
                    skillsContainer.getChildren().add(skillTag);
                    skillCount++;
                }
            }
        }

        Label statusBadge = new Label(offer.getStatus() != null ? offer.getStatus() : "Active");
        String statusColor = switch (statusBadge.getText().toLowerCase()) {
            case "active" -> "#10b981";
            case "pending" -> "#f59e0b";
            case "closed" -> "#ef4444";
            default -> "#6b7280";
        };
        statusBadge.setStyle("-fx-background-color: " + statusColor + "15; -fx-text-fill: " + statusColor + "; " +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: 600;");

        Label typeBadge = new Label(offer.getType() != null ? offer.getType() : "Full-time");
        typeBadge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; " +
                "-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: 500;");

        HBox badgesContainer = new HBox(8);
        badgesContainer.setAlignment(Pos.CENTER_LEFT);
        badgesContainer.getChildren().addAll(statusBadge, typeBadge);

        Label dateLabel = new Label();
        if (offer.getPostingDate() != null) dateLabel.setText("Posted: " + offer.getPostingDate().toString());
        else dateLabel.setText("Posted recently");
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        card.getChildren().addAll(titleBox, recruiterBox, descriptionLabel, skillsContainer, badgesContainer, dateLabel);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) openJobOfferDetails(offer);
        });

        card.setCursor(javafx.scene.Cursor.HAND);
        return card;
    }

    private void openJobOfferDetails(JobOffer offer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BackOffice/JobOfferDetails.fxml"));
            Parent root = loader.load();

            JobOfferDetailsController controller = loader.getController();
            controller.setJobOffer(offer);

            Stage stage = new Stage();
            stage.setTitle("Job Offer Details - " + offer.getTitle());
            stage.setScene(new Scene(root));
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.setOnHidden(e -> refresh());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open job offer details.");
        }
    }

    private void showEmptyState() {
        VBox empty = new VBox(15);
        empty.setAlignment(Pos.CENTER);
        empty.setPrefWidth(280);
        empty.setStyle("-fx-padding: 40;");

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label messageLabel = new Label("No job offers found");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        Label subMessageLabel = new Label("Try adjusting your search or add a new job offer");
        subMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");

        empty.getChildren().addAll(iconLabel, messageLabel, subMessageLabel);
        cardsContainer.getChildren().add(empty);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void refresh() {
        loadJobOffers();
    }
}