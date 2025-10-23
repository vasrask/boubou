package com.vasrask.boubou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vasrask.boubou.entities.User;
import com.vasrask.boubou.utils.LocaleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;
    private Button loginButton;
    private Button registerButton;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.editEmailProfile);
        passwordInput = findViewById(R.id.editPassword);

        loginButton = findViewById(R.id.loginButton);

        registerButton = findViewById(R.id.registerBtn);

        loginButton.setOnClickListener(v -> {
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        handleLogin();
                    });
                });

        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(this,RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError(getString(R.string.email_required));
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError(getString(R.string.password_required));
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        String languageCode = documentSnapshot.getString("language_code");
                        Log.d("LANGUAGE", "language_code " + languageCode);
                        if(languageCode != null) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                            prefs.edit().putString("app_lang", languageCode).apply();
                            Log.d("LANGUAGE", "app_lang" + prefs.getString("app_lang", "none"));
                            LocaleHelper.applySavedLocale(this);
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.invalid_credentials), Toast.LENGTH_SHORT).show();
                    }
                });

            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            } else {
                Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
            }
        });

    }
}