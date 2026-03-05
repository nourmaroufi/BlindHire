package DAO;

import Utils.Mydb;
import Model.Role;
import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class userDAO {

    private Role parseRole(String s) {
        if (s == null) return Role.client;
        for (Role r : Role.values())
            if (r.name().equalsIgnoreCase(s)) return r;
        throw new IllegalArgumentException("Unknown role: " + s);
    }

    private User mapRow(ResultSet rs) throws Exception {
        String username = null;
        try { username = rs.getString("username"); } catch (Exception ignored) {}

        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("mail"),
                rs.getString("mdp"),
                parseRole(rs.getString("role")),
                rs.getString("skills"),
                rs.getString("diplomas"),
                rs.getString("experience"),
                rs.getString("bio"),
                rs.getString("phone"),
                rs.getInt("is_verified") == 1,
                rs.getString("verification_code"),
                rs.getString("face_data"),
                username
        );
    }

    // ── INSERT ────────────────────────────────────────────────────────────────
    public void insert(User user) {
        String sql = "INSERT INTO user(nom, prenom, mail, mdp, role, skills, diplomas, " +
                "experience, bio, phone, is_verified, verification_code, face_data, username) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  user.getNom());
            ps.setString(2,  user.getPrenom());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getMdp());
            ps.setString(5,  user.getRole().name());
            ps.setString(6,  user.getSkills());
            ps.setString(7,  user.getDiplomas());
            ps.setString(8,  user.getExperience());
            ps.setString(9,  user.getBio());
            ps.setString(10, user.getPhone());
            ps.setInt(11,    user.isVerified() ? 1 : 0);
            ps.setString(12, user.getVerificationCode());
            ps.setString(13, user.getFaceData());
            ps.setString(14, user.getUsername());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting user: " + e.getMessage());
        }
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────
    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
        return list;
    }

    // ── FIND BY ID ────────────────────────────────────────────────────────────
    public User findById(int id) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── FIND BY EMAIL ─────────────────────────────────────────────────────────
    public User findByEmail(String email) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE mail=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    // ── FIND BY USERNAME ──────────────────────────────────────────────────────
    public User findByUsername(String username) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    // ── FIND BY PHONE ─────────────────────────────────────────────────────────
    public User findByPhone(String phone) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE phone=?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error finding user by phone: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE (full) ─────────────────────────────────────────────────────────
    public void update(User user) {
        String sql = "UPDATE user SET nom=?,prenom=?,mail=?,mdp=?,role=?," +
                "skills=?,diplomas=?,experience=?,bio=?,phone=?," +
                "is_verified=?,verification_code=?,face_data=?,username=? WHERE id=?";
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  user.getNom());
            ps.setString(2,  user.getPrenom());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getMdp());
            ps.setString(5,  user.getRole().name());
            ps.setString(6,  user.getSkills());
            ps.setString(7,  user.getDiplomas());
            ps.setString(8,  user.getExperience());
            ps.setString(9,  user.getBio());
            ps.setString(10, user.getPhone());
            ps.setInt(11,    user.isVerified() ? 1 : 0);
            ps.setString(12, user.getVerificationCode());
            ps.setString(13, user.getFaceData());
            ps.setString(14, user.getUsername());
            ps.setInt(15,    user.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating user: " + e.getMessage());
        }
    }

    // ── UPDATE face data only ─────────────────────────────────────────────────
    public void updateFaceData(int userId, String faceData) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user SET face_data=? WHERE id=?")) {
            ps.setString(1, faceData);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating face data: " + e.getMessage());
        }
    }

    // ── UPDATE verification code only ─────────────────────────────────────────
    public void updateVerificationCode(int userId, String code) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user SET verification_code=? WHERE id=?")) {
            ps.setString(1, code);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating verification code: " + e.getMessage());
        }
    }

    // ── MARK AS VERIFIED ──────────────────────────────────────────────────────
    public void markVerified(int userId) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user SET is_verified=1, verification_code=NULL WHERE id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error marking user as verified: " + e.getMessage());
        }
    }

    // ── UPDATE PASSWORD ───────────────────────────────────────────────────────
    public void updatePassword(int userId, String newMdp) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user SET mdp=?, verification_code=NULL WHERE id=?")) {
            ps.setString(1, newMdp);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating password: " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    public void delete(int id) {
        try (Connection conn = Mydb.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }
}