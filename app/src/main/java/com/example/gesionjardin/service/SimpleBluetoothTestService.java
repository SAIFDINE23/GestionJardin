package com.example.gesionjardin.service;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;

public class SimpleBluetoothTestService extends Service {

    private static final String TAG = "BTTestService";
    private static final String MAC = "00:18:91:D7:27:7A";
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // pas utilisé
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this::runTest).start();
        return START_NOT_STICKY;
    }

    private void runTest() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            show("Pas de Bluetooth sur cet appareil");
            stopSelf();
            return;
        }
        if (!adapter.isEnabled()) {
            show("Active le Bluetooth puis relance le test");
            stopSelf();
            return;
        }

        try {
            BluetoothDevice device = adapter.getRemoteDevice(MAC);

            // Sur Android 12+ vérifier la permission BLUETOOTH_CONNECT
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    show("Permission BLUETOOTH_CONNECT manquante");
                    stopSelf();
                    return;
                }
            }

            Log.d(TAG, "Connexion à " + MAC);
            show("Connexion à " + MAC);

            BluetoothSocket socket = device
                    .createRfcommSocketToServiceRecord(SPP_UUID);

            // Sécurité : gérer SecurityException
            try {
                socket.connect();
            } catch (SecurityException se) {
                Log.e(TAG, "Permission BLUETOOTH_CONNECT refusée", se);
                show("Permission BLUETOOTH_CONNECT refusée");
                socket.close();
                stopSelf();
                return;
            }

            Log.d(TAG, "Connecté au HC-05");
            show("Connecté au HC-05");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = in.readLine(); // lire une ligne
            Log.d(TAG, "Reçu: " + line);
            show("Reçu: " + line);

            socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Erreur BT", e);
            show("Erreur BT: " + e.getMessage());
        } finally {
            stopSelf();
        }
    }

    private void show(String msg) {
        Log.d(TAG, msg);
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TestService détruit");
    }
}
