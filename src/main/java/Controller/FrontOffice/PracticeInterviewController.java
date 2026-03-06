package Controller.FrontOffice;

import Utils.NavigationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import Service.AIPracticeService;
import javafx.geometry.Pos;

public class PracticeInterviewController {

    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
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
        messageBox.setStyle("-fx-alignment: center-right; -fx-padding: 4 8;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.setStyle(
                "-fx-background-color: linear-gradient(to right, #0fafdd, #057995);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 18 18 4 18;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-family: 'Segoe UI';"
        );

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addAIMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setStyle("-fx-alignment: center-left; -fx-padding: 4 8;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: #0f172a;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 18 18 18 4;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 18 18 18 4;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }
    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }
    /** Scrolls the chat to the bottom after a new message is added. */
    private void scrollToBottom() {
        if (chatScrollPane != null) {
            javafx.application.Platform.runLater(() ->
                    chatScrollPane.setVvalue(1.0));
        }
    }


}