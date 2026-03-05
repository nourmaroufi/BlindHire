package Controller.FrontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.AIPracticeService;
import javafx.geometry.Pos;

public class PracticeInterviewController {

    @FXML private VBox chatContainer;
    @FXML private TextField messageField;

    private AIPracticeService aiService = new AIPracticeService();

    // Tracks whether the user has submitted a job title yet
    private boolean jobTitleReceived = false;
    private String jobTitle = "";

    @FXML
    public void initialize() {
        addAIMessage("👋 Welcome to AI Interview Practice!\n\nWhat job position would you like to be interviewed for?\n(e.g. Java Developer, Data Analyst, UI Designer...)");
    }

    @FXML
    private void handleSend() {
        String userMessage = messageField.getText().trim();
        if (userMessage.isEmpty()) return;

        addUserMessage(userMessage);
        messageField.clear();

        if (!jobTitleReceived) {
            // First message = job title
            jobTitle = userMessage;
            jobTitleReceived = true;

            addAIMessage("⏳ Generating interview questions for: " + jobTitle + "...");

            new Thread(() -> {
                String prompt =
                        "You are a professional HR recruiter. " +
                                "The candidate wants to practice for a \"" + jobTitle + "\" interview. " +
                                "Generate 5 interview questions (mix of technical and behavioral). " +
                                "Then immediately ask the first question to start the interview. " +
                                "Format: briefly list all 5 questions, then say 'Let\\'s begin!' and ask question 1.";

                String aiResponse = aiService.askAI(prompt);
                Platform.runLater(() -> addAIMessage(aiResponse));
            }).start();

        } else {
            // Subsequent messages = candidate answers
            String prompt =
                    "You are a professional HR recruiter conducting a \"" + jobTitle + "\" interview. " +
                            "Ask one question at a time and briefly evaluate the candidate's answer before asking the next one.\n\n" +
                            "Candidate answer: " + userMessage;

            new Thread(() -> {
                String aiResponse = aiService.askAI(prompt);
                Platform.runLater(() -> addAIMessage(aiResponse));
            }).start();
        }
    }


    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setStyle("-fx-alignment: center-right;");

        Label label = new Label(message);
        label.getStyleClass().add("user-message");
        label.setWrapText(true);
        label.setMaxWidth(500);

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }

    private void addAIMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setStyle("-fx-alignment: center-left;");

        Label label = new Label(message);
        label.getStyleClass().add("bot-message");
        label.setWrapText(true);

        // Important: allow it to expand properly
        label.setMaxWidth(500);

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }
}