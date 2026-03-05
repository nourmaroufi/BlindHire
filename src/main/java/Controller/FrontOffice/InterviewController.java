package Controller.FrontOffice;

import Controller.ChatController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Interview;
import services.InterviewService;
import services.MessageService;
import services.VideoCallService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InterviewController {


    @FXML private VBox interviewContainer;
    @FXML private VBox pastContainer;
    @FXML private VBox notificationDropdown;
    @FXML private VBox notificationList;
    @FXML private Label notifBadge;
    @FXML private StackPane notificationPane;

    private MessageService messageService = new MessageService();
    private VideoCallService videoCallService = new VideoCallService();
    private final int connectedCandidatId = 1; // temporary
    private InterviewService interviewService = new InterviewService();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadInterviews();

        // Toggle notification dropdown on bell click
        notificationPane.setOnMouseClicked(e -> {
            boolean showing = notificationDropdown.isVisible();
            notificationDropdown.setVisible(!showing);
            notificationDropdown.setManaged(!showing);
        });
    }

    private void loadInterviews() {
        interviewContainer.getChildren().clear();
        pastContainer.getChildren().clear();
        notificationList.getChildren().clear();

        try {
            List<Interview> interviews =
                    interviewService.afficherByCandidat(connectedCandidatId);

            LocalDateTime now = LocalDateTime.now();
            List<Interview> notifications = new ArrayList<>();

            for (Interview interview : interviews) {
                if (interview.getDate().isAfter(now)) {
                    // Upcoming
                    interviewContainer.getChildren().add(createInterviewCard(interview, false));
                } else {
                    // Past
                    pastContainer.getChildren().add(createInterviewCard(interview, true));

                    // Collect notifications for accepted/rejected
                    if (interview.getStatus() != null &&
                            !interview.getStatus().equals("Pending")) {
                        notifications.add(interview);
                    }
                }
            }

            // Empty states
            if (interviewContainer.getChildren().isEmpty()) {
                Label lbl = new Label("No upcoming interviews scheduled.");
                lbl.setStyle("-fx-text-fill:gray;");
                interviewContainer.getChildren().add(lbl);
            }
            if (pastContainer.getChildren().isEmpty()) {
                Label lbl = new Label("No past interviews yet.");
                lbl.setStyle("-fx-text-fill:gray;");
                pastContainer.getChildren().add(lbl);
            }

            // Build notification bell
            buildNotifications(notifications);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buildNotifications(List<Interview> notifications) {
        if (notifications.isEmpty()) {
            notifBadge.setVisible(false);
            Label none = new Label("No new notifications.");
            none.setStyle("-fx-text-fill:#888; -fx-font-size:12px;");
            notificationList.getChildren().add(none);
            return;
        }

        notifBadge.setText(String.valueOf(notifications.size()));
        notifBadge.setVisible(true);

        for (Interview interview : notifications) {
            String status = interview.getStatus();
            String emoji  = status.equals("Accepted") ? "🎉" : "❌";
            String color  = status.equals("Accepted") ? "#27ae60" : "#e74c3c";
            String msg    = status.equals("Accepted")
                    ? "You were accepted for \"" + interview.getJob_offer() + "\""
                    : "You were rejected for \"" + interview.getJob_offer() + "\"";

            HBox notifItem = new HBox(10);
            notifItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            notifItem.setStyle(
                    "-fx-background-color:#f9f9f9;" +
                            "-fx-background-radius:8;" +
                            "-fx-padding: 8 12;" +
                            "-fx-border-color:" + color + ";" +
                            "-fx-border-radius:8;" +
                            "-fx-border-width: 0 0 0 3;"
            );

            Label emojiLbl = new Label(emoji);
            emojiLbl.setStyle("-fx-font-size:16px;");

            VBox textBox = new VBox(2);
            Label msgLbl = new Label(msg);
            msgLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
            Label dateLbl = new Label("📅 " + interview.getDate().format(displayFormatter));
            dateLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#888;");
            textBox.getChildren().addAll(msgLbl, dateLbl);

            // Spacer to push X to the right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // X dismiss button
            Button dismissBtn = new Button("✕");
            dismissBtn.setStyle(
                    "-fx-background-color:transparent;" +
                            "-fx-text-fill:#aaa;" +
                            "-fx-font-size:12px;" +
                            "-fx-cursor:hand;" +
                            "-fx-padding: 0 4;"
            );
            dismissBtn.setOnAction(e -> {
                notificationList.getChildren().remove(notifItem);

                // Update badge count
                int remaining = notificationList.getChildren().size();
                if (remaining == 0) {
                    notifBadge.setVisible(false);
                    Label none = new Label("No new notifications.");
                    none.setStyle("-fx-text-fill:#888; -fx-font-size:12px;");
                    notificationList.getChildren().add(none);
                } else {
                    notifBadge.setText(String.valueOf(remaining));
                }
            });

            notifItem.getChildren().addAll(emojiLbl, textBox, spacer, dismissBtn);
            notificationList.getChildren().add(notifItem);
        }
    }

    private HBox createInterviewCard(Interview interview, boolean isPast) {
        HBox card = new HBox();
        card.getStyleClass().add("interview-card");
        card.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(12);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Header row
        HBox headerRow = new HBox(12);
        headerRow.getStyleClass().add("card-header");

        Label iconLabel = new Label(getIconForType(interview.getType()));
        iconLabel.getStyleClass().add("type-icon");

        Label typeBadge = new Label(interview.getType());
        typeBadge.getStyleClass().add("type-badge");

        // Status badge (shown on past interviews)
        if (isPast) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            String status = interview.getStatus() != null ? interview.getStatus() : "Pending";
            String badgeColor = switch (status) {
                case "Accepted" -> "#27ae60";
                case "Rejected" -> "#e74c3c";
                default         -> "#f39c12";
            };
            Label statusBadge = new Label(status);
            statusBadge.setStyle(
                    "-fx-background-color:" + badgeColor + ";" +
                            "-fx-text-fill:white;" +
                            "-fx-padding: 3 10;" +
                            "-fx-background-radius:20;" +
                            "-fx-font-size:12px;"
            );
            headerRow.getChildren().addAll(iconLabel, typeBadge, spacer, statusBadge);
        } else {
            headerRow.getChildren().addAll(iconLabel, typeBadge);
        }

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.getStyleClass().add("details-grid");

        Label dateLabel = new Label("📅 Date:");
        dateLabel.getStyleClass().add("detail-label");
        Label dateValue = new Label(interview.getDate().format(displayFormatter));
        dateValue.getStyleClass().add("detail-value");
        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(dateValue, 1, 0);

        Label jobLabel = new Label("💼 Job:");
        jobLabel.getStyleClass().add("detail-label");
        Label jobValue = new Label(interview.getJob_offer());
        jobValue.getStyleClass().add("detail-value");
        detailsGrid.add(jobLabel, 0, 1);
        detailsGrid.add(jobValue, 1, 1);

        Label interviewerLabel = new Label("👤 Interviewer:");
        interviewerLabel.getStyleClass().add("detail-label");
        Label interviewerValue = new Label(interview.getInterviewer());
        interviewerValue.getStyleClass().add("detail-value");
        detailsGrid.add(interviewerLabel, 0, 2);
        detailsGrid.add(interviewerValue, 1, 2);

        // Location hyperlink
        if (interview.getLocationLink() != null && !interview.getLocationLink().isEmpty()) {
            Label locationLabel = new Label("📍 Location:");
            locationLabel.getStyleClass().add("detail-label");

            Hyperlink locationLink = new Hyperlink(
                    interview.getType().equals("Online") ? "Join Meeting" : "View Location"
            );
            locationLink.setStyle("-fx-text-fill:#1a2980; -fx-font-size:13px;");
            locationLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            new java.net.URI(interview.getLocationLink())
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            detailsGrid.add(locationLabel, 0, 3);
            detailsGrid.add(locationLink, 1, 3);
        }


        Separator separator = new Separator();
        separator.getStyleClass().add("card-separator");

        // Actions row (only for upcoming)
        HBox actionsRow = new HBox(10);
        actionsRow.getStyleClass().add("card-actions");

        if (!isPast) {
            // Get unread count
            int unread = 0;
            try {
                unread = messageService.countUnread(interview.getId(), "CANDIDATE");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Button + badge in a StackPane
            Button chatButton = new Button("💬 Open Chat");
            chatButton.getStyleClass().add("chat-button");

            StackPane chatStack = new StackPane(chatButton);

            if (unread > 0) {
                Label badge = new Label(String.valueOf(unread));
                badge.setStyle(
                        "-fx-background-color:#e74c3c;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:10;" +
                                "-fx-font-size:10px;" +
                                "-fx-padding: 1 5;" +
                                "-fx-font-weight:bold;"
                );
                StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
                StackPane.setMargin(badge, new javafx.geometry.Insets(-5, -5, 0, 0));
                chatStack.getChildren().add(badge);

                // Mark as read when chat is opened
                chatButton.setOnAction(e -> {
                    try {
                        messageService.markAsRead(interview.getId(), "CANDIDATE");
                        badge.setVisible(false);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    openChat(interview);
                });
            } else {
                chatButton.setOnAction(e -> openChat(interview));
            }

            actionsRow.getChildren().add(chatStack);

            if ("Online".equals(interview.getType())) {
                Button joinCallBtn = new Button("📹 Join Call");
                joinCallBtn.setStyle(
                        "-fx-background-color:#1a2980;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:8;" +
                                "-fx-padding: 6 14;"
                );
                joinCallBtn.setOnAction(e ->
                        videoCallService.openVideoCall(interview, "CANDIDATE")
                );
                actionsRow.getChildren().add(joinCallBtn);
            }
        }

        content.getChildren().addAll(headerRow, detailsGrid, separator, actionsRow);
        card.getChildren().add(content);
        return card;
    }

    private void openChat(Interview interview) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Scene scene = new Scene(loader.load());
            ChatController controller = loader.getController();
            controller.setInterview(interview, "CANDIDATE");
            Stage stage = new Stage();
            stage.setTitle("Interview Chat");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getIconForType(String type) {
        if (type == null) return "📅";
        switch (type.toLowerCase()) {
            case "technical": return "💻";
            case "hr":        return "🤝";
            case "online":    return "🌐";
            case "in person": return "🏢";
            default:          return "📅";
        }
    }
}
