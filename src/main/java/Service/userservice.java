package Service;

import DAO.userDAO;
import Model.User;
import Model.Role;

import java.util.ArrayList;
import java.util.List;

public class userservice {

    private final userDAO userDAO = new userDAO();
    private User currentUser;

    // Register a new user
    public User register(User user) {
        if (user.getNom() == null || user.getNom().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getMdp() == null || user.getMdp().isEmpty()) {
            throw new IllegalArgumentException("All fields are required!");
        }
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email address!");
        }
        if (userDAO.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered!");
        }
        userDAO.insert(user);
        return userDAO.findByEmail(user.getEmail());
    }

    // Authenticate user (login)
    public User authenticate(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required!");
        }
        User user = userDAO.findByEmail(email);
        if (user == null || !user.getMdp().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password!");
        }
        return user;
    }

    // Update user information
    public void updateUser(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null!");
        if (user.getNom() == null || user.getNom().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getMdp() == null || user.getMdp().isEmpty()) {
            throw new IllegalArgumentException("All fields are required!");
        }
        if (!user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email address!");
        }
        userDAO.update(user);
    }

    // Delete user by ID
    public void deleteUser(int id) {
        userDAO.delete(id);
    }

    // Get user by email
    public User getUserByEmail(String email) {
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be empty!");
        return userDAO.findByEmail(email);
    }

    // Get user by ID
    public User getUserById(int id) {
        for (User user : userDAO.getAll()) {
            if (user.getId() == id) return user;
        }
        return null;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userDAO.getAll();
    }

    // Get users by role
    public List<User> getUsersByRole(Role role) {
        List<User> filtered = new ArrayList<>();
        for (User user : userDAO.getAll()) {
            if (user.getRole() == role) filtered.add(user);
        }
        return filtered;
    }

    // Get all admins
    public List<User> getAdmins() {
        return getUsersByRole(Role.admin);
    }

    // Get all recruiters
    public List<User> getRecruiters() {
        return getUsersByRole(Role.recruteur);
    }

    // Get all clients
    public List<User> getClients() {
        return getUsersByRole(Role.client);
    }

    // Session management
    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser()          { return currentUser; }

    // Email existence check
    public boolean emailExists(String email) {
        if (email == null || email.isEmpty()) return false;
        return userDAO.findByEmail(email) != null;
    }

    // Counts
    public int getUserCount()      { return userDAO.getAll().size(); }
    public int getAdminCount()     { return getAdmins().size(); }
    public int getRecruiterCount() { return getRecruiters().size(); }
    public int getClientCount()    { return getClients().size(); }
}