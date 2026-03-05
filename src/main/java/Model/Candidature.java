package Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Candidature {
    private int id;
    private int candidateId;
    private int jobOfferId;
    private LocalDate applicationDate;
    private String status;

    private String coverLetter;
    private String cvPath;
    private String portfolioUrl;
    private Double expectedSalary;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Candidature() {}

    public Candidature(int candidateId, int jobOfferId, LocalDate applicationDate, String status) {
        this.candidateId = candidateId;
        this.jobOfferId = jobOfferId;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public Candidature(int id, int candidateId, int jobOfferId,
                       LocalDate applicationDate, String status) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobOfferId = jobOfferId;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public int getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(int jobOfferId) { this.jobOfferId = jobOfferId; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // New getters and setters
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }

    public Double getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(Double expectedSalary) { this.expectedSalary = expectedSalary; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method for status badge styling
    public String getStatusBadgeStyle() {
        switch (status != null ? status.toLowerCase() : "pending") {
            case "pending":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "accepted":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "rejected":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            default:
                return "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        }
    }

    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id +
                ", candidateId=" + candidateId +
                ", jobOfferId=" + jobOfferId +
                ", status='" + status + '\'' +
                ", applicationDate=" + applicationDate +
                '}';
    }
}

