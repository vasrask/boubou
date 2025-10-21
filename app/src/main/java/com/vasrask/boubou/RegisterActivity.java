package com.vasrask.boubou;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vasrask.boubou.entities.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button registerButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.editEmailProfile);
        registerButton = findViewById(R.id.registerBtn);
        passwordInput = findViewById(R.id.editPassword);
        usernameInput = findViewById(R.id.editUsernameProfile);
        loginButton = findViewById(R.id.loginButton);

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
             usernameInput.setError("Username is required");
             return;
         }
         if (email.isEmpty()) {
             emailInput.setError("Email is required");
             return;
         }
         if (password.isEmpty()) {
             passwordInput.setError("Password is required");
             return;
         }
         if (password.length() < 5) {
             passwordInput.setError("Password must be at least 5 characters");
             return;
         }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                FirebaseUser firebaseUser = mAuth.getCurrentUser();

                if (firebaseUser != null) {

                    User user = new User(firebaseUser.getUid(), username, firebaseUser.getEmail());

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("users").document(firebaseUser.getUid()).set(user).addOnSuccessListener(v -> {
                        Log.d(TAG, "User was added to firebase");
                        Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(v -> {
                        Log.d(TAG, "Failed to create user");
                        Toast.makeText(this, "User was not created", Toast.LENGTH_SHORT).show();
                    });

                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Firebase user is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}