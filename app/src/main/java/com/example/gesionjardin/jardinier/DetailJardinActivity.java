package com.example.gesionjardin.jardinier;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gesionjardin.R;
import com.example.gesionjardin.admin.ListePlantesActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetailJardinActivity extends AppCompatActivity {

    public static final String EXTRA_GARDEN_ID = "com.example.gesionjardin.EXTRA_GARDEN_ID";
    private ViewPager2 vpGardenImages;
    private TextView tvGardenerName, tvGardenAddress, tvGardenSurface;
    private Button btnViewPlants;

    private String gardenId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_jardin);

        // Récupérer l'ID du jardin depuis l'intent
        gardenId = getIntent().getStringExtra("gardenId");
        if (gardenId == null) {
            Toast.makeText(this, "Jardin non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialisation des vues
        vpGardenImages = findViewById(R.id.vpGardenImages);
        tvGardenerName = findViewById(R.id.tvGardenerName);
        tvGardenAddress = findViewById(R.id.tvGardenAddress);
        tvGardenSurface = findViewById(R.id.tvGardenSurface);
        btnViewPlants = findViewById(R.id.btnViewPlants);

        // Charger les données du jardin
        loadGardenDetails(gardenId);

        // Bouton Voir les Plantes
        btnViewPlants.setOnClickListener(view -> {
            Intent intent = new Intent(this, ListePlantesJardinActivity.class);
            intent.putExtra(ListePlantesJardinActivity.EXTRA_GARDEN_ID, gardenId);
            startActivity(intent);
        });

    }

    private void loadGardenDetails(String gardenId) {
        DatabaseReference gardenRef = FirebaseDatabase.getInstance().getReference("gardens").child(gardenId);
        gardenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DetailJardinActivity.this, "Détails du jardin non trouvés", Toast.LENGTH_SHORT).show();
                    return;
                }

                String address = snapshot.child("address").getValue(String.class);
                int surface = snapshot.child("surface").getValue(Integer.class);
                String ownerId = snapshot.child("ownerId").getValue(String.class);

                List<String> images = new ArrayList<>();
                for (DataSnapshot imgSnapshot : snapshot.child("images").getChildren()) {
                    images.add(imgSnapshot.getValue(String.class));
                }

                // Mettre à jour l'UI
                tvGardenAddress.setText("Adresse : " + address);
                tvGardenSurface.setText("Surface : " + surface + " m²");
                vpGardenImages.setAdapter(new ImagePagerAdapter(DetailJardinActivity.this, images));

                // Charger nom du jardinier
                loadGardenerName(ownerId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailJardinActivity.this, "Erreur : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGardenerName(String ownerId) {
        if (ownerId == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(ownerId);
        userRef.child("nom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                tvGardenerName.setText("Jardinier : " + (name != null ? name : "Inconnu"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvGardenerName.setText("Jardinier : Inconnu");
            }
        });
    }
}
