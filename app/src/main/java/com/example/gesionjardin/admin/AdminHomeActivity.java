package com.example.gesionjardin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gesionjardin.LoginActivity;
import com.example.gesionjardin.MainActivity;
import com.example.gesionjardin.R;
import com.example.gesionjardin.WelcomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    private MaterialCardView cardJardiniers, cardJardins, cardPlantes;
    private MaterialButton btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        // veille à nommer ton XML correctement ou adapte ci-dessus

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        cardJardiniers = findViewById(R.id.card_jardiniers);
        cardJardins    = findViewById(R.id.card_jardins);
        cardPlantes    = findViewById(R.id.card_plantes);
        btnLogout      = findViewById(R.id.btn_logout);

        // Actions sur les cartes
        cardJardiniers.setOnClickListener(v -> {
            Intent i = new Intent(AdminHomeActivity.this, JardinierListActivity.class);
            startActivity(i);
        });

        cardJardins.setOnClickListener(v -> {
            Intent i = new Intent(AdminHomeActivity.this, MainActivity.class);
            startActivity(i);
        });

        cardPlantes.setOnClickListener(v -> {
            Intent i = new Intent(AdminHomeActivity.this, ListePlantesActivity.class);
            startActivity(i);
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Déconnecté", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(AdminHomeActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
