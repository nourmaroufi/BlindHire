package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Interview;
import model.Message;
import services.MessageService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController {

    @FXML
    private ListView<Message> messageListView;
    @FXML
    private TextField messageField;
    @FXML
    private Label contactName;
    @FXML
    private Label contactStatus;
    @FXML
    private ImageView profileImage;

    private Interview interview;
    private String senderType; // "Recruiter" or "Candidate"
    private MessageService messageService = new MessageService();
    private ObservableList<Message> messageList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up the ListView with a custom cell factory
        messageListView.setItems(messageList);
        messageListView.setCellFactory(lv -> new ListCell<Message>() {
            private final HBox container = new HBox(10);
            private final Label messageLabel = new Label();
            private final Label timestampLabel = new Label();
            private final ImageView avatar = new ImageView();
            private final VBox bubbleWithTime = new VBox(2);


            {
                // Style the message bubble
                messageLabel.getStyleClass().add("message-bubble");
                messageLabel.setWrapText(true);

                timestampLabel.getStyleClass().add("timestamp");

                // VBox to hold bubble + timestamp (timestamp below bubble for received, above for sent? We'll handle alignment)
                bubbleWithTime.getChildren().addAll(messageLabel, timestampLabel);
                bubbleWithTime.setAlignment(Pos.CENTER_RIGHT); // default for sent

                avatar.setFitHeight(30);
                avatar.setFitWidth(30);
                avatar.setStyle("-fx-background-radius: 15; -fx-background-color: #ccc;");
                // If you have actual avatar URLs, set them later
            }

            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                } else {
                    // Set message content
                    messageLabel.setText(msg.getContent());



                    // Determine if message is from current user (based on senderType)
                    boolean isSentByMe = msg.getSenderType().equals(senderType);

                    if (isSentByMe) {
                        // Sent message: bubble on right, no avatar, timestamp inside bubble (or below)
                        messageLabel.getStyleClass().removeAll("received-message");
                        messageLabel.getStyleClass().add("sent-message");
                        timestampLabel.setStyle("-fx-text-fill: #b3d9ff;"); // lighter for dark bubble
                        container.setAlignment(Pos.CENTER_RIGHT);
                        container.getChildren().setAll(bubbleWithTime);
                    } else {
                        // Received message: bubble on left, avatar, timestamp below bubble
                        messageLabel.getStyleClass().removeAll("sent-message");
                        messageLabel.getStyleClass().add("received-message");
                        timestampLabel.setStyle("-fx-text-fill: #8e8e8e;");
                        container.setAlignment(Pos.CENTER_LEFT);
                        // Optionally set avatar image (if available)
                        // avatar.setImage(new Image(msg.getSenderAvatarUrl()));
                        container.getChildren().setAll(avatar, bubbleWithTime);
                        // Align timestamp to left within bubble
                        bubbleWithTime.setAlignment(Pos.CENTER_LEFT);
                    }

                    setGraphic(container);
                }
            }
        });
    }

    public void setInterview(Interview interview, String senderType) {
        this.interview = interview;
        this.senderType = senderType;

        // Update header with interview info (customize as needed)

        contactStatus.setText("Interview ID: " + interview.getId()); // or "Online" if you have status
        Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/rana.jpg"));
        profileImage.setImage(defaultAvatar);
        loadMessages();
    }

    private void loadMessages() {
        try {
            List<Message> messages = messageService.getMessagesByInterview(interview.getId());
            messageList.setAll(messages);
            // Scroll to bottom after loading
            messageListView.scrollTo(messageList.size() - 1);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load messages.");
        }
    }

    @FXML
    private void handleSend() {
        String content = messageField.getText();
        if (content.trim().isEmpty()) return;

        try {
            Message message = new Message(interview.getId(), senderType, content);
            messageService.sendMessage(message);
            messageField.clear();
            loadMessages(); // refresh list
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Could not send message.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}