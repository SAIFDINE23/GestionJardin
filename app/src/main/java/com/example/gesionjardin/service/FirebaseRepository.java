package com.example.gesionjardin.service;

import com.example.gesionjardin.model.ReleveCapteur;
import com.example.gesionjardin.model.InstancePlante;
import com.example.gesionjardin.model.Plante;
import com.example.gesionjardin.model.Notification;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseRepository {
    private final DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    /**
     * Clef composite retournée lors de la recherche :
     * gardenId + instanceId
     */
    public static class InstanceKey {
        public String gardenId;
        public String instanceId;

        public InstanceKey() {}
        public InstanceKey(String gardenId, String instanceId) {
            this.gardenId = gardenId;
            this.instanceId = instanceId;
        }
    }

    public interface Callback<T> { void onComplete(T result); }

    /**
     * Recherche une instance par capteurId dans tous les jardins,
     * retourne gardenId + instanceId si trouvée.
     */
    public void findInstanceByCapteurId(String capteurId, Callback<InstanceKey> cb) {
        db.child("gardens").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapGardens) {
                for (DataSnapshot gardenSnap : snapGardens.getChildren()) {
                    String gardenId = gardenSnap.getKey();
                    DataSnapshot instances = gardenSnap.child("instances");
                    for (DataSnapshot instSnap : instances.getChildren()) {
                        String stored = instSnap.child("capteurId").getValue(String.class);
                        if (capteurId.equals(stored)) {
                            cb.onComplete(new InstanceKey(gardenId, instSnap.getKey()));
                            return;
                        }
                    }
                }
                cb.onComplete(null);
            }
            @Override public void onCancelled(DatabaseError e) {
                cb.onComplete(null);
            }
        });
    }

    /**
     * Charge une instance de plante via gardenId et instanceId
     */
    public void loadInstance(String gardenId, String instanceId, Callback<InstancePlante> cb) {
        db.child("gardens")
                .child(gardenId)
                .child("instances")
                .child(instanceId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        cb.onComplete(snap.getValue(InstancePlante.class));
                    }
                    @Override public void onCancelled(DatabaseError e) {
                        cb.onComplete(null);
                    }
                });
    }

    /**
     * Charge une plante (espèce) via son ID
     */
    public void loadPlante(String planteId, Callback<Plante> cb) {
        db.child("especes_plante")
                .child(planteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot snap) {
                        cb.onComplete(snap.getValue(Plante.class));
                    }
                    @Override public void onCancelled(DatabaseError e) {
                        cb.onComplete(null);
                    }
                });
    }

    /** Sauvegarde un relevé capteur */
    public void saveReleve(ReleveCapteur r) {
        db.child("releves_capteurs").push().setValue(r);
    }

    /** Sauvegarde une notification */
    public void saveNotification(Notification n) {
        db.child("notifications").push().setValue(n);
    }
}
