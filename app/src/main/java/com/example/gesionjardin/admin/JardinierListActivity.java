package com.example.gesionjardin.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.MainActivity;
import com.example.gesionjardin.R;
import com.example.gesionjardin.jardinier.JardinierDetailActivity;
import com.example.gesionjardin.model.Utilisateur;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JardinierListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private LinearLayout layoutEmpty;
    private List<Utilisateur> listJardiniers = new ArrayList<>();
    private JardinierAdapter adapter;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jardinier_list);

        recycler     = findViewById(R.id.recycler_jardiniers);
        layoutEmpty  = findViewById(R.id.layout_empty);
        usersRef     = FirebaseDatabase.getInstance().getReference("users");

        // Setup RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JardinierAdapter(listJardiniers, user -> {
            // Au clic sur un item, ouvrir le détail
            Intent intent = new Intent(this, JardinierDetailActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        loadJardiniers();
    }

    private void loadJardiniers() {
        usersRef.orderByChild("role").equalTo("jardinier")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        listJardiniers.clear();
                        for (DataSnapshot child : snap.getChildren()) {
                            Utilisateur u = child.getValue(Utilisateur.class);
                            if (u != null) listJardiniers.add(u);
                        }
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(listJardiniers.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        // Gérer l'erreur si besoin
                    }
                });
    }
}
