package com.example.gesionjardin.jardinier;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Garden;
import com.example.gesionjardin.model.InstancePlante;
import com.example.gesionjardin.model.Plante;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AjouterJardinActivity extends AppCompatActivity {
    private static final int PICK_IMAGES = 123;
    private static final String TAG = "AjouterJardin";

    private EditText etAdresse, etSurface;
    private Button btnAjouterImages, btnAjouterPlante, btnEnregistrer;
    private RecyclerView rvImages, rvPlantes;

    private ImagesAdapter imagesAdapter;
    private PlantInstanceAdapter plantAdapter;

    private final List<Uri>    imageUris  = new ArrayList<>();
    private final List<String> imageUrls  = new ArrayList<>();

    private OkHttpClient httpClient;
    private DatabaseReference gardensRef;
    private String ownerId;

    private final List<String> speciesIds   = new ArrayList<>();
    private final List<String> speciesNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_jardin);

        // Initialisation vues
        etAdresse        = findViewById(R.id.etAdresse);
        etSurface        = findViewById(R.id.etSurface);
        btnAjouterImages = findViewById(R.id.btnAjouterImages);
        btnAjouterPlante = findViewById(R.id.btnAjouterPlante);
        btnEnregistrer   = findViewById(R.id.btnEnregistrerJardin);
        rvImages         = findViewById(R.id.rvImages);
        rvPlantes        = findViewById(R.id.rvPlantes);

        httpClient = new OkHttpClient();
        gardensRef = FirebaseDatabase.getInstance().getReference("gardens");
        ownerId    = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1) Charger les espèces pour le spinner
        DatabaseReference plantesRef = FirebaseDatabase.getInstance()
                .getReference("plantes");
        plantesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                for (DataSnapshot ch : snap.getChildren()) {
                    Plante p = ch.getValue(Plante.class);
                    if (p != null) {
                        speciesIds  .add(p.getId());
                        speciesNames.add(p.getNom());
                    }
                }
                if (plantAdapter != null) {
                    plantAdapter.setSpecies(speciesIds, speciesNames);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {
                Log.e(TAG, "Échec chargement espèces", e.toException());
            }
        });

        // 2) Configuration du RecyclerView images
        imagesAdapter = new ImagesAdapter(imageUris);
        rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imagesAdapter);

        // 3) Configuration du RecyclerView plantes
        plantAdapter = new PlantInstanceAdapter();
        rvPlantes.setLayoutManager(new LinearLayoutManager(this));
        rvPlantes.setAdapter(plantAdapter);

        // 4) Listeners des boutons
        btnAjouterImages.setOnClickListener(v -> selectImages());
        btnAjouterPlante.setOnClickListener(v -> {
            plantAdapter.addEmpty();
            Toast.makeText(this,
                    "Instances maintenant : " + plantAdapter.getItemCount(),
                    Toast.LENGTH_SHORT).show();
        });
        btnEnregistrer.setOnClickListener(v -> onSaveGarden());
    }

    /** Lance le picker de documents pour plusieurs images */
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Choisir des images"),
                PICK_IMAGES
        );
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGES && res == RESULT_OK && data != null) {
            ClipData clip = data.getClipData();
            if (clip != null) {
                for (int i = 0; i < clip.getItemCount(); i++) {
                    imageUris.add(clip.getItemAt(i).getUri());
                }
            } else {
                Uri uri = data.getData();
                if (uri != null) imageUris.add(uri);
            }
            imagesAdapter.notifyDataSetChanged();
        }
    }

    private void onSaveGarden() {
        String addr = etAdresse.getText().toString().trim();
        String surf = etSurface.getText().toString().trim();
        if (addr.isEmpty() || surf.isEmpty()
                || imageUris.isEmpty()
                || plantAdapter.getItemCount() == 0) {
            Toast.makeText(this,
                    "Remplis tous les champs, images et plantes",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        imageUrls.clear();
        uploadImage(0);
    }

    /** Envoie séquentiellement toutes les images à Cloudinary */
    private void uploadImage(int idx) {
        if (idx >= imageUris.size()) {
            saveGarden();
            return;
        }
        Uri uri = imageUris.get(idx);
        try {
            InputStream is   = getContentResolver().openInputStream(uri);
            byte[] bytes     = toBytes(is);
            String endpoint  = "https://api.cloudinary.com/v1_1/dhucobhzv/image/upload";

            RequestBody fileRb = RequestBody.create(bytes,
                    MediaType.parse("image/*"));
            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file","upload.jpg",fileRb)
                    .addFormDataPart("upload_preset","ml_default")
                    .build();
            Request req = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build();

            httpClient.newCall(req).enqueue(new Callback(){
                @Override public void onFailure(Call c, IOException e){
                    Log.e(TAG,"Upload échoué",e);
                    runOnUiThread(() ->
                            Toast.makeText(AjouterJardinActivity.this,
                                    "Erreur upload image",
                                    Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call c, Response r) throws IOException {
                    if (!r.isSuccessful()) {
                        Log.e(TAG,"Cloudinary KO: " + r.message());
                        return;
                    }
                    String json = r.body().string();
                    try {
                        String link = new JSONObject(json)
                                .getString("secure_url");
                        imageUrls.add(link);
                        uploadImage(idx + 1);
                    } catch(Exception ex) {
                        Log.e(TAG,"Parse JSON",ex);
                    }
                }
            });
        } catch(Exception e) {
            Log.e(TAG,"toBytes",e);
        }
    }

    /** Quand toutes les images sont uploadées, on enregistre le jardin + instances */
    private void saveGarden() {
        String id = gardensRef.push().getKey();
        double surface = Double.parseDouble(
                etSurface.getText().toString().trim());
        Garden g = new Garden();
        g.setId(id);
        g.setOwnerId(ownerId);
        g.setAddress(etAdresse.getText().toString().trim());
        g.setSurface(surface);
        g.setImages(imageUrls);

        List<InstancePlante> insts = plantAdapter.getInstances(id);
        gardensRef.child(id).setValue(g)
                .addOnSuccessListener(a -> {
                    DatabaseReference instRef = FirebaseDatabase
                            .getInstance().getReference("instances").child(id);
                    for (InstancePlante ip : insts) {
                        instRef.child(ip.getId()).setValue(ip);
                    }
                    Toast.makeText(this,
                            "Jardin créé",Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur création jardin",Toast.LENGTH_SHORT).show());
    }

    /** Lit un InputStream en tableau de bytes */
    private byte[] toBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int n; byte[] data = new byte[1024];
        while ((n = is.read(data)) != -1) buf.write(data,0,n);
        return buf.toByteArray();
    }

    // --- Adapter pour les images sélectionnées ---
    private static class ImagesAdapter
            extends RecyclerView.Adapter<ImagesAdapter.VH> {
        private final List<Uri> uris;
        ImagesAdapter(List<Uri> u) { uris = u; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p,int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_image_preview, p, false);
            return new VH(view);
        }

        @Override public void onBindViewHolder(@NonNull VH h,int pos) {
            h.iv.setImageURI(uris.get(pos));
        }

        @Override public int getItemCount() {
            return uris.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            ImageView iv;
            VH(View v) {
                super(v);
                iv = v.findViewById(R.id.ivImage);
            }
        }
    }

    // --- Adapter pour les instances de plantes ---
    private class PlantInstanceAdapter
            extends RecyclerView.Adapter<PlantInstanceAdapter.VH> {
        private final List<InstanceData> data = new ArrayList<>();
        private ArrayAdapter<String> spinnerAdapter;

        /** Reçoit la liste des espèces chargée depuis Firebase */
        void setSpecies(List<String> ids,List<String> names) {
            spinnerAdapter = new ArrayAdapter<>(
                    AjouterJardinActivity.this,
                    android.R.layout.simple_spinner_item,
                    names);
            spinnerAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            notifyDataSetChanged();
        }

        /** Ajoute une NOUVELLE instance à chaque appel */
        void addEmpty() {
            data.add(new InstanceData());
            notifyItemInserted(data.size()-1);
        }

        /** Construit la liste finale d’InstancePlante */
        List<InstancePlante> getInstances(String gardenId) {
            List<InstancePlante> out = new ArrayList<>();
            for (InstanceData d : data) {
                InstancePlante ip = new InstancePlante();
                ip.setId(UUID.randomUUID().toString());
                ip.setJardinId(gardenId);
                ip.setEspeceId(d.especeId);
                ip.setLibelle(d.libelle);
                ip.setCapteurId(d.capteurId);
                ip.setDatePlantation(System.currentTimeMillis());
                out.add(ip);
            }
            return out;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p,int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_plante_instance, p, false);
            return new VH(view);
        }

        @Override public void onBindViewHolder(@NonNull VH h,int pos) {
            InstanceData d = data.get(pos);
            // Spinner des espèces
            if (spinnerAdapter != null) {
                h.spinner.setAdapter(spinnerAdapter);
                int sel = speciesIds.indexOf(d.especeId);
                if (sel >= 0) h.spinner.setSelection(sel);
                h.spinner.setOnItemSelectedListener(
                        new android.widget.AdapterView.OnItemSelectedListener() {
                            @Override public void onItemSelected(
                                    android.widget.AdapterView<?> a,View v,int i,long l) {
                                d.especeId = speciesIds.get(i);
                            }
                            @Override public void onNothingSelected(
                                    android.widget.AdapterView<?> a) {}
                        });
            }
            // Libellé
            h.etLibelle.setText(d.libelle);
            h.etLibelle.addTextChangedListener(new TextWatcher(){
                @Override public void beforeTextChanged(
                        CharSequence s,int st,int b,int c){}
                @Override public void onTextChanged(
                        CharSequence s,int st,int b,int c){}
                @Override public void afterTextChanged(Editable s){
                    d.libelle = s.toString();
                }
            });
            // Suppression d’instance
            h.btnDelete.setOnClickListener(x -> {
                data.remove(pos);
                notifyItemRemoved(pos);
            });
        }

        @Override public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            Spinner   spinner;
            EditText  etLibelle;
            ImageView btnDelete;
            VH(View v) {
                super(v);
                spinner   = v.findViewById(R.id.spinnerEspece);
                etLibelle = v.findViewById(R.id.etLibelle);
                btnDelete = v.findViewById(R.id.btnSupprimerPlante);
            }
        }

        class InstanceData {
            String especeId = "";
            String libelle  = "";
            String capteurId= "";
        }
    }
}
