package model;

import java.time.LocalDateTime;

public class Interview {

    private int id;
    private int id_candidat;
    private LocalDateTime date;
    private String type;
    private String job_offer;
    private String interviewer;
    private String locationLink;
    private String status;
    private String notes;



    // Constructor with id (for reading from DB)
    public Interview(int id, int id_candidat, LocalDateTime date, String type,
                     String job_offer, String interviewer, String locationLink,
                     String status, String notes) {
        this.id = id;
        this.id_candidat = id_candidat;
        this.date = date;
        this.type = type;
        this.job_offer = job_offer;
        this.interviewer = interviewer;
        this.locationLink = locationLink;
        this.status = status;
        this.notes = notes;
    }

    // Constructor without id (for insert) — status defaults to Pending
    public Interview(int id_candidat, LocalDateTime date, String type,
                     String job_offer, String interviewer, String locationLink) {
        this.id_candidat = id_candidat;
        this.date = date;
        this.type = type;
        this.job_offer = job_offer;
        this.interviewer = interviewer;
        this.locationLink = locationLink;
        this.status = "Pending";
        this.notes = null;
    }

    // Getters
    public int getId() { return id; }
    public void setId (int id) {this.id=id;}
    public int getId_candidat() { return id_candidat; }
    public void setId_candidat(int id_candidat){this.id_candidat=id_candidat;}
    public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date){this.date=date;}
    public String getType() { return type; }
    public void setType( String type){this.type=type;}
    public String getJob_offer() { return job_offer; }
    public void setJob_offer(String job_offer){this.job_offer=job_offer;}
    public String getInterviewer() { return interviewer; }
    public void setInterviewer(String interviewer ){this.interviewer=interviewer;}
    public String getLocationLink() { return locationLink; }
    public void setLocationLink(String locationLink) { this.locationLink = locationLink; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
