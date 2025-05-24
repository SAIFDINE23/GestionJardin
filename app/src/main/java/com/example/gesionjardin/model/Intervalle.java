package com.example.gesionjardin.model;

import java.io.Serializable;

/**
 * Représente un intervalle numérique (min/max).
 */
public class Intervalle implements Serializable {
    private double min;  // valeur minimale
    private double max;  // valeur maximale

    // Constructeur vide requis pour Firebase Realtime Database
    public Intervalle() {}

    public Intervalle(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
