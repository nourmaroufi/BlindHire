package Controller.BackOffice.Candidature;

import Model.Candidature;
import Model.User;
import Service.CandidatureService;
import Service.EmailService;
import Service.JobOfferService;
import Service.NotificationCService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CandidatureDetailsController implements Initializable {

    @FXML private Label candidateNameLabel;
    @FXML private Label jobTitleLabel;
    @FXML private Label statusBadge;
    @FXML private Label avatarInitials;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label educationLabel;
    @FXML private Label applicationDateLabel;
    @FXML private Label experienceLabel;
    @FXML private TextArea coverLetterArea;
    @FXML private FlowPane skillsContainer;
    @FXML private VBox rejectionReasonContainer;
    @FXML private Label rejectionReasonLabel;
    @FXML private Button acceptButton;
    @FXML private Button rejectButton;
    @FXML private Button viewResumeButton;

    private Candidature candidature;
    private User candidate;
    private CandidatureController parentController;
    private CandidatureService candidatureService;
    private JobOfferService jobOfferService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        candidatureService = new CandidatureService();
        jobOfferService = new JobOfferService();
        setupButtonHoverEffects(); // Add this line
    }

    public void setCandidature(Candidature candidature, CandidatureController parentController) {
        this.candidature = candidature;
        this.parentController = parentController;
        loadCandidatureDetails();
    }

    private void setupButtonHoverEffects() {
        // View Resume button hover
        viewResumeButton.setOnMouseEntered(e -> {
            viewResumeButton.setStyle("-fx-background-color: rgba(99,102,241,0.28); -fx-text-fill: #a5b4fc; " +
                    "-fx-border-color: linear-gradient(to right, #1e40af, #2563eb); " +
                    "-fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; " +
                    "-fx-padding: 14 25; -fx-font-size: 14px; -fx-font-weight: bold; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, #2563eb80, 10, 0, 0, 2);");
        });

        viewResumeButton.setOnMouseExited(e -> {
            viewResumeButton.setStyle("-fx-background-color: rgba(99,102,241,0.15); -fx-text-fill: #818cf8; " +
                    "-fx-border-color: linear-gradient(to right, #2563eb, #3b82f6); " +
                    "-fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; " +
                    "-fx-padding: 14 25; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        });

        // Accept button hover
        acceptButton.setOnMouseEntered(e -> {
            acceptButton.setStyle("-fx-background-color: linear-gradient(to right, #059669, #047857); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 15; -fx-padding: 14 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #10b981, 20, 0.4, 0, 6);");
        });

        acceptButton.setOnMouseExited(e -> {
            acceptButton.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 15; -fx-padding: 14 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #10b98180, 15, 0.3, 0, 4);");
        });

        // Reject button hover
        rejectButton.setOnMouseEntered(e -> {
            rejectButton.setStyle("-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 15; -fx-padding: 14 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #ef4444, 20, 0.4, 0, 6);");
        });

        rejectButton.setOnMouseExited(e -> {
            rejectButton.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; " +
                    "-fx-background-radius: 15; -fx-padding: 14 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, #ef444480, 15, 0.3, 0, 4);");
        });

        // Close button hover
        Button closeButton = (Button) viewResumeButton.getScene().lookup("#closeButton");
        if (closeButton != null) {
            closeButton.setOnMouseEntered(e -> {
                closeButton.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #334155; " +
                        "-fx-border-color: linear-gradient(to right, #64748b, #475569); " +
                        "-fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; " +
                        "-fx-padding: 14 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, #64748b80, 10, 0, 0, 2);");
            });

            closeButton.setOnMouseExited(e -> {
                closeButton.setStyle("-fx-background-color: white; -fx-text-fill: #475569; " +
                        "-fx-border-color: linear-gradient(to right, #94a3b8, #64748b); " +
                        "-fx-border-width: 2; -fx-border-radius: 15; -fx-background-radius: 15; " +
                        "-fx-padding: 14 30; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            });
        }
    }
    private void loadCandidatureDetails() {
        try {
            // Load candidate details
            candidate = candidatureService.getCandidateUserById(candidature.getCandidateId());
            String jobTitle = candidatureService.getJobTitleById(candidature.getJobOfferId());

            // Set basic information
            if (candidate != null) {
                String fullName = candidate.getNom() + " " + candidate.getPrenom();
                candidateNameLabel.setText(fullName);

                // Set avatar initials
                String initials = (candidate.getNom() != null && !candidate.getNom().isEmpty() ?
                        candidate.getNom().substring(0, 1) : "") +
                        (candidate.getPrenom() != null && !candidate.getPrenom().isEmpty() ?
                                candidate.getPrenom().substring(0, 1) : "");
                avatarInitials.setText(initials.isEmpty() ? "?" : initials);

                // Contact information
                emailLabel.setText(candidate.getEmail() != null ? candidate.getEmail() : "Not provided");
                phoneLabel.setText(candidate.getPhone() != null ? candidate.getPhone() : "Not provided");

                // Education
                educationLabel.setText(candidate.getDiplomas() != null ?
                        candidate.getDiplomas() : "Not specified");

                // Experience
                experienceLabel.setText(candidate.getExperience() != null ?
                        candidate.getExperience() : "Not specified");
            } else {
                candidateNameLabel.setText("Unknown Candidate");
                avatarInitials.setText("?");
                emailLabel.setText("Not provided");
                phoneLabel.setText("Not provided");
                educationLabel.setText("Not specified");
                experienceLabel.setText("Not specified");
            }

            // Job title
            jobTitleLabel.setText("Applying for: " + (jobTitle != null ? jobTitle : "Unknown Position"));

            // Application date
            applicationDateLabel.setText(candidature.getApplicationDate() != null ?
                    candidature.getApplicationDate().toString() : "Not specified");

            // Status badge
            updateStatusBadge(candidature.getStatus());

            // Skills (parse from candidate's skills if available)
            if (candidate != null && candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
                List<String> skills = Arrays.asList(candidate.getSkills().split(","));
                for (String skill : skills) {
                    Label skillLabel = new Label(skill.trim());
                    skillLabel.setStyle("-fx-background-color: rgba(99,102,241,0.18);" +
                            "-fx-text-fill: #818cf8;" +
                            "-fx-padding: 5 14; -fx-background-radius: 30;" +
                            "-fx-border-color: rgba(99,102,241,0.35); -fx-border-width: 1; -fx-border-radius: 30;" +
                            "-fx-font-size: 12px; -fx-font-weight: 600; -fx-font-family: 'Segoe UI';");
                    skillsContainer.getChildren().add(skillLabel);
                }
            } else {
                Label noSkillsLabel = new Label("No skills listed");
                noSkillsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.30); -fx-font-style: italic; -fx-font-family: 'Segoe UI';");
                skillsContainer.getChildren().add(noSkillsLabel);
            }

            // Cover letter / message
            if (candidature.getCoverLetter() != null && !candidature.getCoverLetter().isEmpty()) {
                coverLetterArea.setText(candidature.getCoverLetter());
            } else {
                coverLetterArea.setText("No cover letter or message provided with this application.");
            }

            // Rejection reason (if rejected)
            if ("rejected".equalsIgnoreCase(candidature.getStatus()) &&
                    candidature.getRejectionReason() != null) {
                rejectionReasonContainer.setVisible(true);
                rejectionReasonContainer.setManaged(true);
                rejectionReasonLabel.setText(candidature.getRejectionReason());
            } else {
                rejectionReasonContainer.setVisible(false);
                rejectionReasonContainer.setManaged(false);
            }

            // Show/hide action buttons based on status
            boolean isPending = "pending".equalsIgnoreCase(candidature.getStatus());
            acceptButton.setVisible(isPending);
            acceptButton.setManaged(isPending);
            rejectButton.setVisible(isPending);
            rejectButton.setManaged(isPending);

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load application details: " + e.getMessage());
        }
    }

    private void updateStatusBadge(String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "pending" -> {
                statusBadge.setText("⏳  Pending Review");
                statusBadge.setStyle("-fx-background-color: rgba(245,158,11,0.18);" +
                        "-fx-text-fill: #f59e0b; -fx-padding: 8 22;" +
                        "-fx-background-radius: 30; -fx-border-color: rgba(245,158,11,0.35);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 30;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            }
            case "accepted" -> {
                statusBadge.setText("✅  Accepted");
                statusBadge.setStyle("-fx-background-color: rgba(16,185,129,0.18);" +
                        "-fx-text-fill: #10b981; -fx-padding: 8 22;" +
                        "-fx-background-radius: 30; -fx-border-color: rgba(16,185,129,0.35);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 30;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            }
            case "rejected" -> {
                statusBadge.setText("❌  Rejected");
                statusBadge.setStyle("-fx-background-color: rgba(244,63,94,0.18);" +
                        "-fx-text-fill: #f43f5e; -fx-padding: 8 22;" +
                        "-fx-background-radius: 30; -fx-border-color: rgba(244,63,94,0.35);" +
                        "-fx-border-width: 1.5; -fx-border-radius: 30;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            }
        }
    }

    @FXML
    private void handleViewResume() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resume/CV");
        alert.setHeaderText("Resume for " + candidateNameLabel.getText());

        if (candidature != null && candidature.getCvPath() != null && !candidature.getCvPath().isEmpty()) {
            alert.setContentText("CV/Resume path: " + candidature.getCvPath() + "\n\n" +
                    "In a real implementation, this would open the candidate's CV/Resume file.");
        } else {
            alert.setContentText("No resume/CV has been uploaded for this candidate.");
        }

        alert.showAndWait();
    }

    @FXML
    private void handleAccept() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accept Application");
        confirm.setHeaderText("Accept " + candidateNameLabel.getText() + " for " + jobTitleLabel.getText());
        confirm.setContentText("Are you sure? All other applicants for this job will be automatically rejected.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 1. Accept this one & reject all others
                candidatureService.acceptAndRejectOthers(candidature.getId(), candidature.getJobOfferId());

                // 2. Close the job offer
                try {
                    jobOfferService.closeJobOffer(candidature.getJobOfferId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 3. Send acceptance email & notification
                if (candidate != null) {
                    String jobTitle = candidatureService.getJobTitleById(candidature.getJobOfferId());
                    int jobid =candidature.getJobOfferId();

                    // Email
                    if (candidate.getEmail() != null) {
                        new EmailService().sendEmail(
                                candidate.getEmail(),
                                "Your application has been accepted – " + jobTitle,
                                "Dear " + candidate.getNom() + " " + candidate.getPrenom() + ",\n\n" +
                                        "Congratulations! We are pleased to inform you that your application for \"" + jobTitle + "\" has been accepted.\n\n" +
                                        "Our team will be in touch with you shortly regarding the next steps.\n\n" +
                                        "Best regards,\nBlindHire Team"
                        );
                    }

                    // In-app notification (jobOfferId passed so clicking the notification opens the quiz)
                    new NotificationCService().createNotification(
                            candidate.getId(),
                            "accepted",
                            "🎉 Application Accepted — " + jobTitle,
                            "Click this notification to take your quiz.",
                            jobid  // ← this was missing
                    );
                }

                // 4. Send rejection emails to others
                try {
                    String jobTitle = candidatureService.getJobTitleById(candidature.getJobOfferId());
                    List<Candidature> others = candidatureService.getCandidaturesByJobOfferId(candidature.getJobOfferId());

                    for (Candidature other : others) {
                        if (other.getId() == candidature.getId()) continue;
                        User otherCandidate = candidatureService.getCandidateUserById(other.getCandidateId());

                        if (otherCandidate != null && otherCandidate.getEmail() != null) {
                            new EmailService().sendEmail(
                                    otherCandidate.getEmail(),
                                    "Update on your application – " + jobTitle,
                                    "Dear " + otherCandidate.getNom() + " " + otherCandidate.getPrenom() + ",\n\n" +
                                            "Thank you for applying for \"" + jobTitle + "\".\n\n" +
                                            "After careful consideration, another candidate was selected for this position.\n\n" +
                                            "We encourage you to apply for future opportunities that match your profile.\n\n" +
                                            "Best regards,\nBlindHire Team"
                            );
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Update local status
                candidature.setStatus("accepted");
                updateStatusBadge("accepted");
                acceptButton.setVisible(false);
                acceptButton.setManaged(false);
                rejectButton.setVisible(false);
                rejectButton.setManaged(false);

                // Refresh parent table using the new public method
                if (parentController != null) {
                    parentController.refreshApplications();
                }

                showSuccessAlert("Success", "Application accepted successfully.");

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorAlert("Database Error", "Failed to update application status: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReject() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Application");
        dialog.setHeaderText("Reject " + candidateNameLabel.getText());
        dialog.setContentText("Please provide a reason for rejection:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Rejection");
            confirm.setHeaderText("Reject " + candidateNameLabel.getText());
            confirm.setContentText("Are you sure you want to reject this application?\nReason: " + reason);

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                try {
                    // Update candidature
                    candidature.setStatus("rejected");
                    candidature.setRejectionReason(reason);
                    candidatureService.updateCandidature(candidature);

                    // Send rejection email
                    if (candidate != null && candidate.getEmail() != null) {
                        String jobTitle = candidatureService.getJobTitleById(candidature.getJobOfferId());
                        new EmailService().sendEmail(
                                candidate.getEmail(),
                                "Update on your application – " + jobTitle,
                                "Dear " + candidate.getNom() + " " + candidate.getPrenom() + ",\n\n" +
                                        "Thank you for applying for \"" + jobTitle + "\".\n\n" +
                                        "After careful consideration, we regret to inform you that your application has not been selected at this time.\n\n" +
                                        "Reason: " + reason + "\n\n" +
                                        "We encourage you to apply for future opportunities.\n\n" +
                                        "Best regards,\nBlindHire Team"
                        );
                    }

                    // Update UI
                    updateStatusBadge("rejected");
                    acceptButton.setVisible(false);
                    acceptButton.setManaged(false);
                    rejectButton.setVisible(false);
                    rejectButton.setManaged(false);

                    // Show rejection reason
                    rejectionReasonContainer.setVisible(true);
                    rejectionReasonContainer.setManaged(true);
                    rejectionReasonLabel.setText(reason);

                    // Refresh parent table using the new public method
                    if (parentController != null) {
                        parentController.refreshApplications();
                    }

                    showSuccessAlert("Success", "Application rejected successfully.");

                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Database Error", "Failed to update application status: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) candidateNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}