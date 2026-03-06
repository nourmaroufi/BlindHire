package Controller;

import Service.InterviewService;
import Service.MessageService;
import Service.SentimentService;
import Service.VideoCallService;
import Service.scoreService;
import Model.score;
import Model.User;
import Service.userservice;
import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Model.Interview;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InterviewPageController {

    // ── FXML — custom tab bar and content panes ───────────────────────────────
    @FXML private HBox       tabBar;
    @FXML private StackPane  tabContentArea;

    // Content panes (one per tab, toggled via visible/managed)
    @FXML private ScrollPane overviewPane;
    @FXML private VBox       dashboardPane;
    @FXML private VBox       calendarPane;
    @FXML private VBox       pastPane;
    @FXML private VBox       acceptedPane;
    @FXML private VBox       acceptedContainer;

    // Data containers inside each pane
    @FXML private VBox               overviewContainer;
    @FXML private VBox               interviewContainer;
    @FXML private VBox               calendarContainer;
    @FXML private VBox               pastInterviewContainer;

    // Controls
    @FXML private TextField          searchField;
    @FXML private ComboBox<String>   filterTypeComboBox;
    @FXML private ComboBox<String>   pastStatusFilterComboBox;

    // ── Dashboard injection ───────────────────────────────────────────────────
    private BorderPane dashboardRoot;
    public void setDashboardCenter(BorderPane root) { this.dashboardRoot = root; }

    // ── Services ──────────────────────────────────────────────────────────────
    private final InterviewService interviewService = new InterviewService();
    private final MessageService   messageService   = new MessageService();
    private final SentimentService sentimentService = new SentimentService();
    private final VideoCallService videoCallService = new VideoCallService();
    private final scoreService     scoreService       = new scoreService();

    // ── State ─────────────────────────────────────────────────────────────────
    private List<Interview>        allInterviews    = new ArrayList<>();
    private CalendarView           calendarView;
    private Calendar               interviewCalendar;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Design tokens — mirror DashboardPage constants
    private static final String PAGE_BG    = "#0f172a";
    private static final String CARD_BG    = "#1e293b";
    private static final String CARD_HOVER = "#243044";
    private static final String INDIGO     = "#6366f1";
    private static final String INDIGO_LT  = "#818cf8";
    private static final String INDIGO_DIM = "#a5b4fc";
    private static final String CYAN       = "#06b6d4";
    private static final String GREEN      = "#10b981";
    private static final String AMBER      = "#f59e0b";
    private static final String ROSE       = "#f43f5e";
    private static final String TEXT_WHITE = "white";
    private static final String TEXT_SOFT  = "#e2e8f0";   // bright enough to read
    private static final String TEXT_DIM   = "#94a3b8";   // only for tiny helper labels
    private static final String BORDER     = "rgba(99,102,241,0.22)";

    // Active tab index
    private int activeTab = 0;
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<javafx.scene.Node> tabPanes = new ArrayList<>();

    // ── Initialize ────────────────────────────────────────────────────────────
    public void initialize() {
        // Wire pane list (order matches tab order)
        tabPanes.add(overviewPane);
        tabPanes.add(dashboardPane);
        tabPanes.add(calendarPane);
        tabPanes.add(pastPane);
        tabPanes.add(acceptedPane);

        // Build custom tab buttons
        String[][] tabs = {
                {"📋", "Overview"},
                {"🗂", "Dashboard"},
                {"📆", "Calendar"},
                {"🕘", "Past Interviews"},
                {"🏆", "Accepted Candidates"}
        };
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            Button btn = buildTabButton(tabs[i][0] + "  " + tabs[i][1], i == 0);
            btn.setOnAction(e -> switchTab(idx));
            tabButtons.add(btn);
            tabBar.getChildren().add(btn);
        }

        // ComboBox setup
        filterTypeComboBox.setItems(
                FXCollections.observableArrayList("All", "Online", "In Person"));
        filterTypeComboBox.setValue("All");

        pastStatusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Pending", "Accepted", "Rejected"));
        pastStatusFilterComboBox.setValue("All");

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        filterTypeComboBox.setOnAction(e -> applyFilters());
        pastStatusFilterComboBox.setOnAction(e -> filterPastInterviews());

        setupCalendar();
        loadInterviews();
    }

    // ── Custom tab button builder ─────────────────────────────────────────────
    private Button buildTabButton(String label, boolean active) {
        Button btn = new Button(label);
        applyTabStyle(btn, active);
        return btn;
    }

    private void applyTabStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + CARD_BG + ";" +
                            "-fx-text-fill: " + INDIGO_LT + ";" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 12 12 0 0;" +
                            "-fx-padding: 11 22 13 22;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: " + INDIGO + " " + INDIGO + " transparent " + INDIGO + ";" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 12 12 0 0;"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(15,23,42,0.60);" +
                            "-fx-text-fill: " + TEXT_DIM + ";" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 12 12 0 0;" +
                            "-fx-padding: 11 22 13 22;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: rgba(99,102,241,0.18) rgba(99,102,241,0.18) transparent rgba(99,102,241,0.18);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 12 12 0 0;"
            );
        }
    }

    private void switchTab(int idx) {
        activeTab = idx;
        for (int i = 0; i < tabButtons.size(); i++) {
            applyTabStyle(tabButtons.get(i), i == idx);
        }
        for (int i = 0; i < tabPanes.size(); i++) {
            boolean show = (i == idx);
            tabPanes.get(i).setVisible(show);
            tabPanes.get(i).setManaged(show);
        }
    }

    // ── "Create Interviews" button ────────────────────────────────────────────
    @FXML
    private void openCreateInterviewsPage() {
        if (dashboardRoot != null) {
            ui.InterviewPage page = new ui.InterviewPage();
            dashboardRoot.setCenter(page.getRoot());
        } else {
            ui.InterviewPage page = new ui.InterviewPage();
            Stage stage = new Stage();
            stage.setTitle("Create Interviews");
            stage.setScene(new Scene(page.getRoot(), 1100, 700));
            stage.show();
        }
    }

    // ── Load & refresh ────────────────────────────────────────────────────────
    private void loadInterviews() {
        try {
            allInterviews = interviewService.afficherAll();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load interviews: " + e.getMessage());
        }
        applyFilters();
        loadOverviewCards();
        loadPastInterviews();
        loadAcceptedCandidates();
        markInterviewDates();
    }

    // ── Dashboard — filter + rows ─────────────────────────────────────────────
    private void applyFilters() {
        interviewContainer.getChildren().clear();
        String search  = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String selType = filterTypeComboBox.getValue();
        boolean found  = false;

        for (Interview iv : allInterviews) {
            boolean matchType   = "All".equals(selType) || selType == null
                    || iv.getType().equalsIgnoreCase(selType);
            boolean matchSearch = search.isEmpty()
                    || iv.getJob_offer().toLowerCase().contains(search)
                    || iv.getInterviewer().toLowerCase().contains(search)
                    || iv.getType().toLowerCase().contains(search)
                    || (iv.getStatus() != null && iv.getStatus().toLowerCase().contains(search))
                    || String.valueOf(iv.getIdScore()).contains(search);

            if (matchType && matchSearch) {
                interviewContainer.getChildren().add(createInterviewRow(iv));
                found = true;
            }
        }
        if (!found) {
            Label empty = new Label("No interviews match your search.");
            empty.setStyle("-fx-text-fill:" + TEXT_SOFT + "; -fx-font-size:13px; -fx-padding:16;");
            interviewContainer.getChildren().add(empty);
        }
    }

    private HBox createInterviewRow(Interview iv) {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: rgba(30,41,59,0.90);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(99,102,241,0.18);" +
                        "-fx-border-width: 1; -fx-border-radius: 12;" +
                        "-fx-padding: 12 16;"
        );

        // ── Candidate username — resolved via scoreService ─────────────────────
        String candidateName = "—";
        try {
            // iv.getIdScore() is the FK to score.id_score; get job_offer_id from it
            // then fetch accepted scores for that job to find the matching candidate
            java.sql.Connection tmpConn = Utils.Mydb.getInstance().getConnection();
            java.sql.PreparedStatement tmpPs = tmpConn.prepareStatement(
                    "SELECT COALESCE(NULLIF(u.username,''), CONCAT(u.nom,' ',u.prenom)) AS uname " +
                            "FROM score s JOIN user u ON s.id_user = u.id WHERE s.id_score = ? LIMIT 1");
            tmpPs.setLong(1, iv.getIdScore());
            java.sql.ResultSet tmpRs = tmpPs.executeQuery();
            if (tmpRs.next()) candidateName = tmpRs.getString("uname");
        } catch (Exception ignored) {}

        Label candidateLabel = new Label("👤  " + candidateName);
        candidateLabel.setPrefWidth(155);
        candidateLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:" + INDIGO_DIM + "; -fx-font-size:13px;");

        // ── Job offer title (already joined by InterviewService) ──────────────
        Label jobLabel = new Label("💼  " + iv.getJob_offer());
        jobLabel.setPrefWidth(145);
        jobLabel.setStyle("-fx-text-fill:" + TEXT_SOFT + "; -fx-font-size:13px;");
        jobLabel.setWrapText(false);

        // ── Date (editable) ──────────────────────────────────────────────────
        TextField dateField = new TextField(iv.getDate().format(fmt));
        dateField.setPrefWidth(155);
        styleField(dateField);

        // ── Type ─────────────────────────────────────────────────────────────
        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("Online", "In Person"));
        typeBox.setValue(iv.getType());
        typeBox.setPrefWidth(110);

        // ── Location ─────────────────────────────────────────────────────────
        TextField locationField = new TextField(iv.getLocationLink() != null ? iv.getLocationLink() : "");
        locationField.setPrefWidth(130);
        locationField.setPromptText("Paste link...");
        styleField(locationField);

        Button openLink = new Button("🔗");
        openLink.setStyle(
                "-fx-background-color: rgba(6,182,212,0.18); -fx-text-fill:" + CYAN + ";" +
                        "-fx-background-radius:8; -fx-padding:7 10; -fx-cursor:hand;" +
                        "-fx-border-color: rgba(6,182,212,0.30); -fx-border-width:1; -fx-border-radius:8;"
        );
        openLink.setOnAction(e -> {
            String link = locationField.getText().trim();
            if (!link.isEmpty()) {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(link)); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        HBox locBox = new HBox(4, locationField, openLink);
        locBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ── Notes ─────────────────────────────────────────────────────────────
        TextField notesField = new TextField(iv.getNotes() != null ? iv.getNotes() : "");
        notesField.setPrefWidth(150);
        notesField.setPromptText("Notes...");
        styleField(notesField);

        // ── Icon-only action buttons ──────────────────────────────────────────
        Button updateBtn = iconBtn("✏️",
                "rgba(6,182,212,0.18)", CYAN, "rgba(6,182,212,0.35)");
        updateBtn.setTooltip(new Tooltip("Update interview"));
        updateBtn.setOnAction(e -> {
            try {
                LocalDateTime dt = LocalDateTime.parse(dateField.getText(), fmt);
                String loc   = locationField.getText().trim();
                String notes = notesField.getText().trim();
                interviewService.update(iv.getId(), dt, typeBox.getValue(),
                        loc.isEmpty() ? null : loc, iv.getIdScore(), iv.getInterviewerId());
                interviewService.updateNotes(iv.getId(), notes.isEmpty() ? null : notes);
                showAlert("Success", "Interview updated!");
                loadInterviews();
            } catch (Exception ex) {
                showAlert("Error", "Failed to update: " + ex.getMessage());
            }
        });

        Button deleteBtn = iconBtn("🗑️",
                "rgba(244,63,94,0.15)", ROSE, "rgba(244,63,94,0.30)");
        deleteBtn.setTooltip(new Tooltip("Delete interview"));
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Confirmation");
            confirm.setHeaderText("Delete this interview?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    try { interviewService.delete(iv.getId()); loadInterviews(); }
                    catch (SQLException ex) {
                        showAlert("Error", "Failed to delete: " + ex.getMessage());
                    }
                }
            });
        });

        Button chatBtn = iconBtn("💬",
                "rgba(16,185,129,0.15)", GREEN, "rgba(16,185,129,0.30)");
        chatBtn.setTooltip(new Tooltip("Open chat"));
        chatBtn.setOnAction(e -> openChat(iv));

        row.getChildren().addAll(candidateLabel, jobLabel, dateField, typeBox,
                locBox, notesField, updateBtn, deleteBtn, chatBtn);
        return row;
    }

    /** Compact square icon button — no text, just emoji + consistent padding. */
    private Button iconBtn(String icon, String bg, String fg, String border) {
        Button b = new Button(icon);
        b.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-background-radius: 8; -fx-border-color: " + border + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 8;" +
                        "-fx-padding: 7 10; -fx-cursor: hand; -fx-font-size: 14px;"
        );
        return b;
    }

    // ── Calendar ──────────────────────────────────────────────────────────────
    private void setupCalendar() {
        interviewCalendar = new Calendar("Interviews");
        interviewCalendar.setStyle(Calendar.Style.STYLE1);
        interviewCalendar.setReadOnly(true);

        calendarView = new CalendarView();
        calendarView.getCalendarSources().clear();
        CalendarSource src = new CalendarSource("My Calendars");
        src.getCalendars().add(interviewCalendar);
        calendarView.getCalendarSources().add(src);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setRequestedTime(java.time.LocalTime.now());
        calendarView.setShowToolBar(true);
        calendarView.setShowPageToolBarControls(false);

        calendarContainer.getChildren().setAll(calendarView);
        markInterviewDates();
    }

    private void markInterviewDates() {
        try {
            interviewCalendar.clear();
            for (Interview iv : interviewService.afficherAll()) {
                Entry<String> entry = new Entry<>(iv.getJob_offer());
                entry.setInterval(iv.getDate(), iv.getDate().plusHours(1));
                entry.setLocation("Score #" + iv.getIdScore());
                interviewCalendar.addEntry(entry);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load calendar: " + e.getMessage());
        }
    }

    // ── Overview (upcoming cards) ─────────────────────────────────────────────
    private void loadOverviewCards() {
        overviewContainer.getChildren().clear();
        try {
            LocalDateTime now = LocalDateTime.now();
            HBox row = new HBox(20);
            row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            int count = 0;

            for (Interview iv : interviewService.afficherAll()) {
                if (iv.getDate().isAfter(now)) {
                    row.getChildren().add(createOverviewCard(iv));
                    count++;
                    if (count % 3 == 0) {
                        overviewContainer.getChildren().add(row);
                        row = new HBox(20);
                        row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                    }
                }
            }
            if (!row.getChildren().isEmpty())
                overviewContainer.getChildren().add(row);
            if (count == 0) {
                Label empty = new Label("No upcoming interviews.");
                empty.setStyle("-fx-text-fill:" + TEXT_SOFT + "; -fx-font-size:14px;");
                overviewContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load overview: " + e.getMessage());
        }
    }

    private VBox createOverviewCard(Interview iv) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setStyle(
                "-fx-background-color: " + CARD_HOVER + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 18;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 18, 0, 0, 5);"
        );

        String typeIcon = "Online".equals(iv.getType()) ? "🌐" : "🏢";
        Label typeBadge = new Label(typeIcon + "  " + iv.getType());
        typeBadge.setStyle(
                "-fx-background-color: rgba(99,102,241,0.22); -fx-text-fill:" + INDIGO_LT + ";" +
                        "-fx-padding:5 12; -fx-background-radius:20;" +
                        "-fx-font-size:12px; -fx-font-weight:bold;" +
                        "-fx-border-color: rgba(99,102,241,0.35); -fx-border-width:1; -fx-border-radius:20;"
        );

        String status = iv.getStatus() != null ? iv.getStatus() : "Pending";
        String[] statusStyle = statusStyle(status);
        Label statusBadge = new Label(status);
        statusBadge.setStyle(statusStyle[0]);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox topRow = new HBox(8, typeBadge, sp, statusBadge);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // ── Candidate username resolved directly from score table ──────────────
        String candidateNameCard = "Candidate";
        try {
            java.sql.PreparedStatement cps = Utils.Mydb.getInstance().getConnection().prepareStatement(
                    "SELECT COALESCE(NULLIF(u.username,''), CONCAT(u.nom,' ',u.prenom)) AS uname " +
                            "FROM score s JOIN user u ON s.id_user = u.id WHERE s.id_score = ? LIMIT 1");
            cps.setLong(1, iv.getIdScore());
            java.sql.ResultSet crs = cps.executeQuery();
            if (crs.next()) candidateNameCard = crs.getString("uname");
        } catch (Exception ignored) {}

        Label scoreLabel = new Label("👤  " + candidateNameCard);
        scoreLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:" + INDIGO_LT + ";");

        Label jobLabel = new Label("💼  " + iv.getJob_offer());
        jobLabel.setStyle("-fx-font-size:14px; -fx-text-fill:" + TEXT_SOFT + ";");
        jobLabel.setWrapText(true);

        Label dateLabel = new Label("📅  " + iv.getDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        dateLabel.setStyle("-fx-font-size:13px; -fx-text-fill:" + INDIGO_DIM + ";");

        Label interviewerLabel = new Label("🎙  " + iv.getInterviewer());
        interviewerLabel.setStyle("-fx-font-size:13px; -fx-text-fill:" + TEXT_SOFT + ";");

        Hyperlink locationLink = null;
        if (iv.getLocationLink() != null && !iv.getLocationLink().isEmpty()
                && "In Person".equals(iv.getType())) {
            locationLink = new Hyperlink("📍  View Location");
            locationLink.setStyle("-fx-text-fill:" + CYAN + "; -fx-font-size:13px;");
            final String link = iv.getLocationLink();
            locationLink.setOnAction(e -> {
                try { java.awt.Desktop.getDesktop().browse(new java.net.URI(link)); }
                catch (Exception ex) { ex.printStackTrace(); }
            });
        }

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        // Chat button
        Button chatBtn = new Button("💬  Open Chat");
        chatBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.18); -fx-text-fill:" + INDIGO_LT + ";" +
                        "-fx-font-weight:bold; -fx-background-radius:14;" +
                        "-fx-padding:9 20; -fx-font-size:13px; -fx-cursor:hand;" +
                        "-fx-border-color: rgba(99,102,241,0.35); -fx-border-width:1; -fx-border-radius:14;"
        );
        int unread = 0;
        try { unread = messageService.countUnread(iv.getId(), "RECRUITER"); }
        catch (SQLException ignored) {}

        StackPane chatStack = new StackPane(chatBtn);
        chatStack.setAlignment(javafx.geometry.Pos.CENTER);
        if (unread > 0) {
            Label badge = new Label(String.valueOf(unread));
            badge.setStyle(
                    "-fx-background-color:" + ROSE + "; -fx-text-fill:white;" +
                            "-fx-background-radius:10; -fx-font-size:10px;" +
                            "-fx-padding:1 5; -fx-font-weight:bold;"
            );
            StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new javafx.geometry.Insets(-5,-5,0,0));
            chatStack.getChildren().add(badge);
            chatBtn.setOnAction(e -> { badge.setVisible(false); openChat(iv); });
        } else {
            chatBtn.setOnAction(e -> openChat(iv));
        }

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER);
        btnRow.getChildren().add(chatStack);

        if ("Online".equals(iv.getType())) {
            Button videoBtn = new Button("📹  Start Call");
            videoBtn.setStyle(
                    "-fx-background-color: rgba(6,182,212,0.18); -fx-text-fill:" + CYAN + ";" +
                            "-fx-font-weight:bold; -fx-background-radius:14;" +
                            "-fx-padding:9 16; -fx-font-size:13px; -fx-cursor:hand;" +
                            "-fx-border-color: rgba(6,182,212,0.35); -fx-border-width:1; -fx-border-radius:14;"
            );
            videoBtn.setOnAction(e -> videoCallService.openVideoCall(iv, "RECRUITER"));
            btnRow.getChildren().add(videoBtn);
        }

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(99,102,241,0.20);");

        card.getChildren().addAll(topRow, scoreLabel, jobLabel, dateLabel, interviewerLabel);
        if (locationLink != null) card.getChildren().add(locationLink);
        card.getChildren().addAll(bottomSpacer, sep, btnRow);
        return card;
    }

    // ── Past Interviews ───────────────────────────────────────────────────────
    private void loadPastInterviews() {
        pastInterviewContainer.getChildren().clear();
        try {
            LocalDateTime now = LocalDateTime.now();
            boolean any = false;
            for (Interview iv : interviewService.afficherAll()) {
                if (iv.getDate().isBefore(now)) {
                    pastInterviewContainer.getChildren().add(createPastCard(iv));
                    any = true;
                }
            }
            if (!any) {
                Label empty = new Label("No past interviews yet.");
                empty.setStyle("-fx-text-fill:" + TEXT_SOFT + "; -fx-font-size:14px;");
                pastInterviewContainer.getChildren().add(empty);
            }
            pastStatusFilterComboBox.setValue("All");
            filterPastInterviews();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load past interviews: " + e.getMessage());
        }
    }

    private void filterPastInterviews() {
        pastInterviewContainer.getChildren().clear();
        String selected = pastStatusFilterComboBox.getValue();
        try {
            LocalDateTime now = LocalDateTime.now();
            boolean found = false;
            for (Interview iv : interviewService.afficherAll()) {
                if (iv.getDate().isBefore(now)) {
                    String st = iv.getStatus() != null ? iv.getStatus() : "Pending";
                    if ("All".equals(selected) || st.equals(selected)) {
                        pastInterviewContainer.getChildren().add(createPastCard(iv));
                        found = true;
                    }
                }
            }
            if (!found) {
                Label empty = new Label("No " +
                        ("All".equals(selected) ? "" : selected.toLowerCase() + " ")
                        + "past interviews.");
                empty.setStyle("-fx-text-fill:" + TEXT_SOFT + "; -fx-font-size:14px;");
                pastInterviewContainer.getChildren().add(empty);
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to filter: " + e.getMessage());
        }
    }

    private VBox createPastCard(Interview iv) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: " + CARD_HOVER + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 14;" +
                        "-fx-padding: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 12, 0, 0, 3);"
        );

        String status = iv.getStatus() != null ? iv.getStatus() : "Pending";
        String[] statusSt = statusStyle(status);

        // Summary row
        Label scoreLabel = new Label("Score #" + iv.getIdScore());
        scoreLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:white;");

        Label dateLabel = new Label("📅  " + iv.getDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        dateLabel.setStyle("-fx-text-fill:" + INDIGO_DIM + "; -fx-font-size:13px;");

        Label typeLabel = new Label(iv.getType());
        typeLabel.setStyle(
                "-fx-text-fill:" + CYAN + "; -fx-font-size:12px; -fx-font-weight:bold;" +
                        "-fx-background-color: rgba(6,182,212,0.12); -fx-background-radius:8;" +
                        "-fx-padding: 3 8;"
        );

        Label statusBadge = new Label(status);
        statusBadge.setStyle(statusSt[0]);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label expandHint = new Label("▼  Details");
        expandHint.setStyle("-fx-text-fill:" + INDIGO_LT + "; -fx-cursor:hand; -fx-font-size:12px; -fx-font-weight:bold;");

        HBox summary = new HBox(12, scoreLabel, dateLabel, typeLabel, spacer, statusBadge, expandHint);
        summary.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        summary.setStyle("-fx-cursor:hand;");

        // Detail panel
        VBox details = new VBox(10);
        details.setVisible(false);
        details.setManaged(false);
        details.setStyle(
                "-fx-border-color: rgba(99,102,241,0.20); -fx-border-width:1 0 0 0;" +
                        "-fx-padding:14 0 0 0;"
        );

        Label jobLabel         = new Label("💼  Job Offer: " + iv.getJob_offer());
        Label interviewerLabel = new Label("👤  Interviewer: " + iv.getInterviewer());
        Label notesLabel       = new Label("📝  Notes: " +
                (iv.getNotes() != null ? iv.getNotes() : "No notes yet."));
        notesLabel.setWrapText(true);
        for (Label l : new Label[]{jobLabel, interviewerLabel, notesLabel})
            l.setStyle("-fx-font-size:13px; -fx-text-fill:" + TEXT_SOFT + ";");

        // Sentiment
        String initSent = (iv.getNotes() != null && !iv.getNotes().isEmpty())
                ? sentimentService.analyze(iv.getNotes()) : null;
        Label sentimentLabel = new Label("🧠  AI Sentiment: " +
                (initSent != null ? initSent : "No notes yet."));
        sentimentLabel.setStyle("-fx-font-size:12px; -fx-text-fill:" + INDIGO_DIM + "; -fx-font-style:italic;");
        VBox sentimentBarBox = new VBox(4);
        if (initSent != null) sentimentBarBox.getChildren().add(createSentimentBar(initSent));

        // Notes button
        Button notesBtn = new Button("📝  Add / Edit Notes");
        notesBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.18); -fx-text-fill:" + INDIGO_LT + ";" +
                        "-fx-background-radius:9; -fx-padding:8 14; -fx-cursor:hand; -fx-font-weight:bold;" +
                        "-fx-border-color: rgba(99,102,241,0.35); -fx-border-width:1; -fx-border-radius:9;"
        );
        notesBtn.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog(iv.getNotes() != null ? iv.getNotes() : "");
            dlg.setTitle("Interview Notes");
            dlg.setHeaderText("Add notes for this interview");
            dlg.setContentText("Notes:");
            dlg.showAndWait().ifPresent(note -> {
                try {
                    interviewService.updateNotes(iv.getId(), note);
                    iv.setNotes(note);
                    notesLabel.setText("📝  Notes: " + note);
                    sentimentLabel.setText("🧠  AI Sentiment: Analyzing...");
                    new Thread(() -> {
                        String result = sentimentService.analyze(note);
                        javafx.application.Platform.runLater(() -> {
                            sentimentLabel.setText("🧠  AI Sentiment: " + result);
                            sentimentBarBox.getChildren().setAll(createSentimentBar(result));
                        });
                    }).start();
                } catch (SQLException ex) {
                    showAlert("Error", "Could not save notes: " + ex.getMessage());
                }
            });
        });

        // Accept / Reject
        Button acceptBtn = new Button("✅  Accept");
        acceptBtn.setStyle(
                "-fx-background-color: rgba(16,185,129,0.18); -fx-text-fill:" + GREEN + ";" +
                        "-fx-background-radius:9; -fx-padding:8 14; -fx-cursor:hand; -fx-font-weight:bold;" +
                        "-fx-border-color: rgba(16,185,129,0.35); -fx-border-width:1; -fx-border-radius:9;"
        );
        Button rejectBtn = new Button("❌  Reject");
        rejectBtn.setStyle(
                "-fx-background-color: rgba(244,63,94,0.15); -fx-text-fill:" + ROSE + ";" +
                        "-fx-background-radius:9; -fx-padding:8 14; -fx-cursor:hand; -fx-font-weight:bold;" +
                        "-fx-border-color: rgba(244,63,94,0.30); -fx-border-width:1; -fx-border-radius:9;"
        );

        acceptBtn.setOnAction(e ->
                new Alert(Alert.AlertType.CONFIRMATION, "Accept for " + iv.getJob_offer() + "?")
                        .showAndWait().ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                try {
                                    interviewService.updateStatus(iv.getId(), "Accepted");
                                    iv.setStatus("Accepted");
                                    statusBadge.setText("Accepted");
                                    statusBadge.setStyle(statusStyle("Accepted")[0]);
                                    acceptBtn.setDisable(true); rejectBtn.setDisable(true);
                                    loadAcceptedCandidates();
                                } catch (SQLException ex) { showAlert("Error", "Could not update status."); }
                            }
                        })
        );
        rejectBtn.setOnAction(e ->
                new Alert(Alert.AlertType.CONFIRMATION, "Reject for " + iv.getJob_offer() + "?")
                        .showAndWait().ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                try {
                                    interviewService.updateStatus(iv.getId(), "Rejected");
                                    iv.setStatus("Rejected");
                                    statusBadge.setText("Rejected");
                                    statusBadge.setStyle(statusStyle("Rejected")[0]);
                                    acceptBtn.setDisable(true); rejectBtn.setDisable(true);
                                } catch (SQLException ex) { showAlert("Error", "Could not update status."); }
                            }
                        })
        );

        if ("Accepted".equals(status) || "Rejected".equals(status)) {
            acceptBtn.setDisable(true); rejectBtn.setDisable(true);
        }

        HBox actionRow = new HBox(10, notesBtn, acceptBtn, rejectBtn);
        actionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        if ("Accepted".equals(status) || "Pending".equals(status)) {
            Button chatBtn = new Button("💬  Chat");
            chatBtn.setStyle(
                    "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill:" + GREEN + ";" +
                            "-fx-background-radius:9; -fx-padding:8 14; -fx-cursor:hand; -fx-font-weight:bold;" +
                            "-fx-border-color: rgba(16,185,129,0.30); -fx-border-width:1; -fx-border-radius:9;"
            );
            int unread = 0;
            try { unread = messageService.countUnread(iv.getId(), "RECRUITER"); }
            catch (SQLException ignored) {}
            StackPane chatStack = new StackPane(chatBtn);
            if (unread > 0) {
                Label badge = new Label(String.valueOf(unread));
                badge.setStyle(
                        "-fx-background-color:" + ROSE + "; -fx-text-fill:white;" +
                                "-fx-background-radius:10; -fx-font-size:10px;" +
                                "-fx-padding:1 5; -fx-font-weight:bold;"
                );
                StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
                StackPane.setMargin(badge, new javafx.geometry.Insets(-5,-5,0,0));
                chatStack.getChildren().add(badge);
                chatBtn.setOnAction(ev -> { badge.setVisible(false); openChat(iv); });
            } else {
                chatBtn.setOnAction(ev -> openChat(iv));
            }
            actionRow.getChildren().add(chatStack);
        }

        details.getChildren().addAll(jobLabel, interviewerLabel, notesLabel,
                sentimentLabel, sentimentBarBox, actionRow);

        summary.setOnMouseClicked(e -> {
            boolean showing = details.isVisible();
            details.setVisible(!showing); details.setManaged(!showing);
            expandHint.setText(showing ? "▼  Details" : "▲  Details");
        });

        card.getChildren().addAll(summary, details);
        return card;
    }

    // ── Sentiment bar ─────────────────────────────────────────────────────────
    private HBox createSentimentBar(String sentimentResult) {
        String lower = sentimentResult.toLowerCase();
        String color = lower.contains("positive") ? GREEN
                : lower.contains("negative") ? ROSE : AMBER;
        double progress;
        try {
            int start = sentimentResult.indexOf("(") + 1;
            int end   = sentimentResult.indexOf("%");
            progress  = Double.parseDouble(sentimentResult.substring(start, end).trim()) / 100.0;
        } catch (Exception e) { progress = 0.5; }

        StackPane track = new StackPane();
        track.setPrefSize(200, 8);
        track.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius:4;");
        StackPane fill = new StackPane();
        fill.setPrefSize(200 * progress, 8);
        fill.setStyle("-fx-background-color:" + color + "; -fx-background-radius:4;");
        fill.setTranslateX(-(200 * (1 - progress)) / 2);
        track.getChildren().add(fill);

        Label pct = new Label(String.format("%.0f%%", progress * 100));
        pct.setStyle("-fx-font-size:11px; -fx-text-fill:" + TEXT_SOFT + ";");

        HBox bar = new HBox(8, track, pct);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void styleField(TextField f) {
        f.setStyle(
                "-fx-background-color: rgba(15,23,42,0.70);" +
                        "-fx-border-color: rgba(99,102,241,0.25); -fx-border-radius:8;" +
                        "-fx-background-radius:8; -fx-text-fill:white;" +
                        "-fx-prompt-text-fill:#64748b; -fx-padding:6 10;"
        );
    }

    private void styleFieldReadOnly(TextField f) {
        f.setStyle(
                "-fx-background-color: rgba(15,23,42,0.40);" +
                        "-fx-border-color: rgba(99,102,241,0.12); -fx-border-radius:8;" +
                        "-fx-background-radius:8; -fx-text-fill:" + INDIGO_DIM + ";" +
                        "-fx-padding:6 10;"
        );
    }

    /** Returns [badgeStyle] for a given interview status */
    private String[] statusStyle(String status) {
        return switch (status) {
            case "Accepted" -> new String[]{
                    "-fx-background-color: rgba(16,185,129,0.20); -fx-text-fill:" + GREEN + ";" +
                            "-fx-padding:4 12; -fx-background-radius:20;" +
                            "-fx-font-size:12px; -fx-font-weight:bold;" +
                            "-fx-border-color: rgba(16,185,129,0.40); -fx-border-width:1; -fx-border-radius:20;"
            };
            case "Rejected" -> new String[]{
                    "-fx-background-color: rgba(244,63,94,0.18); -fx-text-fill:" + ROSE + ";" +
                            "-fx-padding:4 12; -fx-background-radius:20;" +
                            "-fx-font-size:12px; -fx-font-weight:bold;" +
                            "-fx-border-color: rgba(244,63,94,0.35); -fx-border-width:1; -fx-border-radius:20;"
            };
            default -> new String[]{
                    "-fx-background-color: rgba(245,158,11,0.18); -fx-text-fill:" + AMBER + ";" +
                            "-fx-padding:4 12; -fx-background-radius:20;" +
                            "-fx-font-size:12px; -fx-font-weight:bold;" +
                            "-fx-border-color: rgba(245,158,11,0.35); -fx-border-width:1; -fx-border-radius:20;"
            };
        };
    }

    private void openChat(Interview iv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            ChatController ctrl = loader.getController();
            ctrl.setInterview(iv, "RECRUITER");
            Stage stage = new Stage();
            stage.setTitle("Interview Chat");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // ═══════════════════════════════════════════════════════════════════════════
    // ACCEPTED CANDIDATES TAB — reveals full identity after interview accepted
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadAcceptedCandidates() {
        if (acceptedContainer == null) return;
        acceptedContainer.getChildren().clear();

        // Filter allInterviews for Accepted ones
        List<Interview> accepted = new ArrayList<>();
        for (Interview iv : allInterviews) {
            if ("Accepted".equalsIgnoreCase(iv.getStatus())) {
                accepted.add(iv);
            }
        }

        if (accepted.isEmpty()) {
            Label none = new Label("No candidates have been accepted yet.");
            none.setStyle(
                    "-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 14px;" +
                            "-fx-padding: 30 0; -fx-alignment: center;"
            );
            acceptedContainer.getChildren().add(none);
            return;
        }

        for (Interview iv : accepted) {
            try {
                // JOIN score → user to get full candidate info
                User candidate = null;
                try (java.sql.PreparedStatement ps = Utils.Mydb.getInstance().getConnection()
                        .prepareStatement(
                                "SELECT u.id, u.nom, u.prenom, u.mail, u.username, " +
                                        "       u.phone, u.skills, u.experience, u.diplomas " +
                                        "FROM score s " +
                                        "JOIN user u ON s.id_user = u.id " +
                                        "WHERE s.id_score = ?")) {
                    ps.setLong(1, iv.getIdScore());
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        candidate = new User();
                        candidate.setId(rs.getInt("id"));
                        candidate.setNom(rs.getString("nom"));
                        candidate.setPrenom(rs.getString("prenom"));
                        candidate.setEmail(rs.getString("mail"));
                        candidate.setUsername(rs.getString("username"));
                        candidate.setPhone(rs.getString("phone"));
                        candidate.setSkills(rs.getString("skills"));
                        candidate.setExperience(rs.getString("experience"));
                        candidate.setDiplomas(rs.getString("diplomas"));
                    }
                }
                acceptedContainer.getChildren().add(buildAcceptedCard(iv, candidate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private javafx.scene.layout.VBox buildAcceptedCard(Interview iv, User u) {
        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(0);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: rgba(16,185,129,0.35);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.30),12,0,0,4);"
        );

        // ── Header ────────────────────────────────────────────────────────────
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(16);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new javafx.geometry.Insets(18, 24, 18, 24));
        header.setStyle(
                "-fx-background-color: rgba(16,185,129,0.10);" +
                        "-fx-background-radius: 18 18 0 0;"
        );

        // Avatar circle with initials
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.setPrefSize(52, 52); avatar.setMinSize(52, 52);
        avatar.setStyle(
                "-fx-background-color: linear-gradient(to bottom right,#10b981,#06b6d4);" +
                        "-fx-background-radius: 50;"
        );
        String initials = "";
        if (u != null) {
            if (u.getNom()    != null && !u.getNom().isEmpty())    initials += u.getNom().substring(0,1).toUpperCase();
            if (u.getPrenom() != null && !u.getPrenom().isEmpty()) initials += u.getPrenom().substring(0,1).toUpperCase();
        }
        Label avLbl = new Label(initials.isEmpty() ? "?" : initials);
        avLbl.setStyle("-fx-text-fill:white; -fx-font-weight:bold; -fx-font-size:17px;");
        avatar.getChildren().add(avLbl);

        // Name + username
        javafx.scene.layout.VBox nameBox = new javafx.scene.layout.VBox(4);
        javafx.scene.layout.HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);
        String fullName = u != null
                ? ((u.getNom()    != null ? u.getNom()    : "") + " " +
                (u.getPrenom() != null ? u.getPrenom() : "")).trim()
                : "Unknown";
        Label nameLbl = new Label(fullName.isEmpty() ? "Unknown" : fullName);
        nameLbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        String uname = (u != null && u.getUsername() != null) ? "@" + u.getUsername() : "";
        Label usernameLbl = new Label(uname);
        usernameLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#10b981;");
        nameBox.getChildren().addAll(nameLbl, usernameLbl);

        // Badge
        Label badge = new Label("✓  Interview Accepted");
        badge.setStyle(
                "-fx-background-color:rgba(16,185,129,0.15); -fx-text-fill:#10b981;" +
                        "-fx-padding:5 16; -fx-background-radius:20;" +
                        "-fx-font-size:12px; -fx-font-weight:bold;"
        );
        header.getChildren().addAll(avatar, nameBox, badge);

        // ── Details grid ──────────────────────────────────────────────────────
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(30); grid.setVgap(14);
        grid.setPadding(new javafx.geometry.Insets(22, 24, 22, 24));

        String[][] rows = {
                {"📧  Email",       u != null && u.getEmail()      != null ? u.getEmail()      : "—"},
                {"📱  Phone",       u != null && u.getPhone()      != null ? u.getPhone()      : "—"},
                {"💼  Job Offer",   iv.getJob_offer() != null ? iv.getJob_offer() : "—"},
                {"📅  Interview",   iv.getDate() != null
                        ? iv.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm"))
                        : "—"},
                {"🛠  Skills",      u != null && u.getSkills()     != null ? u.getSkills()     : "—"},
                {"🎓  Education",   u != null && u.getDiplomas()   != null ? u.getDiplomas()   : "—"},
                {"💡  Experience",  u != null && u.getExperience() != null ? u.getExperience() : "—"},
        };

        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-size:11px; -fx-font-weight:bold; -fx-text-fill:rgba(255,255,255,0.45); -fx-min-width:130;");
            Label val = new Label(rows[i][1]);
            val.setStyle("-fx-font-size:13px; -fx-text-fill:white;");
            val.setWrapText(true);
            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }

        // Separator
        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.07);");
        sep.setPadding(new javafx.geometry.Insets(0, 24, 0, 24));

        card.getChildren().addAll(header, sep, grid);
        return card;
    }


}