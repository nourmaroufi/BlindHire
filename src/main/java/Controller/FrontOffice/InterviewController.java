package Controller.FrontOffice;

import Controller.ChatController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Interview;
import services.InterviewService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InterviewController {

    @FXML
    private VBox interviewContainer;

    private final int connectedCandidatId = 1; // temporary

    private InterviewService interviewService = new InterviewService();

    private DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        loadUpcomingInterviews();
    }

    private void loadUpcomingInterviews() {

        interviewContainer.getChildren().clear();

        try {
            List<Interview> interviews =
                    interviewService.afficherByCandidat(connectedCandidatId);

            LocalDateTime now = LocalDateTime.now();

            for (Interview interview : interviews) {
                if (interview.getDate().isAfter(now)) {
                    interviewContainer.getChildren()
                            .add(createInterviewCard(interview));
                }
            }

            if (interviewContainer.getChildren().isEmpty()) {
                Label noInterview =
                        new Label("No upcoming interviews scheduled.");
                noInterview.setStyle("-fx-text-fill: gray;");
                interviewContainer.getChildren().add(noInterview);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void openChat(Interview interview) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Chat.fxml")
            );

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

    private HBox createInterviewCard(Interview interview) {
        // Main card container
        HBox card = new HBox();
        card.getStyleClass().add("interview-card");
        card.setMaxWidth(Double.MAX_VALUE); // make it stretch

        // Content VBox inside the card (to hold all elements vertically)
        VBox content = new VBox(12);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);

        // ----- Header Row (icon + type badge) -----
        HBox headerRow = new HBox(12);
        headerRow.getStyleClass().add("card-header");

        // Icon based on interview type (you can replace with actual icons)
        Label iconLabel = new Label(getIconForType(interview.getType()));
        iconLabel.getStyleClass().add("type-icon");

        // Type badge
        Label typeBadge = new Label(interview.getType());
        typeBadge.getStyleClass().add("type-badge");

        headerRow.getChildren().addAll(iconLabel, typeBadge);

        // ----- Details Grid -----
        GridPane detailsGrid = new GridPane();
        detailsGrid.getStyleClass().add("details-grid");

        // Row 0: Date
        Label dateLabel = new Label("📅 Date:");
        dateLabel.getStyleClass().add("detail-label");
        Label dateValue = new Label(interview.getDate().format(formatter));
        dateValue.getStyleClass().add("detail-value");
        detailsGrid.add(dateLabel, 0, 0);
        detailsGrid.add(dateValue, 1, 0);

        // Row 1: Job
        Label jobLabel = new Label("💼 Job:");
        jobLabel.getStyleClass().add("detail-label");
        Label jobValue = new Label(interview.getJob_offer());
        jobValue.getStyleClass().add("detail-value");
        detailsGrid.add(jobLabel, 0, 1);
        detailsGrid.add(jobValue, 1, 1);

        // Row 2: Interviewer
        Label interviewerLabel = new Label("👤 Interviewer:");
        interviewerLabel.getStyleClass().add("detail-label");
        Label interviewerValue = new Label(interview.getInterviewer());
        interviewerValue.getStyleClass().add("detail-value");
        detailsGrid.add(interviewerLabel, 0, 2);
        detailsGrid.add(interviewerValue, 1, 2);

        // ----- Separator -----
        Separator separator = new Separator();
        separator.getStyleClass().add("card-separator");

        // ----- Actions Row -----
        HBox actionsRow = new HBox(10);
        actionsRow.getStyleClass().add("card-actions");

        Button chatButton = new Button("💬 Open Chat");
        chatButton.getStyleClass().add("chat-button");
        chatButton.setOnAction(e -> openChat(interview));

        actionsRow.getChildren().add(chatButton);

        // Assemble the card
        content.getChildren().addAll(headerRow, detailsGrid, separator, actionsRow);
        card.getChildren().add(content);

        return card;
    }

    // Helper to pick an icon based on interview type
    private String getIconForType(String type) {
        if (type == null) return "📅";
        switch (type.toLowerCase()) {
            case "technical": return "💻";
            case "hr": return "🤝";
            case "online": return "🌐";
            case "in person": return "🏢";
            default: return "📅";
        }
    }
}