package model;

import java.time.LocalDateTime;

public class Interview {

    private int id;
    private int id_candidat;
    private LocalDateTime date;
    private String type;
    private String job_offer;
    private String interviewer;



    // Constructor with id
    public Interview(int id, int id_candidat, LocalDateTime date, String type, String job_offer, String interviewer) {
        this.id = id;
        this.id_candidat = id_candidat;
        this.date = date;
        this.type = type;
        this.job_offer = job_offer;
        this.interviewer = interviewer;
    }
    // Constructor without id (for insert)
    public Interview(int id_candidat, LocalDateTime date, String type, String job_offer, String interviewer) {
        this.id_candidat = id_candidat;
        this.date = date;
        this.type = type;
        this.job_offer = job_offer;
        this.interviewer = interviewer;
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
}
