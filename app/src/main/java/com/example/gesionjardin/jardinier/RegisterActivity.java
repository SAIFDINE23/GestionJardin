package com.example.gesionjardin.jardinier;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import com.example.gesionjardin.LoginActivity;
import com.example.gesionjardin.R;
import com.example.gesionjardin.model.Utilisateur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

public class RegisterActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;
    private static final String TAG = "RegisterActivity";

    private ImageView ivPreview;
    private Button btnSelectPhoto, btnRegister;
    private TextView tvLoginLink;
    private TextInputEditText etNom, etPrenom, etTel, etEmail, etPassword;

    private Uri selectedImageUri;
    private OkHttpClient httpClient;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ivPreview      = findViewById(R.id.iv_profile_preview);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        etNom          = findViewById(R.id.et_nom);
        etPrenom       = findViewById(R.id.et_prenom);
        etTel          = findViewById(R.id.et_tel);
        etEmail        = findViewById(R.id.et_email);
        etPassword     = findViewById(R.id.et_password);
        btnRegister    = findViewById(R.id.btn_register_main);
        tvLoginLink    = findViewById(R.id.tv_login_link);

        httpClient = new OkHttpClient();
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnSelectPhoto.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pick.setType("image/*");
            startActivityForResult(pick, PICK_IMAGE);
        });

        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginLink .setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivPreview.setImageURI(selectedImageUri);
        }
    }

    private void registerUser() {
        String nom    = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String tel    = etTel.getText().toString().trim();
        String email  = etEmail.getText().toString().trim();
        String pass   = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nom))   { etNom.setError("Nom requis"); return; }
        if (TextUtils.isEmpty(prenom)){ etPrenom.setError("Prénom requis"); return; }
        if (TextUtils.isEmpty(tel))   { etTel.setError("Téléphone requis"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email requis"); return; }
        if (TextUtils.isEmpty(pass) || pass.length()<6){
            etPassword.setError("6 caractères min"); return;
        }
        if (selectedImageUri==null){
            Toast.makeText(this,"Choisis une photo",Toast.LENGTH_SHORT).show();
            return;
        }

        // D'abord upload image
        uploadPhotoAndCreateUser(nom,prenom,tel,email,pass);
    }

    private void uploadPhotoAndCreateUser(String nom,String prenom,
                                          String tel,String email,
                                          String pass) {
        try {
            String cloudName    = "dhucobhzv";
            String uploadPreset = "ml_default";
            String url          = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

            InputStream is     = getContentResolver().openInputStream(selectedImageUri);
            byte[] bytes       = toBytes(is);
            RequestBody fileRb = RequestBody.create(bytes, MediaType.parse("image/*"));
            MultipartBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "upload.jpg", fileRb)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build();

            Request req = new Request.Builder().url(url).post(body).build();
            httpClient.newCall(req).enqueue(new Callback(){
                @Override public void onFailure(Call c, IOException e){
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this,
                            "Upload photo échoué",Toast.LENGTH_SHORT).show());
                }
                @Override public void onResponse(Call c,Response r)throws IOException {
                    if (!r.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this,
                                "Erreur serveur Cloudinary",Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String resp = r.body().string();
                    try {
                        String imageUrl = new JSONObject(resp).getString("secure_url");
                        runOnUiThread(() -> createAuthAndSave(
                                nom,prenom,tel,email,pass,imageUrl));
                    } catch (Exception ex){
                        Log.e(TAG,"JSON parse",ex);
                    }
                }
            });
        } catch(Exception e){
            Log.e(TAG,"upload",e);
        }
    }

    private void createAuthAndSave(String nom,String prenom,
                                   String tel,String email,
                                   String pass,String imageUrl) {
        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()){
                        Toast.makeText(this,
                                "Auth failed: "+task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    if (fbUser==null) return;
                    String uid = fbUser.getUid();
                    Utilisateur u = new Utilisateur();
                    u.setId(uid);
                    u.setNom(nom);
                    u.setPrenom(prenom);
                    u.setTel(tel);
                    u.setEmail(email);
                    u.setPassword("");
                    u.setImageUrl(imageUrl);
                    u.setRole("jardinier");

                    usersRef.child(uid).setValue(u)
                            .addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()){
                                    Toast.makeText(this,
                                            "Inscription ok",Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(this,LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this,
                                            "DB save failed",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }

    private byte[] toBytes(InputStream is)throws Exception {
        ByteArrayOutputStream buf=new ByteArrayOutputStream();
        int n; byte[] data=new byte[1024];
        while((n=is.read(data))!=-1) buf.write(data,0,n);
        return buf.toByteArray();
    }
}
