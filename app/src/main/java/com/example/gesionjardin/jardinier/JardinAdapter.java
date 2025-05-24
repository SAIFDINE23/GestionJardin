package com.example.gesionjardin.jardinier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Garden;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JardinAdapter extends RecyclerView.Adapter<JardinAdapter.JardinViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Garden jardin);
    }

    private final Context context;
    private final List<Garden> jardins;
    private final OnItemClickListener listener;

    public JardinAdapter(Context context, List<Garden> jardins, OnItemClickListener listener) {
        this.context  = context;
        this.jardins  = jardins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JardinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_jardin, parent, false);
        return new JardinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull JardinViewHolder holder, int position) {
        holder.bind(jardins.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return jardins.size();
    }

    static class JardinViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final ImageView ivImage;
        private final TextView tvAddress, tvSurface;

        JardinViewHolder(@NonNull View itemView) {
            super(itemView);
            card      = itemView.findViewById(R.id.card_jardin);
            ivImage   = itemView.findViewById(R.id.iv_jardin_image);
            tvAddress = itemView.findViewById(R.id.tv_jardin_address);
            tvSurface = itemView.findViewById(R.id.tv_jardin_surface);
        }

        void bind(Garden jardin, OnItemClickListener listener) {
            tvAddress.setText(jardin.getAddress());
            tvSurface.setText("Surface : " + jardin.getSurface() + " m²");

            if (jardin.getImages() != null && !jardin.getImages().isEmpty()) {
                Picasso.get()
                        .load(jardin.getImages().get(0))
                        .placeholder(R.drawable.ic_garden_placeholder)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_garden_placeholder);
            }

            card.setOnClickListener(v -> listener.onItemClick(jardin));
        }
    }
}
