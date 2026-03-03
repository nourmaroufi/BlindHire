package Model;

public class Candidat {
    private int id_c;
    private String name;       // Added name
    private String email;
    private String password;
    private String skills;
    private String experiences;
    private String projects;
    private String cv;
    private boolean anonym;

    public Candidat(String name, String email, String password, String skills,
                    String experiences, String projects, String cv, boolean anonym) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.skills = skills;
        this.experiences = experiences;
        this.projects = projects;
        this.cv = cv;
        this.anonym = anonym;
    }

    public Candidat(int id_c, String name, String email, String password,
                    String skills, String experiences, String projects,
                    String cv, boolean anonym) {
        this.id_c = id_c;
        this.name = name;
        this.email = email;
        this.password = password;
        this.skills = skills;
        this.experiences = experiences;
        this.projects = projects;
        this.cv = cv;
        this.anonym = anonym;
    }

    public Candidat(int id, String nom, String email, String s) {
    }

    public Candidat() {

    }


    public int getId_c() {
        return id_c;
    }

    public void setId_c(int id_c) {
        this.id_c = id_c;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperiences() {
        return experiences;
    }

    public void setExperiences(String experiences) {
        this.experiences = experiences;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public boolean isAnonym() {
        return anonym;
    }

    public void setAnonym(boolean anonym) {
        this.anonym = anonym;
    }
}