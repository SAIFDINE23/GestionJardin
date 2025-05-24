package com.example.gesionjardin.model;


/**
 * Représente un pied physique d'une espèce planté dans un jardin.
 */
public class InstancePlante {

    private String id;           // clé Firebase de l'instance
    private String jardinId;     // référence à Garden.id
    private String especeId;     // référence à Plante.id
    private String libelle;      // nom personnalisé de l'instance (ex. "Tomate poterie gauche")
    private String capteurId;    // identifiant du capteur (MAC ou UID)
    private long datePlantation; // timestamp de la plantation (ms depuis epoch)

    /**
     * Constructeur vide requis pour Firebase Realtime Database.
     */
    public InstancePlante() {
        // Firebase nécessite un constructeur sans arguments
    }

    /**
     * Constructeur complet.
     * @param id               Identifiant de l'instance
     * @param jardinId         ID du jardin dans lequel est planté cet exemplaire
     * @param especeId         ID de l'espèce (catalogue)
     * @param libelle          Nom personnalisé pour distinguer plusieurs pieds
     * @param capteurId        Identifiant du capteur associe
     * @param datePlantation   Date de plantation (timestamp)
     */
    public InstancePlante(String id, String jardinId, String especeId,
                          String libelle, String capteurId, long datePlantation) {
        this.id = id;
        this.jardinId = jardinId;
        this.especeId = especeId;
        this.libelle = libelle;
        this.capteurId = capteurId;
        this.datePlantation = datePlantation;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getJardinId() {
        return jardinId;
    }

    public String getEspeceId() {
        return especeId;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getCapteurId() {
        return capteurId;
    }

    public long getDatePlantation() {
        return datePlantation;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setJardinId(String jardinId) {
        this.jardinId = jardinId;
    }

    public void setEspeceId(String especeId) {
        this.especeId = especeId;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setCapteurId(String capteurId) {
        this.capteurId = capteurId;
    }

    public void setDatePlantation(long datePlantation) {
        this.datePlantation = datePlantation;
    }
}

