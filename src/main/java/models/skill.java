package models;

public class skill {
    private int idSkill;
    private String name;
    private String description;

    public skill() {}

    public skill(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public skill(int idSkill, String name, String description) {
        this.idSkill = idSkill;
        this.name = name;
        this.description = description;
    }

    public int getIdSkill() {
        return idSkill;
    }

    public void setIdSkill(int idSkill) {
        this.idSkill = idSkill;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
