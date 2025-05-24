package com.example.gesionjardin.jardinier;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.R;
import com.example.gesionjardin.admin.DetailPlanteActivity;
import com.example.gesionjardin.admin.PlanteAdapter;
import com.example.gesionjardin.model.Plante;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListePlantesJardinActivity extends AppCompatActivity {

    public static final String EXTRA_GARDEN_ID = "gardenId";

    private RecyclerView recyclerView;
    private FloatingActionButton btnAddPlante;
    private PlanteAdapter adapter;
    private List<Plante> listePlantes = new ArrayList<>();

    // on pointe sur "instances" et "plantes"
    private DatabaseReference instancesRef;
    private DatabaseReference plantesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_plantes);

        String gardenId = getIntent().getStringExtra(EXTRA_GARDEN_ID);
        if (gardenId == null) {
            Toast.makeText(this, "ID de jardin manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI
        recyclerView = findViewById(R.id.recycler_view_plantes);
        btnAddPlante = findViewById(R.id.btn_add_plante);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlanteAdapter(listePlantes, plante -> {
            Intent i = new Intent(this, DetailPlanteActivity.class);
            i.putExtra("planteId", plante.getId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);
        btnAddPlante.setVisibility(View.GONE);

        // Firebase
        instancesRef = FirebaseDatabase.getInstance().getReference("instances");
        plantesRef   = FirebaseDatabase.getInstance().getReference("plantes");

        // → on va sur le sous-nœud gardenId
        instancesRef.child(gardenId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapGardenInst) {
                        if (!snapGardenInst.exists()) {
                            Toast.makeText(
                                    ListePlantesJardinActivity.this,
                                    "Aucune plante pour ce jardin",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        // Collecte des especeId
                        Set<String> especeIds = new HashSet<>();
                        for (DataSnapshot inst : snapGardenInst.getChildren()) {
                            String especeId = inst.child("especeId").getValue(String.class);
                            if (especeId != null) especeIds.add(especeId);
                        }

                        if (especeIds.isEmpty()) {
                            Toast.makeText(
                                    ListePlantesJardinActivity.this,
                                    "Aucune plante pour ce jardin",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        listePlantes.clear();

                        // Pour chaque especeId, on va chercher la plante
                        for (String id : especeIds) {
                            plantesRef.child(id)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapPlant) {
                                            Plante p = snapPlant.getValue(Plante.class);
                                            if (p != null) {
                                                listePlantes.add(p);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                ListePlantesJardinActivity.this,
                                "Erreur de chargement",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
