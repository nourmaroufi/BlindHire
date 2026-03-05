package Service;


import Model.Notification;
import Utils.Mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService — stores and retrieves in-app notifications.
 *
 * Requires a `notification` table:
 *
 *   CREATE TABLE IF NOT EXISTS notification (
 *     id           INT AUTO_INCREMENT PRIMARY KEY,
 *     user_id      INT NOT NULL,
 *     type         VARCHAR(50)  NOT NULL DEFAULT 'info',
 *     title        VARCHAR(255) NOT NULL,
 *     message      TEXT,
 *     is_read      TINYINT(1)   NOT NULL DEFAULT 0,
 *     created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
 *     FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
 *   );
 */
public class NotificationCService {

    private final Connection cnx;

    public NotificationCService() {
        this.cnx = Mydb.getInstance().getConnection();
        ensureTableExists();
    }

    // ── TABLE AUTO-CREATE ─────────────────────────────────────────────────────

    private void ensureTableExists() {
        try {
            cnx.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS notification (" +
                            "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                            "  user_id    INT NOT NULL," +
                            "  type       VARCHAR(50)  NOT NULL DEFAULT 'info'," +
                            "  title      VARCHAR(255) NOT NULL," +
                            "  message    TEXT," +
                            "  is_read      TINYINT(1)   NOT NULL DEFAULT 0," +
                            "  job_offer_id INT         NOT NULL DEFAULT 0," +
                            "  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                            "  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                            ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Add job_offer_id column if it doesn't exist yet (safe migration)
        try {
            cnx.createStatement().executeUpdate(
                    "ALTER TABLE notification ADD COLUMN IF NOT EXISTS job_offer_id INT NOT NULL DEFAULT 0"
            );
        } catch (SQLException ignored) { /* column already exists or DB doesn't support IF NOT EXISTS */ }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Creates a new notification for the given user.
     * @param userId   recipient user.id
     * @param type     "accepted" | "rejected" | "info" | ...
     * @param title    short headline
     * @param message  full message body
     */
    /** Create a notification not linked to a specific job. */
    public void createNotification(int userId, String type, String title, String message)
            throws SQLException {
        createNotification(userId, type, title, message, 0);
    }

    /** Create a notification linked to a job offer (used for quiz redirect). */
    public void createNotification(int userId, String type, String title, String message, int jobOfferId)
            throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO notification (user_id, type, title, message, job_offer_id) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setString(3, title);
        ps.setString(4, message);
        ps.setInt(5, jobOfferId);
        ps.executeUpdate();
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** All notifications for a user, newest first. */
    public List<Notification> getByUserId(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM notification WHERE user_id = ? ORDER BY created_at DESC");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(extract(rs));
        return list;
    }

    /** Count of unread notifications for a user. */
    public int countUnread(int userId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Mark a single notification as read. */
    public void markRead(int notificationId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "UPDATE notification SET is_read = 1 WHERE id = ?");
        ps.setInt(1, notificationId);
        ps.executeUpdate();
    }

    /** Mark all of a user's notifications as read. */
    public void markAllRead(int userId) throws SQLException {
        PreparedStatement ps = cnx.prepareStatement(
                "UPDATE notification SET is_read = 1 WHERE user_id = ?");
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    private Notification extract(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
        try { n.setJobOfferId(rs.getInt("job_offer_id")); } catch (Exception ignored) {}
        return n;
    }
}