package Model;

public class User {
    private int     id;
    private String  nom;
    private String  prenom;
    private String  email;
    private String  mdp;
    private Role    role;
    private String  skills;
    private String  diplomas;
    private String  experience;
    private String  bio;
    private String  phone;
    private boolean isVerified;
    private String  verificationCode;
    private String  faceData;
    private String  username;

    // ── NEW fields to match the database ─────────────────────────────────────
    private boolean fingerprintEnabled;
    private String  bannedUntil;           // datetime stored as String
    private String  banReason;
    private int     failedLoginAttempts;
    private String  recruiterRequestStatus;
    private String  recruiterRequestReviewedAt;  // datetime stored as String
    private String  cvPath;
    // ─────────────────────────────────────────────────────────────────────────

    public User() {}

    public User(String nom, String prenom, String email, String mdp, Role role) {
        this.nom = nom; this.prenom = prenom;
        this.email = email; this.mdp = mdp; this.role = role;
    }

    public User(int id, String nom, String prenom, String email, String mdp, Role role) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.mdp = mdp; this.role = role;
    }

    public User(int id, String nom, String prenom, String email, String mdp, Role role,
                String skills, String diplomas, String experience, String bio,
                String phone, boolean isVerified, String verificationCode,
                String faceData, String username) {
        this.id = id; this.nom = nom; this.prenom = prenom;
        this.email = email; this.mdp = mdp; this.role = role;
        this.skills = skills; this.diplomas = diplomas;
        this.experience = experience; this.bio = bio;
        this.phone = phone; this.isVerified = isVerified;
        this.verificationCode = verificationCode;
        this.faceData = faceData;
        this.username = username;
    }

    // backward-compat constructor without username
    public User(int id, String nom, String prenom, String email, String mdp, Role role,
                String skills, String diplomas, String experience, String bio,
                String phone, boolean isVerified, String verificationCode, String faceData) {
        this(id, nom, prenom, email, mdp, role, skills, diplomas, experience, bio,
                phone, isVerified, verificationCode, faceData, null);
    }

    // ── Existing getters/setters (unchanged) ──────────────────────────────────
    public int     getId()                              { return id; }
    public void    setId(int id)                        { this.id = id; }
    public String  getNom()                             { return nom; }
    public void    setNom(String nom)                   { this.nom = nom; }
    public String  getPrenom()                          { return prenom; }
    public void    setPrenom(String prenom)             { this.prenom = prenom; }
    public String  getEmail()                           { return email; }
    public void    setEmail(String email)               { this.email = email; }
    public String  getMdp()                             { return mdp; }
    public void    setMdp(String mdp)                   { this.mdp = mdp; }
    public Role    getRole()                            { return role; }
    public void    setRole(Role role)                   { this.role = role; }
    public String  getSkills()                          { return skills; }
    public void    setSkills(String skills)             { this.skills = skills; }
    public String  getDiplomas()                        { return diplomas; }
    public void    setDiplomas(String diplomas)         { this.diplomas = diplomas; }
    public String  getExperience()                      { return experience; }
    public void    setExperience(String experience)     { this.experience = experience; }
    public String  getBio()                             { return bio; }
    public void    setBio(String bio)                   { this.bio = bio; }
    public String  getPhone()                           { return phone; }
    public void    setPhone(String phone)               { this.phone = phone; }
    public boolean isVerified()                         { return isVerified; }
    public void    setVerified(boolean verified)        { this.isVerified = verified; }
    public String  getVerificationCode()                { return verificationCode; }
    public void    setVerificationCode(String code)     { this.verificationCode = code; }
    public String  getFaceData()                        { return faceData; }
    public void    setFaceData(String faceData)         { this.faceData = faceData; }
    public String  getUsername()                        { return username; }
    public void    setUsername(String username)         { this.username = username; }

    // ── NEW getters/setters ───────────────────────────────────────────────────
    public boolean isFingerprintEnabled()                          { return fingerprintEnabled; }
    public void    setFingerprintEnabled(boolean fingerprintEnabled){ this.fingerprintEnabled = fingerprintEnabled; }

    public String  getBannedUntil()                                { return bannedUntil; }
    public void    setBannedUntil(String bannedUntil)              { this.bannedUntil = bannedUntil; }

    public String  getBanReason()                                  { return banReason; }
    public void    setBanReason(String banReason)                  { this.banReason = banReason; }

    public int     getFailedLoginAttempts()                        { return failedLoginAttempts; }
    public void    setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public String  getRecruiterRequestStatus()                          { return recruiterRequestStatus; }
    public void    setRecruiterRequestStatus(String recruiterRequestStatus){ this.recruiterRequestStatus = recruiterRequestStatus; }

    public String  getRecruiterRequestReviewedAt()                      { return recruiterRequestReviewedAt; }
    public void    setRecruiterRequestReviewedAt(String recruiterRequestReviewedAt){ this.recruiterRequestReviewedAt = recruiterRequestReviewedAt; }

    public String  getCvPath()                                     { return cvPath; }
    public void    setCvPath(String cvPath)                        { this.cvPath = cvPath; }
    // ─────────────────────────────────────────────────────────────────────────

    public String getDisplayName() {
        return (username != null && !username.isBlank()) ? username : nom + " " + prenom;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', role=" + role + "}";
    }
}