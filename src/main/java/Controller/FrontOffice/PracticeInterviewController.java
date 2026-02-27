package Controller.FrontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.AIPracticeService;

public class PracticeInterviewController {

    @FXML
    private VBox chatContainer;

    @FXML
    private TextField messageField;

    private AIPracticeService aiService = new AIPracticeService();

    private boolean interviewStarted = false;

    @FXML
    public void initialize() {
        addAIMessage("Hello 👋 I will simulate your job interview today.\n" +
                "Position: Java Developer.\n" +
                "Let’s begin.\n\nTell me about yourself.");
        interviewStarted = true;
    }

    @FXML
    private void handleSend() {

        String userMessage = messageField.getText().trim();

        if (userMessage.isEmpty()) return;

        addUserMessage(userMessage);
        messageField.clear();

        String structuredPrompt =
                "You are a professional HR recruiter conducting a job interview " +
                        "for a Java Developer position. " +
                        "Ask one question at a time. " +
                        "After the candidate answers, briefly evaluate their answer " +
                        "and then ask the next question.\n\n" +
                        "Candidate answer: " + userMessage;

        new Thread(() -> {
            String aiResponse = aiService.askAI(structuredPrompt);

            Platform.runLater(() -> {
                addAIMessage(aiResponse);
            });
        }).start();
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setStyle("-fx-alignment: center-right;");

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color:#26d0ce;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:10;"
        );

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }

    private void addAIMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setStyle("-fx-alignment: center-left;");

        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.setStyle(
                "-fx-background-color:white;" +
                        "-fx-text-fill:black;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-color:#e0e0e0;"
        );

        messageBox.getChildren().add(label);
        chatContainer.getChildren().add(messageBox);
    }



}