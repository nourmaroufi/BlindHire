package Controller;

import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Interview;
import services.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import model.Candidat;


public class InterviewPageController {
    private VideoCallService videoCallService = new VideoCallService();

    @FXML private VBox candidateCardsContainer;
    private MessageService messageService = new MessageService();
    private SentimentService sentimentService = new SentimentService();
    @FXML
    private VBox pastInterviewContainer;
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
    @FXML private VBox overviewContainer;


    @FXML
    public void initialize() {
        loadPastInterviews();
        loadInterviews();
        setupCalendar();
        loadOverviewCards();



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
        // Block past dates
        datePicker.setDayCellFactory(picker -> new DateCell() {

        });


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
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Candidate ComboBox
        ComboBox<Candidat> candidateComboBox = new ComboBox<>();
        candidateComboBox.setPrefWidth(120);
        try {
            List<Candidat> candidats = candidatService.afficherAll();
            candidateComboBox.setItems(FXCollections.observableArrayList(candidats));
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
        ComboBox<String> typeComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Online", "In Person")
        );
        typeComboBox.setValue(interview.getType());
        typeComboBox.setPrefWidth(110);

        // Job Offer
        TextField jobOfferField = new TextField(interview.getJob_offer());
        jobOfferField.setPrefWidth(120);

        // Interviewer
        TextField interviewerField = new TextField(interview.getInterviewer());
        interviewerField.setPrefWidth(120);

        // Location — editable text field + clickable hyperlink button
        TextField locationField = new TextField(
                interview.getLocationLink() != null ? interview.getLocationLink() : ""
        );
        locationField.setPrefWidth(130);
        locationField.setPromptText("Paste link...");

        Button openLinkBtn = new Button("🔗");
        openLinkBtn.setStyle(
                "-fx-background-color:#1a2980; -fx-text-fill:white;" +
                        "-fx-background-radius:6; -fx-padding: 4 8;"
        );
        openLinkBtn.setTooltip(new Tooltip("Open link"));
        openLinkBtn.setOnAction(e -> {
            String link = locationField.getText().trim();
            if (!link.isEmpty()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(link));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox locationBox = new HBox(4, locationField, openLinkBtn);
        locationBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        locationBox.setPrefWidth(180);

        // Notes — editable text field
        TextField notesField = new TextField(
                interview.getNotes() != null ? interview.getNotes() : ""
        );
        notesField.setPrefWidth(150);
        notesField.setPromptText("Add notes...");
        if (interview.getNotes() != null && !interview.getNotes().isEmpty()) {
            notesField.setTooltip(new Tooltip(interview.getNotes()));
        }

        // Update button
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color:#26d0ce; -fx-text-fill:white; -fx-background-radius:8;");
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
                String location = locationField.getText().trim();
                String notes = notesField.getText().trim();
                LocalDateTime dateTime = LocalDateTime.parse(dateField.getText(), formatter);

                interviewService.update(id, candidateId, dateTime, type,
                        jobOffer, interviewer, location.isEmpty() ? null : location);

                // Save notes separately
                interviewService.updateNotes(id, notes.isEmpty() ? null : notes);

                showAlert("Success", "Interview updated!");
                loadInterviews();
            } catch (Exception ex) {
                showAlert("Error", "Failed to update interview: " + ex.getMessage());
            }
        });

        // Delete button
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color:#ff6b6b; -fx-text-fill:white; -fx-background-radius:8;");
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

        // Chat button
        Button chatBtn = new Button("Chat");
        chatBtn.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white; -fx-background-radius:8;");
        chatBtn.setOnAction(e -> openChat(interview));

        card.getChildren().addAll(
                candidateComboBox, dateField, typeComboBox,
                jobOfferField, interviewerField,
                locationBox, notesField,
                updateBtn, deleteBtn, chatBtn
        );

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
            Parent root = loader.load();

            // Create scene with explicit dimensions (width, height)
            Scene scene = new Scene(root, 700, 650);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Interview");
            stage.setResizable(false); // optional: prevents resizing, keeps exact size

