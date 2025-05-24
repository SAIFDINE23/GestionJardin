package com.example.gesionjardin.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Plante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class DetailPlanteActivity extends AppCompatActivity {

    public static final String EXTRA_PLANTE_ID = "planteId";

    private ImageView btnBack, ivPlante;
    private EditText etNom, etDescription,
            etTempMin, etTempMax,
            etHumidMin, etHumidMax,
            etHumidSolMin, etHumidSolMax;
    private Button btnModifier, btnEnregistrer;

    private String planteId;
    private DatabaseReference plantesRef;
    private Plante currentPlante;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_plante);

        // Auth et userRef
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());
        }

        // 1. Récupérer l'ID et init Firebase
        planteId   = getIntent().getStringExtra(EXTRA_PLANTE_ID);
        plantesRef = FirebaseDatabase.getInstance()
                .getReference("plantes")
                .child(planteId);

        // 2. Bind views
        btnBack        = findViewById(R.id.btn_back);
        ivPlante       = findViewById(R.id.iv_plante_detail);
        etNom          = findViewById(R.id.et_plante_nom);
        etDescription  = findViewById(R.id.et_plante_description);
        etTempMin      = findViewById(R.id.et_plante_temp_min);
        etTempMax      = findViewById(R.id.et_plante_temp_max);
        etHumidMin     = findViewById(R.id.et_plante_humid_min);
        etHumidMax     = findViewById(R.id.et_plante_humid_max);
        etHumidSolMin  = findViewById(R.id.et_plante_humid_sol_min);
        etHumidSolMax  = findViewById(R.id.et_plante_humid_sol_max);
        btnModifier    = findViewById(R.id.btn_modifier_plante);
        btnEnregistrer = findViewById(R.id.btn_enregistrer_plante);

        // 3. Vérifier le rôle
        if (userRef != null) {
            userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snap) {
                    String role = snap.getValue(String.class);
                    if ("jardinier".equals(role)) {
                        btnModifier.setVisibility(View.GONE);
                        btnEnregistrer.setVisibility(View.GONE);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // 4. Chargement des données
        loadPlante();

        // 5. Bouton Retour
        btnBack.setOnClickListener(v -> finish());

        // 6. Bouton Modifier → mode édition
        btnModifier.setOnClickListener(v -> {
            setEditable(true);
            btnModifier.setVisibility(View.GONE);
            btnEnregistrer.setVisibility(View.VISIBLE);
        });

        // 7. Bouton Enregistrer → push update
        btnEnregistrer.setOnClickListener(v -> saveChanges());
    }

    private void loadPlante() {
        plantesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                currentPlante = snap.getValue(Plante.class);
                if (currentPlante == null) {
                    finish();
                    return;
                }
                // Image
                String url = currentPlante.getImageUrl();
                if (url != null && !url.isEmpty()) {
                    Picasso.get()
                            .load(url)
                            .placeholder(R.drawable.ic_plant_placeholder)
                            .into(ivPlante);
                }
                // Textes
                etNom.setText(currentPlante.getNom());
                etDescription.setText(currentPlante.getDescription());
                etTempMin.setText(String.valueOf(currentPlante.getTempMinMax().getMin()));
                etTempMax.setText(String.valueOf(currentPlante.getTempMinMax().getMax()));
                etHumidMin.setText(String.valueOf(currentPlante.getHumidAirMinMax().getMin()));
                etHumidMax.setText(String.valueOf(currentPlante.getHumidAirMinMax().getMax()));
                etHumidSolMin.setText(String.valueOf(currentPlante.getHumidSolMinMax().getMin()));
                etHumidSolMax.setText(String.valueOf(currentPlante.getHumidSolMinMax().getMax()));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setEditable(boolean editable) {
        etNom.setEnabled(editable);
        etDescription.setEnabled(editable);
        etTempMin.setEnabled(editable);
        etTempMax.setEnabled(editable);
        etHumidMin.setEnabled(editable);
        etHumidMax.setEnabled(editable);
        etHumidSolMin.setEnabled(editable);
        etHumidSolMax.setEnabled(editable);
    }

    private void saveChanges() {
        currentPlante.setNom(etNom.getText().toString().trim());
        currentPlante.setDescription(etDescription.getText().toString().trim());
        currentPlante.setTempMinMax(new com.example.gesionjardin.model.Intervalle(
                Double.parseDouble(etTempMin.getText().toString().trim()),
                Double.parseDouble(etTempMax.getText().toString().trim())
        ));
        currentPlante.setHumidAirMinMax(new com.example.gesionjardin.model.Intervalle(
                Double.parseDouble(etHumidMin.getText().toString().trim()),
                Double.parseDouble(etHumidMax.getText().toString().trim())
        ));
        currentPlante.setHumidSolMinMax(new com.example.gesionjardin.model.Intervalle(
                Double.parseDouble(etHumidSolMin.getText().toString().trim()),
                Double.parseDouble(etHumidSolMax.getText().toString().trim())
        ));

        plantesRef.setValue(currentPlante)
                .addOnSuccessListener(a -> {
                    setEditable(false);
                    btnModifier.setVisibility(View.VISIBLE);
                    btnEnregistrer.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show());
    }
}
