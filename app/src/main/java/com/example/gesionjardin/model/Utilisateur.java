package com.example.gesionjardin.model;

public class Utilisateur {

    private String id;
    private String nom;
    private String prenom;
    private String tel;
    private String email;
    private String password;
    private String imageUrl;
    private String role; // "jardinier" ou "admin"


    public Utilisateur() {
        this.role = "jardinier";
        this.imageUrl = null;
    }

    public Utilisateur(String id, String nom, String prenom,
                       String tel, String email, String password,
                       String imageUrl, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.tel = tel;
        this.email = email;
        this.password = password;
        this.imageUrl = imageUrl;
        this.role = role;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRole() {
        return role;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public void setRole(String role) {
        this.role = role;
    }
}

