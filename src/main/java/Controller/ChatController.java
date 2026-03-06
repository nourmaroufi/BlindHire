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
import Model.Interview;
import Model.Message;
import Model.User;
import Service.MessageService;
import Service.userservice;
import Utils.Mydb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ChatController {

    @FXML
    private ListView<Message> messageListView;
    @FXML
    private TextField messageField;
    @FXML private Label contactName;
    @FXML private Label contactStatus;
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

        // Fetch the other person's display name from DB
        String otherName = resolveOtherPersonName(interview, senderType);
        contactName.setText(otherName);

        // Status line shows the interview type
        String typeLabel = "Online".equalsIgnoreCase(interview.getType())
                ? "🌐 Online Interview" : "🏢 In-Person Interview";
        contactStatus.setText(typeLabel);

        // Avatar
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/rana.jpg"));
            profileImage.setImage(defaultAvatar);
        } catch (Exception ignored) {}

        loadMessages();
    }

    /**
     * Resolves the display name of the person on the OTHER side of the chat.
     *
     * If senderType == "CANDIDATE"  → other side is the RECRUITER  → use interviewer_id
     * If senderType == "RECRUITER"  → other side is the CANDIDATE  → join score → user
     */
    private String resolveOtherPersonName(Interview iv, String senderType) {
        try {
            Connection conn = Mydb.getInstance().getConnection();
            if ("CANDIDATE".equals(senderType)) {
                // We are the candidate — show recruiter's name
                String sql = "SELECT COALESCE(NULLIF(username,''), CONCAT(nom,' ',prenom)) AS display_name " +
                        "FROM user WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, iv.getInterviewerId());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("display_name");
            } else {
                // We are the recruiter — show candidate's name (via score → user)
                String sql = "SELECT COALESCE(NULLIF(u.username,''), CONCAT(u.nom,' ',u.prenom)) AS display_name " +
                        "FROM score s JOIN user u ON s.id_user = u.id " +
                        "WHERE s.id_score = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setLong(1, iv.getIdScore());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("display_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Fallback
        return "CANDIDATE".equals(senderType) ? "Recruiter" : "Candidate";
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