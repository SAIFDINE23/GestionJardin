package com.example.gesionjardin.model;

import java.util.List;

public class Garden {

    private String id;             // key Firebase
    private String ownerId;        // référence à User.id
    private String address;        // adresse du jardin
    private double surface;        // surface en m²
    private List<String> images;   // URLs des images (peuvent être null)


    public Garden() {
        // Pas d'initialisation spécifique, images peut rester null ou être assigné plus tard
    }


    public Garden(String id, String ownerId, String address,
                  double surface, List<String> images) {
        this.id = id;
        this.ownerId = ownerId;
        this.address = address;
        this.surface = surface;
        this.images = images;
    }

    // ----- Getters -----

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getAddress() {
        return address;
    }

    public double getSurface() {
        return surface;
    }

    public List<String> getImages() {
        return images;
    }

    // ----- Setters -----

    public void setId(String id) {
        this.id = id;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSurface(double surface) {
        this.surface = surface;
    }


    public void setImages(List<String> images) {
        this.images = images;
    }
}

