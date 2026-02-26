package Controller;

import Model.Role;
import Model.User;
import Service.userservice;

import java.util.List;

public class userController {

    private final userservice userService;

    public userController() {
        this.userService = new userservice();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Register / create a new user.
     * Returns the created user (with DB-assigned id).
     */
    public User createUser(String nom, String prenom, String email, String mdp, String roleStr) {
        validateFields(nom, prenom, email, mdp, roleStr);

        Role role = parseRole(roleStr);
        User user = new User(nom, prenom, email, mdp, role);
        return userService.register(user);
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /** Return every user in the database. */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /** Return users filtered by role. */
    public List<User> getUsersByRole(Role role) {
        return userService.getUsersByRole(role);
    }

    /** Return all admins. */
    public List<User> getAdmins() {
        return userService.getAdmins();
    }

    /** Return all recruiters. */
    public List<User> getRecruiters() {
        return userService.getRecruiters();
    }

    /** Return all clients. */
    public List<User> getClients() {
        return userService.getClients();
    }

    /** Find a user by their email address. */
    public User getUserByEmail(String email) {
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be empty!");
        return userService.getUserByEmail(email);
    }

    /** Find a user by their id. */
    public User getUserById(int id) {
        User user = userService.getUserById(id);
        if (user == null)
            throw new IllegalArgumentException("No user found with id: " + id);
        return user;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Update an existing user's information.
     * Pass the full updated fields; id must already be set on the user object.
     */
    public void updateUser(User user, String nom, String prenom, String email, String mdp, String roleStr) {
        validateFields(nom, prenom, email, mdp, roleStr);

        // If email changed, make sure the new email isn't taken by someone else
        if (!email.equals(user.getEmail()) && userService.emailExists(email)) {
            throw new IllegalArgumentException("Email already in use by another account!");
        }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMdp(mdp);
        user.setRole(parseRole(roleStr));

        userService.updateUser(user);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    /** Delete a user by id. */
    public void deleteUser(int id) {
        userService.deleteUser(id);
    }

    // ─── AUTH ─────────────────────────────────────────────────────────────────

    /** Authenticate a user (login). Returns the authenticated user. */
    public User login(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            throw new IllegalArgumentException("Email and password are required!");
        return userService.authenticate(email, password);
    }

    // ─── STATS ────────────────────────────────────────────────────────────────

    public int getTotalUserCount()     { return userService.getUserCount(); }
    public int getAdminCount()         { return userService.getAdminCount(); }
    public int getRecruiterCount()     { return userService.getRecruiterCount(); }
    public int getClientCount()        { return userService.getClientCount(); }

    // ─── SESSION ──────────────────────────────────────────────────────────────

    public void setCurrentUser(User user) { userService.setCurrentUser(user); }
    public User getCurrentUser()          { return userService.getCurrentUser(); }
    public void logout()                  { userService.setCurrentUser(null); }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void validateFields(String nom, String prenom, String email, String mdp, String roleStr) {
        if (nom == null || nom.isEmpty())
            throw new IllegalArgumentException("First name is required!");
        if (prenom == null || prenom.isEmpty())
            throw new IllegalArgumentException("Last name is required!");
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email is required!");
        if (!email.contains("@"))
            throw new IllegalArgumentException("Invalid email address!");
        if (mdp == null || mdp.isEmpty())
            throw new IllegalArgumentException("Password is required!");
        if (roleStr == null || roleStr.isEmpty())
            throw new IllegalArgumentException("Role is required!");
    }

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleStr + ". Must be admin, recruteur, or client.");
        }
    }
}