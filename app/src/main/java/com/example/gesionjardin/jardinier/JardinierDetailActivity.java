package com.example.gesionjardin.jardinier;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Utilisateur;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class JardinierDetailActivity extends AppCompatActivity {

    private ImageView ivPhoto;
    private TextView tvNom, tvEmail, tvTelephone;

    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardinier_detail);

        ivPhoto     = findViewById(R.id.iv_jardinier_detail_photo);
        tvNom       = findViewById(R.id.tv_jardinier_detail_nom);
        tvEmail     = findViewById(R.id.tv_jardinier_detail_email);
        tvTelephone = findViewById(R.id.tv_jardinier_detail_telephone);

        usersRef = FirebaseDatabase.getInstance().getReference("utilisateurs");

        String userId = getIntent().getStringExtra("userId");

        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Utilisateur jardinier = snapshot.getValue(Utilisateur.class);
                        if (jardinier != null) {
                            tvNom.setText(jardinier.getPrenom() + " " + jardinier.getNom());
                            tvEmail.setText(jardinier.getEmail());
                            tvTelephone.setText(jardinier.getTel() != null ? jardinier.getTel() : "Non renseigné");

                            if (jardinier.getImageUrl() != null && !jardinier.getImageUrl().isEmpty()) {
                                Picasso.get().load(jardinier.getImageUrl()).into(ivPhoto);
                            }
                        }
                    } else {
                        Toast.makeText(JardinierDetailActivity.this, "Jardinier introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(JardinierDetailActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "ID utilisateur manquant", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }
}
