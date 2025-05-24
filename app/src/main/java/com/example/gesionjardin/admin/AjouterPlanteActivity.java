package com.example.gesionjardin.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Intervalle;
import com.example.gesionjardin.model.Plante;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AjouterPlanteActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = "AjouterPlante";

    private ImageView ivPreview;
    private MaterialButton btnSave;
    private Button btnSelectImage;

    private TextInputEditText etNom, etDescription,
            etTempMin, etTempMax,
            etHumidMin, etHumidMax,
            etHumidSolMin, etHumidSolMax;

    private Uri selectedImageUri;
    private OkHttpClient httpClient;
    private DatabaseReference plantesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_plante);

        ivPreview      = findViewById(R.id.iv_preview_plante);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSave        = findViewById(R.id.btn_save_plante);

        etNom          = findViewById(R.id.et_nom_plante);
        etDescription  = findViewById(R.id.et_description_plante);
        etTempMin      = findViewById(R.id.et_temp_min);
        etTempMax      = findViewById(R.id.et_temp_max);
        etHumidMin     = findViewById(R.id.et_humid_min);
        etHumidMax     = findViewById(R.id.et_humid_max);
        etHumidSolMin  = findViewById(R.id.et_humid_sol_min);
        etHumidSolMax  = findViewById(R.id.et_humid_sol_max);

        httpClient = new OkHttpClient();
        plantesRef = FirebaseDatabase.getInstance()
                .getReference("plantes");

        btnSelectImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pick.setType("image/*");
            startActivityForResult(pick, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> {
            if (validateForm() && selectedImageUri != null) {
                uploadToCloudinary(selectedImageUri);
            } else {
                Toast.makeText(this,
                        "Remplis tous les champs et choisis une image",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            selectedImageUri = data.getData();
            ivPreview.setImageURI(selectedImageUri);
        }
    }

    private boolean validateForm() {
        return !etNom.getText().toString().trim().isEmpty()
                && !etDescription.getText().toString().trim().isEmpty()
                && !etTempMin.getText().toString().trim().isEmpty()
                && !etTempMax.getText().toString().trim().isEmpty()
                && !etHumidMin.getText().toString().trim().isEmpty()
                && !etHumidMax.getText().toString().trim().isEmpty()
                && !etHumidSolMin.getText().toString().trim().isEmpty()
                && !etHumidSolMax.getText().toString().trim().isEmpty();
    }

    private void uploadToCloudinary(Uri imageUri) {
        try {
            String cloudName    = "dhucobhzv";
            String uploadPreset = "ml_default";
            String url          = "https://api.cloudinary.com/v1_1/"
                    + cloudName + "/image/upload";

            InputStream is = getContentResolver().openInputStream(imageUri);
            byte[] bytes   = toBytes(is);

            RequestBody fileBody = RequestBody
                    .create(bytes, MediaType.parse("image/*"));
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "upload.jpg", fileBody)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload error", e);
                    runOnUiThread(() ->
                            Toast.makeText(AjouterPlanteActivity.this,
                                    "Échec upload image", Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        try {
                            String imageUrl = new JSONObject(body)
                                    .getString("secure_url");
                            runOnUiThread(() -> savePlante(imageUrl));
                        } catch (JSONException je) {
                            Log.e(TAG, "JSON parse error", je);
                        }
                    } else {
                        Log.e(TAG, "Upload failed: " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "uploadToCloudinary", e);
        }
    }

    private void savePlante(String imageUrl) {
        String id = plantesRef.push().getKey();
        Plante p = new Plante();
        p.setId(id);
        p.setNom(etNom.getText().toString().trim());
        p.setDescription(etDescription.getText().toString().trim());
        p.setImageUrl(imageUrl);
        p.setTempMinMax(new Intervalle(
                Double.parseDouble(etTempMin.getText().toString().trim()),
                Double.parseDouble(etTempMax.getText().toString().trim())
        ));
        p.setHumidAirMinMax(new Intervalle(
                Double.parseDouble(etHumidMin.getText().toString().trim()),
                Double.parseDouble(etHumidMax.getText().toString().trim())
        ));
        p.setHumidSolMinMax(new Intervalle(
                Double.parseDouble(etHumidSolMin.getText().toString().trim()),
                Double.parseDouble(etHumidSolMax.getText().toString().trim())
        ));

        plantesRef.child(id).setValue(p)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this,
                            "Plante ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Échec enregistrement plante", Toast.LENGTH_SHORT).show();
                });
    }

    private byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int n;
        byte[] data = new byte[1024];
        while ((n = is.read(data)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
