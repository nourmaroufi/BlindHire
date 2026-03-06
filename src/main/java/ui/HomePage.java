package ui;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import Controller.FrontOffice.jobController;
import Model.Notification;
import Model.User;
import Service.NotificationCService;
import Service.userservice;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Popup;

import java.util.List;

import static ui.BlindHireApp.primaryStage;


public class HomePage {

    private BorderPane root;
    private User currentUser;
    private userservice userService;
    private Button notifBellBtn;
    private Label notifBadge;

    private final NotificationCService notificationService = new NotificationCService();

    private Popup notifPopup;
    private boolean popupOpen = false;
    private ListView<Notification> notifListView;
    public HomePage(User user) {
        this.currentUser = user;
        this.userService = new userservice();
        userService.setCurrentUser(user);
        createUI();
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #e8f5f3, #d5edf4, #e2eef8);");
        root.setLeft(createSidebar());
        root.setCenter(createMainContent());

        refreshNotifBadge(); // ✅ ADD THIS
    }

    // ─── SIDEBAR ──────────────────────────────────────────────────────────────

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-pref-width: 250;" +
                        "-fx-border-color: transparent rgba(0,0,0,0.07) transparent transparent;" +
                        "-fx-border-width: 0 1 0 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 18, 0, 4, 0);"
        );

        // ── TOP: Logo image + BlindHire text ─────────────────────────────────
        VBox topSection = new VBox(0);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(30, 16, 0, 16));

        // Logo image + text on same row
        HBox logoRow = new HBox(12);
        logoRow.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.image.ImageView logoImg = new javafx.scene.image.ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/images/blindhire_logo.png")
            );
            logoImg.setImage(img);
        } catch (Exception ex) {
            // fallback: no image
        }
        logoImg.setFitWidth(46); logoImg.setFitHeight(46);
        logoImg.setPreserveRatio(true);

        VBox logoTextBox = new VBox(2);
        Text logoTitle = new Text("BlindHire");
        logoTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 21));
        logoTitle.setFill(Color.web("#0f172a"));
        Text logoSub = new Text("RH Agency");
        logoSub.setFont(Font.font("Segoe UI", 11));
        logoSub.setFill(Color.web("#94a3b8"));
        logoTextBox.getChildren().addAll(logoTitle, logoSub);

        Region logoRowSpacer = new Region();
        HBox.setHgrow(logoRowSpacer, Priority.ALWAYS);

        // Notification bell — light blue style
        notifBellBtn = new Button();
        notifBellBtn.setOnAction(e -> handleToggleNotifications());
        String bellNormal = "-fx-background-color: #f1f5f9; -fx-background-radius: 50;" +
                "-fx-border-radius: 50; -fx-border-color: rgba(0,0,0,0.07); -fx-border-width: 1;" +
                "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; -fx-cursor: hand;";
        String bellHover = "-fx-background-color: #e2fdf8; -fx-background-radius: 50;" +
                "-fx-border-radius: 50; -fx-border-color: rgba(13,101,148,0.45); -fx-border-width: 1.5;" +
                "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(13,107,148,0.22), 8, 0, 0, 2);";
        notifBellBtn.setStyle(bellNormal);
        notifBellBtn.setOnMouseEntered(ev -> notifBellBtn.setStyle(bellHover));
        notifBellBtn.setOnMouseExited(ev  -> notifBellBtn.setStyle(bellNormal));
        javafx.scene.shape.SVGPath bellSvg = new javafx.scene.shape.SVGPath();
        bellSvg.setFill(Color.web("#0d9488"));
        bellSvg.setContent("M10 2a6 6 0 0 0-6 6v3.586l-.707.707A1 1 0 0 0 4 14h12a1 1 0 0 0 .707-1.707L16 11.586V8a6 6 0 0 0-6-6zm0 18a2 2 0 0 1-2-2h4a2 2 0 0 1-2 2z");
        bellSvg.setScaleX(0.9); bellSvg.setScaleY(0.9);
        notifBellBtn.setGraphic(bellSvg);
        notifBadge = new Label("0");
        notifBadge.setVisible(false); notifBadge.setManaged(false);
        notifBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;" +
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-background-radius: 10;" +
                "-fx-padding: 1 4; -fx-min-width: 16;");
        StackPane bellStack = new StackPane(notifBellBtn, notifBadge);
        StackPane.setAlignment(notifBadge, Pos.TOP_RIGHT);
        notifBadge.setTranslateX(3); notifBadge.setTranslateY(-3);

        logoRow.getChildren().addAll(logoImg, logoTextBox, logoRowSpacer, bellStack);

        // ── PROFILE SECTION: centered avatar + name + role ────────────────────
        VBox profileSection = new VBox(12);
        profileSection.setAlignment(Pos.CENTER);
        profileSection.setPadding(new Insets(28, 16, 26, 16));
        profileSection.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-border-color: #e2e8f0 transparent #e2e8f0 transparent;" +
                        "-fx-border-width: 1 0 1 0;"
        );

        String nom    = currentUser.getNom()    != null ? currentUser.getNom()    : "";
        String prenom = currentUser.getPrenom() != null ? currentUser.getPrenom() : "";
        String inits  = (nom.isEmpty()    ? "?" : nom.substring(0,1).toUpperCase()) +
                (prenom.isEmpty() ? ""  : prenom.substring(0,1).toUpperCase());

        StackPane avatarOuter = new StackPane();
        avatarOuter.setMinSize(72, 72); avatarOuter.setMaxSize(72, 72);
        avatarOuter.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #14aab8, #057995);" +
                        "-fx-background-radius: 999;" +
                        "-fx-effect: dropshadow(gaussian, rgba(13,132,148,0.5), 14, 0, 0, 4);"
        );
        Text initLbl = new Text(inits);
        initLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        initLbl.setFill(Color.WHITE);
        avatarOuter.getChildren().add(initLbl);

        Text displayNameTxt = new Text(currentUser.getDisplayName());
        displayNameTxt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        displayNameTxt.setFill(Color.web("#0f172a"));

        Label roleBadge = new Label(currentUser.getRole().name().toUpperCase());
        roleBadge.setStyle(
                "-fx-background-color: rgba(20,86,184,0.12);" +
                        "-fx-background-radius: 999; -fx-border-radius: 999;" +
                        "-fx-border-color: rgba(20,165,184,0.35); -fx-border-width: 1;" +
                        "-fx-text-fill: #0FAFDDFF; -fx-font-size: 9px; -fx-font-weight: bold;" +
                        "-fx-padding: 3 12;"
        );

        profileSection.getChildren().addAll(avatarOuter, displayNameTxt, roleBadge);
        topSection.getChildren().addAll(logoRow, profileSection);

        // ── NAVIGATION ────────────────────────────────────────────────────────
        VBox menuBox = new VBox(4);
        menuBox.setPadding(new Insets(22, 10, 14, 10));

        Label menuLabel = new Label("NAVIGATION");
        menuLabel.setStyle(
                "-fx-text-fill: #94a3b8; -fx-font-size: 9px; -fx-font-weight: bold;" +
                        "-fx-padding: 0 8 10 8;"
        );

        Button[] navBtns = new Button[7];
        navBtns[0] = createNavButton(new String(Character.toChars(0x1F3E0)), "Home",       true);
        navBtns[1] = createNavButton(new String(Character.toChars(0x1F4BC)), "Jobs",       false);
        navBtns[2] = createNavButton(new String(Character.toChars(0x1F3E2)), "Companies",  false);
        navBtns[3] = createNavButton(new String(Character.toChars(0x1F464)), "My Profile", false);
        navBtns[4] = createNavButton(new String(Character.toChars(0x1F4CA)), "Dashboard",  false);
        navBtns[5] = createNavButton(new String(Character.toChars(0x1F4DD)), "My Quizzes", false);
        navBtns[6] = createNavButton(new String(Character.toChars(0x1F4C5)), "Interviews",  false);

        navBtns[0].setOnAction(e -> { setActiveBtn(navBtns, 0); root.setCenter(createMainContent()); });
        navBtns[1].setOnAction(e -> {
            setActiveBtn(navBtns, 1);
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/FrontOffice/JobOffer.fxml"));
                javafx.scene.Parent jobRoot = loader.load();
                jobController controller = loader.getController();
                controller.setHomeBorderPane(root);
                root.setCenter(jobRoot);
                BlindHireApp.getPrimaryStage().setMaximized(true);
            } catch (Exception ex) { ex.printStackTrace(); showComingSoon("Jobs"); }
        });
        navBtns[2].setOnAction(e -> { setActiveBtn(navBtns, 2); showComingSoon("Companies"); });
        navBtns[3].setOnAction(e -> { setActiveBtn(navBtns, 3); root.setCenter(new Profilepage(currentUser).getRoot()); });
        navBtns[4].setOnAction(e -> { setActiveBtn(navBtns, 4); showComingSoon("Dashboard"); });
        navBtns[5].setOnAction(e -> { setActiveBtn(navBtns, 5); openQuizzesPage(); });
        navBtns[6].setOnAction(e -> {
            setActiveBtn(navBtns, 6);
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/CandidatInterview.fxml"));

                Parent view = loader.load();

                Controller.FrontOffice.InterviewController ctrl = loader.getController();
                ctrl.setHomeBorderPane(root);   // ⭐ VERY IMPORTANT
                ctrl.setCurrentUser(currentUser); // pass real user ID

                root.setCenter(view);

            } catch (Exception ex) {
                ex.printStackTrace();
                showComingSoon("Interviews");
            }
        });

        menuBox.getChildren().add(menuLabel);
        for (Button b : navBtns) menuBox.getChildren().add(b);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── LOGOUT ────────────────────────────────────────────────────────────
        VBox bottomBox = new VBox(0);
        bottomBox.setPadding(new Insets(10, 10, 20, 10));
        bottomBox.setStyle(
                "-fx-border-color: #e2e8f0 transparent transparent transparent;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        Button logoutBtn = createNavButton(new String(Character.toChars(0x1F6AA)), "Logout", false);
        logoutBtn.setStyle(logoutBtn.getStyle().replace("-fx-text-fill: #64748b;", "-fx-text-fill: #ef4444;"));
        logoutBtn.setOnAction(e -> {
            Utils.SessionManager.clearSession();
            userService.setCurrentUser(null);
            BlindHireApp.loadSceneFullscreen(new WelcomePage().getRoot());

        });
        bottomBox.getChildren().add(logoutBtn);

        sidebar.getChildren().addAll(topSection, menuBox, spacer, bottomBox);
        return sidebar;
    }

    /** Switches active highlight to button at index activeIdx */
    private void setActiveBtn(Button[] btns, int activeIdx) {
        String activeStyle =
                "-fx-background-color: #0FAFDDFF;" +
                        "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-background-radius: 12;" +
                        "-fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(13,128,148,0.35), 10, 0, 0, 3);";
        String normalStyle =
                "-fx-background-color: transparent; -fx-text-fill: #64748b;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-alignment: center-left;" +
                        "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;";
        String hoverStyle =
                "-fx-background-color: rgba(8,60,136,0.08); -fx-text-fill: #0FAFDDFF;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-alignment: center-left;" +
                        "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;";
        for (int i = 0; i < btns.length; i++) {
            if (i == activeIdx) {
                btns[i].setStyle(activeStyle);
                btns[i].setOnMouseEntered(null);
                btns[i].setOnMouseExited(null);
            } else {
                final Button b = btns[i];
                b.setStyle(normalStyle);
                b.setOnMouseEntered(ev -> b.setStyle(hoverStyle));
                b.setOnMouseExited(ev  -> b.setStyle(normalStyle));
            }
        }
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
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 28,0,0,8);"
        );
        container.setPrefWidth(400);
        container.setMaxHeight(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle("-fx-background-color: linear-gradient(to right, #F8FAFF, #F0F4FF); -fx-background-radius: 18 18 0 0; -fx-border-color: transparent transparent #E2E8F0 transparent; -fx-border-width: 0 0 1 0;");

        Label title = new Label(new String(Character.toChars(0x1F514)) + "  Notifications");
        title.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #1e3a8a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLbl = new Label(notifications.size() + " total");
        countLbl.setStyle("-fx-background-color:#EEF2FF; -fx-background-radius:999; -fx-border-radius:999; -fx-border-color:#C7D2FE; -fx-border-width:1; -fx-text-fill:#4F46E5; -fx-font-size:11; -fx-font-weight:700; -fx-font-family:'Segoe UI'; -fx-padding:3 10;");

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
            empty.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 40 30;");
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
        double x = bounds.getMaxX() - 400;
        double y = bounds.getMaxY() + 8;
        notifPopup.show(notifBellBtn.getScene().getWindow(), x, y);
        popupOpen = true;

        // Refresh badge (now all read)
        refreshNotifBadge();
    }

    private VBox buildNotifRow(Notification n) {
        boolean isAccepted = "accepted".equalsIgnoreCase(n.getType());
        boolean isRejected = "rejected".equalsIgnoreCase(n.getType());
        boolean unread     = !n.isRead();

        // ── Accent color per type ─────────────────────────────────────────────
        String accentColor  = isAccepted ? "#3B82F6" : isRejected ? "#EF4444" : "#6366F1";
        String accentBg     = isAccepted ? "#EFF6FF" : isRejected ? "#FFF1F2" : "#F5F3FF";
        String accentBorder = isAccepted ? "#BFDBFE" : isRejected ? "#FECDD3" : "#DDD6FE";
        String bgNormal     = unread ? (isAccepted ? "#EFF6FF" : isRejected ? "#FFF5F5" : "#F5F3FF") : "white";

        // ── Row container ─────────────────────────────────────────────────────
        VBox row = new VBox(0);
        row.setPadding(new Insets(0));
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setStyle("-fx-background-color:" + bgNormal + ";");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#F8FAFC;"));
        row.setOnMouseExited(e  -> row.setStyle("-fx-background-color:" + bgNormal + ";"));

        // ── Left accent bar + content ─────────────────────────────────────────
        HBox inner = new HBox(0);
        inner.setAlignment(Pos.TOP_LEFT);

        // Colored left stripe
        Region stripe = new Region();
        stripe.setPrefWidth(4);
        stripe.setMinWidth(4);
        stripe.setMaxWidth(4);
        stripe.setStyle("-fx-background-color:" + accentColor + ";");
        inner.getChildren().add(stripe);

        // Content area
        VBox content = new VBox(5);
        content.setPadding(new Insets(12, 16, 12, 12));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Top line: icon badge + title + unread dot
        HBox topLine = new HBox(8);
        topLine.setAlignment(Pos.CENTER_LEFT);

        // Icon badge
        Label iconBadge = new Label(n.getIcon());
        iconBadge.setMinSize(30, 30); iconBadge.setMaxSize(30, 30);
        iconBadge.setAlignment(Pos.CENTER);
        iconBadge.setStyle(
                "-fx-background-color:" + accentBg + ";" +
                        "-fx-background-radius:999;" +
                        "-fx-border-radius:999;" +
                        "-fx-border-color:" + accentBorder + ";" +
                        "-fx-border-width:1;" +
                        "-fx-font-size:13;");

        Label titleLbl = new Label(n.getTitle());
        titleLbl.setStyle(
                "-fx-font-family:'Segoe UI'; -fx-font-size:13; -fx-font-weight:800;" +
                        "-fx-text-fill:#0f172a;");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(220);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        // Unread indicator dot
        if (unread) {
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
            dot.setFill(javafx.scene.paint.Color.web(accentColor));
            topLine.getChildren().addAll(iconBadge, titleLbl, dot);
        } else {
            topLine.getChildren().addAll(iconBadge, titleLbl);
        }

        // Message
        Label msgLbl = new Label(n.getMessage());
        msgLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:11; -fx-text-fill:#64748B;");
        msgLbl.setWrapText(true);

        // Bottom row: time + Go to Quiz button (accepted only)
        HBox bottomRow = new HBox(8);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        Label timeLbl = new Label(new String(Character.toChars(0x1F550)) + "  " + n.getFormattedTime());
        timeLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:10; -fx-text-fill:#94a3b8; -fx-font-weight:600;");
        bottomRow.getChildren().add(timeLbl);

        if (isAccepted && n.getJobOfferId() > 0) {
            Region bSpacer = new Region(); HBox.setHgrow(bSpacer, Priority.ALWAYS);
            Label quizBtn = new Label(new String(Character.toChars(0x1F4DD)) + "  Pass Quiz");
            quizBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3B82F6, #2563EB);" +
                            "-fx-text-fill: white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                            "-fx-font-size:13; -fx-background-radius:999; -fx-padding:8 18;" +
                            "-fx-cursor:hand;");
            quizBtn.setEffect(new javafx.scene.effect.DropShadow(8,
                    javafx.scene.paint.Color.web("#2563EB", 0.35)));
            quizBtn.setOnMouseEntered(ev -> quizBtn.setScaleX(1.05));
            quizBtn.setOnMouseExited(ev  -> quizBtn.setScaleX(1.0));
            quizBtn.setOnMouseClicked(ev -> {
                ev.consume();
                if (notifPopup != null) { notifPopup.hide(); popupOpen = false; }
                openQuizPanel(n.getJobOfferId());
            });
            bottomRow.getChildren().addAll(bSpacer, quizBtn);
        }

        content.getChildren().addAll(topLine, msgLbl, bottomRow);
        inner.getChildren().add(content);
        row.getChildren().add(inner);

        // Whole row click (not for accepted — button handles it)
        if (!isAccepted) {
            row.setOnMouseClicked(e -> handleNotificationClick(n));
        }

        return row;
    }

    private void handleNotificationClick(Notification n) {
        if (notifPopup != null) {
            notifPopup.hide();
            popupOpen = false;
        }

        if ("accepted".equalsIgnoreCase(n.getType())) {
            openQuizPanel(n.getJobOfferId());
        }

        refreshNotifBadge();
    }

    /**
     * Embeds TakeQuizPanel inside the main BorderPane center,
     * pre-filling the userId and filtering by the accepted job's skills.
     */
    private void openQuizPanel(int jobOfferId) {
        // Check if user already completed this quiz
        if (currentUser != null && jobOfferId > 0) {
            try {
                Service.scoreService ss = new Service.scoreService();
                if (ss.hasScore(currentUser.getId(), jobOfferId)) {
                    showAlreadyPassedDialog(jobOfferId);
                    return;
                }
            } catch (Exception ignored) {}
        }

        ui.TakeQuizPanel quizPanel = new ui.TakeQuizPanel();

        // Wire the Back button to return to quizzes page
        quizPanel.btnBack.setOnAction(e -> openQuizzesPage());

        root.setCenter(quizPanel);
        BlindHireApp.getPrimaryStage().setMaximized(true);

        if (currentUser != null && jobOfferId > 0) {
            quizPanel.controller.autoLoad(currentUser.getId(), jobOfferId);
        }
    }

    private void showAlreadyPassedDialog(int jobOfferId) {
        // Fetch score info
        String scoreText = "";
        try {
            java.sql.Connection cnx = Utils.Mydb.getInstance().getConnection();
            java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT score, status FROM score WHERE id_user=? AND job_offer_id=? " +
                            "ORDER BY created_at DESC LIMIT 1");
            ps.setInt(1, currentUser.getId()); ps.setInt(2, jobOfferId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                scoreText = rs.getBigDecimal("score").toPlainString() + "%";
                String st = rs.getString("status");
                if (st != null) scoreText += "  (" + st.toUpperCase() + ")";
            }
        } catch (Exception ignored) {}

        final String finalScore = scoreText;

        // Build dialog overlay inside current scene
        javafx.stage.Stage owner = (javafx.stage.Stage) root.getScene().getWindow();

        javafx.stage.Stage dialog = new javafx.stage.Stage(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);

        // Backdrop
        javafx.scene.layout.StackPane backdrop = new javafx.scene.layout.StackPane();
        backdrop.setStyle("-fx-background-color: rgba(8,18,55,0.62);");

        // Card
        VBox card = new VBox(18);
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36, 36, 32, 36));
        card.setStyle("-fx-background-color:white; -fx-background-radius:22; -fx-border-radius:22;");
        card.setEffect(new javafx.scene.effect.DropShadow(36, 0, 6, Color.rgb(0,0,0,0.26)));

        Label emojiLbl = new Label(new String(Character.toChars(0x1F3C6)));
        emojiLbl.setStyle("-fx-font-size:42;");

        Label titleLbl = new Label("Quiz Already Completed");
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        titleLbl.setTextFill(Color.web("#1e3a8a"));

        Label scoreLbl = new Label("Your score: " + (finalScore.isBlank() ? "N/A" : finalScore));
        scoreLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:14; -fx-text-fill:#64748B;");

        Label msgLbl = new Label("You have already submitted this quiz. View your results in the My Quizzes page.");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-family:'Segoe UI'; -fx-font-size:12; -fx-text-fill:#94A3B8;" +
                "-fx-text-alignment:center; -fx-alignment:center;");

        // Buttons
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        Label btnConsult = new Label(new String(Character.toChars(0x1F4CB)) + "  My Quizzes");
        btnConsult.setStyle("-fx-background-color:linear-gradient(to right,#3B82F6,#2563EB);" +
                "-fx-text-fill:white; -fx-font-family:'Segoe UI'; -fx-font-weight:800;" +
                "-fx-font-size:13; -fx-background-radius:999; -fx-padding:11 22; -fx-cursor:hand;");
        btnConsult.setEffect(new javafx.scene.effect.DropShadow(8, Color.web("#2563EB",0.3)));
        btnConsult.setOnMouseEntered(e -> btnConsult.setScaleX(1.04));
        btnConsult.setOnMouseExited(e  -> btnConsult.setScaleX(1.0));
        btnConsult.setOnMouseClicked(e -> { dialog.close(); openQuizzesPage(); });

        Label btnClose = new Label("Close");
        btnClose.setStyle("-fx-background-color:#F1F5F9; -fx-text-fill:#64748B;" +
                "-fx-font-family:'Segoe UI'; -fx-font-weight:700;" +
                "-fx-font-size:13; -fx-background-radius:999; -fx-padding:11 22; -fx-cursor:hand;");
        btnClose.setOnMouseClicked(e -> dialog.close());

        btnRow.getChildren().addAll(btnConsult, btnClose);
        card.getChildren().addAll(emojiLbl, titleLbl, scoreLbl, msgLbl, btnRow);
        backdrop.getChildren().add(card);

        // Fade in
        backdrop.setOpacity(0);
        card.setTranslateY(30); card.setOpacity(0);
        javafx.scene.Scene sc = new javafx.scene.Scene(backdrop,
                owner.getWidth(), owner.getHeight());
        sc.setFill(Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.setX(owner.getX()); dialog.setY(owner.getY());
        dialog.show();

        javafx.animation.FadeTransition fd =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), backdrop);
        fd.setToValue(1);
        javafx.animation.FadeTransition cf =
                new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), card);
        cf.setToValue(1); cf.setDelay(javafx.util.Duration.millis(80));
        javafx.animation.TranslateTransition ct =
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), card);
        ct.setToY(0); ct.setDelay(javafx.util.Duration.millis(80));
        new javafx.animation.ParallelTransition(fd, cf, ct).play();
    }

    public void openQuizzesPage() {
        ui.MyQuizzesPanel quizzesPanel = new ui.MyQuizzesPanel(
                currentUser,
                () -> root.setCenter(createMainContent()),  // back → home
                (jobOfferId) -> openQuizPanel(jobOfferId)  // reuse the same method notifications use
        );
        root.setCenter(quizzesPanel);
    }
    // 2-arg legacy shim (e.g. logout button called with text+active)
    private Button createNavButton(String text, boolean active) {
        String[] parts = text.trim().split("  ", 2);
        String icon  = parts.length == 2 ? parts[0] : "";
        String label = parts.length == 2 ? parts[1] : text.trim();
        return createNavButton(icon, label, active);
    }

    private Button createNavButton(String icon, String label, boolean active) {
        // CoachPro: solid teal rounded pill for active, transparent for rest
        String activeStyle =
                "-fx-background-color: #0FAFDDFF;" +
                        "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;" +
                        "-fx-font-size: 13px; -fx-alignment: center-left; -fx-background-radius: 12;" +
                        "-fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(13,101,148,0.35), 10, 0, 0, 3);";
        String normalStyle =
                "-fx-background-color: transparent; -fx-text-fill: #64748b;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-alignment: center-left;" +
                        "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;";
        String hoverStyle =
                "-fx-background-color: rgba(13,148,136,0.08); -fx-text-fill: #0FAFDDFF;" +
                        "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-alignment: center-left;" +
                        "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-border-width: 0;";

        String display = icon.isBlank() ? label : icon + "  " + label;
        Button btn = new Button(display);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(active ? activeStyle : normalStyle);
        if (!active) {
            btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
            btn.setOnMouseExited(e  -> btn.setStyle(normalStyle));
        }
        return btn;
    }

    // ─── MAIN CONTENT ─────────────────────────────────────────────────────────

    private StackPane createMainContent() {
        StackPane wrapper = new StackPane();
        wrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0; -fx-padding: 0;");
        scroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scroll.prefWidthProperty().bind(wrapper.widthProperty());
        scroll.prefHeightProperty().bind(wrapper.heightProperty());

        VBox page = new VBox(22);
        page.setPadding(new Insets(32, 32, 32, 32));
        page.setMaxWidth(Double.MAX_VALUE);
        page.setFillWidth(true);
        page.setStyle("-fx-background-color: transparent;");

        // ── GREETING HEADER ───────────────────────────────────────────────────
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setMaxWidth(Double.MAX_VALUE);

        VBox greetBox = new VBox(3);
        String uname = currentUser.getNom() != null ? currentUser.getNom() : "User";
        Label greetLbl = new Label("Welcome back, " + uname + " " + new String(Character.toChars(0x1F44B)));
        greetLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0FAFDDFF; -fx-font-family: 'Segoe UI';");
        Label dashTitle = new Label("Dashboard");
        dashTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-family: 'Segoe UI';");
        greetBox.getChildren().addAll(greetLbl, dashTitle);
        Region topSpacer = new Region(); HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button browseBtn = new Button("Browse All Jobs  \u2192");
        String bNorm = "-fx-background-color: linear-gradient(to right, #0FAFDDFF, #0891b2); -fx-text-fill: white;" +
                " -fx-font-weight: bold; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                " -fx-background-radius: 22; -fx-padding: 11 26; -fx-cursor: hand; -fx-border-width: 0;" +
                " -fx-effect: dropshadow(gaussian, rgba(13,101,148,0.4), 12, 0, 0, 4);";
        String bHov  = "-fx-background-color: linear-gradient(to right, #0FAFDDFF, #0369a1); -fx-text-fill: white;" +
                " -fx-font-weight: bold; -fx-font-size: 13px; -fx-font-family: 'Segoe UI';" +
                " -fx-background-radius: 22; -fx-padding: 11 26; -fx-cursor: hand; -fx-border-width: 0;";
        browseBtn.setStyle(bNorm);
        browseBtn.setOnAction(e -> showComingSoon("Jobs"));
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(bHov));
        browseBtn.setOnMouseExited(e  -> browseBtn.setStyle(bNorm));
        topBar.getChildren().addAll(greetBox, topSpacer, browseBtn);

        // ── STAT TILES ────────────────────────────────────────────────────────
        HBox tilesRow = new HBox(16);
        tilesRow.setMaxWidth(Double.MAX_VALUE);
        tilesRow.setFillHeight(true);
        HBox[] tiles = {
                makeTile(new String(Character.toChars(0x1F4BC)), "Open Jobs",       "128", "#0d9488", "#f0fdfa", "#99f6e4"),
                makeTile(new String(Character.toChars(0x1F4DD)), "Applications",    "6",   "#0891b2", "#f0f9ff", "#bae6fd"),
                makeTile(new String(Character.toChars(0x1F4C5)), "Interviews",      "2",   "#7c3aed", "#faf5ff", "#ddd6fe"),
                makeTile(new String(Character.toChars(0x1F3C6)), "Offers Received", "1",   "#d97706", "#fffbeb", "#fde68a")
        };
        for (HBox t : tiles) { HBox.setHgrow(t, Priority.ALWAYS); tilesRow.getChildren().add(t); }

        // ── MAIN 2-COLUMN GRID ────────────────────────────────────────────────
        HBox mainGrid = new HBox(20);
        mainGrid.setMaxWidth(Double.MAX_VALUE);
        mainGrid.setFillHeight(true);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);

        // LEFT COLUMN
        VBox leftCol = new VBox(18);
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        leftCol.setMaxWidth(Double.MAX_VALUE);

        // Hero gradient banner
        VBox heroBanner = new VBox(13);
        heroBanner.setPadding(new Insets(28, 32, 28, 32));
        heroBanner.setMaxWidth(Double.MAX_VALUE);
        heroBanner.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0FAFDDFF, #0284c7);" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(14,54,154,0.3), 18, 0, 0, 6);");
        Label heroTitle = new Label("Find Your Dream Job  \u2736");
        heroTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        Label heroSub = new Label("Connect with top companies and discover opportunities tailored to your skills.");
        heroSub.setWrapText(true);
        heroSub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.82); -fx-font-family: 'Segoe UI';");
        Button heroBtn = new Button("Explore Opportunities  \u2192");
        heroBtn.setStyle("-fx-background-color: white; -fx-text-fill: #0FAFDDFF; -fx-font-weight: bold;" +
                "-fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-background-radius: 22;" +
                "-fx-padding: 10 22; -fx-cursor: hand; -fx-border-width: 0;");
        heroBtn.setOnAction(e -> showComingSoon("Jobs"));
        heroBanner.getChildren().addAll(heroTitle, heroSub, heroBtn);

        // Recent Applications card
        VBox appCard = new VBox(0);
        appCard.setMaxWidth(Double.MAX_VALUE);
        appCard.setStyle("-fx-background-color: white; -fx-background-radius: 20;" +
                "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 16, 0, 0, 4);");

        HBox appHeader = new HBox(10);
        appHeader.setAlignment(Pos.CENTER_LEFT);
        appHeader.setPadding(new Insets(20, 24, 16, 24));
        appHeader.setStyle("-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");
        Label appTitle = new Label("Recent Applications");
        appTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-family: 'Segoe UI';");
        Region appSpacer = new Region(); HBox.setHgrow(appSpacer, Priority.ALWAYS);
        Label appViewAll = new Label("View all  \u2192");
        appViewAll.setStyle("-fx-font-size: 12px; -fx-text-fill: #0FAFDDFF; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        appHeader.getChildren().addAll(appTitle, appSpacer, appViewAll);

        Label emptyLbl = new Label("No recent applications.");
        emptyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI'; -fx-padding: 20 24;");
        appCard.getChildren().add(emptyLbl);
        leftCol.getChildren().addAll(heroBanner, appCard);

        // RIGHT COLUMN
        VBox rightCol = new VBox(18);
        rightCol.setPrefWidth(300);
        rightCol.setMinWidth(260);
        rightCol.setMaxWidth(Double.MAX_VALUE);

        // Job Market Stats card
        VBox statsCard = new VBox(18);
        statsCard.setPadding(new Insets(22, 24, 24, 24));
        statsCard.setMaxWidth(Double.MAX_VALUE);
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 20;" +
                "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 16, 0, 0, 4);");

        HBox statsHdr = new HBox(10);
        statsHdr.setAlignment(Pos.CENTER_LEFT);
        Label statsTitle2 = new Label("Job Market Stats");
        statsTitle2.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-family: 'Segoe UI';");
        Region stSpacer = new Region(); HBox.setHgrow(stSpacer, Priority.ALWAYS);
        Label stViewAll = new Label("View all  \u2192");
        stViewAll.setStyle("-fx-font-size: 12px; -fx-text-fill: #0FAFDDFF; -fx-font-weight: 700; -fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        statsHdr.getChildren().addAll(statsTitle2, stSpacer, stViewAll);

        String[][] mkt = {
                {"Technology", "45", "#0d9488"},
                {"Design",     "25", "#0891b2"},
                {"Marketing",  "18", "#7c3aed"},
                {"Finance",    "12", "#d97706"},
        };
        VBox mktList = new VBox(14);
        mktList.setMaxWidth(Double.MAX_VALUE);
        for (String[] m : mkt) {
            VBox mRow = new VBox(6);
            mRow.setMaxWidth(Double.MAX_VALUE);
            HBox mLabel = new HBox(6);
            mLabel.setAlignment(Pos.CENTER_LEFT);
            Circle mdot = new Circle(4); mdot.setFill(Color.web(m[2]));
            Label mName = new Label(m[0]);
            mName.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-family: 'Segoe UI';");
            Region mSpacer = new Region(); HBox.setHgrow(mSpacer, Priority.ALWAYS);
            Label mPct = new Label(m[1] + "%");
            mPct.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + m[2] + "; -fx-font-family: 'Segoe UI';");
            mLabel.getChildren().addAll(mdot, mName, mSpacer, mPct);

            StackPane track = new StackPane();
            track.setAlignment(Pos.CENTER_LEFT);
            track.setMaxWidth(Double.MAX_VALUE);
            Rectangle trackBg = new Rectangle(0, 8);
            trackBg.setArcWidth(8); trackBg.setArcHeight(8);
            trackBg.setFill(Color.web("#f1f5f9"));
            trackBg.widthProperty().bind(mRow.widthProperty());
            Rectangle trackFill = new Rectangle(0, 8);
            trackFill.setArcWidth(8); trackFill.setArcHeight(8);
            trackFill.setFill(Color.web(m[2]));
            trackFill.widthProperty().bind(mRow.widthProperty().multiply(Integer.parseInt(m[1]) / 100.0));
            track.getChildren().addAll(trackBg, trackFill);
            mRow.getChildren().addAll(mLabel, track);
            mktList.getChildren().add(mRow);
        }
        statsCard.getChildren().addAll(statsHdr, mktList);

        // CTA card
        VBox ctaCard = new VBox(12);
        ctaCard.setPadding(new Insets(24, 26, 24, 26));
        ctaCard.setMaxWidth(Double.MAX_VALUE);
        ctaCard.setStyle("-fx-background-color: linear-gradient(to bottom right, #0fafdd, #0c4a6e); -fx-background-radius: 20;");
        Label ctaBadge = new Label("\u2736  DON'T FORGET");
        ctaBadge.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.60); -fx-font-family: 'Segoe UI';");
        Label ctaTitle2 = new Label("Complete your profile for better job matches");
        ctaTitle2.setWrapText(true);
        ctaTitle2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        Label ctaDesc = new Label("87% of employers look at a full profile before shortlisting.");
        ctaDesc.setWrapText(true);
        ctaDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.65); -fx-font-family: 'Segoe UI';");
        Button ctaBtn = new Button("Update Profile  \u2192");
        String ctaN = "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-background-radius: 22;" +
                "-fx-padding: 9 20; -fx-cursor: hand; -fx-border-width: 1;" +
                "-fx-border-color: rgba(255,255,255,0.30); -fx-border-radius: 22;";
        String ctaH = "-fx-background-color: rgba(255,255,255,0.30); -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 12px; -fx-font-family: 'Segoe UI'; -fx-background-radius: 22;" +
                "-fx-padding: 9 20; -fx-cursor: hand; -fx-border-width: 1;" +
                "-fx-border-color: rgba(255,255,255,0.50); -fx-border-radius: 22;";
        ctaBtn.setStyle(ctaN);
        ctaBtn.setOnMouseEntered(e -> ctaBtn.setStyle(ctaH));
        ctaBtn.setOnMouseExited(e  -> ctaBtn.setStyle(ctaN));
        ctaCard.getChildren().addAll(ctaBadge, ctaTitle2, ctaDesc, ctaBtn);

        rightCol.getChildren().addAll(statsCard, ctaCard);
        mainGrid.getChildren().addAll(leftCol, rightCol);
        page.getChildren().addAll(topBar, tilesRow, mainGrid);

        scroll.setContent(page);
        wrapper.getChildren().add(scroll);
        return wrapper;
    }

    /** Stat tile with hover effect */
    private HBox makeTile(String icon, String label, String value, String color, String bg, String border) {
        HBox tile = new HBox(14);
        tile.setAlignment(Pos.CENTER_LEFT);
        tile.setPadding(new Insets(18, 20, 18, 20));
        tile.setMaxWidth(Double.MAX_VALUE);
        String baseStyle = "-fx-background-color: white; -fx-background-radius: 18;" +
                "-fx-border-color: " + border + "; -fx-border-width: 1.5; -fx-border-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 3);";
        String hoverStyle2 = "-fx-background-color: " + bg + "; -fx-background-radius: 18;" +
                "-fx-border-color: " + color + "; -fx-border-width: 1.5; -fx-border-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.11), 16, 0, 0, 5); -fx-cursor: hand;";
        tile.setStyle(baseStyle);
        tile.setOnMouseEntered(e -> tile.setStyle(hoverStyle2));
        tile.setOnMouseExited(e  -> tile.setStyle(baseStyle));

        StackPane iconWrap = new StackPane();
        iconWrap.setMinSize(46, 46); iconWrap.setMaxSize(46, 46);
        iconWrap.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 14;");
        Label ico = new Label(icon); ico.setStyle("-fx-font-size: 20px;");
        iconWrap.getChildren().add(ico);

        VBox info = new VBox(3);
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-family: 'Segoe UI';");
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-family: 'Segoe UI';");
        info.getChildren().addAll(valLbl, nameLbl);
        tile.getChildren().addAll(iconWrap, info);
        return tile;
    }
    private Rectangle makeCard() {
        Rectangle r = new Rectangle(175, 20);
        r.setArcWidth(8); r.setArcHeight(8);
        r.setFill(Color.WHITE); r.setOpacity(0.9);
        return r;
    }

    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(feature);
        alert.setContentText(feature + " feature coming soon!");
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }
}