package ui;

import Model.JobOffer;
import Model.User;
import Service.FavJobService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel showing the current user's saved / favourite jobs.
 * Embedded into the HomePage center (root.setCenter) — no own sidebar.
 */
public class FavJobsPanel extends StackPane {

    private final User            currentUser;
    private final Runnable        onBack;
    private final Consumer<JobOffer> onOpenDetails;   // opens job details view
    private final FavJobService   favService;

    private VBox  cardGrid;
    private Label countLabel;

    public FavJobsPanel(User user, Runnable onBack, Consumer<JobOffer> onOpenDetails) {
        this.currentUser   = user;
        this.onBack        = onBack;
        this.onOpenDetails = onOpenDetails;
        this.favService    = new FavJobService();
        build();
        loadFavourites();
    }

    // ── BUILD UI ──────────────────────────────────────────────────────────────

    private void build() {
        setStyle("-fx-background-color: #f0f6ff;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f6ff;");

        // ── Hero header ───────────────────────────────────────────────────────
        VBox hero = new VBox(8);
        hero.setPadding(new Insets(42, 56, 36, 56));
        hero.setStyle(
                "-fx-background-color: linear-gradient(to right, #0c4a6e, #0369a1, #0ea5e9);" +
                        "-fx-effect: dropshadow(gaussian, rgba(12,74,110,0.35), 20, 0, 0, 6);"
        );

        // top row: back button + title
        HBox topRow = new HBox(16);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("←");
        backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;" +
                        "-fx-font-size: 18px; -fx-font-weight: bold;" +
                        "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 20; -fx-border-width: 1.5;"
        );
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.32); -fx-text-fill: white;" +
                        "-fx-font-size: 18px; -fx-font-weight: bold;" +
                        "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;" +
                        "-fx-border-color: white; -fx-border-radius: 20; -fx-border-width: 1.5;"
        ));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;" +
                        "-fx-font-size: 18px; -fx-font-weight: bold;" +
                        "-fx-min-width: 40; -fx-min-height: 40; -fx-max-width: 40; -fx-max-height: 40;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 20; -fx-border-width: 1.5;"
        ));
        backBtn.setOnAction(e -> onBack.run());

        VBox titleBox = new VBox(4);
        Text pageTitle = new Text("⭐  My Favourite Jobs");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 30));
        pageTitle.setFill(Color.WHITE);

        countLabel = new Label("Loading...");
        countLabel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;" +
                        "-fx-background-radius: 999; -fx-border-radius: 999;" +
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-width: 1;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 14;"
        );
        titleBox.getChildren().addAll(pageTitle, countLabel);

        topRow.getChildren().addAll(backBtn, titleBox);

        Text subtitle = new Text("Jobs you've saved — apply when you're ready.");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setFill(Color.web("#bae6fd"));

        hero.getChildren().addAll(topRow, subtitle);

        // ── Card grid ─────────────────────────────────────────────────────────
        cardGrid = new VBox(18);
        cardGrid.setPadding(new Insets(36, 56, 48, 56));

        root.getChildren().addAll(hero, cardGrid);
        scroll.setContent(root);
        getChildren().add(scroll);
    }

    // ── DATA LOAD ─────────────────────────────────────────────────────────────

    private void loadFavourites() {
        cardGrid.getChildren().clear();
        cardGrid.getChildren().add(buildLoadingSpinner());

        Task<List<JobOffer>> task = new Task<>() {
            @Override protected List<JobOffer> call() throws Exception {
                return favService.getFavourites(currentUser.getId());
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<JobOffer> jobs = task.getValue();
            countLabel.setText(jobs.size() + (jobs.size() == 1 ? " saved job" : " saved jobs"));
            cardGrid.getChildren().clear();
            if (jobs.isEmpty()) {
                cardGrid.getChildren().add(buildEmptyState());
            } else {
                for (JobOffer job : jobs) {
                    cardGrid.getChildren().add(buildJobCard(job));
                }
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            cardGrid.getChildren().clear();
            cardGrid.getChildren().add(buildErrorState());
            countLabel.setText("Error loading");
        }));

        new Thread(task).start();
    }

    // ── CARD ──────────────────────────────────────────────────────────────────

    private HBox buildJobCard(JobOffer job) {
        HBox card = new HBox(0);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(12,74,110,0.10), 16, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        );
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(12,74,110,0.22), 22, 0, 0, 8);" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #bae6fd; -fx-border-radius: 18; -fx-border-width: 1.5;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(12,74,110,0.10), 16, 0, 0, 4);" +
                        "-fx-cursor: hand;"
        ));
        card.setOnMouseClicked(e -> { if (onOpenDetails != null) onOpenDetails.accept(job); });

        // Left accent bar (status color)
        Region accentBar = new Region();
        accentBar.setPrefWidth(6);
        accentBar.setStyle(
                "-fx-background-color: " + statusColor(job.getStatus()) + ";" +
                        "-fx-background-radius: 18 0 0 18;"
        );

        // Main content
        VBox content = new VBox(10);
        content.setPadding(new Insets(22, 24, 22, 22));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Row 1: title + status badge
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Text titleTxt = new Text(job.getTitle());
        titleTxt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleTxt.setFill(Color.web("#0c4a6e"));

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Label statusBadge = buildStatusBadge(job.getStatus());
        titleRow.getChildren().addAll(titleTxt, titleSpacer, statusBadge);

        // Row 2: type + salary chips
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getChildren().addAll(
                buildChip("💼  " + formatType(job.getType()), "#e0f2fe", "#0369a1"),
                buildChip("💰  " + formatSalary(job.getOfferedSalary()), "#f0fdf4", "#16a34a")
        );

        // Row 3: skills (first 4)
        FlowPane skillsRow = new FlowPane(8, 6);
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isBlank()) {
            String[] skills = job.getRequiredSkills().split(",");
            int shown = Math.min(skills.length, 4);
            for (int i = 0; i < shown; i++) {
                String sk = skills[i].trim();
                if (!sk.isEmpty()) skillsRow.getChildren().add(buildSkillPill(sk));
            }
            if (skills.length > 4) {
                Label more = new Label("+" + (skills.length - 4) + " more");
                more.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-style: italic;");
                skillsRow.getChildren().add(more);
            }
        }

        // Row 4: action buttons
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(6, 0, 0, 0));

        Button removeBtn = buildActionBtn("🗑  Remove", "#fef2f2", "#dc2626", "#fecaca");
        removeBtn.setOnAction(e -> {
            e.consume();
            handleRemove(job, card);
        });

        Button viewBtn = buildActionBtn("👁  View Details", "#eff6ff", "#1d4ed8", "#bfdbfe");
        viewBtn.setOnAction(e -> { e.consume(); if (onOpenDetails != null) onOpenDetails.accept(job); });

        actions.getChildren().addAll(removeBtn, viewBtn);

        content.getChildren().addAll(titleRow, metaRow, skillsRow, actions);
        card.getChildren().addAll(accentBar, content);
        return card;
    }

    // ── REMOVE ────────────────────────────────────────────────────────────────

    private void handleRemove(JobOffer job, HBox card) {
        // Animate out then remove
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(250), card
        );
        ft.setToValue(0);
        ft.setOnFinished(ev -> {
            cardGrid.getChildren().remove(card);
            try {
                favService.remove(currentUser.getId(), job.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int remaining = cardGrid.getChildren().size();
            countLabel.setText(remaining + (remaining == 1 ? " saved job" : " saved jobs"));
            if (remaining == 0) {
                cardGrid.getChildren().add(buildEmptyState());
                countLabel.setText("0 saved jobs");
            }
        });
        ft.play();
    }

    // ── EMPTY / LOADING / ERROR STATES ────────────────────────────────────────

    private VBox buildEmptyState() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(80, 0, 80, 0));

        Text icon = new Text("🔖");
        icon.setFont(Font.font(64));

        Text title = new Text("No saved jobs yet");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setFill(Color.web("#0369a1"));

        Text sub = new Text("Browse job listings and click \"Save for Later\" to bookmark\njobs you want to revisit.");
        sub.setFont(Font.font("Segoe UI", 14));
        sub.setFill(Color.web("#64748b"));
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(icon, title, sub);
        return box;
    }

    private HBox buildLoadingSpinner() {
        HBox box = new HBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));
        ProgressIndicator pi = new ProgressIndicator(-1);
        pi.setPrefSize(36, 36);
        Label lbl = new Label("Loading your saved jobs...");
        lbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        box.getChildren().addAll(pi, lbl);
        return box;
    }

    private VBox buildErrorState() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 60, 0));
        Text icon = new Text("⚠️");
        icon.setFont(Font.font(48));
        Text msg = new Text("Could not load saved jobs. Please try again.");
        msg.setFont(Font.font("Segoe UI", 14));
        msg.setFill(Color.web("#ef4444"));
        box.getChildren().addAll(icon, msg);
        return box;
    }

    // ── SMALL HELPERS ─────────────────────────────────────────────────────────

    private Label buildStatusBadge(String status) {
        String bg, fg;
        String label;
        if (status == null) { bg = "#f1f5f9"; fg = "#64748b"; label = "Unknown"; }
        else switch (status.toLowerCase()) {
            case "open"    -> { bg = "#f0fdf4"; fg = "#16a34a"; label = "● Open"; }
            case "closed"  -> { bg = "#fef2f2"; fg = "#dc2626"; label = "● Closed"; }
            case "pending" -> { bg = "#fffbeb"; fg = "#d97706"; label = "● Pending"; }
            default        -> { bg = "#f1f5f9"; fg = "#64748b"; label = "● " + status; }
        }
        Label badge = new Label(label);
        badge.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12;" +
                        "-fx-background-radius: 999;"
        );
        return badge;
    }

    private String statusColor(String status) {
        if (status == null) return "#94a3b8";
        return switch (status.toLowerCase()) {
            case "open"    -> "#22c55e";
            case "closed"  -> "#ef4444";
            case "pending" -> "#f59e0b";
            default        -> "#94a3b8";
        };
    }

    private Label buildChip(String text, String bg, String fg) {
        Label chip = new Label(text);
        chip.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-background-radius: 999; -fx-padding: 5 14;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;"
        );
        return chip;
    }

    private Label buildSkillPill(String skill) {
        Label pill = new Label(skill);
        pill.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #334155;" +
                        "-fx-background-radius: 999; -fx-border-color: #e2e8f0; -fx-border-radius: 999;" +
                        "-fx-border-width: 1; -fx-padding: 4 12; -fx-font-size: 11px;"
        );
        return pill;
    }

    private Button buildActionBtn(String text, String bg, String fg, String border) {
        Button btn = new Button(text);
        String base = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 18;" +
                "-fx-background-radius: 999; -fx-border-color: " + border + ";" +
                "-fx-border-radius: 999; -fx-border-width: 1; -fx-cursor: hand;";
        String hover = "-fx-background-color: " + fg + "; -fx-text-fill: white;" +
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 18;" +
                "-fx-background-radius: 999; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private String formatType(String type) {
        if (type == null || type.isBlank()) return "Not specified";
        return switch (type.toLowerCase()) {
            case "full-time"  -> "Full Time";
            case "part-time"  -> "Part Time";
            case "contract"   -> "Contract";
            case "internship" -> "Internship";
            default           -> type;
        };
    }

    private String formatSalary(Double salary) {
        if (salary == null || salary <= 0) return "Competitive";
        return String.format("%.0f TND/mo", salary);
    }
}