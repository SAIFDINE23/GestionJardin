package com.example.gesionjardin.model;

import java.io.Serializable;

/**
 * Représente une espèce de plante dans le catalogue (gérée par l'administrateur).
 * Les besoins en température, humidité de l'air et humidité du sol sont exprimés sous forme d'intervalles.
 */
public class Plante implements Serializable {

    private String id;                  // clé Firebase
    private String nom;                 // nom de l'espèce
    private String imageUrl;            // URL de l'image type d'espèce (peut être null)
    private String description;         // description de l'espèce
    private Intervalle tempMinMax;      // intervalle de température (min/max en °C)
    private Intervalle humidAirMinMax;  // intervalle d'humidité de l'air (min/max en %)
    private Intervalle humidSolMinMax;  // intervalle d'humidité du sol (min/max en %)

    /**
     * Constructeur vide requis pour Firebase Realtime Database.
     */
    public Plante() {
        // valeurs par défaut ou resteront null
    }

    /**
     * Constructeur complet.
     * @param id                  Identifiant Firebase de la plante
     * @param nom                 Nom de l'espèce
     * @param imageUrl            URL de l'image (peut être null)
     * @param description         Description de l'espèce
     * @param tempMinMax          Intervalle de température en °C
     * @param humidAirMinMax      Intervalle d'humidité de l'air en %
     * @param humidSolMinMax      Intervalle d'humidité du sol en %
     */
    public Plante(String id, String nom, String imageUrl,
                  String description,
                  Intervalle tempMinMax,
                  Intervalle humidAirMinMax,
                  Intervalle humidSolMinMax) {
        this.id = id;
        this.nom = nom;
        this.imageUrl = imageUrl;
        this.description = description;
        this.tempMinMax = tempMinMax;
        this.humidAirMinMax = humidAirMinMax;
        this.humidSolMinMax = humidSolMinMax;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public Intervalle getTempMinMax() {
        return tempMinMax;
    }

    public Intervalle getHumidAirMinMax() {
        return humidAirMinMax;
    }

    public Intervalle getHumidSolMinMax() {
        return humidSolMinMax;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTempMinMax(Intervalle tempMinMax) {
        this.tempMinMax = tempMinMax;
    }

    public void setHumidAirMinMax(Intervalle humidAirMinMax) {
        this.humidAirMinMax = humidAirMinMax;
    }

    public void setHumidSolMinMax(Intervalle humidSolMinMax) {
        this.humidSolMinMax = humidSolMinMax;
    }
}
