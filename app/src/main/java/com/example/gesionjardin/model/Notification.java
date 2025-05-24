package com.example.gesionjardin.model;

/**
 * Représente une notification générée pour une instance de plante.
 */
public class Notification {

    private String id;            // clé Firebase de la notification
    private String instanceId;    // référence à InstancePlante.id
    private long date;            // timestamp de la notification (ms depuis epoch)
    private String content;       // message à afficher
    private boolean read;         // false = non-lu, true = lu

    /**
     * Constructeur vide requis pour Firebase Realtime Database.
     */
    public Notification() {
        // nécessaire pour la désérialisation Firebase
    }

    /**
     * Constructeur complet.
     * @param id          Identifiant Firebase de la notification
     * @param instanceId  ID de l'instance de plante concernée
     * @param date        Timestamp de création (ms depuis epoch)
     * @param content     Contenu du message
     * @param read        État de lecture (false = non-lu)
     */
    public Notification(String id, String instanceId,
                        long date, String content, boolean read) {
        this.id = id;
        this.instanceId = instanceId;
        this.date = date;
        this.content = content;
        this.read = read;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public long getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return read;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}

