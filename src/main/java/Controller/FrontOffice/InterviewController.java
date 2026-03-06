package Controller.FrontOffice;

import Controller.ChatController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import Model.Interview;
import Model.User;
import Service.InterviewService;
import Service.MessageService;
import Service.VideoCallService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class InterviewController {

    private javafx.scene.layout.BorderPane homeBorderPane;
    public void setHomeBorderPane(javafx.scene.layout.BorderPane bp) { this.homeBorderPane = bp; }

    // ── Session user — set by HomePage after loading FXML ────────────────────
    private User currentUser;
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadInterviews(); // reload with the real user id
    }

    // ── Design tokens ─────────────────────────────────────────────────────────
    // ── Light palette matching HomePage aesthetic ────────────────────────────
    private static final String CARD_BG    = "white";
    private static final String CYAN       = "#0fafdd";
    private static final String CYAN_DARK  = "#057995";
    private static final String BLUE_DARK  = "#0c4a6e";
    private static final String INDIGO     = "#1a2980";
    private static final String INDIGO_LT  = "#0fafdd";
    private static final String INDIGO_DIM = "#057995";
    private static final String GREEN      = "#10b981";
    private static final String AMBER      = "#f59e0b";
    private static final String ROSE       = "#ef4444";
    private static final String TEXT_SOFT  = "#0f172a";
    private static final String TEXT_DIM   = "#64748b";
    private static final String BORDER     = "#e2e8f0";

    // ── FXML injections ───────────────────────────────────────────────────────
    @FXML private HBox       tabBar;
    @FXML private StackPane  tabContentArea;
    @FXML private ScrollPane upcomingPane;
    @FXML private VBox       pastPane;
    @FXML private VBox       acceptedOfferPane;
    @FXML private VBox       acceptedOfferContainer;
    @FXML private VBox       interviewContainer;
    @FXML private VBox       pastContainer;
    @FXML private VBox       notificationDropdown;
    @FXML private VBox       notificationList;
    @FXML private Label      notifBadge;
    @FXML private StackPane  notificationPane;
    @FXML private ComboBox<String> statusFilterComboBox;

    // ── Tab state ─────────────────────────────────────────────────────────────
    private final List<Button>            tabButtons = new ArrayList<>();
    private final List<javafx.scene.Node> tabPanes   = new ArrayList<>();

    // ── Services ──────────────────────────────────────────────────────────────
    private final MessageService   messageService   = new MessageService();
    private final VideoCallService videoCallService = new VideoCallService();
    private final InterviewService interviewService = new InterviewService();

    private final DateTimeFormatter displayFmt =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ── Initialize ────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        tabPanes.add(upcomingPane);
        tabPanes.add(pastPane);
        tabPanes.add(acceptedOfferPane);

        String[] labels = {"📋  Upcoming", "🕘  Past Interviews", "🎉  My acceptances"};
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            Button btn = buildTabBtn(labels[i], i == 0);
            btn.setOnAction(e -> switchTab(idx));
            tabButtons.add(btn);
            tabBar.getChildren().add(btn);
        }

        statusFilterComboBox.setItems(
                FXCollections.observableArrayList("All", "Pending", "Accepted", "Rejected"));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterPastInterviews());

        notificationPane.setOnMouseClicked(e -> {
            boolean showing = notificationDropdown.isVisible();
            notificationDropdown.setVisible(!showing);
            notificationDropdown.setManaged(!showing);
        });

        loadInterviews(); // initial load (may use id=1 fallback until setCurrentUser)
    }

    // ── Tab helpers ───────────────────────────────────────────────────────────
    private Button buildTabBtn(String label, boolean active) {
        Button btn = new Button(label);
        applyTabStyle(btn, active);
        return btn;
    }

    private void applyTabStyle(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #0fafdd;" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 12 12 0 0;" +
                            "-fx-padding: 11 26 14 26;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: #0fafdd #0fafdd white #0fafdd;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 12 12 0 0;"
            );
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.30);" +
                            "-fx-text-fill: rgba(255,255,255,0.85);" +
                            "-fx-font-size: 13px; -fx-font-weight: bold;" +
                            "-fx-background-radius: 12 12 0 0;" +
                            "-fx-padding: 11 26 14 26;" +
                            "-fx-cursor: hand;" +
                            "-fx-border-color: rgba(255,255,255,0.35);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 12 12 0 0;"
            );
        }
    }

    private void switchTab(int idx) {
        for (int i = 0; i < tabButtons.size(); i++)
            applyTabStyle(tabButtons.get(i), i == idx);
        for (int i = 0; i < tabPanes.size(); i++) {
            tabPanes.get(i).setVisible(i == idx);
            tabPanes.get(i).setManaged(i == idx);
        }
    }

    // ── Load interviews ───────────────────────────────────────────────────────
    private void loadInterviews() {
        if (interviewContainer == null) return; // not yet injected
        interviewContainer.getChildren().clear();
        pastContainer.getChildren().clear();
        notificationList.getChildren().clear();

        int userId = (currentUser != null) ? currentUser.getId() : 1;

        try {
            List<Interview> interviews = interviewService.afficherByCandidat(userId);
            LocalDateTime now = LocalDateTime.now();
            List<Object[]> notifications = new ArrayList<>();

            for (Interview iv : interviews) {
                if (iv.getDate().isAfter(now)) {
                    interviewContainer.getChildren().add(createUpcomingCard(iv));
                    if (iv.getDate().toLocalDate().equals(java.time.LocalDate.now().plusDays(1)))
                        notifications.add(new Object[]{iv, "reminder"});
                } else {
                    pastContainer.getChildren().add(createPastCard(iv));
                    if (iv.getStatus() != null && !iv.getStatus().equals("Pending"))
                        notifications.add(new Object[]{iv, "status"});
                }
            }

            if (interviewContainer.getChildren().isEmpty())
                interviewContainer.getChildren().add(
                        emptyState("📅", "No upcoming interviews",
                                "Your scheduled interviews will appear here."));
            if (pastContainer.getChildren().isEmpty())
                pastContainer.getChildren().add(
                        emptyState("🕘", "No past interviews",
                                "Completed interviews will be listed here."));

            loadAcceptedOffer(userId);

            buildNotifications(notifications);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Empty state ───────────────────────────────────────────────────────────
    private VBox emptyState(String emoji, String title, String sub) {
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 40px;");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox box = new VBox(10, icon, titleLbl, subLbl);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 18; -fx-border-width: 1;" +
                        "-fx-padding: 50 40;"
        );
        return box;
    }

    // ── Upcoming card ─────────────────────────────────────────────────────────
    private VBox createUpcomingCard(Interview iv) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,175,221,0.12), 14, 0, 0, 4);"
        );

        // Colored top accent strip
        String accentColor = "Online".equals(iv.getType()) ? "#0fafdd" : "#0c4a6e";
        HBox accent = new HBox();
        accent.setPrefHeight(4);
        accent.setStyle(
                "-fx-background-color: linear-gradient(to right, " + accentColor + ", transparent);" +
                        "-fx-background-radius: 18 18 0 0;"
        );

        VBox body = new VBox(14);
        body.setPadding(new Insets(18, 22, 18, 22));

        // Top row: type badge + status chip
        String typeIcon = "Online".equals(iv.getType()) ? "🌐" : "🏢";
        Label typeBadge = new Label(typeIcon + "  " + iv.getType());
        typeBadge.setStyle(
                "-fx-background-color: #e0f7fd; -fx-text-fill: #057995;" +
                        "-fx-padding: 5 14; -fx-background-radius: 20;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-border-color: #bae6fd; -fx-border-width:1; -fx-border-radius:20;"
        );
        String status = iv.getStatus() != null ? iv.getStatus() : "Pending";
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        HBox topRow = new HBox(10, typeBadge, hSpacer, statusChip(status));
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Job title
        Label jobLabel = new Label("💼  " + iv.getJob_offer());
        jobLabel.setStyle(
                "-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        jobLabel.setWrapText(true);

        // Info rows
        VBox info = new VBox(8,
                infoRow("📅", "Date",        iv.getDate().format(displayFmt)),
                infoRow("👤", "Interviewer", iv.getInterviewer().isEmpty() ? "TBD" : iv.getInterviewer())
        );

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2f7fd;");

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        int unread = 0;
        try { unread = messageService.countUnread(iv.getId(), "CANDIDATE"); }
        catch (SQLException ignored) {}

        Button chatBtn = actionBtn("💬  Open Chat", CYAN, "rgba(15,175,221,0.12)", CYAN);
        StackPane chatStack = new StackPane(chatBtn);
        chatStack.setAlignment(Pos.CENTER);
        if (unread > 0) {
            Label badge = unreadBadge(unread);
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            StackPane.setMargin(badge, new Insets(-5, -5, 0, 0));
            chatStack.getChildren().add(badge);
            chatBtn.setOnAction(e -> {
                try { messageService.markAsRead(iv.getId(), "CANDIDATE"); badge.setVisible(false); }
                catch (SQLException ignored) {}
                openChat(iv);
            });
        } else {
            chatBtn.setOnAction(e -> openChat(iv));
        }
        actions.getChildren().add(chatStack);

        if ("Online".equals(iv.getType())) {
            Button callBtn = actionBtn("📹  Join Call", CYAN, "rgba(6,182,212,0.18)", CYAN);
            callBtn.setOnAction(e -> videoCallService.openVideoCall(iv, "CANDIDATE"));
            actions.getChildren().add(callBtn);
        } else if (iv.getLocationLink() != null && !iv.getLocationLink().isEmpty()) {
            Button locBtn = actionBtn("📍  View Location", AMBER, "rgba(245,158,11,0.15)", AMBER);
            final String rawLink = iv.getLocationLink();
            locBtn.setOnAction(e -> {
                try {
                    // If it looks like coordinates or a plain address, open in Google Maps
                    String mapUrl;
                    if (rawLink.startsWith("http")) {
                        mapUrl = rawLink; // already a full URL
                    } else {
                        mapUrl = "https://www.google.com/maps/search/?api=1&query="
                                + java.net.URLEncoder.encode(rawLink, "UTF-8");
                    }
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(mapUrl));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
            actions.getChildren().add(locBtn);
        }

        body.getChildren().addAll(topRow, jobLabel, info, sep, actions);
        card.getChildren().addAll(accent, body);
        return card;
    }

    // ── Past card ─────────────────────────────────────────────────────────────
    private VBox createPastCard(Interview iv) {
        String status = iv.getStatus() != null ? iv.getStatus() : "Pending";
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1; -fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,175,221,0.08), 12, 0, 0, 3);"
        );

        String accentColor;
        if ("Accepted".equals(status)) accentColor = GREEN;
        else if ("Rejected".equals(status)) accentColor = ROSE;
        else accentColor = AMBER;
        HBox accent = new HBox();
        accent.setPrefHeight(4);
        accent.setStyle(
                "-fx-background-color: linear-gradient(to right, " + accentColor + ", transparent);" +
                        "-fx-background-radius: 18 18 0 0;"
        );

        VBox body = new VBox(14);
        body.setPadding(new Insets(18, 22, 18, 22));

        String typeIcon = "Online".equals(iv.getType()) ? "🌐" : "🏢";
        Label typeBadge = new Label(typeIcon + "  " + iv.getType());
        typeBadge.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;" +
                        "-fx-padding: 5 14; -fx-background-radius: 20;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-border-color: #e2e8f0; -fx-border-width:1; -fx-border-radius:20;"
        );
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        HBox topRow = new HBox(10, typeBadge, hSpacer, statusChip(status));
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label jobLabel = new Label("💼  " + iv.getJob_offer());
        jobLabel.setStyle(
                "-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;" +
                        "-fx-font-family: 'Segoe UI';"
        );
        jobLabel.setWrapText(true);

        VBox info = new VBox(8,
                infoRow("📅", "Date",        iv.getDate().format(displayFmt)),
                infoRow("👤", "Interviewer", iv.getInterviewer().isEmpty() ? "—" : iv.getInterviewer())
        );


        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        if ("Accepted".equals(status) || "Pending".equals(status)) {
            Button chatBtn = actionBtn("💬  Open Chat", CYAN, "rgba(15,175,221,0.12)", CYAN);
            chatBtn.setOnAction(e -> openChat(iv, "RECRUITER"));
            actions.getChildren().add(chatBtn);
        }

        body.getChildren().addAll(topRow, jobLabel, info, sep, actions);
        card.getChildren().addAll(accent, body);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private HBox infoRow(String emoji, String labelText, String value) {
        Label lbl = new Label(emoji + "  " + labelText + ":");
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-min-width: 110;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;");
        val.setWrapText(true);
        HBox row = new HBox(8, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label statusChip(String status) {
        String[] s;
        if ("Accepted".equals(status)) s = new String[]{"rgba(16,185,129,0.18)", GREEN, "rgba(16,185,129,0.40)"};
        else if ("Rejected".equals(status)) s = new String[]{"rgba(244,63,94,0.15)", ROSE, "rgba(244,63,94,0.35)"};
        else s = new String[]{"rgba(245,158,11,0.18)", AMBER, "rgba(245,158,11,0.35)"};
        Label chip = new Label(status);
        chip.setStyle(
                "-fx-background-color: " + s[0] + "; -fx-text-fill: " + s[1] + ";" +
                        "-fx-padding: 5 14; -fx-background-radius: 20;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold;" +
                        "-fx-border-color: " + s[2] + "; -fx-border-width:1; -fx-border-radius:20;"
        );
        return chip;
    }

    private Button actionBtn(String text, String borderColor, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                        "-fx-font-weight: bold; -fx-background-radius: 14;" +
                        "-fx-padding: 9 20; -fx-font-size: 13px; -fx-cursor: hand;" +
                        "-fx-border-color: " + borderColor + "; -fx-border-width:1; -fx-border-radius:14;"
        );
        return btn;
    }

    private Label unreadBadge(int count) {
        Label badge = new Label(String.valueOf(count));
        badge.setStyle(
                "-fx-background-color: " + ROSE + "; -fx-text-fill: white;" +
                        "-fx-background-radius: 10; -fx-font-size: 10px;" +
                        "-fx-padding: 1 5; -fx-font-weight: bold;"
        );
        return badge;
    }

    // ── Notifications ─────────────────────────────────────────────────────────
    private void buildNotifications(List<Object[]> notifications) {
        if (notifications.isEmpty()) {
            notifBadge.setVisible(false);
            Label none = new Label("No new notifications.");
            none.setStyle("-fx-text-fill: #64748b; -fx-font-size:12px;");
            notificationList.getChildren().add(none);
            return;
        }
        notifBadge.setText(String.valueOf(notifications.size()));
        notifBadge.setVisible(true);

        for (Object[] entry : notifications) {
            Interview iv = (Interview) entry[0];
            String type  = (String) entry[1];
            String emoji, color, msg;

            if ("reminder".equals(type)) {
                emoji = "⏰"; color = "#0fafdd";
                msg = "Interview tomorrow for \"" + iv.getJob_offer() + "\" at "
                        + iv.getDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                String st = iv.getStatus();
                emoji = "Accepted".equals(st) ? "🎉" : "❌";
                color = "Accepted".equals(st) ? GREEN : ROSE;
                msg   = "Accepted".equals(st)
                        ? "You were accepted for \"" + iv.getJob_offer() + "\""
                        : "You were rejected for \"" + iv.getJob_offer() + "\"";
            }

            HBox item = new HBox(10);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle(
                    "-fx-background-color: #f8fafc;" +
                            "-fx-background-radius: 10; -fx-padding: 10 14;" +
                            "-fx-border-color: " + color + ";" +
                            "-fx-border-radius: 10; -fx-border-width: 0 0 0 3;"
            );

            Label emojiLbl = new Label(emoji);
            emojiLbl.setStyle("-fx-font-size: 16px;");
            VBox textBox = new VBox(2);
            Label msgLbl = new Label(msg);
            msgLbl.setWrapText(true); msgLbl.setMaxWidth(280);
            msgLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            Label dateLbl = new Label("📅 " + iv.getDate().format(displayFmt));
            dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            textBox.getChildren().addAll(msgLbl, dateLbl);

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Button dismiss = new Button("✕");
            dismiss.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8;" +
                    "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0 4;");
            dismiss.setOnAction(e -> {
                notificationList.getChildren().remove(item);
                int rem = notificationList.getChildren().size();
                notifBadge.setVisible(rem > 0);
                if (rem > 0) notifBadge.setText(String.valueOf(rem));
            });

            item.getChildren().addAll(emojiLbl, textBox, spacer, dismiss);
            notificationList.getChildren().add(item);
        }
    }

    // ── Filter past ───────────────────────────────────────────────────────────
    private void filterPastInterviews() {
        String selected = statusFilterComboBox.getValue();
        pastContainer.getChildren().clear();
        int userId = (currentUser != null) ? currentUser.getId() : 1;
        try {
            List<Interview> all = interviewService.afficherByCandidat(userId);
            LocalDateTime now = LocalDateTime.now();
            boolean found = false;
            for (Interview iv : all) {
                if (iv.getDate().isBefore(now)) {
                    String st = iv.getStatus() != null ? iv.getStatus() : "Pending";
                    if ("All".equals(selected) || st.equals(selected)) {
                        pastContainer.getChildren().add(createPastCard(iv));
                        found = true;
                    }
                }
            }
            if (!found)
                pastContainer.getChildren().add(
                        emptyState("🕘",
                                "No " + ("All".equals(selected) ? "" : selected.toLowerCase() + " ") + "past interviews",
                                ""));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Opens the chat as CANDIDATE (front-office / candidate view). */
    private void openChat(Interview iv) {
        openChat(iv, "CANDIDATE");
    }

    /** Opens the chat as the given senderType ("CANDIDATE" or "RECRUITER"). */
    private void openChat(Interview iv, String senderType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Scene scene = new Scene(loader.load());
            ChatController ctrl = loader.getController();
            ctrl.setInterview(iv, senderType);
            Stage stage = new Stage();
            stage.setTitle("Interview Chat");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToPracticeInterview() {
        try {
            Parent view = new FXMLLoader(
                    getClass().getResource("/PracticeInterview.fxml")).load();
            if (homeBorderPane != null) {
                homeBorderPane.setCenter(view);
            } else {
                Stage stage = (Stage) interviewContainer.getScene().getWindow();
                stage.getScene().setRoot(view);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // ═══════════════════════════════════════════════════════════════════════════
    // MY OFFERS TAB — Congratulations letter with recruiter identity
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadAcceptedOffer(int userId) {
        if (acceptedOfferContainer == null) return;
        acceptedOfferContainer.getChildren().clear();

        try {
            java.util.List<Interview> allIvs = interviewService.afficherByCandidat(userId);
            java.util.List<Interview> accepted = new java.util.ArrayList<>();
            for (Interview iv : allIvs) {
                if ("Accepted".equalsIgnoreCase(iv.getStatus())) accepted.add(iv);
            }

            if (accepted.isEmpty()) {
                VBox empty = new VBox(16);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(60, 0, 0, 0));
                Label icon = new Label("📭");
                icon.setStyle("-fx-font-size: 48px;");
                Label msg = new Label("No accepted offers yet");
                msg.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + BLUE_DARK + ";");
                Label sub = new Label("This tab will show your congratulations letter once an interview is accepted.");
                sub.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_DIM + ";");
                sub.setWrapText(true);
                empty.getChildren().addAll(icon, msg, sub);
                acceptedOfferContainer.getChildren().add(empty);
                return;
            }

            for (Interview iv : accepted) {
                // Get recruiter info via interviewer_id
                User recruiter = null;
                try (java.sql.PreparedStatement ps = Utils.Mydb.getInstance().getConnection()
                        .prepareStatement(
                                "SELECT id, nom, prenom, mail, phone FROM user WHERE id = ?")) {
                    ps.setLong(1, iv.getInterviewerId());
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        recruiter = new User();
                        recruiter.setId(rs.getInt("id"));
                        recruiter.setNom(rs.getString("nom"));
                        recruiter.setPrenom(rs.getString("prenom"));
                        recruiter.setEmail(rs.getString("mail"));
                        recruiter.setPhone(rs.getString("phone"));
                    }
                }
                acceptedOfferContainer.getChildren().add(buildCongratsLetter(iv, recruiter));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildCongratsLetter(Interview iv, User recruiter) {
        VBox letter = new VBox(0);
        letter.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #0fafdd;" +
                        "-fx-border-width: 2; -fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,175,221,0.18), 18, 0, 0, 6);"
        );

        // ── Top banner ────────────────────────────────────────────────────────
        VBox banner = new VBox(8);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(32, 36, 28, 36));
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, #0c4a6e, #0fafdd);" +
                        "-fx-background-radius: 16 16 0 0;"
        );

        Label confettiLbl = new Label("🎉");
        confettiLbl.setStyle("-fx-font-size: 44px;");

        Label congratsTitle = new Label("Congratulations!");
        congratsTitle.setStyle(
                "-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;"
        );

        String candidateName = currentUser != null
                ? (currentUser.getNom() != null ? currentUser.getNom() : "") + " " +
                (currentUser.getPrenom() != null ? currentUser.getPrenom() : "")
                : "Candidate";
        Label personalLine = new Label("Dear " + candidateName.trim() + ",");
        personalLine.setStyle("-fx-font-size: 15px; -fx-text-fill: rgba(255,255,255,0.85);");

        banner.getChildren().addAll(confettiLbl, congratsTitle, personalLine);

        // ── Body ──────────────────────────────────────────────────────────────
        VBox body = new VBox(20);
        body.setPadding(new Insets(32, 40, 32, 40));
        body.setStyle("-fx-background-color: white;");

        // Acceptance message
        String jobTitle = iv.getJob_offer() != null ? iv.getJob_offer() : "the position";
        Label msgLbl = new Label(
                "We are thrilled to inform you that your interview for the position of \n" +
                        "\"" + jobTitle + "\" has been successfully completed and you have been \n" +
                        "officially accepted for this role!\n\n" +
                        "Your skills and performance throughout the process truly stood out. \n" +
                        "We look forward to welcoming you to the team."
        );
        msgLbl.setStyle(
                "-fx-font-size: 14px; -fx-text-fill: #334155; -fx-line-spacing: 4;"
        );
        msgLbl.setWrapText(true);

        // Interview details box
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle(
                "-fx-background-color: #f0f9ff;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #bae6fd;" +
                        "-fx-border-width: 1; -fx-border-radius: 12;" +
                        "-fx-padding: 18 22;"
        );
        Label detailsTitle = new Label("📋  Interview Summary");
        detailsTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;");

        String[][] details = {
                {"Position",  jobTitle},
                {"Date",      iv.getDate() != null
                        ? iv.getDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  'at'  HH:mm"))
                        : "—"},
                {"Type",      iv.getType() != null ? iv.getType() : "—"},
                {"Status",    "✅  Accepted"},
        };

        javafx.scene.layout.GridPane dGrid = new javafx.scene.layout.GridPane();
        dGrid.setHgap(20); dGrid.setVgap(8);
        for (int i = 0; i < details.length; i++) {
            Label k = new Label(details[i][0] + ":");
            k.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-min-width: 90;");
            Label v = new Label(details[i][1]);
            v.setStyle("-fx-font-size: 13px; -fx-text-fill: #0c4a6e; -fx-font-weight: bold;");
            dGrid.add(k, 0, i);
            dGrid.add(v, 1, i);
        }
        detailsBox.getChildren().addAll(detailsTitle, dGrid);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0;");

        // Contact recruiter section
        VBox contactBox = new VBox(14);
        contactBox.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1; -fx-border-radius: 12;" +
                        "-fx-padding: 20 22;"
        );

        Label contactTitle = new Label("📞  Contact Your Recruiter");
        contactTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;");

        Label contactSub = new Label("Reach out to discuss next steps, start date, and onboarding details:");
        contactSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        contactSub.setWrapText(true);

        // Recruiter card
        HBox recruiterCard = new HBox(16);
        recruiterCard.setAlignment(Pos.CENTER_LEFT);
        recruiterCard.setPadding(new Insets(16, 20, 16, 20));
        recruiterCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #bae6fd;" +
                        "-fx-border-width: 1.5; -fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,175,221,0.12), 8, 0, 0, 3);"
        );

        // Recruiter avatar
        StackPane rAvatar = new StackPane();
        rAvatar.setPrefSize(52, 52); rAvatar.setMinSize(52, 52);
        rAvatar.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0c4a6e, #0fafdd);" +
                        "-fx-background-radius: 50;"
        );
        String rInitials = "";
        if (recruiter != null) {
            if (recruiter.getNom()    != null && !recruiter.getNom().isEmpty())    rInitials += recruiter.getNom().substring(0,1).toUpperCase();
            if (recruiter.getPrenom() != null && !recruiter.getPrenom().isEmpty()) rInitials += recruiter.getPrenom().substring(0,1).toUpperCase();
        }
        Label rAvLbl = new Label(rInitials.isEmpty() ? "R" : rInitials);
        rAvLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;");
        rAvatar.getChildren().add(rAvLbl);

        // Recruiter info
        VBox rInfo = new VBox(6);
        HBox.setHgrow(rInfo, Priority.ALWAYS);

        String rFullName = recruiter != null
                ? ((recruiter.getNom()    != null ? recruiter.getNom()    : "") + " " +
                (recruiter.getPrenom() != null ? recruiter.getPrenom() : "")).trim()
                : "Your Recruiter";
        Label rName = new Label(rFullName.isEmpty() ? "Your Recruiter" : rFullName);
        rName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e;");

        Label rRole = new Label("Recruiter · BlindHire");
        rRole.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        VBox rContacts = new VBox(4);
        rContacts.setPadding(new Insets(6, 0, 0, 0));

        String rEmail = (recruiter != null && recruiter.getEmail() != null) ? recruiter.getEmail() : "—";
        String rPhone = (recruiter != null && recruiter.getPhone() != null) ? recruiter.getPhone() : "—";

        HBox emailRow = contactRow("📧", rEmail, CYAN);
        HBox phoneRow = contactRow("📱", rPhone, GREEN);

        rContacts.getChildren().addAll(emailRow, phoneRow);
        rInfo.getChildren().addAll(rName, rRole, rContacts);
        recruiterCard.getChildren().addAll(rAvatar, rInfo);

        contactBox.getChildren().addAll(contactTitle, contactSub, recruiterCard);

        // Closing note
        Label closingNote = new Label(
                "We wish you great success in your new role. Welcome aboard! 🚀"
        );
        closingNote.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #0c4a6e; -fx-font-style: italic;"
        );
        closingNote.setWrapText(true);

        body.getChildren().addAll(msgLbl, detailsBox, sep, contactBox, closingNote);

        // ── Footer ────────────────────────────────────────────────────────────
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(14, 36, 18, 36));
        footer.setStyle(
                "-fx-background-color: #f0f9ff;" +
                        "-fx-background-radius: 0 0 16 16;" +
                        "-fx-border-color: #bae6fd;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        Label footerLbl = new Label("BlindHire · Fair hiring, one candidate at a time");
        footerLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        footer.getChildren().add(footerLbl);

        letter.getChildren().addAll(banner, body, footer);
        return letter;
    }

    /** Helper: a small labelled contact row */
    private HBox contactRow(String icon, String value, String color) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label ic = new Label(icon);
        ic.setStyle("-fx-font-size: 13px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        row.getChildren().addAll(ic, val);
        return row;
    }


}