            stage.showAndWait(); // now the stage will appear at 700x650

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
    private void loadPastInterviews() {
        pastInterviewContainer.getChildren().clear();
        try {
            List<Interview> all = interviewService.afficherAll();
            LocalDateTime now = LocalDateTime.now();

            for (Interview interview : all) {
                if (interview.getDate().isBefore(now)) {
                    pastInterviewContainer.getChildren().add(
                            createPastInterviewCard(interview)
                    );
                }
            }

            if (pastInterviewContainer.getChildren().isEmpty()) {
                Label empty = new Label("No past interviews yet.");
                empty.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
                pastInterviewContainer.getChildren().add(empty);
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load past interviews: " + e.getMessage());
        }
    }
    private HBox createSentimentBar(String sentimentResult) {
        // Parse sentiment and score from result like "🟢 Positive (92% confidence)"
        String color;
        double progress;

        String lower = sentimentResult.toLowerCase();
        if (lower.contains("positive")) {
            color = "#27ae60";
        } else if (lower.contains("negative")) {
            color = "#e74c3c";
        } else {
            color = "#f39c12";
        }

        // Extract percentage number
        try {
            int start = sentimentResult.indexOf("(") + 1;
            int end = sentimentResult.indexOf("%");
            progress = Double.parseDouble(sentimentResult.substring(start, end).trim()) / 100.0;
        } catch (Exception e) {
            progress = 0.5;
        }

        // Background track
        javafx.scene.layout.StackPane track = new javafx.scene.layout.StackPane();
        track.setPrefHeight(10);
        track.setPrefWidth(200);
        track.setStyle("-fx-background-color:#e0e0e0; -fx-background-radius:5;");

        // Filled bar
        javafx.scene.layout.StackPane fill = new javafx.scene.layout.StackPane();
        fill.setPrefHeight(10);
        fill.setPrefWidth(200 * progress);
        fill.setStyle("-fx-background-color:" + color + "; -fx-background-radius:5;");
        fill.setTranslateX(-(200 * (1 - progress)) / 2);

        track.getChildren().add(fill);

        // Percentage label
        Label pctLabel = new Label(String.format("%.0f%%", progress * 100));
        pctLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#555;");

        HBox barRow = new HBox(8);
        barRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        barRow.getChildren().addAll(track, pctLabel);

        return barRow;
    }
    private VBox createPastInterviewCard(Interview interview) {
        // ── Outer card ──────────────────────────────────────────
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        // ── Status badge color ───────────────────────────────────
        String status = interview.getStatus() != null ? interview.getStatus() : "Pending";
        String badgeColor = switch (status) {
            case "Accepted" -> "#27ae60";
            case "Rejected" -> "#e74c3c";
            default         -> "#f39c12";
        };

        // ── Summary row (always visible) ─────────────────────────
        HBox summary = new HBox(15);
        summary.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label candidateLabel = new Label("Candidate #" + interview.getId_candidat());
        candidateLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

        Label dateLabel = new Label("📅 " + interview.getDate().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
        dateLabel.setStyle("-fx-text-fill:#555;");

        Label typeLabel = new Label(interview.getType());
        typeLabel.setStyle("-fx-text-fill:#555;");

        Label statusBadge = new Label(status);
        statusBadge.setStyle(
                "-fx-background-color:" + badgeColor + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-padding: 3 10;" +
                        "-fx-background-radius:20;" +
                        "-fx-font-size:12px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label expandHint = new Label("▼ Details");
        expandHint.setStyle("-fx-text-fill:#1a2980; -fx-cursor:hand; -fx-font-size:12px;");

        summary.getChildren().addAll(candidateLabel, dateLabel, typeLabel, spacer, statusBadge, expandHint);

        // ── Detail panel (hidden by default) ─────────────────────
        VBox details = new VBox(8);
        details.setVisible(false);
        details.setManaged(false);
        details.setStyle("-fx-border-color:#f0f0f0; -fx-border-width:1 0 0 0; -fx-padding:10 0 0 0;");

        Label jobLabel = new Label("💼 Job Offer: " + interview.getJob_offer());
        Label interviewerLabel = new Label("👤 Interviewer: " + interview.getInterviewer());
        Label notesLabel = new Label("📝 Notes: " +
                (interview.getNotes() != null ? interview.getNotes() : "No notes yet."));
        notesLabel.setWrapText(true);

        for (Label l : new Label[]{jobLabel, interviewerLabel, notesLabel}) {
            l.setStyle("-fx-font-size:13px; -fx-text-fill:#444;");
        }

        // ── Sentiment ─────────────────────────────────────────────
        String initialSentiment;
        if (interview.getNotes() != null && !interview.getNotes().isEmpty()) {
            initialSentiment = sentimentService.analyze(interview.getNotes());
        } else {
            initialSentiment = null;
        }

        Label sentimentLabel = new Label("🧠 AI Sentiment: " +
                (initialSentiment != null ? initialSentiment : "No notes yet.")
        );
        sentimentLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#000; -fx-font-style:italic;");

        VBox sentimentBarBox = new VBox(4);
        if (initialSentiment != null) {
            sentimentBarBox.getChildren().add(createSentimentBar(initialSentiment));
        }

        // ── Notes button ─────────────────────────────────────────
        Button notesBtn = new Button("📝 Add/Edit Notes");
        notesBtn.setStyle(
                "-fx-background-color:#1a2980; -fx-text-fill:white;" +
                        "-fx-background-radius:8; -fx-padding: 5 12;"
        );
        notesBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(
                    interview.getNotes() != null ? interview.getNotes() : ""
            );
            dialog.setTitle("Interview Notes");
            dialog.setHeaderText("Add notes for this interview");
            dialog.setContentText("Notes:");
            dialog.showAndWait().ifPresent(note -> {
                try {
                    interviewService.updateNotes(interview.getId(), note);
                    interview.setNotes(note);
                    notesLabel.setText("📝 Notes: " + note);

                    sentimentLabel.setText("🧠 AI Sentiment: Analyzing...");
                    new Thread(() -> {
                        String result = sentimentService.analyze(note);
                        javafx.application.Platform.runLater(() -> {
                            sentimentLabel.setText("🧠 AI Sentiment: " + result);
                            sentimentBarBox.getChildren().clear();
                            sentimentBarBox.getChildren().add(createSentimentBar(result));
                        });
                    }).start();

                } catch (SQLException ex) {
                    showAlert("Error", "Could not save notes: " + ex.getMessage());
                }
            });
        });

        // ── Accept / Reject buttons ───────────────────────────────
        Button acceptBtn = new Button("✅ Accept");
        acceptBtn.setStyle(
                "-fx-background-color:#27ae60; -fx-text-fill:white;" +
                        "-fx-background-radius:8; -fx-padding: 5 12;"
        );
        Button rejectBtn = new Button("❌ Reject");
        rejectBtn.setStyle(
                "-fx-background-color:#e74c3c; -fx-text-fill:white;" +
                        "-fx-background-radius:8; -fx-padding: 5 12;"
        );

        acceptBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Accept");
            confirm.setHeaderText("Accept Candidate #" + interview.getId_candidat() + "?");
            confirm.setContentText("Are you sure you want to accept this candidate for the position of " + interview.getJob_offer() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        interviewService.updateStatus(interview.getId(), "Accepted");
                        interview.setStatus("Accepted");
                        statusBadge.setText("Accepted");
                        statusBadge.setStyle(
                                "-fx-background-color:#27ae60; -fx-text-fill:white;" +
                                        "-fx-padding: 3 10; -fx-background-radius:20; -fx-font-size:12px;"
                        );
                        acceptBtn.setDisable(true);
                        rejectBtn.setDisable(true);
                    } catch (SQLException ex) {
                        showAlert("Error", "Could not update status.");
                    }
                }
            });
        });

        rejectBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Reject");
            confirm.setHeaderText("Reject Candidate #" + interview.getId_candidat() + "?");
            confirm.setContentText("Are you sure you want to reject this candidate for the position of " + interview.getJob_offer() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        interviewService.updateStatus(interview.getId(), "Rejected");
                        interview.setStatus("Rejected");
                        statusBadge.setText("Rejected");
                        statusBadge.setStyle(
                                "-fx-background-color:#e74c3c; -fx-text-fill:white;" +
                                        "-fx-padding: 3 10; -fx-background-radius:20; -fx-font-size:12px;"
                        );
                        acceptBtn.setDisable(true);
                        rejectBtn.setDisable(true);
                    } catch (SQLException ex) {
                        showAlert("Error", "Could not update status.");
                    }
                }
            });
        });

        // Disable buttons if already decided
        if ("Accepted".equals(status) || "Rejected".equals(status)) {
            acceptBtn.setDisable(true);
            rejectBtn.setDisable(true);
        }

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionRow.getChildren().addAll(notesBtn, acceptBtn, rejectBtn);

        details.getChildren().addAll(
                jobLabel, interviewerLabel, notesLabel,
                sentimentLabel, sentimentBarBox, actionRow
        );

        // ── Toggle details on click ───────────────────────────────
        summary.setOnMouseClicked(e -> {
            boolean showing = details.isVisible();
            details.setVisible(!showing);
            details.setManaged(!showing);
            expandHint.setText(showing ? "▼ Details" : "▲ Details");
        });
        summary.setStyle("-fx-cursor:hand;");

        card.getChildren().addAll(summary, details);
        return card;
    }
    // ── Add this method to InterviewPageController.java ──────────────

    private void loadOverviewCards() {

        overviewContainer.getChildren().clear();

        try {
            List<Interview> all = interviewService.afficherAll();
            LocalDateTime now = LocalDateTime.now();

            HBox currentRow = new HBox(20);
            currentRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            int count = 0;

            for (Interview interview : all) {
                if (interview.getDate().isAfter(now)) {
                    currentRow.getChildren().add(createOverviewCard(interview));
                    count++;
                    if (count % 3 == 0) {
                        overviewContainer.getChildren().add(currentRow);
                        currentRow = new HBox(20);
                        currentRow.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                    }
                }
            }

            // Add remaining cards
            if (!currentRow.getChildren().isEmpty()) {
                overviewContainer.getChildren().add(currentRow);
            }

            if (count == 0) {
                Label empty = new Label("No upcoming interviews.");
                empty.setStyle("-fx-text-fill:#888; -fx-font-size:14px;");
                overviewContainer.getChildren().add(empty);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load overview: " + e.getMessage());
        }
    }

    private VBox createOverviewCard(Interview interview) {

        VBox card = new VBox(12);
        card.setPrefWidth(270);
        card.setPrefHeight(270);
        card.setStyle(
                "-fx-background-color:#dbeafe;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0.2, 0, 4);"
        );

        // ── Type badge + status ───────────────────────────────────
        HBox topRow = new HBox(8);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String typeIcon = interview.getType() != null &&
                interview.getType().equals("Online") ? "🌐" : "🏢";
        Label typeBadge = new Label(typeIcon + " " + interview.getType());
        typeBadge.setStyle(
                "-fx-background-color:#1a2980;" +
                        "-fx-text-fill:white;" +
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius:20;" +
                        "-fx-font-size:12px;" +
                        "-fx-font-weight:bold;"
        );

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
                        "-fx-padding: 4 10;" +
                        "-fx-background-radius:20;" +
                        "-fx-font-size:12px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        topRow.getChildren().addAll(typeBadge, spacer, statusBadge);

        // ── Candidate ─────────────────────────────────────────────
        Label candidateLabel = new Label("👤 Candidate #" + interview.getId_candidat());
        candidateLabel.setStyle(
                "-fx-font-size:16px;" +
                        "-fx-font-weight:bold;" +
                        "-fx-text-fill:#1a2980;"
        );

        // ── Job offer ─────────────────────────────────────────────
        Label jobLabel = new Label("💼 " + interview.getJob_offer());
        jobLabel.setStyle("-fx-font-size:14px; -fx-text-fill:#333;");
        jobLabel.setWrapText(true);

        // ── Date ─────────────────────────────────────────────────
        Label dateLabel = new Label("📅 " + interview.getDate().format(
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        ));
        dateLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

        // ── Interviewer ───────────────────────────────────────────
        Label interviewerLabel = new Label("🎙 " + interview.getInterviewer());
        interviewerLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#555;");

        // ── Location hyperlink ────────────────────────────────────
        Hyperlink locationLink = null;
        if (interview.getLocationLink() != null &&
                !interview.getLocationLink().isEmpty() &&
                "In Person".equals(interview.getType())) {

            locationLink = new Hyperlink("📍 View Location");
            locationLink.setStyle("-fx-text-fill:#1a2980; -fx-font-size:13px;");
            final String link = interview.getLocationLink();
            locationLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(link));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        }

        // ── Spacer to push button to bottom ──────────────────────
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, javafx.scene.layout.Priority.ALWAYS);

        // ── Separator ─────────────────────────────────────────────
        Separator sep = new Separator();

        // ── Chat button with unread badge ─────────────────────────
        Button chatBtn = new Button("💬 Open Chat");
        chatBtn.setStyle(
                "-fx-background-color:white;" +
                        "-fx-text-fill:#1a2980;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:15;" +
                        "-fx-padding: 8 20;" +
                        "-fx-font-size:13px;" +
                        "-fx-cursor:hand;"
        );


        int unread = 0;
        try {
            unread = messageService.countUnread(interview.getId(), "RECRUITER");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        javafx.scene.layout.StackPane chatStack = new javafx.scene.layout.StackPane(chatBtn);
        chatStack.setAlignment(javafx.geometry.Pos.CENTER);

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
            javafx.scene.layout.StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
            javafx.scene.layout.StackPane.setMargin(badge,
                    new javafx.geometry.Insets(-5, -5, 0, 0));
            chatStack.getChildren().add(badge);

            chatBtn.setOnAction(e -> {
                badge.setVisible(false);
                openChat(interview);
            });
        } else {
            chatBtn.setOnAction(e -> openChat(interview));
        }

        // Assemble card
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER);
        btnRow.getChildren().add(chatStack);

// Only show video call button for online interviews
        if ("Online".equals(interview.getType())) {
            Button videoBtn = new Button("📹 Start Call");
            videoBtn.setStyle(
                    "-fx-background-color:#1a2980;" +
                            "-fx-text-fill:white;" +
                            "-fx-font-weight:bold;" +
                            "-fx-background-radius:15;" +
                            "-fx-padding: 8 16;" +
                            "-fx-font-size:13px;" +
                            "-fx-cursor:hand;"
            );
            videoBtn.setOnAction(e -> videoCallService.openVideoCall(interview, "RECRUITER"));
            btnRow.getChildren().add(videoBtn);
        }

        card.getChildren().addAll(topRow, candidateLabel, jobLabel,
                dateLabel, interviewerLabel);
        if (locationLink != null) card.getChildren().add(locationLink);
        card.getChildren().addAll(bottomSpacer, sep, btnRow);

        return card;
    }
}