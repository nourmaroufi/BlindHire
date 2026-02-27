package models;

import java.math.BigDecimal;

public class question {
    private int idQuestion;
    private int idSkill;
    private String statement;
    private BigDecimal points;

    public question() {}

    public question(int idSkill, String statement, BigDecimal points) {
        this.idSkill = idSkill;
        this.statement = statement;
        this.points = points;
    }

    public question(int idQuestion, int idSkill, String statement, BigDecimal points) {
        this.idQuestion = idQuestion;
        this.idSkill = idSkill;
        this.statement = statement;
        this.points = points;
    }

    public int getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(int idQuestion) {
        this.idQuestion = idQuestion;
    }

    public int getIdSkill() {
        return idSkill;
    }

    public void setIdSkill(int idSkill) {
        this.idSkill = idSkill;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }
}
