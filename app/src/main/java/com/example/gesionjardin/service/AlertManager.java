package com.example.gesionjardin.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gesionjardin.model.ReleveCapteur;
import com.example.gesionjardin.model.InstancePlante;
import com.example.gesionjardin.model.Plante;
import com.example.gesionjardin.model.Notification;
import com.example.gesionjardin.service.FirebaseRepository;

public class AlertManager {
    private static final String CHANNEL_ID = "alert_channel";
    private final FirebaseRepository repo;
    private final Context ctx;

    public AlertManager(Context context, FirebaseRepository repository) {
        this.ctx = context;
        this.repo = repository;
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Alertes Plantes", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Vérifie l'anomalie puis crée notification si besoin.
     * Ici inst contient déjà l'InstancePlante liée.
     */
    public void checkAndNotify(ReleveCapteur r, InstancePlante inst) {
        // Charger l'espèce pour récupérer les seuils
        repo.loadPlante(inst.getEspeceId(), plante -> {
            if (plante == null) return;
            if (isAnomalie(r, plante)) {
                Notification notif = buildNotification(r, inst);
                repo.saveNotification(notif);
                showLocalNotification(notif.getContent());
            }
        });
    }

    private boolean isAnomalie(ReleveCapteur r, Plante p) {
        return r.getTemperature() < p.getTempMinMax().getMin()
                || r.getTemperature() > p.getTempMinMax().getMax()
                || r.getHumiditeAir() < p.getHumidAirMinMax().getMin()
                || r.getHumiditeAir() > p.getHumidAirMinMax().getMax()
                || r.getHumiditeSol() < p.getHumidSolMinMax().getMin()
                || r.getHumiditeSol() > p.getHumidSolMinMax().getMax();
    }

    private Notification buildNotification(ReleveCapteur r, InstancePlante inst) {
        Notification n = new Notification();
        n.setInstanceId(inst.getId());
        n.setDate(System.currentTimeMillis());
        n.setContent("Alerte: valeur anormale pour " + inst.getLibelle());
        n.setRead(false);
        return n;
    }

    private void showLocalNotification(String message) {
        // Pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Alerte Jardin")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(ctx).notify((int) System.currentTimeMillis(), builder.build());
    }
}