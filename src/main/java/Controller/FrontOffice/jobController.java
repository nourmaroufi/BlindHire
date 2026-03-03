package Controller.FrontOffice;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import Service.candidatService;
import Model.Candidat;
import Model.JobOffer;
import Service.JobOfferService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Region;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class jobController {

    @FXML
    private HBox cardsContainer;

    @FXML
    private HBox pageIndicator;

    @FXML
    private TextField searchField;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label jobCountLabel;

    private JobOfferService jobService;
    private List<JobOffer> allJobs = new ArrayList<>();
    private List<JobOffer> filteredJobs = new ArrayList<>();
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 3;
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }



    private void openJobDetails(JobOffer job) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/FrontOffice/JobDetails.fxml"));




            VBox detailsView = loader.load();

            JobDetailsController controller = loader.getController();
            controller.setJob(job);
            controller.setHomeController(homeController);

            // replace center content (same pattern you use elsewhere)
            homeController.getContentArea().getChildren().setAll(detailsView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        jobService = new JobOfferService();
        loadJobsFromDatabase();
        prevButton.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10,0,0,2);");
        nextButton.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 25; -fx-border-color: #e2e8f0; -fx-border-radius: 25; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10,0,0,2);");
    }

    private void loadJobsFromDatabase() {
        try {
            allJobs = jobService.getJobOffers();
            filteredJobs = new ArrayList<>(allJobs);
            updateDisplay();
            updateJobCount();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load jobs from database");
        }
    }

    private void updateDisplay() {
        cardsContainer.getChildren().clear();
        pageIndicator.getChildren().clear();

        int totalPages = (int) Math.ceil((double) filteredJobs.size() / CARDS_PER_PAGE);

        if (filteredJobs.isEmpty()) {
            showEmptyState();
            return;
        }

        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, filteredJobs.size());

        for (int i = start; i < end; i++) {
            VBox card = createJobCard(filteredJobs.get(i));

            cardsContainer.getChildren().add(card);
        }

        for (int i = 0; i < totalPages; i++) {
            Circle dot = new Circle(5);

            if (i == currentPage) {
                dot.setFill(Color.web("#4f46e5"));
                dot.setEffect(createGlowEffect());
            } else {
                dot.setFill(Color.web("#cbd5e1"));
            }

            final int pageIndex = i;
            dot.setOnMouseClicked(e -> goToPage(pageIndex));
            pageIndicator.getChildren().add(dot);
        }

        updateNavigationButtons();
    }

    private DropShadow createGlowEffect() {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#4f46e580"));
        glow.setRadius(10);
        glow.setSpread(0.3);
        glow.setBlurType(BlurType.GAUSSIAN);
        return glow;
    }

    private VBox createJobCard(JobOffer job) {
        VBox card = new VBox(15);
        card.setPrefWidth(400);
        card.setMaxWidth(400);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("""
            -fx-background-color: white;
            -fx-padding: 25;
            -fx-background-radius: 20;
            -fx-border-radius: 20;
            -fx-border-color: #e2e8f0;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15,0,0,5);
        """);
        card.setOnMouseClicked(e -> openJobDetails(job));

        applyHoverEffect(card,
                """
                -fx-background-color: white;
                -fx-padding: 25;
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: #4f46e5;
                -fx-border-width: 2;
                -fx-effect: dropshadow(gaussian, rgba(79,70,229,0.2), 20,0,0,8);
                """,
                """
                -fx-background-color: white;
                -fx-padding: 25;
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: #e2e8f0;
                -fx-border-width: 1;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15,0,0,5);
                """
        );

        HBox typeBadge = new HBox(5);
        typeBadge.setAlignment(Pos.CENTER_LEFT);

        Circle badgeDot = new Circle(4);
        badgeDot.setFill(getTypeColor(job.getType()));

        Label typeLabel = new Label(job.getType());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + getTypeTextColor(job.getType()) + ";");

        typeBadge.getChildren().addAll(badgeDot, typeLabel);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label titleIcon = new Label("📋");
        titleIcon.setStyle("-fx-font-size: 24px;");

        Label title = new Label(job.getTitle());
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        titleBox.getChildren().addAll(titleIcon, title);

        Label description = new Label(truncateText(job.getDescription(), 120));
        description.setTooltip(new javafx.scene.control.Tooltip(job.getDescription()));
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-line-spacing: 5;");

        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusIcon = new Label(getStatusIcon(job.getStatus()));
        statusIcon.setStyle("-fx-font-size: 14px;");

        Label status = new Label(job.getStatus());
        status.setStyle("-fx-text-fill: " + getStatusColor(job.getStatus()) + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        statusBox.getChildren().addAll(statusIcon, status);

        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 14px;");

        String dateText = job.getPostingDate() != null
                ? job.getPostingDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "Date not specified";

        Label date = new Label(dateText);
        date.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        dateBox.getChildren().addAll(dateIcon, date);

        Button applyBtn = new Button("Apply Now →");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #4f46e5, #6366f1);
            -fx-text-fill: white;
            -fx-background-radius: 15;
            -fx-padding: 12 20;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            -fx-border-width: 0;
        """);

        applyBtn.setOnAction(e -> openApplicationForm(job));

        Region separator = new Region();
        separator.setStyle("-fx-background-color: #e2e8f0; -fx-min-height: 1; -fx-max-height: 1;");
        VBox.setMargin(separator, new javafx.geometry.Insets(5, 0, 5, 0));

        card.getChildren().addAll(typeBadge, titleBox, description, separator, statusBox, dateBox, applyBtn);

        return card;
    }

    private void openApplicationForm(JobOffer job) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/FrontOffice/CandidatureForm.fxml"));

            ScrollPane form = loader.load(); // ✅ CORRECT TYPE

            AddCandidatureController formController = loader.getController();
            formController.setJobAndCandidate(job, candidatService.getCurrentCandidate());

            homeController.getContentArea().getChildren().setAll(form);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void applyHoverEffect(Region node, String hoverStyle, String normalStyle) {
        node.setOnMouseEntered(e -> node.setStyle(hoverStyle));
        node.setOnMouseExited(e -> node.setStyle(normalStyle));
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    private Color getTypeColor(String type) {
        if (type == null) return Color.web("#94a3b8");
        return switch (type.toLowerCase()) {
            case "full-time" -> Color.web("#059669");
            case "part-time" -> Color.web("#d97706");
            case "contract" -> Color.web("#7c3aed");
            case "internship" -> Color.web("#2563eb");
            default -> Color.web("#94a3b8");
        };
    }

    private String getTypeTextColor(String type) {
        if (type == null) return "#94a3b8";
        return switch (type.toLowerCase()) {
            case "full-time" -> "#059669";
            case "part-time" -> "#d97706";
            case "contract" -> "#7c3aed";
            case "internship" -> "#2563eb";
            default -> "#94a3b8";
        };
    }

    private String getStatusIcon(String status) {
        if (status == null) return "⚪";
        return switch (status.toLowerCase()) {
            case "open" -> "🟢";
            case "closed" -> "🔴";
            case "pending" -> "🟡";
            default -> "⚪";
        };
    }

    private String getStatusColor(String status) {
        if (status == null) return "#94a3b8";
        return switch (status.toLowerCase()) {
            case "open" -> "#059669";
            case "closed" -> "#dc2626";
            case "pending" -> "#d97706";
            default -> "#94a3b8";
        };
    }

    private void showEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("No jobs found");
        message.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #475569;");

        emptyBox.getChildren().addAll(icon, message);
        cardsContainer.getChildren().add(emptyBox);
    }

    private void updateNavigationButtons() {
        int totalPages = (int) Math.ceil((double) filteredJobs.size() / CARDS_PER_PAGE);
        prevButton.setDisable(currentPage == 0 || filteredJobs.isEmpty());
        nextButton.setDisable(currentPage >= totalPages - 1 || filteredJobs.isEmpty());
    }

    @FXML
    private void handleNext() {
        currentPage++;
        updateDisplay();
    }

    @FXML
    private void handlePrevious() {
        currentPage--;
        updateDisplay();
    }

    private void goToPage(int pageIndex) {
        currentPage = pageIndex;
        updateDisplay();
    }

    @FXML
    private void handleSearchHover(javafx.scene.input.MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: #6366f1; -fx-text-fill: white;");
    }

    @FXML
    private void handleSearchExit(javafx.scene.input.MouseEvent event) {
        ((Button) event.getSource()).setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white;");
    }



    @FXML
    private void handleMyApplications() {
        if (homeController != null) {
            homeController.loadMyApplications();
        }
    }


    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        filteredJobs = new ArrayList<>();

        for (JobOffer job : allJobs) {
            if (job.getTitle().toLowerCase().contains(keyword) ||
                    job.getDescription().toLowerCase().contains(keyword)) {
                filteredJobs.add(job);
            }
        }

        currentPage = 0;
        updateDisplay();
        updateJobCount();
    }

    private void updateJobCount() {
        jobCountLabel.setText("📊 " + filteredJobs.size() + " jobs available");
    }
}