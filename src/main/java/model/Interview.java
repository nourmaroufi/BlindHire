package Model;

import java.time.LocalDateTime;

public class Interview {

    private int id;
    private LocalDateTime date;
    private String type;
    private String locationLink;
    private String status;
    private String notes;
    private long idScore;
    private long interviewerId;

    // Fields joined from score + job_offer (not stored in interview table)
    private String jobOffer;   // from job_offer.title via score.job_offer_id
    private String interviewer; // display name, derived from interviewer_id if needed

    // Full constructor (from DB)
    public Interview(int id, LocalDateTime date, String type,
                     String locationLink, String status,
                     String notes, long idScore, long interviewerId) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.locationLink = locationLink;
        this.status = status;
        this.notes = notes;
        this.idScore = idScore;
        this.interviewerId = interviewerId;
    }

    // Insert constructor
    public Interview(LocalDateTime date, String type,
                     String locationLink, long idScore, long interviewerId) {
        this.date = date;
        this.type = type;
        this.locationLink = locationLink;
        this.idScore = idScore;
        this.interviewerId = interviewerId;
        this.status = "Pending";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocationLink() { return locationLink; }
    public void setLocationLink(String locationLink) { this.locationLink = locationLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getIdScore() { return idScore; }
    public void setIdScore(long idScore) { this.idScore = idScore; }

    public long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(long interviewerId) { this.interviewerId = interviewerId; }

    // Joined fields (set by service after query)
    public String getJob_offer() { return jobOffer != null ? jobOffer : "Interview"; }
    public void setJob_offer(String jobOffer) { this.jobOffer = jobOffer; }

    public String getInterviewer() { return interviewer != null ? interviewer : ""; }
    public void setInterviewer(String interviewer) { this.interviewer = interviewer; }
}