package com.cesba.pokeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;


    private Button btnTabLogin, btnTabRegister, btnSubmit;
    private MaterialButton btnGoogle;
    private TextInputLayout tilNombre, tilCorreo, tilPassword;
    private TextInputEditText etNombre, etCorreo, etPassword;
    private CheckBox cbRememberMe;
    private TextView txtForgotPassword, txtToggleMode;
    private RelativeLayout layoutOptions;

    private boolean isLoginMode = true;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    private static final String PREF_NAME = "PokeAppPrefs";
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        configureGoogleSignIn();
        setupListeners();

        // Cargar correo guardado si existe "Recordarme"
        String savedEmail = prefs.getString(KEY_REMEMBER_EMAIL, "");
        if (!savedEmail.isEmpty()) {
            etCorreo.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
    }

    private void initViews() {
        btnTabLogin = findViewById(R.id.btnTabLogin);
        btnTabRegister = findViewById(R.id.btnTabRegister);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoogle = findViewById(R.id.btnGoogle);

        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilPassword = findViewById(R.id.tilPassword);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);

        cbRememberMe = findViewById(R.id.cbRememberMe);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtToggleMode = findViewById(R.id.txtToggleMode);
        layoutOptions = findViewById(R.id.layoutOptions);
    }

    private void setupListeners() {
        btnTabLogin.setOnClickListener(v -> setMode(true));
        btnTabRegister.setOnClickListener(v -> setMode(false));
        txtToggleMode.setOnClickListener(v -> setMode(!isLoginMode));

        btnSubmit.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });

        txtForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this, "Error de Google Sign In: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Guardar datos del usuario en Firestore si es la primera vez
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nombre", user.getDisplayName());
                            userData.put("email", user.getEmail());
                            userData.put("victorias", 0);
                            userData.put("derrotas", 0);

                            // Usamos set con merge para no sobreescribir victorias/derrotas si el usuario ya existía
                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge());
                        }

                        Toast.makeText(AuthActivity.this, "¡Bienvenido con Google!", Toast.LENGTH_SHORT).show();
                        goToMainMenu();
                    } else {
                        Toast.makeText(AuthActivity.this, "Error en Firebase Auth", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setMode(boolean login) {
        isLoginMode = login;
        if (isLoginMode) {
            btnTabLogin.setBackgroundResource(R.drawable.btn_pokemon_console_blue);
            btnTabLogin.setBackgroundTintList(null);
            btnTabLogin.setTextColor(getColor(R.color.white));

            btnTabRegister.setBackgroundResource(R.drawable.btn_pokemon_console_light);
            btnTabRegister.setBackgroundTintList(null);
            btnTabRegister.setTextColor(getColor(R.color.poke_text_title));

            tilNombre.setVisibility(View.GONE);
            layoutOptions.setVisibility(View.VISIBLE);
            btnSubmit.setText("INICIAR SESIÓN");
            txtToggleMode.setText("Crear una cuenta");
        } else {
            btnTabRegister.setBackgroundResource(R.drawable.btn_pokemon_console_blue);
            btnTabRegister.setBackgroundTintList(null);
            btnTabRegister.setTextColor(getColor(R.color.white));

            btnTabLogin.setBackgroundResource(R.drawable.btn_pokemon_console_light);
            btnTabLogin.setBackgroundTintList(null);
            btnTabLogin.setTextColor(getColor(R.color.poke_text_title));

            tilNombre.setVisibility(View.VISIBLE);
            layoutOptions.setVisibility(View.GONE);
            btnSubmit.setText("REGISTRARSE");
            txtToggleMode.setText("¿Ya tienes una cuenta? Inicia Sesión");
        }
    }

    private void performLogin() {
        String email = etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Manejo de "Recordarme"
        if (cbRememberMe.isChecked()) {
            prefs.edit().putString(KEY_REMEMBER_EMAIL, email).apply();
        } else {
            prefs.edit().remove(KEY_REMEMBER_EMAIL).apply();
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "¡Bienvenido a PokéApp V2!", Toast.LENGTH_SHORT).show();
                        goToMainMenu();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error al iniciar sesión";
                        Toast.makeText(AuthActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void performRegister() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String email = etCorreo.getText() != null ? etCorreo.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Guardar datos iniciales en Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nombre", nombre);
                            userData.put("email", email);
                            userData.put("victorias", 0);
                            userData.put("derrotas", 0);

                            db.collection("users").document(user.getUid())
                                    .set(userData);
                        }
                        Toast.makeText(AuthActivity.this, "Registro exitoso. ¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        goToMainMenu();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error al registrarse";
                        Toast.makeText(AuthActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        final TextInputEditText inputEmail = new TextInputEditText(this);
        inputEmail.setHint("Tu correo registrado");

        new AlertDialog.Builder(this)
                .setTitle("Recuperar Contraseña")
                .setMessage("Ingresa tu correo electrónico para enviarte un enlace de restablecimiento:")
                .setView(inputEmail)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
                    if (!email.isEmpty()) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AuthActivity.this, "Enlace enviado a tu correo", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AuthActivity.this, "Error al enviar enlace", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(AuthActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}