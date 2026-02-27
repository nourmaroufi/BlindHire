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
}