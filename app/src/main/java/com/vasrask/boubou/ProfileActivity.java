package com.vasrask.boubou;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.vasrask.boubou.views.UserViewModel;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private TextInputEditText usernameEdit;
    private TextInputEditText emailEditText;
    private MaterialButton updateProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        // Back arrow
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        usernameEdit = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        updateProfileButton = findViewById(R.id.saveProfileButton);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        userViewModel.getUsername().observe(this, username -> {
            if (username != null) usernameEdit.setText(username);
        });
        userViewModel.getEmail().observe(this, email -> {
            if (email != null) emailEditText.setText(email);
        });
    }

    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> {
            String newUsername = usernameEdit.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                userViewModel.updateUsername(newUsername);
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
            } else {
                usernameEdit.setError("Username cannot be empty");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
