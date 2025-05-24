package com.example.gesionjardin.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Plante;
import com.bumptech.glide.Glide;

import java.util.List;

public class PlanteAdapter
        extends RecyclerView.Adapter<PlanteAdapter.VH> {

    public interface OnItemClick {
        void onClick(Plante plante);
    }

    private final List<Plante> data;
    private final OnItemClick listener;

    public PlanteAdapter(List<Plante> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plante, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Plante p = data.get(pos);
        h.tvNom.setText(p.getNom());
        h.tvDesc.setText(p.getDescription());

        // charger image (placeholder si null)
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(p.getImageUrl())
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .centerCrop()
                    .into(h.ivImage);
        } else {
            h.ivImage.setImageResource(R.drawable.ic_plant_placeholder);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvNom, tvDesc;
        VH(View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_plante_image);
            tvNom   = v.findViewById(R.id.tv_plante_nom);
            tvDesc  = v.findViewById(R.id.tv_plante_description);
        }
    }
}
