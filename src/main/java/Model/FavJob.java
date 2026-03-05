package Model;

import java.time.LocalDateTime;

/**
 * Model for the fav_job table.
 *
 * Table schema:
 *   CREATE TABLE IF NOT EXISTS fav_job (
 *       id       INT AUTO_INCREMENT PRIMARY KEY,
 *       user_id  INT      NOT NULL,
 *       job_id   INT      NOT NULL,
 *       saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
 *       UNIQUE KEY uq_fav (user_id, job_id)
 *   );
 */
public class FavJob {

    private int           id;
    private int           userId;
    private int           jobId;
    private LocalDateTime savedAt;

    // ── Constructors ──────────────────────────────────────────────────────────

    public FavJob() {}

    /** Convenience constructor for inserting a new favourite (id / savedAt set by DB). */
    public FavJob(int userId, int jobId) {
        this.userId = userId;
        this.jobId  = jobId;
    }

    /** Full constructor used when reading from the database. */
    public FavJob(int id, int userId, int jobId, LocalDateTime savedAt) {
        this.id      = id;
        this.userId  = userId;
        this.jobId   = jobId;
        this.savedAt = savedAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public int getUserId()                { return userId; }
    public void setUserId(int userId)     { this.userId = userId; }

    public int getJobId()                 { return jobId; }
    public void setJobId(int jobId)       { this.jobId = jobId; }

    public LocalDateTime getSavedAt()              { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt)  { this.savedAt = savedAt; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "FavJob{id=" + id +
                ", userId=" + userId +
                ", jobId=" + jobId +
                ", savedAt=" + savedAt + "}";
    }

    // ── equals / hashCode (based on userId + jobId uniqueness) ────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavJob)) return false;
        FavJob other = (FavJob) o;
        return this.userId == other.userId && this.jobId == other.jobId;
    }

    @Override
    public int hashCode() {
        return 31 * userId + jobId;
    }
}