package com.example.gesionjardin.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Utilisateur;
import com.squareup.picasso.Picasso; // ou Glide

import java.util.List;

public class JardinierAdapter
        extends RecyclerView.Adapter<JardinierAdapter.VH> {

    public interface OnItemClick {
        void onClick(Utilisateur user);
    }

    private final List<Utilisateur> data;
    private final OnItemClick listener;

    public JardinierAdapter(List<Utilisateur> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jardinier, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Utilisateur u = data.get(pos);
        h.tvNom.setText(u.getPrenom() + " " + u.getNom());
        // Charger la photo si imageUrl non null
        if (u.getImageUrl() != null) {
            Picasso.get().load(u.getImageUrl()).into(h.ivPhoto);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView  tvNom;
        VH(View v) {
            super(v);
            ivPhoto = v.findViewById(R.id.iv_jardinier_photo);
            tvNom   = v.findViewById(R.id.tv_jardinier_nom);
        }
    }
}
