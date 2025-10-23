package com.vasrask.boubou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vasrask.boubou.entities.User;
import com.vasrask.boubou.utils.LocaleHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private AutoCompleteTextView languageDropdown;
    private Button registerButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private String selectedLanguageCode = "en";

    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.editEmailProfile);
        registerButton = findViewById(R.id.registerBtn);
        passwordInput = findViewById(R.id.editPassword);
        usernameInput = findViewById(R.id.editUsernameProfile);
        loginButton = findViewById(R.id.loginButton);

        languageDropdown  = findViewById(R.id.languageDropdown);
        String[] languages = {"English", "Ελληνικά"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, R.layout.dropdown_item, R.id.dropdownItemText, languages);
        languageDropdown.setAdapter(languageAdapter);
        languageDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            if (selected.equals("Ελληνικά")) {
                selectedLanguageCode = "el";
            } else {
                selectedLanguageCode = "en";
            }
            Log.d(TAG, "User selected language: " + selectedLanguageCode);
        });

        registerButton.setOnClickListener(v -> {
            handleRegister();
        });

        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    private void handleRegister() {
         String username = Objects.requireNonNull(usernameInput.getText()).toString().trim();
         String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
         String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

         if (username.isEmpty()) {
             usernameInput.setError(getString(R.string.username_required));
             return;
         }
         if (email.isEmpty()) {
             emailInput.setError(getString(R.string.email_required));
             return;
         }
         if (password.isEmpty()) {
             passwordInput.setError(getString(R.string.password_required));
             return;
         }
         if (password.length() < 5) {
             passwordInput.setError(getString(R.string.password_restriction));
             return;
         }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                if (firebaseUser != null) {

                    User user = new User(firebaseUser.getUid(), username, firebaseUser.getEmail(), selectedLanguageCode);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("users").document(firebaseUser.getUid()).set(user).addOnSuccessListener(v -> {
                        Log.d(TAG, "User was added to firebase");
                        Toast.makeText(this, getString(R.string.user_created), Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        prefs.edit().putString("app_lang", selectedLanguageCode).apply();

                        LocaleHelper.applySavedLocale(this);

                        startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                        finish();
                    }).addOnFailureListener(v -> {
                        Log.d(TAG, "Failed to create user");
                        Toast.makeText(this, getString(R.string.user_not_created), Toast.LENGTH_SHORT).show();
                    });

                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.invalid_user), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }
}