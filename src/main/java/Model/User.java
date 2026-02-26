package Model;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String mdp;
    private Role role;

    // Client-specific fields (null for recruteur/admin)
    private String skills;
    private String diplomas;
    private String experience;
    private String bio;

    public User() {}

    // Constructor without id (before DB insert) — basic fields
    public User(String nom, String prenom, String email, String mdp, Role role) {
        this.nom    = nom;
        this.prenom = prenom;
        this.email  = email;
        this.mdp    = mdp;
        this.role   = role;
    }

    // Constructor with id (after DB read) — basic fields
    public User(int id, String nom, String prenom, String email, String mdp, Role role) {
        this.id     = id;
        this.nom    = nom;
        this.prenom = prenom;
        this.email  = email;
        this.mdp    = mdp;
        this.role   = role;
    }

    // Full constructor including client fields
    public User(int id, String nom, String prenom, String email, String mdp, Role role,
                String skills, String diplomas, String experience, String bio) {
        this.id         = id;
        this.nom        = nom;
        this.prenom     = prenom;
        this.email      = email;
        this.mdp        = mdp;
        this.role       = role;
        this.skills     = skills;
        this.diplomas   = diplomas;
        this.experience = experience;
        this.bio        = bio;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getNom()           { return nom; }
    public void setNom(String nom)   { this.nom = nom; }

    public String getPrenom()              { return prenom; }
    public void setPrenom(String prenom)   { this.prenom = prenom; }

    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }

    public String getMdp()                 { return mdp; }
    public void setMdp(String mdp)         { this.mdp = mdp; }

    public Role getRole()                  { return role; }
    public void setRole(Role role)         { this.role = role; }

    public String getSkills()              { return skills; }
    public void setSkills(String skills)   { this.skills = skills; }

    public String getDiplomas()                { return diplomas; }
    public void setDiplomas(String diplomas)   { this.diplomas = diplomas; }

    public String getExperience()                  { return experience; }
    public void setExperience(String experience)   { this.experience = experience; }

    public String getBio()                 { return bio; }
    public void setBio(String bio)         { this.bio = bio; }

    @Override
    public String toString() {
        return "User{id=" + id + ", nom='" + nom + "', prenom='" + prenom +
                "', email='" + email + "', role=" + role + "}";
    }
}