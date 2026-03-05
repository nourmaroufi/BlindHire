package Model;

import java.math.BigDecimal;

public class question {
    private int idQuestion;
    private int jobOfferId;   // replaces idSkill — questions are grouped by job offer
    private String statement;
    private BigDecimal points;

    public question() {}

    public question(int jobOfferId, String statement, BigDecimal points) {
        this.jobOfferId = jobOfferId;
        this.statement = statement;
        this.points = points;
    }

    public question(int idQuestion, int jobOfferId, String statement, BigDecimal points) {
        this.idQuestion = idQuestion;
        this.jobOfferId = jobOfferId;
        this.statement = statement;
        this.points = points;
    }

    public int getIdQuestion() { return idQuestion; }
    public void setIdQuestion(int idQuestion) { this.idQuestion = idQuestion; }

    public int getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(int jobOfferId) { this.jobOfferId = jobOfferId; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public BigDecimal getPoints() { return points; }
    public void setPoints(BigDecimal points) { this.points = points; }
}