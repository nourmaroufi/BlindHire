package Controller;

import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Interview;
import services.InterviewService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import model.Candidat;
import services.CandidatService;


public class InterviewPageController {

    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private VBox calendarContainer;

    private CalendarView calendarView;
    private Calendar interviewCalendar;

    @FXML
    private VBox interviewContainer;
    @FXML

    private List<LocalDate> interviewDates;

    private InterviewService interviewService = new InterviewService();
    private CandidatService candidatService = new CandidatService();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DatePicker datePicker;
    private ComboBox<String> timeComboBox;
    private ComboBox<String> typeComboBox;

    private ComboBox<Integer> candidateComboBox;

    //private TextField candidateIdField;
    private TextField jobOfferField;
    private TextField interviewerField;


    @FXML
    public void initialize() {
        loadInterviews();
        setupCalendar();


        filterTypeComboBox.setOnAction(e -> loadInterviews());
        filterTypeComboBox.getItems().addAll("All", "Online", "In Person");
        filterTypeComboBox.setValue("All");

// When selection changes
        filterTypeComboBox.setOnAction(e -> loadInterviews());
        datePicker = new DatePicker();
        timeComboBox = new ComboBox<>();
        typeComboBox = new ComboBox<>();
        jobOfferField = new TextField();
        interviewerField = new TextField();


    }


    private void loadInterviews() {

        interviewContainer.getChildren().clear();

        try {
            List<Interview> interviews = interviewService.afficherAll();

            String selectedType = filterTypeComboBox.getValue();

            for (Interview interview : interviews) {

                // If "All" → show everything
                if (selectedType == null || selectedType.equals("All")) {
                    interviewContainer.getChildren().add(createInterviewCard(interview));
                }

                // If filtering → only add matching type
                else if (interview.getType().equalsIgnoreCase(selectedType)) {
                    interviewContainer.getChildren().add(createInterviewCard(interview));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load interviews: " + e.getMessage());
        }
    }

    private void setupCalendar() {
        // Create a CalendarFX calendar for interviews
        interviewCalendar = new Calendar("Interviews");
        interviewCalendar.setStyle(Calendar.Style.STYLE1); // pick a style
        interviewCalendar.setReadOnly(true); // so users can't modify

        // CalendarView
        calendarView = new CalendarView();
        calendarView.getCalendarSources().clear(); // remove default
        com.calendarfx.model.CalendarSource mySource = new com.calendarfx.model.CalendarSource("My Calendars");
        mySource.getCalendars().add(interviewCalendar);
        calendarView.getCalendarSources().add(mySource);

        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setRequestedTime(java.time.LocalTime.now());

        calendarView.setShowToolBar(true);
        calendarView.setShowPageToolBarControls(false);

        // Add the CalendarView to the VBox in FXML
        calendarContainer.getChildren().clear();
        calendarContainer.getChildren().add(calendarView);

        // Load entries from DB
        markInterviewDates();
    }
    private void markInterviewDates() {
        try {
            List<Interview> interviews = interviewService.afficherAll();
            interviewCalendar.clear(); // clear previous entries

            for (Interview interview : interviews) {
                Entry<String> entry = new Entry<>("Interview");
                entry.setInterval(interview.getDate(), interview.getDate().plusHours(1)); // 1-hour slot
                entry.setLocation("Candidate ID: " + interview.getId_candidat());
                interviewCalendar.addEntry(entry);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load interview dates: " + e.getMessage());
        }
    }

    private HBox createInterviewCard(Interview interview) {
        HBox card = new HBox(10);
        card.getStyleClass().add("interview-card");

       // Candidate ComboBox
        ComboBox<Candidat> candidateComboBox = new ComboBox<>();
        candidateComboBox.setPrefWidth(100);

        try {
            List<Candidat> candidats = candidatService.afficherAll();
            candidateComboBox.setItems(FXCollections.observableArrayList(candidats));

            // Select current candidate
            for (Candidat c : candidats) {
                if (c.getId_candidat() == interview.getId_candidat()) {
                    candidateComboBox.setValue(c);
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Date
        TextField dateField = new TextField(interview.getDate().format(formatter));
        dateField.setPrefWidth(160);

        // Type
        ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList("Online", "In Person"));
        typeComboBox.setValue(interview.getType());
        typeComboBox.setPrefWidth(120);

        // Job Offer
        TextField jobOfferField = new TextField(interview.getJob_offer());
        jobOfferField.setPrefWidth(120);

        // Interviewer
        TextField interviewerField = new TextField(interview.getInterviewer());
        interviewerField.setPrefWidth(120);

        // Buttons
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #26d0ce; -fx-text-fill: white; -fx-background-radius: 8;");
        updateBtn.setOnAction(e -> {
            try {
                int id = interview.getId();

                Candidat selectedCandidat = candidateComboBox.getValue();
                if (selectedCandidat == null) {
                    showAlert("Error", "Please select a candidate.");
                    return;
                }
                int candidateId = selectedCandidat.getId_candidat();

                String type = typeComboBox.getValue();
                String jobOffer = jobOfferField.getText();
                String interviewer = interviewerField.getText();

                // Convert dateField text back to LocalDateTime
                LocalDateTime dateTime = LocalDateTime.parse(dateField.getText(), formatter);

                interviewService.update(id, candidateId, dateTime, type, jobOffer, interviewer);

                showAlert("Success", "Interview updated!");
                loadInterviews();

            } catch (Exception ex) {
                showAlert("Error", "Failed to update interview: " + ex.getMessage());
            }
        });



        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 8;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Confirmation");
            confirm.setHeaderText("Are you sure you want to delete this interview?");
            confirm.setContentText("Candidate ID: " + interview.getId_candidat());

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        interviewService.delete(interview.getId());
                        loadInterviews();
                    } catch (SQLException ex) {
                        showAlert("Error", "Failed to delete interview: " + ex.getMessage());
                    }
                }
            });
        });
        // Chat Button
        Button chatBtn = new Button("Chat");
        chatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");

        chatBtn.setOnAction(e -> openChat(interview));

        card.getChildren().addAll(candidateComboBox, dateField, typeComboBox, jobOfferField, interviewerField, updateBtn, deleteBtn,chatBtn);

        return card;
    }
    private void openChat(Interview interview) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Chat.fxml"));
            Scene scene = new Scene(loader.load());

            ChatController controller = loader.getController();

            // Recruiter side
            controller.setInterview(interview, "RECRUITER");

            Stage stage = new Stage();
            stage.setTitle("Interview Chat");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAddInterviewForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addInterview.fxml"));
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Interview");
            stage.showAndWait();

            // Reload interviews after adding
            loadInterviews();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open Add Interview form.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}