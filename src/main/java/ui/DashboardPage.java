package ui;

import Model.Role;
import Model.User;
import Service.CandidatureService;
import Service.JobOfferService;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import ui.LeaderboardPanel;
import Controller.LeaderboardController;

public class DashboardPage {

    private BorderPane root;
    private TableView<User> usersTable;
    private Label recrutersCountLabel;
    private Label clientsCountLabel;
    private Label adminsCountLabel;
    private Button showAllBtn;
    private Button showAdminsBtn;
    private Button showRecrutersBtn;
    private Button showClientsBtn;
    private userservice         userService;
    private JobOfferService     jobOfferService;
    private CandidatureService  candidatureService;
    private ObservableList<User> usersList;

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final String SIDEBAR_BG    = "#0f172a";
    private static final String ACCENT_INDIGO = "#6366f1";
    private static final String ACCENT_CYAN   = "#06b6d4";
    private static final String ACCENT_GREEN  = "#10b981";
    private static final String ACCENT_AMBER  = "#f59e0b";
    private static final String ACCENT_ROSE   = "#f43f5e";
    private static final String PAGE_BG       = "#0f172a";
    private static final String CARD_BG       = "#1e293b";

    // Active nav tracking
    private Button activeNavBtn;
    private Button navDashboard;
    private Button navUsers;
    private Button navJobOffers;
    private Button navApplications;
    private Button navMessages;
    private Button navLeaderboard;
    private Button navInterviews;
    private Button navProfile;
    private Button navSettings;

    public DashboardPage() {
        userService        = new userservice();
        jobOfferService    = new JobOfferService();
        candidatureService = new CandidatureService();
        usersList          = FXCollections.observableArrayList();
        createUI();
        showAllUsers();
        updateCounts();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ROOT
    // ─────────────────────────────────────────────────────────────────────────

    private void createUI() {
        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(createDashboardHomeView());
        root.setStyle("-fx-background-color: " + PAGE_BG + ";");
        javafx.application.Platform.runLater(() ->
                BlindHireApp.getPrimaryStage().setMaximized(true));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LEFT SIDEBAR
    // ─────────────────────────────────────────────────────────────────────────

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(255);
        sidebar.setStyle(
                "-fx-background-color: " + SIDEBAR_BG + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 4, 0);"
        );

        // ── LOGO SECTION ──────────────────────────────────────────────────────
        HBox logoRow = new HBox(12);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(28, 20, 22, 20));
        logoRow.setStyle(
                "-fx-border-color: rgba(255,255,255,0.07);" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Logo icon square (swap content for ImageView when path is ready)
        StackPane logoSquare = new StackPane();
        logoSquare.setPrefSize(52, 52);
        logoSquare.setMinSize(52, 52);
        logoSquare.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + ACCENT_INDIGO + ", " + ACCENT_CYAN + ");" +
                        "-fx-background-radius: 14;"
        );
        // ── To use the real logo image, replace the label below with:
        //    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(
        //        new javafx.scene.image.Image(getClass().getResourceAsStream("/blindhire_logo.png")));
        //    iv.setFitWidth(34); iv.setFitHeight(34); iv.setPreserveRatio(true);
        //    logoSquare.getChildren().add(iv);
        Label bh = new Label("BH");
        bh.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        logoSquare.getChildren().add(bh);

        VBox brandText = new VBox(2);
        brandText.setAlignment(Pos.CENTER_LEFT);
        Label brandName = new Label("BlindHire");
        brandName.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI Semibold', Arial;");
        Label brandSub  = new Label("Admin Console");
        brandSub.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 11px;");
        brandText.getChildren().addAll(brandName, brandSub);
        logoRow.getChildren().addAll(logoSquare, brandText);

        // ── USER PROFILE CARD ─────────────────────────────────────────────────
        User currentUser = userService.getCurrentUser();
        String nom    = currentUser != null && currentUser.getNom()    != null ? currentUser.getNom()    : "";
        String prenom = currentUser != null && currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String initials    = (!nom.isEmpty() ? nom.substring(0,1).toUpperCase() : "") +
                (!prenom.isEmpty() ? prenom.substring(0,1).toUpperCase() : "");
        String displayName = currentUser != null ? currentUser.getDisplayName() : "Admin";
        String roleStr     = currentUser != null && currentUser.getRole() != null
                ? capitalize(currentUser.getRole().name()) : "Admin";

