package Model;

import java.time.LocalDate;

public class JobOffer {
    private int id;
    private String title;
    private String description;
    private String requiredSkills;
    private int recruiterId;
    private String type;
    private String status;
    private LocalDate postingDate;

    public JobOffer() {}

    public JobOffer(String title, String description, int recruiterId, String type, String status, LocalDate postingDate) {
        this.title = title;
        this.description = description;
        this.recruiterId = recruiterId;
        this.type = type;
        this.status = status;
        this.postingDate = postingDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public int getRecruiterId() { return recruiterId; }
    public void setRecruiterId(int recruiterId) { this.recruiterId = recruiterId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDate postingDate) { this.postingDate = postingDate; }
}