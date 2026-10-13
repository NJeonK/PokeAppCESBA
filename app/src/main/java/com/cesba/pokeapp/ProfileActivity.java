package com.cesba.pokeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBackProfile, imgProfile;
    private TextInputEditText etProfileName;
    private MaterialButton btnSaveProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("PokeAppPrefs", MODE_PRIVATE);

        btnBackProfile = findViewById(R.id.btnBackProfile);
        imgProfile = findViewById(R.id.imgProfile);
        etProfileName = findViewById(R.id.etProfileName);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBackProfile.setOnClickListener(v -> finish());

        imgProfile.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadProfileData();
    }

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(imgProfile);
                }
            }
    );

    private void loadProfileData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("nombre")) {
                            etProfileName.setText(doc.getString("nombre"));
                        } else {
                            etProfileName.setText(user.getDisplayName());
                        }
                    });

            String savedUri = prefs.getString("profile_image_" + user.getUid(), null);
            if (savedUri != null) {
                selectedImageUri = Uri.parse(savedUri);
                Glide.with(this).load(selectedImageUri).circleCrop().into(imgProfile);
            }
        }
    }

    private void saveProfile() {
        String newName = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("nombre", newName)
                    .addOnSuccessListener(aVoid -> {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();
                        user.updateProfile(profileUpdates);

                        if (selectedImageUri != null) {
                            prefs.edit().putString("profile_image_" + user.getUid(), selectedImageUri.toString()).apply();
                        }

                        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}