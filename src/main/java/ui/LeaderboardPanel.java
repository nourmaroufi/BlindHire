package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import Model.LeaderboardRow;
import Model.JobOffer;

import java.util.List;
import java.util.Map;

/**
 * Two-screen leaderboard panel.
 *
 * Screen 1 — grid of the recruiter's job offer cards.
 * Screen 2 — podium (top-3) + full ranked table for the selected job.
 *
 * Controller wires: btnBack, onJobSelected, setJobOffers(), populateLeaderboard()
 */
public class LeaderboardPanel extends StackPane {

    // ── Public hooks for controller ──────────────────────────────────────────
    public final Button  btnBack     = new Button("← Dashboard");
    public final Label   lblStatus   = new Label("Select a job to view its quiz leaderboard");
    public JobOffer      selectedJob = null;
    public Runnable      onJobSelected;          // set by controller

    // ── Internal screens ─────────────────────────────────────────────────────
    private final StackPane screenHolder = new StackPane();
    private VBox jobListScreen;
    private VBox leaderboardScreen;

    // job-list
    private final FlowPane jobCardsPane = new FlowPane(18, 18);

    // leaderboard
    private final Label lbTitle    = new Label();
    private final Label lbSubtitle = new Label();
    private final HBox  podiumHBox = new HBox(20);
    public  final TableView<LeaderboardRow> table = new TableView<>();

