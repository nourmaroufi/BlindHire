package Controller.BackOffice.joboffer;

import Model.JobOffer;
import Model.User;
import Service.JobOfferService;
import java.util.stream.Collectors;
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

    /** MY_JOBS = show only the logged-in recruiter's jobs; ALL_JOBS = show everything (admin). */
    public enum PageMode { MY_JOBS, ALL_JOBS }

    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;

    @FXML private MenuButton timeFilter;
    @FXML private MenuButton typeFilter;
    private String selectedTime = "All";
    private String selectedType = "All";
    private JobOfferService jobService = new JobOfferService();
    private ObservableList<JobOffer> allJobOffers = FXCollections.observableArrayList();
    private FilteredList<JobOffer> filteredJobOffers;

    // ── injected by JobOfferController after loader.load() ───────────────────
    private User currentUser;
    private PageMode pageMode = PageMode.ALL_JOBS;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setPageMode(PageMode mode) {
        this.pageMode = mode;
    }

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
            java.util.List<JobOffer> jobs = jobService.getJobOffers();

            // MY_JOBS → only show jobs belonging to the logged-in recruiter/admin
            if (pageMode == PageMode.MY_JOBS && currentUser != null) {
                jobs = jobs.stream()
                        .filter(j -> j.getRecruiterId() == currentUser.getId())
                        .collect(java.util.stream.Collectors.toList());
            }

            allJobOffers = FXCollections.observableArrayList(jobs);
            filteredJobOffers = new FilteredList<>(allJobOffers, p -> true);
            displayJobCards(filteredJobOffers);
        } catch (SQLException e) {
            e.printStackTrace(); // log to console only — no popup on refresh
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
        VBox card = new VBox(14);
        card.setPrefWidth(280);
        card.setPadding(new javafx.geometry.Insets(22));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 18; " +
                "-fx-border-color: rgba(255,255,255,0.09); -fx-border-width: 1; -fx-border-radius: 18;");

        String accentColor = accentColors[offer.getId() % accentColors.length];

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: rgba(99,102,241,0.13); -fx-background-radius: 18; " +
                        "-fx-border-color: rgba(99,102,241,0.50); -fx-border-width: 1; -fx-border-radius: 18;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 18; " +
                        "-fx-border-color: rgba(255,255,255,0.09); -fx-border-width: 1; -fx-border-radius: 18;")
        );

        // Coloured top accent bar
        Rectangle accent = new Rectangle(36, 4);
        accent.setFill(Color.web(accentColor));
        accent.setArcWidth(4); accent.setArcHeight(4);
        accent.setEffect(new javafx.scene.effect.DropShadow(5, Color.web(accentColor + "80")));

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-font-family: 'Segoe UI';");
        titleLabel.setWrapText(true);

        HBox recruiterBox = new HBox(6);
        recruiterBox.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4); dot.setFill(Color.web(accentColor));
        Label recruiterLabel = new Label("Recruiter ID: " + offer.getRecruiterId());
        recruiterLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.38); -fx-font-size: 12px; " +
                "-fx-font-family: 'Segoe UI';");
        recruiterBox.getChildren().addAll(dot, recruiterLabel);

        String description = offer.getDescription();
        if (description.length() > 100) description = description.substring(0, 97) + "...";
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 12px; " +
                "-fx-line-spacing: 2; -fx-font-family: 'Segoe UI';");

        FlowPane skillsContainer = new FlowPane(8, 8);
        if (offer.getRequiredSkills() != null) {
            String[] skills = offer.getRequiredSkills().split(",");
            int skillCount = 0;
            for (String skill : skills) {
                if (skillCount >= 3) {
                    Label moreTag = new Label("+" + (skills.length - 3) + " more");
                    moreTag.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 3 10; " +
                            "-fx-background-radius: 20; -fx-font-size: 10px; " +
                            "-fx-text-fill: rgba(255,255,255,0.40); -fx-font-weight: 500;");
                    skillsContainer.getChildren().add(moreTag);
                    break;
                }
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    Label skillTag = new Label(trimmedSkill);
                    skillTag.setStyle("-fx-background-color: " + accentColor + "22; -fx-padding: 3 10; " +
                            "-fx-background-radius: 20; -fx-font-size: 10px; " +
                            "-fx-text-fill: " + accentColor + "; -fx-font-weight: 500;");
                    skillsContainer.getChildren().add(skillTag);
                    skillCount++;
                }
            }
        }

        // Status badge
        String statusText = offer.getStatus() != null ? offer.getStatus() : "Active";
        String statusColor = switch (statusText.toLowerCase()) {
            case "active"  -> "#10b981";
            case "pending" -> "#f59e0b";
            case "closed"  -> "#f43f5e";
            default        -> "#6b7280";
        };
        Label statusBadge = new Label(statusText.toUpperCase());
        statusBadge.setStyle("-fx-background-color: " + statusColor + "22; -fx-text-fill: " + statusColor + "; " +
                "-fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;");

        Label typeBadge = new Label("📌  " + (offer.getType() != null ? offer.getType() : "Full-time"));
        typeBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.38); " +
                "-fx-font-family: 'Segoe UI';");

        HBox badgesContainer = new HBox(10);
        badgesContainer.setAlignment(Pos.CENTER_LEFT);
        badgesContainer.getChildren().addAll(statusBadge, typeBadge);

        // CTA hint
        Label cta = new Label("View details →");
        cta.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #818cf8; " +
                "-fx-font-family: 'Segoe UI';");

        card.getChildren().addAll(accent, titleLabel, recruiterBox, descriptionLabel,
                skillsContainer, badgesContainer, cta);

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
            controller.setContext(currentUser, pageMode); // ← must be before setJobOffer
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
        VBox empty = new VBox(14);
        empty.setAlignment(Pos.CENTER);
        empty.setPrefWidth(320);
        empty.setStyle("-fx-padding: 60;");

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label messageLabel = new Label("No job offers found");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-font-family: 'Segoe UI';");

        Label subMessageLabel = new Label("Try adjusting your search or add a new job offer");
        subMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.35); " +
                "-fx-font-family: 'Segoe UI';");

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