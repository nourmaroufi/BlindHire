package Model;

import java.math.BigDecimal;

public class LeaderboardRow {
    private int userId;
    private BigDecimal score; // percent

    public LeaderboardRow() {}

    public LeaderboardRow(int userId, BigDecimal score) {
        this.userId = userId;
        this.score = score;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
}