    public LeaderboardPanel() {
        setStyle("-fx-background-color: #0f172a;");
        getChildren().addAll(ambientLayer(), screenHolder);
        screenHolder.setAlignment(Pos.TOP_LEFT);

        buildJobListScreen();
        buildLeaderboardScreen();
        showJobList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AMBIENT BACKGROUND
    // ─────────────────────────────────────────────────────────────────────────

    private Pane ambientLayer() {
        Pane p = new Pane();
        p.setMouseTransparent(true);
        // Clip so glows never bleed outside the panel and block sidebar clicks
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(this.widthProperty());
        clip.heightProperty().bind(this.heightProperty());
        p.setClip(clip);
        p.getChildren().addAll(
                glow(260, "#6366f1", 0.15,  60,  60),
                glow(220, "#06b6d4", 0.12, 900,  80),
                glow(180, "#10b981", 0.09, 180, 650)
        );
        return p;
    }

    private Circle glow(double r, String hex, double opacity, double x, double y) {
        Circle c = new Circle(r, Color.web(hex));
        c.setOpacity(opacity);
        c.setEffect(new GaussianBlur(90));
        c.setLayoutX(x); c.setLayoutY(y);
        return c;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCREEN 1 — JOB LIST
    // ─────────────────────────────────────────────────────────────────────────

    private void buildJobListScreen() {
        jobListScreen = new VBox(24);
        jobListScreen.setPadding(new Insets(36, 40, 40, 40));

        // Header
        btnBack.setStyle(ghostBtnStyle());
        btnBack.setOnMouseEntered(e -> btnBack.setStyle(ghostBtnHover()));
        btnBack.setOnMouseExited(e  -> btnBack.setStyle(ghostBtnStyle()));

        Label title = new Label("🏆  Quiz Leaderboards");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        title.setTextFill(Color.WHITE);

        lblStatus.setFont(Font.font("Segoe UI", 13));
        lblStatus.setTextFill(Color.web("rgba(255,255,255,0.40)"));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(btnBack, title, sp, lblStatus);

        // Subtitle
        Label sub = new Label("Click a job offer to view the ranked quiz results of its applicants");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web("rgba(255,255,255,0.35)"));

        // Card grid
        jobCardsPane.setPadding(new Insets(8, 0, 0, 0));
        ScrollPane scroll = wrapScroll(jobCardsPane);

        jobListScreen.getChildren().addAll(header, sub, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    /** Called by controller after loading the recruiter's jobs. */
    public void setJobOffers(List<JobOffer> jobs) {
        jobCardsPane.getChildren().clear();
        if (jobs == null || jobs.isEmpty()) {
            Label none = new Label("No job offers found for your account.");
            none.setTextFill(Color.web("rgba(255,255,255,0.35)"));
            none.setFont(Font.font("Segoe UI", 14));
            jobCardsPane.getChildren().add(none);
            lblStatus.setText("No jobs found");
            return;
        }
        lblStatus.setText(jobs.size() + " job" + (jobs.size() == 1 ? "" : "s") + " found");
        for (JobOffer job : jobs) jobCardsPane.getChildren().add(buildJobCard(job));
    }

    private VBox buildJobCard(JobOffer job) {
        VBox card = new VBox(14);
        card.setPrefWidth(260);
        card.setPadding(new Insets(22));
        card.setStyle(cardStyle(false));
        card.setCursor(Cursor.HAND);

        // Coloured top bar
        Rectangle bar = new Rectangle(36, 4);
        bar.setFill(Color.web("#6366f1"));
        bar.setArcWidth(4); bar.setArcHeight(4);

        Label titleLbl = new Label(job.getTitle());
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLbl.setTextFill(Color.WHITE);
        titleLbl.setWrapText(true);

        // Status badge
        String sc = statusColor(job.getStatus());
        Label badge = new Label(job.getStatus() != null ? job.getStatus().toUpperCase() : "ACTIVE");
        badge.setStyle("-fx-background-color:" + sc + "22;-fx-text-fill:" + sc +
                ";-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 10;-fx-background-radius:20;");

        Label type = new Label("📌  " + (job.getType() != null ? job.getType() : "—"));
        type.setFont(Font.font("Segoe UI", 12));
        type.setTextFill(Color.web("rgba(255,255,255,0.38)"));

        Label cta = new Label("View leaderboard →");
        cta.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        cta.setTextFill(Color.web("#818cf8"));

        card.getChildren().addAll(bar, titleLbl, badge, type, cta);

        card.setOnMouseEntered(e -> card.setStyle(cardStyle(true)));
        card.setOnMouseExited(e  -> card.setStyle(cardStyle(false)));
        card.setOnMouseClicked(e -> {
            selectedJob = job;
            if (onJobSelected != null) onJobSelected.run();
        });
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCREEN 2 — LEADERBOARD
    // ─────────────────────────────────────────────────────────────────────────

    private final Button btnBackToJobs = new Button("← All Jobs");

    private void buildLeaderboardScreen() {
        leaderboardScreen = new VBox(24);
        leaderboardScreen.setPadding(new Insets(36, 40, 40, 40));

        // Header
        btnBackToJobs.setStyle(ghostBtnStyle());
        btnBackToJobs.setOnMouseEntered(e -> btnBackToJobs.setStyle(ghostBtnHover()));
        btnBackToJobs.setOnMouseExited(e  -> btnBackToJobs.setStyle(ghostBtnStyle()));
        btnBackToJobs.setOnAction(e -> showJobList());

        lbTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 26));
        lbTitle.setTextFill(Color.WHITE);

        lbSubtitle.setFont(Font.font("Segoe UI", 13));
        lbSubtitle.setTextFill(Color.web("rgba(255,255,255,0.38)"));

        VBox titles = new VBox(4, lbTitle, lbSubtitle);
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(btnBackToJobs, titles, sp);

        // Podium
        podiumHBox.setAlignment(Pos.BOTTOM_CENTER);
        podiumHBox.setPadding(new Insets(10, 0, 0, 0));

        // Table card
        setupTable();
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(22));
        tableCard.setStyle(cardStyle(false));
        Label tLabel = new Label("📋  Full Ranking");
        tLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        tLabel.setTextFill(Color.WHITE);
        tableCard.getChildren().addAll(tLabel, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox inner = new VBox(28, podiumHBox, tableCard);
        ScrollPane scroll = wrapScroll(inner);

        leaderboardScreen.getChildren().addAll(header, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    /** Called by controller once rows + names are ready. */
    public void populateLeaderboard(JobOffer job, List<LeaderboardRow> rows, Map<Integer, String> names) {
        lbTitle.setText("🏆  " + job.getTitle());
        lbSubtitle.setText(rows.size() + " participant" + (rows.size() == 1 ? "" : "s") +
                " · sorted by quiz score");

        buildPodium(rows, names);
        rebuildTableColumns(names);
        table.getItems().setAll(rows);

        showLeaderboard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PODIUM
    // ─────────────────────────────────────────────────────────────────────────

    private void buildPodium(List<LeaderboardRow> rows, Map<Integer, String> names) {
        podiumHBox.getChildren().clear();
        if (rows.isEmpty()) return;

        // Classic podium order: 2nd | 1st | 3rd
        int[] order   = {1, 0, 2};
        String[] med  = {"🥈", "🥇", "🥉"};
        String[] col  = {"#94a3b8", "#f59e0b", "#cd7c2f"};
        int[] heights = {100, 140, 80};

        for (int slot = 0; slot < 3; slot++) {
            int idx = order[slot];
            if (idx >= rows.size()) continue;
            LeaderboardRow row = rows.get(idx);
            String name  = names.getOrDefault(row.getUserId(), "User #" + row.getUserId());
            String score = row.getScore() != null ? row.getScore().toPlainString() + "%" : "—";
            podiumHBox.getChildren().add(
                    podiumSlot(med[slot], name, score, col[slot], heights[slot], idx + 1));
        }
    }

    private VBox podiumSlot(String medal, String name, String score,
                            String color, int height, int rank) {
        VBox slot = new VBox(8);
        slot.setAlignment(Pos.BOTTOM_CENTER);
        slot.setPrefWidth(190);

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(56, 56); avatar.setMinSize(56, 56);
        Circle bg = new Circle(28, Color.web(color + "33"));
        bg.setStroke(Color.web(color)); bg.setStrokeWidth(2.5);
        Label mLbl = new Label(medal); mLbl.setFont(Font.font(22));
        avatar.getChildren().addAll(bg, mLbl);

        Label nameLbl = new Label(name.length() > 18 ? name.substring(0, 16) + "…" : name);
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        nameLbl.setTextFill(Color.WHITE);

        // Colour the score by pass/fail
        double val = 0;
        try { val = Double.parseDouble(score.replace("%", "")); } catch (Exception ignored) {}
        String scoreColor = val >= 50 ? "#10b981" : "#f43f5e";
        Label scoreLbl = new Label(score);
        scoreLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        scoreLbl.setTextFill(Color.web(scoreColor));

        // Pass / fail badge
        Label resultBadge = new Label(val >= 50 ? "✅ Passed" : "❌ Failed");
        resultBadge.setStyle(
                "-fx-background-color:" + (val >= 50 ? "rgba(16,185,129,0.15)" : "rgba(244,63,94,0.15)") + ";" +
                        "-fx-text-fill:" + (val >= 50 ? "#10b981" : "#f43f5e") + ";" +
                        "-fx-font-size:10px;-fx-font-weight:bold;" +
                        "-fx-padding:3 10;-fx-background-radius:20;"
        );

        // Pedestal
        VBox pedestal = new VBox();
        pedestal.setAlignment(Pos.CENTER);
        pedestal.setPrefWidth(160); pedestal.setMinHeight(height);
        pedestal.setStyle(
                "-fx-background-color:linear-gradient(to bottom," + color + "33," + color + "0a);" +
                        "-fx-background-radius:12 12 0 0;" +
                        "-fx-border-color:" + color + "55;" +
                        "-fx-border-radius:12 12 0 0;" +
                        "-fx-border-width:1 1 0 1;"
        );
        Label rankLbl = new Label("#" + rank);
        rankLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        rankLbl.setTextFill(Color.web(color + "bb"));
        pedestal.getChildren().add(rankLbl);

        slot.getChildren().addAll(avatar, nameLbl, scoreLbl, resultBadge, pedestal);
        return slot;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABLE
    // ─────────────────────────────────────────────────────────────────────────

    private void setupTable() {
        rebuildTableColumns(null);
        table.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-control-inner-background:rgba(255,255,255,0.04);" +
                        "-fx-control-inner-background-alt:rgba(255,255,255,0.02);" +
                        "-fx-table-cell-border-color:rgba(255,255,255,0.06);"
        );
        table.setFixedCellSize(50);
        table.setPrefHeight(320);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label empty = new Label("No quiz results yet");
        empty.setTextFill(Color.web("rgba(255,255,255,0.30)"));
        empty.setFont(Font.font("Segoe UI", 13));
        table.setPlaceholder(empty);
    }

    private void rebuildTableColumns(Map<Integer, String> names) {
        // ── Rank ──
        TableColumn<LeaderboardRow, Number> colRank = new TableColumn<>("#");
        colRank.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleIntegerProperty(
                        table.getItems().indexOf(cd.getValue()) + 1));
        colRank.setMinWidth(60); colRank.setMaxWidth(80);
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                int r = n.intValue();
                setText(r == 1 ? "🥇" : r == 2 ? "🥈" : r == 3 ? "🥉" : String.valueOf(r));
                setStyle("-fx-text-fill:white;-fx-font-size:" + (r <= 3 ? "18" : "14") +
                        "px;-fx-alignment:CENTER;-fx-font-weight:" + (r <= 3 ? "bold" : "normal") + ";");
            }
        });

        // ── Applicant name ──
        TableColumn<LeaderboardRow, String> colName = new TableColumn<>("Applicant");
        colName.setCellValueFactory(cd -> {
            String n = names != null
                    ? names.getOrDefault(cd.getValue().getUserId(), "User #" + cd.getValue().getUserId())
                    : "User #" + cd.getValue().getUserId();
            return new javafx.beans.property.SimpleStringProperty(n);
        });
        colName.setMinWidth(200);
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                int row = getIndex();
                String weight = row < 3 ? "bold" : "normal";
                String size   = row < 3 ? "14"   : "13";
                setText(s);
                setStyle("-fx-text-fill:rgba(255,255,255,0.88);-fx-font-weight:" + weight +
                        ";-fx-font-size:" + size + "px;-fx-padding:0 12;");
            }
        });

        // ── Score ──
        TableColumn<LeaderboardRow, String> colScore = new TableColumn<>("Score");
        colScore.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        cd.getValue().getScore() != null
                                ? cd.getValue().getScore().toPlainString() + "%" : "—"));
        colScore.setMinWidth(120);
        colScore.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                double v = 0;
                try { v = Double.parseDouble(s.replace("%", "")); } catch (Exception ignored) {}
                String c = v >= 75 ? "#10b981" : v >= 50 ? "#f59e0b" : "#f43f5e";
                Label lbl = new Label(s);
                lbl.setStyle("-fx-text-fill:" + c + ";-fx-font-weight:bold;-fx-font-size:14px;");
                setGraphic(lbl);
                setStyle("-fx-alignment:CENTER-LEFT;-fx-padding:0 12;");
            }
        });

        // ── Result ──
        TableColumn<LeaderboardRow, String> colResult = new TableColumn<>("Result");
        colResult.setMinWidth(120);
        colResult.setCellValueFactory(cd -> {
            double v = 0;
            if (cd.getValue().getScore() != null)
                try { v = cd.getValue().getScore().doubleValue(); } catch (Exception ignored) {}
            return new javafx.beans.property.SimpleStringProperty(v >= 50 ? "PASSED" : "FAILED");
        });
        colResult.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                boolean ok = "PASSED".equals(s);
                Label badge = new Label(ok ? "✅  Passed" : "❌  Failed");
                badge.setStyle(
                        "-fx-background-color:" + (ok ? "rgba(16,185,129,0.15)" : "rgba(244,63,94,0.15)") + ";" +
                                "-fx-text-fill:" + (ok ? "#10b981" : "#f43f5e") + ";" +
                                "-fx-font-size:11px;-fx-font-weight:bold;" +
                                "-fx-padding:4 12;-fx-background-radius:20;"
                );
                setGraphic(badge);
                setStyle("-fx-alignment:CENTER-LEFT;-fx-padding:0 12;");
            }
        });

        table.getColumns().setAll(colRank, colName, colScore, colResult);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SCREEN SWITCH
    // ─────────────────────────────────────────────────────────────────────────

    public void showJobList()     { screenHolder.getChildren().setAll(jobListScreen); }
    public void showLeaderboard() { screenHolder.getChildren().setAll(leaderboardScreen); }

    // ─────────────────────────────────────────────────────────────────────────
    //  STYLE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private ScrollPane wrapScroll(javafx.scene.Node content) {
        ScrollPane s = new ScrollPane(content);
        s.setFitToWidth(true);
        s.setStyle("-fx-background-color:transparent;-fx-background:transparent;-fx-border-width:0;");
        return s;
    }

    private String cardStyle(boolean hover) {
        return hover
                ? "-fx-background-color:rgba(99,102,241,0.14);-fx-background-radius:18;" +
                "-fx-border-color:rgba(99,102,241,0.50);-fx-border-radius:18;-fx-border-width:1;"
                : "-fx-background-color:rgba(255,255,255,0.05);-fx-background-radius:18;" +
                "-fx-border-color:rgba(255,255,255,0.09);-fx-border-radius:18;-fx-border-width:1;";
    }

    private String ghostBtnStyle() {
        return "-fx-background-color:rgba(255,255,255,0.07);-fx-text-fill:rgba(255,255,255,0.75);" +
                "-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:8 16;-fx-cursor:hand;";
    }
    private String ghostBtnHover() {
        return "-fx-background-color:rgba(255,255,255,0.13);-fx-text-fill:white;" +
                "-fx-font-weight:bold;-fx-background-radius:10;-fx-padding:8 16;-fx-cursor:hand;";
    }
    private String statusColor(String status) {
        if (status == null) return "#f59e0b";
        return switch (status.toLowerCase()) {
            case "active" -> "#10b981";
            case "closed" -> "#f43f5e";
            default       -> "#f59e0b";
        };
    }
}