package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private int           id;
    private int           userId;
    private String        type;       // "accepted", "rejected", "info", ...
    private String        title;
    private String        message;
    private boolean       isRead;
    private LocalDateTime createdAt;
    private int           jobOfferId;  // linked job (0 = none)

    public Notification() {}

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int           getId()                          { return id; }
    public void          setId(int id)                    { this.id = id; }
    public int           getUserId()                      { return userId; }
    public void          setUserId(int userId)            { this.userId = userId; }
    public String        getType()                        { return type; }
    public void          setType(String type)             { this.type = type; }
    public String        getTitle()                       { return title; }
    public void          setTitle(String title)           { this.title = title; }
    public String        getMessage()                     { return message; }
    public void          setMessage(String message)       { this.message = message; }
    public boolean       isRead()                         { return isRead; }
    public void          setRead(boolean read)            { this.isRead = read; }
    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)    { this.createdAt = t; }
    public int           getJobOfferId()                  { return jobOfferId; }
    public void          setJobOfferId(int id)            { this.jobOfferId = id; }

    /** Human-friendly time string, e.g. "Mar 2, 14:35" */
    public String getFormattedTime() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
    }

    /** Emoji icon based on type */
    public String getIcon() {
        if (type == null) return "🔔";
        return switch (type.toLowerCase()) {
            case "accepted" -> "🎉";
            case "rejected" -> "❌";
            case "info"     -> "ℹ️";
            default         -> "🔔";
        };
    }
}