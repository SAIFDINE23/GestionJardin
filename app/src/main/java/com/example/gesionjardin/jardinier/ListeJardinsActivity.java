package com.example.gesionjardin.jardinier;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.R;
import com.example.gesionjardin.jardinier.AjouterJardinActivity;
import com.example.gesionjardin.jardinier.DetailJardinActivity;
import com.example.gesionjardin.model.Garden;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListeJardinsActivity extends AppCompatActivity {
    public static final String EXTRA_GARDEN_ID = "com.example.gesionjardin.EXTRA_GARDEN_ID";

    private RecyclerView recyclerView;
    private FloatingActionButton btnAddGarden;
    private JardinAdapter adapter;
    private List<Garden> listeJardins = new ArrayList<>();
    private DatabaseReference gardensRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_jardins);

        recyclerView  = findViewById(R.id.recycler_view_jardins);
        btnAddGarden  = findViewById(R.id.btn_add_garden);

        // 1. Configurer RecyclerView & Adapter avec listener vers DetailJardinActivity
        adapter = new JardinAdapter(
                this,
                listeJardins,
                jardin -> {
                    Intent intent = new Intent(
                            ListeJardinsActivity.this,
                            DetailJardinActivity.class
                    );

                    intent.putExtra("gardenId", jardin.getId());
                    startActivity(intent);
                }
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 2. Référence Firebase
        gardensRef = FirebaseDatabase.getInstance()
                .getReference("gardens");

        // 3. Charger les jardins en temps réel
        gardensRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listeJardins.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Garden g = child.getValue(Garden.class);
                    if (g != null) listeJardins.add(g);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // TODO: gérer l'erreur (toast ou log)
            }
        });

        // 4. Ajouter un nouveau jardin
        btnAddGarden.setOnClickListener(v ->
                startActivity(new Intent(
                        ListeJardinsActivity.this,
                        AjouterJardinActivity.class
                ))
        );
    }
}
