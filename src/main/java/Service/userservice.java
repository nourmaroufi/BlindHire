package Service;

import DAO.userDAO;
import Model.Role;
import Model.User;

import java.util.ArrayList;
import java.util.List;

public class userservice {

    private final userDAO userDAO = new userDAO();
    private User currentUser;

    // ── REGISTER ──────────────────────────────────────────────────────────────

    /**
     * Registers a new user, generates a verification code, sends it via
     * email AND SMS, then saves the user as unverified.
     * Returns the saved user (with id) so the UI can navigate to VerificationPage.
     */
    public User register(User user) {
        validate(user);
        if (userDAO.findByEmail(user.getEmail()) != null)
            throw new IllegalArgumentException("Email already registered!");
        if (user.getPhone() != null && !user.getPhone().isEmpty()
                && userDAO.findByPhone(user.getPhone()) != null)
            throw new IllegalArgumentException("Phone number already registered!");

        // Save as unverified — VerificationPage will send the code
        // after the user chooses their preferred channel
        user.setVerificationCode(null);
        user.setVerified(false);
        userDAO.insert(user);

        return userDAO.findByEmail(user.getEmail());
    }

    /**
     * Verifies a user's account by checking the code they entered.
     * Marks the account as verified and clears the code on success.
     */
    public void verifyAccount(User user, String enteredCode) {
        if (enteredCode == null || enteredCode.trim().isEmpty())
            throw new IllegalArgumentException("Please enter the verification code.");
        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(enteredCode.trim()))
            throw new IllegalArgumentException("Incorrect code. Please try again.");

        userDAO.markVerified(user.getId());
        user.setVerified(true);
        user.setVerificationCode(null);
    }

    /**
     * Resends a fresh verification code via the chosen channel.
     * @param usePhone true = SMS, false = email
     */
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

    /** Overload kept for LoginPage unverified redirect — defaults to email. */
    public void resendVerificationCode(User user) {
        resendVerificationCode(user, false);
    }

    // ── AUTHENTICATE ──────────────────────────────────────────────────────────

    public User authenticate(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("Email and password are required!");

        User user = userDAO.findByEmail(email);
        if (user == null || !user.getMdp().equals(password))
            throw new IllegalArgumentException("Invalid email or password!");
        if (!user.isVerified())
            throw new IllegalArgumentException("UNVERIFIED"); // special signal to UI

        return user;
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────────────────

    /**
     * Initiates a password reset for the given email or phone.
     * Generates a code, saves it, sends it, and returns the user.
     * Throws if the contact is not found.
     */
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

    /**
     * Verifies the reset code and updates the password.
     */
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

        userDAO.updatePassword(user.getId(), newPassword);
        user.setMdp(newPassword);
        user.setVerificationCode(null);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public void updateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        validate(user);
        userDAO.update(user);
    }

    public void deleteUser(int id)           { userDAO.delete(id); }
    public User getUserByEmail(String email) { return userDAO.findByEmail(email); }

    public void updateFaceData(User user, String faceBase64) {
        userDAO.updateFaceData(user.getId(), faceBase64);
        user.setFaceData(faceBase64);
    }

    public void updateFingerprintEnabled(User user, boolean enabled) {
        userDAO.updateFingerprintEnabled(user.getId(), enabled);
        user.setFingerprintEnabled(enabled);
    }
    public User getUserByPhone(String phone) { return userDAO.findByPhone(phone); }

    public User getUserById(int id) {
        for (User u : userDAO.getAll())
            if (u.getId() == id) return u;
        return null;
    }

    public List<User> getAllUsers()           { return userDAO.getAll(); }

    public List<User> getUsersByRole(Role role) {
        List<User> filtered = new ArrayList<>();
        for (User u : userDAO.getAll())
            if (u.getRole() == role) filtered.add(u);
        return filtered;
    }

    public List<User> getAdmins()     { return getUsersByRole(Role.admin); }
    public List<User> getRecruiters() { return getUsersByRole(Role.recruteur); }
    public List<User> getClients()    { return getUsersByRole(Role.client); }

    public void    setCurrentUser(User user) { this.currentUser = user; }
    public User    getCurrentUser()          { return currentUser; }
    public boolean emailExists(String email) { return userDAO.findByEmail(email) != null; }
    public boolean phoneExists(String phone) { return userDAO.findByPhone(phone) != null; }

    public int getUserCount()      { return userDAO.getAll().size(); }
    public int getAdminCount()     { return getAdmins().size(); }
    public int getRecruiterCount() { return getRecruiters().size(); }
    public int getClientCount()    { return getClients().size(); }

    // ── PRIVATE ───────────────────────────────────────────────────────────────

    private void validate(User u) {
        if (u.getNom() == null || u.getNom().isEmpty()
                || u.getPrenom() == null || u.getPrenom().isEmpty()
                || u.getEmail() == null || u.getEmail().isEmpty()
                || u.getMdp() == null || u.getMdp().isEmpty())
            throw new IllegalArgumentException("All required fields must be filled!");
        if (!u.getEmail().contains("@"))
            throw new IllegalArgumentException("Invalid email address!");
    }
}