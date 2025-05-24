package com.example.gesionjardin.service;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

public class BluetoothService extends Service {

    public interface ReleveListener {
        void onReleveReceived(String capteurId, double temp, double humAir, int humSolPct, long timestamp);
    }

    private static final String CAPTEUR_ID = "00:18:91:D7:27:7A";
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final IBinder binder = new LocalBinder();
    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private HandlerThread thread;
    private ReleveListener listener;

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public void setReleveListener(ReleveListener l) {
        listener = l;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BluetoothService", "Service démarré");
        showToast("BluetoothService démarré");

        thread = new HandlerThread("BT-Thread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        handler.post(this::connectAndListen);

        return START_STICKY;
    }

    private void connectAndListen() {
        Log.d("BluetoothService", "Vérification permission BLUETOOTH_CONNECT");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("BluetoothService", "Permission BLUETOOTH_CONNECT non accordée, arrêt du service");
                showToast("Permission Bluetooth non accordée, service arrêté");
                stopSelf();
                return;
            }
        }

        try {
            Log.d("BluetoothService", "Tentative de connexion au capteur " + CAPTEUR_ID);
            showToast("Connexion au capteur Bluetooth...");

            adapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = adapter.getRemoteDevice(CAPTEUR_ID);
            socket = device.createRfcommSocketToServiceRecord(BT_UUID);
            socket.connect();

            Log.d("BluetoothService", "Connecté au capteur");
            showToast("Connecté au capteur Bluetooth");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                Log.d("BluetoothService", "Trame reçue: " + line);
                showToast("Trame reçue: " + line);
                parseCSV(line);
            }

        } catch (SecurityException se) {
            Log.e("BluetoothService", "Erreur de permission Bluetooth", se);
            showToast("Erreur permission Bluetooth");
            stopSelf();
        } catch (Exception e) {
            Log.e("BluetoothService", "Erreur de connexion ou lecture", e);
            showToast("Erreur Bluetooth: " + e.getMessage());
            stopSelf();
        }
    }

    private void parseCSV(String line) {
        String[] parts = line.split(",");
        try {
            double temp = Double.parseDouble(parts[0]);
            double humAir = Double.parseDouble(parts[1]);
            int humSolPct = Integer.parseInt(parts[2].replace("%", ""));
            long ts = Long.parseLong(parts[3]);

            Log.d("BluetoothService", "Données parsées: temp=" + temp + ", humAir=" + humAir + ", humSol=" + humSolPct + "%");
            showToast("Données: T=" + temp + "°C, HAir=" + humAir + "%, HSol=" + humSolPct + "%");

            if (listener != null) {
                listener.onReleveReceived(CAPTEUR_ID, temp, humAir, humSolPct, ts);
            } else {
                Log.w("BluetoothService", "Aucun listener pour traiter les relevés");
                showToast("Aucun listener actif pour les relevés !");
            }
        } catch (Exception e) {
            Log.e("BluetoothService", "Erreur parsing CSV", e);
            showToast("Erreur parsing: " + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.d("BluetoothService", "Service détruit");
        showToast("BluetoothService arrêté");

        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}

        if (thread != null) thread.quitSafely();
        super.onDestroy();
    }

    private void showToast(String msg) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }
}
