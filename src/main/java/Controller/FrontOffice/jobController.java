package Controller.FrontOffice;
import javafx.scene.layout.VBox;
import javafx.concurrent.Task;
import Service.AiMatchingService;
import javafx.scene.control.Alert;
import Model.JobOffer;
import Model.User;
import Service.JobOfferService;
import Service.userservice;
import javafx.application.Platform;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import Utils.NavigationManager;
import ui.FavJobsPanel;

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

    @FXML
    private Button aiMatchButton;

    @FXML
    private Button backToAllButton;

    private JobOfferService jobService;
    private List<JobOffer> allJobs = new ArrayList<>();
    private List<JobOffer> filteredJobs = new ArrayList<>();
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 3;

    private BorderPane homeBorderPane;

    public void setHomeBorderPane(BorderPane borderPane) {
        this.homeBorderPane = borderPane;
    }


    private void openJobDetails(JobOffer job) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/jobDetails.fxml")
            );
            VBox detailsView = loader.load();

            JobDetailsController controller = loader.getController();
            Model.User sessionUser = new userservice().getCurrentUser();
            controller.setCurrentUser(sessionUser);
            controller.setJob(job);
            controller.setHomeBorderPane(homeBorderPane); // ← so back button works

            NavigationManager.navigateTo(homeBorderPane, detailsView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    @FXML
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

        // Salary badge
        HBox salaryBox = new HBox(8);
        salaryBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 14px;");
        String salaryText = (job.getOfferedSalary() != null && job.getOfferedSalary() > 0)
                ? String.format("%.0f TND/month", job.getOfferedSalary())
                : "Competitive";
        Label salaryLbl = new Label(salaryText);
        salaryLbl.setStyle("-fx-text-fill: #059669; -fx-font-size: 13px; -fx-font-weight: bold;");
        salaryBox.getChildren().addAll(salaryIcon, salaryLbl);

        // AI match score badge — only shown when rankedScores is populated
        if (rankedScores.containsKey(job)) {
            int score = rankedScores.get(job);
            String scoreColor = score >= 70 ? "#22c55e" : score >= 40 ? "#f59e0b" : "#ef4444";
            HBox matchBox = new HBox(8);
            matchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            matchBox.setStyle("-fx-background-color: " + scoreColor + "22; -fx-background-radius: 10; -fx-padding: 6 12;");
            Label matchIcon = new Label("🤖");
            matchIcon.setStyle("-fx-font-size: 13px;");
            Label matchLbl = new Label("AI Match: " + score + "%");
            matchLbl.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            matchBox.getChildren().addAll(matchIcon, matchLbl);
            card.getChildren().addAll(typeBadge, titleBox, description, separator, statusBox, dateBox, salaryBox, matchBox, applyBtn);
        } else {
            card.getChildren().addAll(typeBadge, titleBox, description, separator, statusBox, dateBox, salaryBox, applyBtn);
        }

        return card;
    }

    private void openApplicationForm(JobOffer job) {
        Service.userservice us = new Service.userservice();
        Model.User currentUser = us.getCurrentUser();

        if (currentUser == null) {
            showAlreadyAppliedAlert("Not logged in", "Please log in to apply for a job.");
            return;
        }

        // ✅ Duplicate check — block if already applied
        try {
            boolean alreadyApplied = new Service.CandidatureService()
                    .hasApplied(currentUser.getId(), job.getId());
            if (alreadyApplied) {
                showAlreadyAppliedAlert(
                        "Already Applied",
                        "You have already applied to " + job.getTitle() );
                return;
            }
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/CandidatureForm.fxml")
            );
            ScrollPane form = loader.load();
            AddCandidatureController formController = loader.getController();
            formController.setJobAndUser(job, currentUser);
            formController.setHomeBorderPane(homeBorderPane);  // ✅ needed for back button
            NavigationManager.navigateTo(homeBorderPane, form);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlreadyAppliedAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        ((Button) event.getSource()).setStyle(
                "-fx-background-color: #6366f1; -fx-text-fill: white; " +
                        "-fx-background-radius: 25; -fx-padding: 12 30; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }

    @FXML
    private void handleSearchExit(javafx.scene.input.MouseEvent event) {
        ((Button) event.getSource()).setStyle(
                "-fx-background-color: #4f46e5; -fx-text-fill: white; " +
                        "-fx-background-radius: 25; -fx-padding: 12 30; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
    }



    @FXML
    private void handleMyApplications() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FrontOffice/MyApplications.fxml")
            );
            javafx.scene.Parent view = loader.load();
            MyApplicationsController ctrl = loader.getController();
            ctrl.setHomeBorderPane(homeBorderPane);
            NavigationManager.navigateTo(homeBorderPane, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSavedJobs() {
        Model.User sessionUser = new userservice().getCurrentUser();
        if (sessionUser == null) {
            showAlreadyAppliedAlert("Not Logged In", "Please log in to view your saved jobs.");
            return;
        }
        FavJobsPanel panel = new FavJobsPanel(
                sessionUser,
                () -> NavigationManager.goBack(),
                job -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/FrontOffice/jobDetails.fxml")
                        );
                        javafx.scene.layout.VBox detailsView = loader.load();
                        JobDetailsController ctrl = loader.getController();
                        ctrl.setCurrentUser(sessionUser);
                        ctrl.setJob(job);
                        ctrl.setHomeBorderPane(homeBorderPane);
                        NavigationManager.navigateTo(homeBorderPane, detailsView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        NavigationManager.navigateTo(homeBorderPane, panel);
    }

    @FXML
    private void handleAiMatching() {
        User currentUser = new userservice().getCurrentUser();
        if (currentUser == null) {
            showAlreadyAppliedAlert("Not Logged In", "Please log in to use AI Matching.");
            return;
        }
        String candidateSkills = currentUser.getSkills();
        if (candidateSkills == null || candidateSkills.isBlank()) {
            showAlreadyAppliedAlert("No Skills Found", "Please add skills to your profile to use AI Matching.");
            return;
        }

        // Show loading state on button
        aiMatchButton.setText("⏳ Matching...");
        aiMatchButton.setDisable(true);

        Task<List<JobOffer>> task = new Task<>() {
            @Override
            protected List<JobOffer> call() throws Exception {
                // Score every job against this candidate
                Map<JobOffer, Integer> scores = new LinkedHashMap<>();
                for (JobOffer job : allJobs) {
                    try {
                        int score = Integer.parseInt(
                                AiMatchingService.getMatchScore(job.getRequiredSkills(), candidateSkills).trim()
                        );
                        if (score > 0) scores.put(job, score);
                    } catch (Exception ignored) {
                        // skip jobs that fail to score
                    }
                }

                // Sort descending by score
                List<JobOffer> ranked = new ArrayList<>(scores.keySet());
                ranked.sort((a, b) -> scores.get(b) - scores.get(a));

                // Attach score temporarily via a tag so the card can display it
                // We encode score into requiredSkills display — use a wrapper approach instead
                // Store scores for display in the UI thread
                rankedScores = scores;
                return ranked;
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            aiMatchButton.setText("🤖 AI Matching");
            aiMatchButton.setDisable(false);

            List<JobOffer> ranked = task.getValue();
            if (ranked.isEmpty()) {
                showAlreadyAppliedAlert("No Matches Found", "No jobs matched your skills profile.");
                return;
            }

            // Replace filteredJobs with ranked results and redisplay
            filteredJobs = ranked;
            currentPage = 0;
            updateDisplay();
            jobCountLabel.setText("🤖 " + ranked.size() + " AI-matched jobs");

            // Show the back button
            backToAllButton.setVisible(true);
            backToAllButton.setManaged(true);
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            aiMatchButton.setText("🤖 AI Matching");
            aiMatchButton.setDisable(false);
            if (task.getException() != null) task.getException().printStackTrace();
            showAlreadyAppliedAlert("AI Error", "Could not complete AI matching: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleBackToAll() {
        rankedScores.clear();
        filteredJobs = new ArrayList<>(allJobs);
        currentPage = 0;
        updateDisplay();
        updateJobCount();
        backToAllButton.setVisible(false);
        backToAllButton.setManaged(false);
    }

    // Holds the latest AI scores for rendering score badges on cards
    private Map<JobOffer, Integer> rankedScores = new LinkedHashMap<>();


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

        // Clear AI mode if active
        rankedScores.clear();
        backToAllButton.setVisible(false);
        backToAllButton.setManaged(false);

        currentPage = 0;
        updateDisplay();
        updateJobCount();
    }

    private void updateJobCount() {
        jobCountLabel.setText("📊 " + filteredJobs.size() + " jobs available");
    }
}