        HBox profileCard = new HBox(12);
        profileCard.setAlignment(Pos.CENTER_LEFT);
        profileCard.setPadding(new Insets(14));
        profileCard.setStyle(
                "-fx-background-color: rgba(99,102,241,0.12);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: rgba(99,102,241,0.25);" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;"
        );
        VBox.setMargin(profileCard, new Insets(14, 14, 0, 14));

        // Avatar + online dot
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(42, 42); avatarPane.setMinSize(42, 42);
        StackPane avatarCircle = new StackPane();
        avatarCircle.setPrefSize(42, 42);
        avatarCircle.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + ACCENT_INDIGO + ", " + ACCENT_CYAN + ");" +
                        "-fx-background-radius: 50;"
        );
        Label initialsLbl = new Label(initials.isEmpty() ? "?" : initials);
        initialsLbl.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        avatarCircle.getChildren().add(initialsLbl);
        Circle onlineDot = new Circle(5.5, Color.web(ACCENT_GREEN));
        onlineDot.setStroke(Color.web(SIDEBAR_BG));
        onlineDot.setStrokeWidth(2);
        StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);
        avatarPane.getChildren().addAll(avatarCircle, onlineDot);

        String badgeBg = switch (roleStr.toLowerCase()) {
            case "admin"     -> "rgba(99,102,241,0.3)";
            case "recruteur" -> "rgba(6,182,212,0.3)";
            default          -> "rgba(16,185,129,0.3)";
        };
        String badgeFg = switch (roleStr.toLowerCase()) {
            case "admin"     -> "#a5b4fc";
            case "recruteur" -> "#67e8f9";
            default          -> "#6ee7b7";
        };
        VBox nameBox = new VBox(4);
        Label nameLbl = new Label(displayName);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-max-width: 145;");
        Label roleLbl = new Label(roleStr);
        roleLbl.setStyle(
                "-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg + ";" +
                        "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 8;"
        );
        nameBox.getChildren().addAll(nameLbl, roleLbl);
        profileCard.getChildren().addAll(avatarPane, nameBox);

        // ── NAV SECTION ───────────────────────────────────────────────────────
        Label navSectionLabel = new Label("NAVIGATION");
        navSectionLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.22);" +
                        "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-padding: 20 18 6 18;"
        );

        navDashboard    = createNavBtn("📊", "Dashboard",    true);
        navUsers        = createNavBtn("👥", "Users",         false);
        // Only admins can see the Users section
        User currentUserForNav = userService.getCurrentUser();
        boolean isAdmin = currentUserForNav != null && currentUserForNav.getRole() == Role.admin;
        navUsers.setVisible(isAdmin);
        navUsers.setManaged(isAdmin);
        navJobOffers    = createNavBtn("💼", "Job Offers",    false);
        navApplications = createNavBtn("📋", "Applications",  false);
        navMessages     = createNavBtn("💬", "Messages",      false);
        navLeaderboard  = createNavBtn("🏆", "Leaderboard",   false);
        navInterviews   = createNavBtn("📅", "Interviews",     false);
        navProfile      = createNavBtn("👤", "Profile",       false);
        navSettings     = createNavBtn("⚙",  "Settings",      false);

        navDashboard.setOnAction(e -> { setActiveNav(navDashboard); root.setCenter(createDashboardHomeView()); });
        navUsers.setOnAction(e     -> { setActiveNav(navUsers);     root.setCenter(createUsersTableView()); showAllUsers(); updateCounts(); });
        navJobOffers.setOnAction(e -> { setActiveNav(navJobOffers); handleJobOffers(); });
        navApplications.setOnAction(e -> { setActiveNav(navApplications); handleComingSoon("Applications"); });
        navMessages.setOnAction(e     -> { setActiveNav(navMessages);     handleComingSoon("Messages"); });
        navLeaderboard.setOnAction(e  -> { setActiveNav(navLeaderboard);  handleLeaderboard(); });
        navInterviews.setOnAction(e   -> { setActiveNav(navInterviews);   handleInterviews(); });
        navProfile.setOnAction(e      -> { setActiveNav(navProfile);      handleComingSoon("Profile"); });
        navSettings.setOnAction(e     -> { setActiveNav(navSettings);     handleComingSoon("Settings"); });

        VBox navBox = new VBox(3);
        navBox.setPadding(new Insets(0, 12, 0, 12));
        navBox.getChildren().addAll(navDashboard, navUsers, navJobOffers, navApplications, navMessages, navLeaderboard, navInterviews, navProfile, navSettings);

        // ── SPACER ────────────────────────────────────────────────────────────
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── LOGOUT ────────────────────────────────────────────────────────────
        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(logoutBtn, new Insets(0, 12, 20, 12));
        String logoutNormal = "-fx-background-color: rgba(244,63,94,0.12); -fx-background-radius: 10; -fx-text-fill: rgba(244,63,94,0.85); -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
        String logoutHover  = "-fx-background-color: rgba(244,63,94,0.22); -fx-background-radius: 10; -fx-text-fill: #f43f5e; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
        logoutBtn.setStyle(logoutNormal);
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(logoutHover));
        logoutBtn.setOnMouseExited(e  -> logoutBtn.setStyle(logoutNormal));
        logoutBtn.setOnAction(e -> handleLogout());

        sidebar.getChildren().addAll(logoRow, profileCard, navSectionLabel, navBox, spacer, logoutBtn);
        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  NAV BUTTON HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private Button createNavBtn(String icon, String text, boolean active) {
        Button btn = new Button(icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        applyNavStyle(btn, active);
        if (active) activeNavBtn = btn;
        btn.setOnMouseEntered(e -> { if (btn != activeNavBtn) applyNavHover(btn); });
        btn.setOnMouseExited(e  -> { if (btn != activeNavBtn) applyNavStyle(btn, false); });
        return btn;
    }

    private void applyNavStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + ACCENT_INDIGO + ", " + ACCENT_INDIGO + "cc);" +
                            "-fx-background-radius: 11; -fx-text-fill: white;" +
                            "-fx-font-size: 13.5px; -fx-font-weight: bold;" +
                            "-fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.4), 10, 0, 0, 3);"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-background-radius: 11;" +
                            "-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 13.5px;" +
                            "-fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;"
            );
        }
    }

    private void applyNavHover(Button btn) {
        btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 11;" +
                        "-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 13.5px;" +
                        "-fx-padding: 11 16; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;"
        );
    }

    private void setActiveNav(Button active) {
        activeNavBtn = active;
        for (Button b : new Button[]{navDashboard, navUsers, navJobOffers, navApplications, navMessages, navLeaderboard, navInterviews, navProfile, navSettings})
            applyNavStyle(b, b == active);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DASHBOARD HOME VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private ScrollPane createDashboardHomeView() {
        VBox content = new VBox(28);
        content.setPadding(new Insets(36, 40, 40, 40));
        content.setStyle("-fx-background-color: " + PAGE_BG + ";");

        // ── Header ───────────────────────────────────────────────────────────
        User current = userService.getCurrentUser();
        String name = (current != null && current.getNom() != null) ? current.getNom() : "Admin";

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        VBox greetBox = new VBox(5);
        Label greetLbl = new Label(timeGreeting() + ", " + name + " 👋");
        greetLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subLbl = new Label("Here's what's happening on BlindHire today.");
        subLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.40);");
        greetBox.getChildren().addAll(greetLbl, subLbl);

        Region hSp = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);

        VBox dateBadge = new VBox(2);
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setPadding(new Insets(12, 24, 12, 24));
        dateBadge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 16;" +
                        "-fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 16; -fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8,0,0,2);"
        );
        LocalDate today = LocalDate.now();
        Label dayLbl = new Label(String.valueOf(today.getDayOfMonth()));
        dayLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_INDIGO + ";");
        Label monLbl = new Label(today.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)).toUpperCase());
        monLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.40); -fx-font-weight: bold;");
        dateBadge.getChildren().addAll(dayLbl, monLbl);
        headerRow.getChildren().addAll(greetBox, hSp, dateBadge);

        // ── 4 KPI cards ───────────────────────────────────────────────────────
        int totalAdmins    = safe(userService::getAdminCount);
        int totalRecs      = safe(userService::getRecruiterCount);
        int totalClients   = safe(userService::getClientCount);
        int totalUsers     = totalAdmins + totalRecs + totalClients;
        int totalJobs      = safe(jobOfferService::getJobCount);
        int totalApps = 0, pending = 0, accepted = 0, rejected = 0;
        try {
            totalApps = candidatureService.getAllCandidatures().size();
            accepted  = candidatureService.countByStatus("accepted");
            pending   = candidatureService.countByStatus("pending");
            rejected  = candidatureService.countByStatus("rejected");
        } catch (Exception ignored) {}

        HBox kpiRow = new HBox(18);
        kpiRow.getChildren().addAll(
                kpiCard("👥", "Total Users",    String.valueOf(totalUsers),  "all roles combined", ACCENT_INDIGO, "#ede9fe"),
                kpiCard("💼", "Job Offers",     String.valueOf(totalJobs),   "active listings",    ACCENT_CYAN,   "#ecfeff"),
                kpiCard("📋", "Applications",   String.valueOf(totalApps),   "all time",           ACCENT_GREEN,  "#d1fae5"),
                kpiCard("⏳", "Pending Review", String.valueOf(pending),     "awaiting decision",  ACCENT_AMBER,  "#fef3c7")
        );
        for (javafx.scene.Node n : kpiRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ── Secondary row ─────────────────────────────────────────────────────
        HBox secondRow = new HBox(18);

        VBox breakdownCard = new VBox(16);
        breakdownCard.setPadding(new Insets(24, 26, 24, 26));
        breakdownCard.setStyle(cardStyle());
        breakdownCard.getChildren().add(sectionTitle("👥  User Breakdown"));
        breakdownCard.getChildren().add(statRow("Admins",     totalAdmins,  ACCENT_INDIGO));
        breakdownCard.getChildren().add(statRow("Recruiters", totalRecs,    ACCENT_CYAN));
        breakdownCard.getChildren().add(statRow("Clients",    totalClients, ACCENT_GREEN));
        HBox.setHgrow(breakdownCard, Priority.ALWAYS);

        VBox appStatusCard = new VBox(16);
        appStatusCard.setPadding(new Insets(24, 26, 24, 26));
        appStatusCard.setStyle(cardStyle());
        appStatusCard.getChildren().add(sectionTitle("📊  Application Status"));
        appStatusCard.getChildren().add(statRow("Accepted", accepted, ACCENT_GREEN));
        appStatusCard.getChildren().add(statRow("Pending",  pending,  ACCENT_AMBER));
        appStatusCard.getChildren().add(statRow("Rejected", rejected, ACCENT_ROSE));
        HBox.setHgrow(appStatusCard, Priority.ALWAYS);

        VBox sysCard = systemStatusCard();
        HBox.setHgrow(sysCard, Priority.ALWAYS);

        secondRow.getChildren().addAll(breakdownCard, appStatusCard, sysCard);

        // ── Bottom row — acceptance rate + indigo glance ──────────────────────
        HBox bottomRow = new HBox(18);

        VBox rateCard = new VBox(12);
        rateCard.setPadding(new Insets(24, 26, 24, 26));
        rateCard.setStyle(cardStyle());
        rateCard.getChildren().add(sectionTitle("✅  Acceptance Rate"));
        double rate = totalApps > 0 ? accepted * 100.0 / totalApps : 0;
        Label rateLbl = new Label(totalApps > 0 ? String.format("%.0f%%", rate) : "N/A");
        rateLbl.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_GREEN + ";");
        Label rateSub = new Label(totalApps > 0 ? accepted + " accepted  ·  " + rejected + " rejected  ·  " + pending + " pending" : "No applications yet");
        rateSub.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.35);");
        rateCard.getChildren().addAll(rateLbl, rateSub);
        HBox.setHgrow(rateCard, Priority.ALWAYS);

        VBox glanceCard = new VBox(14);
        glanceCard.setPadding(new Insets(24, 26, 24, 26));
        glanceCard.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + ACCENT_INDIGO + ", #818cf8);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.35), 20, 0, 0, 6);"
        );
        Label glanceTitle = new Label("🚀  Platform at a Glance");
        glanceTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        glanceCard.getChildren().addAll(
                glanceTitle,
                glanceRow("Total Users",  totalUsers),
                glanceRow("Job Offers",   totalJobs),
                glanceRow("Applications", totalApps)
        );
        HBox.setHgrow(glanceCard, Priority.ALWAYS);
        bottomRow.getChildren().addAll(rateCard, glanceCard);

        // ── Quick access ─────────────────────────────────────────────────────
        Label qaLabel = new Label("Quick Access");
        qaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox qaRow = new HBox(14);
        qaRow.getChildren().addAll(
                quickBtn("👥  Manage Users",  () -> { setActiveNav(navUsers);     root.setCenter(createUsersTableView()); showAllUsers(); }),
                quickBtn("💼  Job Offers",     () -> { setActiveNav(navJobOffers); handleJobOffers(); }),
                quickBtn("📋  Applications",   () -> handleComingSoon("Applications")),
                quickBtn("➕  Add New User",   this::handleAddUser)
        );
        for (javafx.scene.Node n : qaRow.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        content.getChildren().addAll(headerRow, kpiRow, secondRow, bottomRow, qaLabel, qaRow);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + PAGE_BG + "; -fx-border-width: 0; -fx-background: " + PAGE_BG + ";");
        return scroll;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  USERS TABLE VIEW
    // ─────────────────────────────────────────────────────────────────────────

    private VBox createUsersTableView() {
        VBox main = new VBox(18);
        main.setPadding(new Insets(36, 40, 40, 40));
        main.setStyle("-fx-background-color: " + PAGE_BG + ";");
        VBox.setVgrow(main, Priority.ALWAYS);

        Label headerLbl = new Label("👥  User Management");
        headerLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subLbl = new Label("View, filter, edit and manage all platform users");
        subLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.40);");

        HBox statsBox = new HBox(16);
        VBox adminsCard    = miniStatsCard("Admins",     ACCENT_INDIGO, "👤");
        VBox recrutersCard = miniStatsCard("Recruiters", ACCENT_CYAN,   "🧑‍💼");
        VBox clientsCard   = miniStatsCard("Clients",    ACCENT_GREEN,  "👥");
        adminsCountLabel    = (Label) ((VBox) adminsCard.getChildren().get(1)).getChildren().get(1);
        recrutersCountLabel = (Label) ((VBox) recrutersCard.getChildren().get(1)).getChildren().get(1);
        clientsCountLabel   = (Label) ((VBox) clientsCard.getChildren().get(1)).getChildren().get(1);
        statsBox.getChildren().addAll(adminsCard, recrutersCard, clientsCard);

        VBox tableCard = new VBox();
        tableCard.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 10, 0, 0, 4); -fx-border-color: rgba(255,255,255,0.09); -fx-border-width: 1; -fx-border-radius: 16;");
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        usersTable = createUsersTable();
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        tableCard.getChildren().add(usersTable);

        main.getChildren().addAll(new VBox(4, headerLbl, subLbl), new Label("Overview") {{
            setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.70);");
        }}, statsBox, createFilterRow(), tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
        return main;
    }

    // Kept as createMainContent() alias for refreshTable()
    private VBox createMainContent() { return createUsersTableView(); }

    // ─────────────────────────────────────────────────────────────────────────
    //  CARD / UI COMPONENT HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private VBox kpiCard(String icon, String title, String value, String sub, String color, String lightBg) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24, 26, 24, 26));
        card.setStyle(cardStyle());

        StackPane bubble = new StackPane();
        bubble.setPrefSize(46, 46); bubble.setMaxSize(46, 46);
        // dim the bubble for dark bg
        bubble.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 14;");
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size: 22px;");
        bubble.getChildren().add(iconLbl);

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.85);");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.35);");

        card.getChildren().addAll(bubble, valLbl, titleLbl, subLbl);

        String base  = cardStyle();
        String hover = "-fx-background-color: " + color + "18; -fx-background-radius: 20;" +
                "-fx-border-color: " + color + "; -fx-border-width: 1.5; -fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 14,0,0,4);" +
                "-fx-scale-x: 1.02; -fx-scale-y: 1.02;";
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e  -> card.setStyle(base));
        return card;
    }

    private HBox statRow(String label, int count, String color) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5, Color.web(color));
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.60); -fx-pref-width: 90;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label cnt = new Label(String.valueOf(count));
        cnt.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        row.getChildren().addAll(dot, lbl, sp, cnt);
        return row;
    }

    private HBox glanceRow(String label, int count) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.75);");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label cnt = new Label(String.valueOf(count));
        cnt.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        row.getChildren().addAll(lbl, sp, cnt);
        return row;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        return l;
    }

    private VBox systemStatusCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(24, 26, 24, 26));
        card.setStyle(cardStyle());
        card.getChildren().add(sectionTitle("📡  System Status"));
        card.getChildren().add(statusRow("Database",      "Connected", ACCENT_GREEN, ACCENT_GREEN));
        card.getChildren().add(statusRow("Email Service", "Active",    ACCENT_GREEN, ACCENT_GREEN));
        card.getChildren().add(statusRow("AI Matching",   "Ready",     ACCENT_CYAN,  ACCENT_CYAN));
        card.getChildren().add(statusRow("Notifications", "Enabled",   ACCENT_GREEN, ACCENT_GREEN));
        return card;
    }

    private HBox statusRow(String label, String status, String dotColor, String badgeColor) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(5, Color.web(dotColor));
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.55);");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        Label badge = new Label(status);
        badge.setStyle(
                "-fx-background-color: " + badgeColor + "22; -fx-text-fill: " + badgeColor + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8;"
        );
        row.getChildren().addAll(dot, lbl, badge);
        return row;
    }

    private Button quickBtn(String label, Runnable action) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        String normal = "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: rgba(255,255,255,0.80); -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.10); -fx-border-width: 1; -fx-border-radius: 14; -fx-padding: 16 20; -fx-cursor: hand;";
        String hover  = "-fx-background-color: rgba(99,102,241,0.18); -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 14; -fx-border-color: " + ACCENT_INDIGO + "; -fx-border-width: 1; -fx-border-radius: 14; -fx-padding: 16 20; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private String cardStyle() {
        return "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.09); -fx-border-width: 1; -fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 14,0,0,4);";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MINI STATS CARDS (for Users table view)
    // ─────────────────────────────────────────────────────────────────────────

    private VBox miniStatsCard(String title, String accentColor, String icon) {
        VBox card = new VBox(0);
        card.setPrefWidth(190);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 4); -fx-border-color: rgba(255,255,255,0.09); -fx-border-width: 1; -fx-border-radius: 16;");
        Rectangle bar = new Rectangle(190, 4);
        bar.setFill(Color.web(accentColor)); bar.setArcWidth(16); bar.setArcHeight(16);
        VBox inner = new VBox(8); inner.setPadding(new Insets(16, 18, 16, 18));
        HBox iconRow = new HBox(8); iconRow.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(icon); ico.setStyle("-fx-font-size: 18px;");
        Label ttl = new Label(title); ttl.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.55);");
        iconRow.getChildren().addAll(ico, ttl);
        Label cnt = new Label("0"); cnt.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        inner.getChildren().addAll(iconRow, cnt);
        card.getChildren().addAll(bar, inner);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FILTER ROW
    // ─────────────────────────────────────────────────────────────────────────

    private HBox createFilterRow() {
        showAllBtn       = createFilterBtn("All Users",  true);
        showAdminsBtn    = createFilterBtn("Admins",     false);
        showRecrutersBtn = createFilterBtn("Recruiters", false);
        showClientsBtn   = createFilterBtn("Clients",    false);

        showAllBtn.setOnAction(e       -> showAllUsers());
        showAdminsBtn.setOnAction(e    -> showAdmins());
        showRecrutersBtn.setOnAction(e -> showRecruters());
        showClientsBtn.setOnAction(e   -> showClients());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        Button addBtn = new Button("➕  Add User");
        addBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.20);" +
                        "-fx-text-fill: #a5b4fc;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(99,102,241,0.45);" +
                        "-fx-border-width: 1; -fx-border-radius: 10;" +
                        "-fx-padding: 9 20; -fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: " + ACCENT_INDIGO + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 1; -fx-border-radius: 10;" +
                        "-fx-padding: 9 20; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.40), 10, 0, 0, 2);"
        ));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.20);" +
                        "-fx-text-fill: #a5b4fc;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(99,102,241,0.45);" +
                        "-fx-border-width: 1; -fx-border-radius: 10;" +
                        "-fx-padding: 9 20; -fx-cursor: hand;"
        ));
        addBtn.setOnAction(e -> handleAddUser());

        return new HBox(10, showAllBtn, showAdminsBtn, showRecrutersBtn, showClientsBtn, sp, addBtn) {{
            setAlignment(Pos.CENTER_LEFT);
        }};
    }

    private Button createFilterBtn(String text, boolean active) {
        Button btn = new Button(text);
        applyFilterStyle(btn, active);
        return btn;
    }

    private void applyFilterStyle(Button btn, boolean active) {
        btn.setStyle(
                "-fx-background-color: " + (active ? ACCENT_INDIGO : "rgba(255,255,255,0.07)") + ";" +
                        "-fx-text-fill: "        + (active ? "white" : "rgba(255,255,255,0.55)") + ";" +
                        "-fx-font-size: 12px; -fx-font-weight: " + (active ? "bold" : "normal") + ";" +
                        "-fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"
        );
    }

    private void updateButtonStyles(Button active) {
        applyFilterStyle(showAllBtn,       showAllBtn       == active);
        applyFilterStyle(showAdminsBtn,    showAdminsBtn    == active);
        applyFilterStyle(showRecrutersBtn, showRecrutersBtn == active);
        applyFilterStyle(showClientsBtn,   showClientsBtn   == active);
    }

    /** Reusable white-text cell for dark table background. */
    private TableCell<User, String> whiteCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v);
                setStyle(empty ? "" : "-fx-text-fill: rgba(255,255,255,0.88); -fx-font-size: 13px; -fx-padding: 0 8;");
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABLE
    // ─────────────────────────────────────────────────────────────────────────

    private TableView<User> createUsersTable() {
        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;" +
                "-fx-control-inner-background: rgba(255,255,255,0.04);" +
                "-fx-control-inner-background-alt: rgba(255,255,255,0.02);" +
                "-fx-table-cell-border-color: rgba(255,255,255,0.06);");

        // Change resize policy to unconstrained to allow custom column widths
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Set a minimum width for the table to ensure buttons fit
        table.setMinWidth(1000);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50); idCol.setMaxWidth(50); idCol.setMinWidth(50);
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty); setText(empty || v == null ? null : String.valueOf(v));
                setStyle(empty ? "" : "-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 13px; -fx-padding: 0 8;");
            }
        });

        TableColumn<User, String> nomCol = new TableColumn<>("Last Name");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(120); nomCol.setMinWidth(100);
        nomCol.setCellFactory(col -> whiteCell());

        TableColumn<User, String> prenomCol = new TableColumn<>("First Name");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        prenomCol.setPrefWidth(120); prenomCol.setMinWidth(100);
        prenomCol.setCellFactory(col -> whiteCell());

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200); emailCol.setMinWidth(150);
        emailCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); setText(empty || v == null ? null : v);
                setStyle(empty ? "" : "-fx-text-fill: " + ACCENT_CYAN + "; -fx-font-size: 13px; -fx-padding: 0 8;");
            }
        });

        TableColumn<User, String> mdpCol = new TableColumn<>("Password");
        mdpCol.setCellValueFactory(new PropertyValueFactory<>("mdp"));
        mdpCol.setPrefWidth(120); mdpCol.setMinWidth(100);
        mdpCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : "••••••••");
                setStyle(empty ? "" : "-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 13px; -fx-padding: 0 8;");
            }
        });

        TableColumn<User, Role> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);
        roleCol.setMinWidth(90);

        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Role role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    return;
                }
                String c = switch (role.toString().toLowerCase()) {
                    case "admin"     -> ACCENT_INDIGO;
                    case "recruteur" -> ACCENT_CYAN;
                    default          -> ACCENT_GREEN;
                };
                Label b = new Label(role.toString());
                b.setStyle("-fx-background-color: " + c + "22; -fx-text-fill: " + c +
                        "; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(b);
            }
        });

        TableColumn<User, Void> editCol = new TableColumn<>("Edit");
        editCol.setPrefWidth(90);
        editCol.setMinWidth(80);
        editCol.setMaxWidth(100);

        editCol.setCellFactory(p -> new TableCell<>() {
            final Button btn = new Button("✎  Edit");
            {
                btn.setStyle("-fx-background-color: " + ACCENT_INDIGO +
                        "; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; " +
                        "-fx-font-size: 11px; -fx-padding: 5 12;");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean empty) {
                super.updateItem(i, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Wrap button in a container to ensure it fills the cell
                    HBox container = new HBox(btn);
                    container.setAlignment(Pos.CENTER);
                    container.setPrefWidth(90);
                    setGraphic(container);
                }
            }
        });

        TableColumn<User, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(100);
        deleteCol.setMinWidth(90);
        deleteCol.setMaxWidth(110);

        deleteCol.setCellFactory(p -> new TableCell<>() {
            final Button btn = new Button("🗑  Delete");
            {
                btn.setStyle("-fx-background-color: rgba(244,63,94,0.15); -fx-text-fill: " + ACCENT_ROSE +
                        "; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 11px; " +
                        "-fx-padding: 5 12;");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean empty) {
                super.updateItem(i, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Wrap button in a container to ensure it fills the cell
                    HBox container = new HBox(btn);
                    container.setAlignment(Pos.CENTER);
                    container.setPrefWidth(100);
                    setGraphic(container);
                }
            }
        });

        table.getColumns().addAll(idCol, nomCol, prenomCol, emailCol, mdpCol, roleCol, editCol, deleteCol);

        // Add a listener to ensure the table width accommodates all columns
        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            double totalWidth = 0;
            for (TableColumn<?, ?> col : table.getColumns()) {
                totalWidth += col.getWidth();
            }
            // If table is wider than total columns, distribute extra space
            if (newVal.doubleValue() > totalWidth && totalWidth > 0) {
                double extra = newVal.doubleValue() - totalWidth;
                // Give extra space to email column primarily
                emailCol.setPrefWidth(emailCol.getWidth() + extra * 0.7);
                nomCol.setPrefWidth(nomCol.getWidth() + extra * 0.15);
                prenomCol.setPrefWidth(prenomCol.getWidth() + extra * 0.15);
            }
        });

        return table;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DATA LOADING
    // ─────────────────────────────────────────────────────────────────────────

    private void showAllUsers()   { loadUsers(userService.getAllUsers());    if (showAllBtn       != null) updateButtonStyles(showAllBtn);       updateCounts(); }
    private void showAdmins()     { loadUsers(userService.getAdmins());      if (showAdminsBtn    != null) updateButtonStyles(showAdminsBtn); }
    private void showRecruters()  { loadUsers(userService.getRecruiters());  if (showRecrutersBtn != null) updateButtonStyles(showRecrutersBtn); }
    private void showClients()    { loadUsers(userService.getClients());     if (showClientsBtn   != null) updateButtonStyles(showClientsBtn); }

    private void loadUsers(java.util.List<User> users) {
        try {
            if (usersTable == null) return;
            usersList.clear(); usersList.addAll(users); usersTable.setItems(usersList);
        } catch (Exception e) { showError("Error loading users: " + e.getMessage()); }
    }

    private void updateCounts() {
        try {
            if (adminsCountLabel    != null) adminsCountLabel.setText(String.valueOf(userService.getAdminCount()));
            if (recrutersCountLabel != null) recrutersCountLabel.setText(String.valueOf(userService.getRecruiterCount()));
            if (clientsCountLabel   != null) clientsCountLabel.setText(String.valueOf(userService.getClientCount()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    private void handleEdit(User user) {
        EditPage ep = new EditPage(user, this);
        Stage s = new Stage(); s.setTitle("Edit User");
        s.setScene(new Scene(ep.getRoot(), 600, 700)); s.show();
    }

    private void handleDelete(User user) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirm Delete"); a.setHeaderText("Delete User");
        a.setContentText("Delete " + user.getNom() + " " + user.getPrenom() + "?");
        Optional<ButtonType> r = a.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            try { userService.deleteUser(user.getId()); showAllUsers(); }
            catch (Exception e) { showError("Error: " + e.getMessage()); }
        }
    }

    private void handleAddUser() {
        AddUserPage ap = new AddUserPage(this);
        Stage s = new Stage(); s.setTitle("Add New User");
        s.setScene(new Scene(ap.getRoot(), 600, 700)); s.show();
    }

    private void handleInterviews() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/interview.fxml"));
            javafx.scene.Parent fxmlRoot = loader.load();
            Controller.InterviewPageController ctrl = loader.getController();
            ctrl.setDashboardCenter(root);
            root.setCenter(fxmlRoot);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Label err = new javafx.scene.control.Label(
                    "Could not load Interviews page: " + e.getMessage());
            err.setStyle("-fx-text-fill:#f43f5e; -fx-padding:20; -fx-font-size:14px;");
            root.setCenter(err);
        }
    }

    private void handleLeaderboard() {
        LeaderboardPanel panel = new LeaderboardPanel();
        new LeaderboardController(panel, () -> {
            setActiveNav(navDashboard);
            root.setCenter(createDashboardHomeView());
        });
        root.setCenter(panel);
    }

    private void handleJobOffers() {
        try {
            javafx.fxml.FXMLLoader l = new javafx.fxml.FXMLLoader(getClass().getResource("/BackOffice/joboffer.fxml"));
            root.setCenter(l.load());
        } catch (Exception e) { e.printStackTrace(); showError("Could not load Job Offers: " + e.getMessage()); }
    }

    private void handleComingSoon(String feature) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(feature); a.setHeaderText(null);
        a.setContentText(feature + " feature coming soon!"); a.showAndWait();
    }

    private void handleLogout() {
        userService.setCurrentUser(null);
        BlindHireApp.loadScene(new WelcomePage().getRoot(), 960, 540);
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Error"); a.setContentText(msg); a.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UTIL
    // ─────────────────────────────────────────────────────────────────────────

    private String timeGreeting() {
        int h = LocalDateTime.now().getHour();
        if (h < 12) return "Good morning";
        if (h < 18) return "Good afternoon";
        return "Good evening";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @FunctionalInterface private interface IntSupplier { int get(); }
    private int safe(IntSupplier s) { try { return s.get(); } catch (Exception e) { return 0; } }

    public void refreshTable() { showAllUsers(); }
    public Parent getRoot()    { return root; }
}