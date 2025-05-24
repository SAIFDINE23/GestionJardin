package com.example.gesionjardin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.MainActivity;
import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Plante;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListePlantesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private com.example.gesionjardin.admin.PlanteAdapter adapter;
    private List<Plante> listePlantes = new ArrayList<>();
    private DatabaseReference plantesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_plantes);

        recyclerView = findViewById(R.id.recycler_view_plantes);
        btnAdd       = findViewById(R.id.btn_add_plante);

        // config RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlanteAdapter(listePlantes, plante -> {
            // TODO : lancer détail / édition de la plante
            Intent intent = new Intent(this, DetailPlanteActivity.class);
            intent.putExtra("planteId", plante.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // référence Realtime DB
        plantesRef = FirebaseDatabase.getInstance()
                .getReference("plantes");

        // charger les plantes
        plantesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listePlantes.clear();
                for (DataSnapshot child: snapshot.getChildren()) {
                    Plante p = child.getValue(Plante.class);
                    if (p != null) listePlantes.add(p);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // gérer l'erreur si besoin
            }
        });

        // action bouton ajouter
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AjouterPlanteActivity.class));
        });
    }
}
