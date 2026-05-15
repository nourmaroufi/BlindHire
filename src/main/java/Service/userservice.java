package Service;

import DAO.userDAO;
import Model.Role;
import Model.User;
import Utils.Mydb;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class userservice {

    private final Connection cnx;

    private final userDAO userDAO = new userDAO();
    private static User currentUser;
    private static final DateTimeFormatter DB_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public userservice() {
        this.cnx = Mydb.getInstance().getConnection();
    }

    // ── REGISTER ──────────────────────────────────────────────────────────────

    public User register(User user) {
        validate(user);
        if (userDAO.findByEmail(user.getEmail()) != null)
            throw new IllegalArgumentException("Email already registered!");
        if (user.getPhone() != null && !user.getPhone().isEmpty()
                && userDAO.findByPhone(user.getPhone()) != null)
            throw new IllegalArgumentException("Phone number already registered!");

        // Auto-generate a blind username if not already set
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            String username;
            do {
                username = Utils.UsernameGenerator.generate();
            } while (userDAO.findByUsername(username) != null);
            user.setUsername(username);
        }

        // ── Hash the password before saving ──────────────────────────────────
        user.setMdp(BCrypt.hashpw(user.getMdp(), BCrypt.gensalt()));

        user.setVerificationCode(null);
        user.setVerified(false);

        if (user.getRole() == Role.recruteur) {
            user.setRecruiterRequestStatus("pending");
            user.setRecruiterRequestReviewedAt(null);
        } else {
            user.setRecruiterRequestStatus("approved");
        }
        userDAO.insert(user);

        return userDAO.findByEmail(user.getEmail());
    }

    // ── VERIFY ────────────────────────────────────────────────────────────────

    public void verifyAccount(User user, String enteredCode) {
        if (user.getRole() == Role.recruteur && !isRecruiterApproved(user))
            throw new IllegalArgumentException("PENDING_APPROVAL");
        if (enteredCode == null || enteredCode.trim().isEmpty())
            throw new IllegalArgumentException("Please enter the verification code.");
        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(enteredCode.trim()))
            throw new IllegalArgumentException("Incorrect code. Please try again.");

        userDAO.markVerified(user.getId());
        user.setVerified(true);
        user.setVerificationCode(null);
    }

    public void resendVerificationCode(User user, boolean usePhone) {
        String code = NotificationService.generateCode();
        userDAO.updateVerificationCode(user.getId(), code);
        user.setVerificationCode(code);

        if (usePhone && user.getPhone() != null && !user.getPhone().isEmpty()) {
            NotificationService.sendSmsCode(user.getPhone(), code, false);
        } else {
            NotificationService.sendEmailCode(
                    user.getEmail(),
                    user.getNom() + " " + user.getPrenom(),
                    code, false
            );
        }
    }

    public void resendVerificationCode(User user) {
        resendVerificationCode(user, false);
    }

    // ── AUTHENTICATE ──────────────────────────────────────────────────────────

    public User authenticate(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("Email and password are required!");

        User user = userDAO.findByEmail(email);
        if (user == null)
            throw new IllegalArgumentException("Invalid email or password!");

        if (isCurrentlyBanned(user))
            throw new IllegalArgumentException("Account is temporarily blocked.");

        if (user.getRole() == Role.recruteur && !isRecruiterApproved(user))
            throw new IllegalArgumentException("PENDING_APPROVAL");

        // ── Support both bcrypt hashes (Symfony) and legacy plain-text ────────
        String stored = user.getMdp();
        String normalized = normalizeBcryptHash(stored);
        boolean valid = isBcryptHash(normalized)       // bcrypt hash from Symfony or new Java registrations
                ? BCrypt.checkpw(password, normalized)
                : stored.equals(password);             // legacy plain-text users already in DB

        if (!valid) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= 3) {
                banForMinutes(user, 15, "Too many failed login attempts");
                userDAO.updateFailedLoginAttempts(user.getId(), 0);
                user.setFailedLoginAttempts(0);
                throw new IllegalArgumentException("Too many failed attempts. Account blocked for 15 minutes.");
            }
            userDAO.updateFailedLoginAttempts(user.getId(), attempts);
            user.setFailedLoginAttempts(attempts);
            throw new IllegalArgumentException("Invalid email or password!");
        }

        if (!user.isVerified())
            throw new IllegalArgumentException("UNVERIFIED");

        if (!isBcryptHash(stored)) {
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            userDAO.updatePasswordOnly(user.getId(), hashed);
            user.setMdp(hashed);
        }

        if (user.getFailedLoginAttempts() > 0) {
            userDAO.updateFailedLoginAttempts(user.getId(), 0);
            user.setFailedLoginAttempts(0);
        }

        return user;
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────────────────

    public User initiatePasswordReset(String contact, boolean isPhone) {
        User user = isPhone
                ? userDAO.findByPhone(contact)
                : userDAO.findByEmail(contact);

        if (user == null)
            throw new IllegalArgumentException(
                    isPhone ? "No account found with this phone number."
                            : "No account found with this email.");

        String code = NotificationService.generateCode();
        userDAO.updateVerificationCode(user.getId(), code);
        user.setVerificationCode(code);

        if (isPhone) {
            NotificationService.sendSmsCode(user.getPhone(), code, true);
        } else {
            NotificationService.sendEmailCode(
                    user.getEmail(),
                    user.getNom() + " " + user.getPrenom(),
                    code, true
            );
        }
        return user;
    }

    public void resetPassword(User user, String enteredCode, String newPassword) {
        if (enteredCode == null || enteredCode.trim().isEmpty())
            throw new IllegalArgumentException("Please enter the reset code.");
        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(enteredCode.trim()))
            throw new IllegalArgumentException("Incorrect code. Please try again.");
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*[0-9].*"))
            throw new IllegalArgumentException("Password must contain at least one letter and one number.");

        // ── Hash the new password before saving ──────────────────────────────
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        userDAO.updatePassword(user.getId(), hashed);
        user.setMdp(hashed);
        user.setVerificationCode(null);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public void updateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        validate(user);
        if (user.getMdp() != null && !isBcryptHash(user.getMdp())) {
            user.setMdp(BCrypt.hashpw(user.getMdp(), BCrypt.gensalt()));
        }
        userDAO.update(user);
    }

    public void approveRecruiter(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        String reviewedAt = LocalDateTime.now().format(DB_DATETIME);
        userDAO.updateRecruiterApproval(user.getId(), "approved", reviewedAt, true);
        user.setRecruiterRequestStatus("approved");
        user.setRecruiterRequestReviewedAt(reviewedAt);
        user.setVerified(true);
    }

    public void banUser(User user, int minutes, String reason) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        banForMinutes(user, minutes, reason);
        userDAO.updateFailedLoginAttempts(user.getId(), 0);
        user.setFailedLoginAttempts(0);
    }

    public void unbanUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        userDAO.updateBan(user.getId(), null, null);
        user.setBannedUntil(null);
        user.setBanReason(null);
    }

    public boolean isUserBanned(User user) {
        return user != null && isCurrentlyBanned(user);
    }

    public void deleteUser(int id)           { userDAO.delete(id); }
    public User getUserByEmail(String email) { return userDAO.findByEmail(email); }

    public void updateFaceData(User user, String faceBase64) {
        userDAO.updateFaceData(user.getId(), faceBase64);
        user.setFaceData(faceBase64);
    }

    public User getUserByPhone(String phone) { return userDAO.findByPhone(phone); }
    public User getUserById(int id)          { return userDAO.findById(id); }
    public List<User> getAllUsers()          { return userDAO.getAll(); }

    public List<User> getUsersByRole(Role role) {
        List<User> filtered = new ArrayList<>();
        for (User u : userDAO.getAll())
            if (u.getRole() == role) filtered.add(u);
        return filtered;
    }

    public List<User> getAdmins()     { return getUsersByRole(Role.admin); }
    public List<User> getRecruiters() { return getUsersByRole(Role.recruteur); }
    public List<User> getClients()    { return getUsersByRole(Role.client); }

    public void    setCurrentUser(User user) { currentUser = user; }
    public User    getCurrentUser()          { return currentUser; }
    public boolean emailExists(String email) { return userDAO.findByEmail(email) != null; }
    public boolean phoneExists(String phone) { return userDAO.findByPhone(phone) != null; }

    public int getUserCount()      { return userDAO.getAll().size(); }
    public int getAdminCount()     { return getAdmins().size(); }
    public int getRecruiterCount() { return getRecruiters().size(); }
    public int getClientCount()    { return getClients().size(); }

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void validate(User u) {
        if (u.getNom() == null || u.getNom().isEmpty()
                || u.getPrenom() == null || u.getPrenom().isEmpty()
                || u.getEmail() == null || u.getEmail().isEmpty()
                || u.getMdp() == null || u.getMdp().isEmpty())
            throw new IllegalArgumentException("All required fields must be filled!");
        if (!u.getEmail().contains("@"))
            throw new IllegalArgumentException("Invalid email address!");
    }

    private boolean isBcryptHash(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private String normalizeBcryptHash(String value) {
        if (value == null) return null;
        if (value.startsWith("$2y$")) {
            return "$2a$" + value.substring(4);
        }
        return value;
    }

    private boolean isRecruiterApproved(User user) {
        String status = user.getRecruiterRequestStatus();
        return status == null || status.isBlank() || "approved".equalsIgnoreCase(status);
    }

    private void banForMinutes(User user, int minutes, String reason) {
        String until = LocalDateTime.now().plusMinutes(minutes).format(DB_DATETIME);
        userDAO.updateBan(user.getId(), until, reason);
        user.setBannedUntil(until);
        user.setBanReason(reason);
    }

    private boolean isCurrentlyBanned(User user) {
        String bannedUntil = user.getBannedUntil();
        if (bannedUntil == null || bannedUntil.isBlank()) return false;
        try {
            LocalDateTime until = LocalDateTime.parse(bannedUntil, DB_DATETIME);
            return until.isAfter(LocalDateTime.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}