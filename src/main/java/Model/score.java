package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class score {
    private int idScore;
    private int idUser;
    private int jobOfferId;
    private BigDecimal score;
    private Timestamp createdAt;
    private String status;   // "accepted" or "rejected"

    public score() {}

    public score(int idUser, int jobOfferId, BigDecimal score, String status) {
        this.idUser = idUser;
        this.jobOfferId = jobOfferId;
        this.score = score;
        this.status = status;
    }

    public score(int idScore, int idUser, int jobOfferId, BigDecimal score, Timestamp createdAt) {
        this.idScore = idScore;
        this.idUser = idUser;
        this.jobOfferId = jobOfferId;
        this.score = score;
        this.createdAt = createdAt;
    }

    public int getIdScore()              { return idScore; }
    public void setIdScore(int v)        { this.idScore = v; }
    public int getIdUser()               { return idUser; }
    public void setIdUser(int v)         { this.idUser = v; }
    public int getJobOfferId()           { return jobOfferId; }
    public void setJobOfferId(int v)     { this.jobOfferId = v; }
    public BigDecimal getScore()         { return score; }
    public void setScore(BigDecimal v)   { this.score = v; }
    public Timestamp getCreatedAt()      { return createdAt; }
    public void setCreatedAt(Timestamp v){ this.createdAt = v; }
    public String getStatus()            { return status; }
    public void setStatus(String v)      { this.status = v; }
}