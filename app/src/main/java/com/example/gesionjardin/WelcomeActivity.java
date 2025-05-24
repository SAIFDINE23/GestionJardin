package com.example.gesionjardin;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gesionjardin.jardinier.RegisterActivity;
import com.example.gesionjardin.service.AlertManager;
import com.example.gesionjardin.service.BluetoothService;
import com.example.gesionjardin.service.FirebaseRepository;
import com.example.gesionjardin.service.ReleveManager;
import com.example.gesionjardin.service.SimpleBluetoothTestService;

public class WelcomeActivity extends AppCompatActivity {

    private static final int REQUEST_BT_PERMS = 100;
    private Button btnRegister, btnLogin;
    private BluetoothService btService;
    private boolean bound = false;

    private final ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            btService = binder.getService();
            bound = true;

            FirebaseRepository repo = new FirebaseRepository();
            AlertManager alertMgr = new AlertManager(WelcomeActivity.this, repo);
            ReleveManager rMgr = new ReleveManager(repo, alertMgr);
            btService.setReleveListener(rMgr);

            Log.d("WelcomeActivity", "BluetoothService lié et listener configuré");
            Toast.makeText(WelcomeActivity.this, "BluetoothService connecté avec succès", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            Log.w("WelcomeActivity", "BluetoothService déconnecté");
            Toast.makeText(WelcomeActivity.this, "BluetoothService déconnecté", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);

        btnRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Ouverture de l'inscription", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RegisterActivity.class));
        });
        startService(new Intent(this, SimpleBluetoothTestService.class));


        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Ouverture de la connexion", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        });

        Toast.makeText(this, "Vérification des permissions Bluetooth...", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] perms = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermsIfNeeded(perms);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            requestPermsIfNeeded(perms);
        } else {
            Toast.makeText(this, "Version Android ancienne : pas de permissions requises", Toast.LENGTH_SHORT).show();
            initBluetoothService();
        }
    }

    private void requestPermsIfNeeded(String[] perms) {
        boolean allGranted = true;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            Log.d("WelcomeActivity", "Toutes les permissions sont déjà accordées");
            Toast.makeText(this, "Permissions déjà accordées", Toast.LENGTH_SHORT).show();
            initBluetoothService();
        } else {
            Log.d("WelcomeActivity", "Demande des permissions Bluetooth et localisation");
            ActivityCompat.requestPermissions(this, perms, REQUEST_BT_PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BT_PERMS) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d("WelcomeActivity", "Permissions accordées après demande");
                Toast.makeText(this, "Permissions accordées", Toast.LENGTH_SHORT).show();
                initBluetoothService();
            } else {
                Log.e("WelcomeActivity", "Permissions refusées !");
                Toast.makeText(this,
                        "Les permissions Bluetooth et localisation sont nécessaires",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initBluetoothService() {
        Log.d("WelcomeActivity", "Initialisation du service Bluetooth");
        Toast.makeText(this, "Démarrage du service Bluetooth...", Toast.LENGTH_SHORT).show();

        Intent svcIntent = new Intent(this, BluetoothService.class);
        startService(svcIntent);
        bindService(svcIntent, btConnection, BIND_AUTO_CREATE);
        Toast.makeText(this, "Démarrage du classe service Bluetooth...", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            unbindService(btConnection);
            bound = false;
            Log.d("WelcomeActivity", "Service Bluetooth délié");
            Toast.makeText(this, "BluetoothService arrêté", Toast.LENGTH_SHORT).show();
        }
    }
}
