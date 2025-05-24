package com.example.gesionjardin.service;

import com.example.gesionjardin.model.ReleveCapteur;
import com.example.gesionjardin.model.InstancePlante;
import com.example.gesionjardin.service.FirebaseRepository;

public class ReleveManager implements BluetoothService.ReleveListener {
    private final FirebaseRepository firebaseRepo;
    private final AlertManager alertManager;

    public ReleveManager(FirebaseRepository repo, AlertManager alertMgr) {
        this.firebaseRepo = repo;
        this.alertManager = alertMgr;
    }

    @Override
    public void onReleveReceived(String capteurId, double temp, double humAir, int humSolPct, long ts) {
        // Recherche composite : gardenId + instanceId
        firebaseRepo.findInstanceByCapteurId(capteurId, key -> {
            if (key == null) return; // aucun
            // Charger l'InstancePlante via gardenId et instanceId
            firebaseRepo.loadInstance(key.gardenId, key.instanceId, inst -> {
                if (inst == null) return;
                // Construire le relevé
                ReleveCapteur r = new ReleveCapteur();
                r.setInstanceId(inst.getId());
                r.setTemperature(temp);
                r.setHumiditeAir(humAir);
                r.setHumiditeSol(humSolPct);
                r.setTimestamp(ts);

                // Stockage dans Firebase
                firebaseRepo.saveReleve(r);
                // Analyse + notification via l'instance chargée
                alertManager.checkAndNotify(r, inst);
            });
        });
    }
}