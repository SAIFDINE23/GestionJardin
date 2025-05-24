package com.example.gesionjardin.jardinier;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gesionjardin.LoginActivity;
import com.example.gesionjardin.MainActivity;
import com.example.gesionjardin.R;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class JardinierHomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private MaterialCardView cardNotifications, cardJardins;
    private MaterialButton btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardinier_home);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        tvGreeting        = findViewById(R.id.tvGreeting);
        cardNotifications = findViewById(R.id.card_notifications);
        cardJardins       = findViewById(R.id.card_jardins);
        btnLogout         = findViewById(R.id.btn_logout);

        // Afficher le prénom ou l'email de l'utilisateur connecté
        if (mAuth.getCurrentUser() != null) {
            String name = mAuth.getCurrentUser().getDisplayName();
            if (name == null || name.isEmpty()) {
                name = mAuth.getCurrentUser().getEmail();
            }
            tvGreeting.setText("Bonjour " + name);
        }

        // Clic sur Mes notifications
        cardNotifications.setOnClickListener(v -> {
            Intent i = new Intent(JardinierHomeActivity.this, MainActivity.class);
            startActivity(i);
        });

        // Clic sur Mes jardins
        cardJardins.setOnClickListener(v -> {
            Intent i = new Intent(JardinierHomeActivity.this, ListeJardinsActivity.class);
            startActivity(i);
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(JardinierHomeActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
