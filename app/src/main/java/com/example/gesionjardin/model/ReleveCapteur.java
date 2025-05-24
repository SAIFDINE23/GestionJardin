package com.example.gesionjardin.model;


/**
 * Représente une mesure effectuée par le capteur pour une instance de plante.
 */
public class ReleveCapteur {

    private String id;             // clé Firebase du relevé
    private String instanceId;     // référence à InstancePlante.id
    private double temperature;    // en °C
    private double humiditeSol;    // en %
    private double humiditeAir;    // en %
    private long timestamp;        // date/heure du relevé en ms depuis epoch

    /**
     * Constructeur vide requis pour Firebase Realtime Database.
     */
    public ReleveCapteur() {
        // nécessaire pour la désérialisation
    }

    /**
     * Constructeur complet.
     * @param id            Identifiant Firebase du relevé
     * @param instanceId    ID de l'instance de plante concernée
     * @param temperature   Température mesurée (°C)
     * @param humiditeSol   Humidité du sol mesurée (%)
     * @param humiditeAir   Humidité de l'air mesurée (%)
     * @param timestamp     Timestamp du relevé (ms depuis epoch)
     */
    public ReleveCapteur(String id, String instanceId,
                         double temperature, double humiditeSol,
                         double humiditeAir, long timestamp) {
        this.id = id;
        this.instanceId = instanceId;
        this.temperature = temperature;
        this.humiditeSol = humiditeSol;
        this.humiditeAir = humiditeAir;
        this.timestamp = timestamp;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumiditeSol() {
        return humiditeSol;
    }

    public double getHumiditeAir() {
        return humiditeAir;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setHumiditeSol(double humiditeSol) {
        this.humiditeSol = humiditeSol;
    }

    public void setHumiditeAir(double humiditeAir) {
        this.humiditeAir = humiditeAir;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
