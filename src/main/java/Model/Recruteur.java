package Model;

import java.sql.Date;

public class Recruteur {

    private int id;
    private String name;
    private String email;
    private String password;
    private String company;
    private String phone;
    private Date createdAt;

    // Constructor without ID
    public Recruteur(String name, String email, String password, String company, String phone, Date createdAt) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.company = company;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // Constructor with ID
    public Recruteur(int id, String name, String email, String password, String company, String phone, Date createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.company = company;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
