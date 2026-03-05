package services;
import model.Candidat;
import model.Message;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    Connection conn = MyConnection.getConnection();

    public void sendMessage(Message message) throws SQLException {

        String sql = "INSERT INTO message (interview_id, sender_type, content) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setInt(1, message.getInterviewId());
        ps.setString(2, message.getSenderType());
        ps.setString(3, message.getContent());

        ps.executeUpdate();
    }

    public List<Message> getMessagesByInterview(int interviewId) throws SQLException {

        List<Message> list = new ArrayList<>();

        String sql = "SELECT * FROM message WHERE interview_id = ? ORDER BY sent_at ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, interviewId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("interview_id"),
                    rs.getString("sender_type"),
                    rs.getString("content"),
                    rs.getTimestamp("sent_at").toLocalDateTime()
            ));
        }

        return list;
    }
    // Count unread messages for a specific interview and receiver
    public int countUnread(int interviewId, String receiverType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM message WHERE interview_id = ? " +
                "AND sender_type != ? AND is_read = FALSE";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, interviewId);
        ps.setString(2, receiverType);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    // Mark all messages as read for a specific interview and receiver
    public void markAsRead(int interviewId, String receiverType) throws SQLException {
        String sql = "UPDATE message SET is_read = TRUE WHERE interview_id = ? " +
                "AND sender_type != ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, interviewId);
        ps.setString(2, receiverType);
        ps.executeUpdate();
    }
}