package Model;

public class Candidat {

    private int id_candidat;
    private int score;
    private String status;

    public Candidat() {}

    public Candidat(int id_candidat, int score, String status) {
        this.id_candidat = id_candidat;
        this.score = score;
        this.status = status;
    }

    public int getId_candidat() {
        return id_candidat;
    }

    public void setId_candidat(int id_candidat) {
        this.id_candidat = id_candidat;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return id_candidat + " | " + status + " | Score: " + score;
    }
}
