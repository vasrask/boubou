package com.vasrask.boubou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.vasrask.boubou.R;
import com.vasrask.boubou.views.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextInputEditText usernameEdit;
    private TextInputEditText emailEditText;
    private MaterialButton updateProfileButton;
    private final String TAG = "ProfileFragment";

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameEdit = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        updateProfileButton = view.findViewById(R.id.saveProfileButton);

        setupObservers();
        setupListeners();

        return view;
    }


    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> {
            String newUsername = usernameEdit.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                userViewModel.updateUsername(newUsername);
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show();
            } else {
                usernameEdit.setError("Username cannot be empty");
            }
        });
    }

    private void setupObservers() {
        userViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                usernameEdit.setText(username);
            }
        });

        userViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) {
                emailEditText.setText(email);
            }
        });

        userViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getBabyActivitiesCount().observe(getViewLifecycleOwner(), count -> {
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        userViewModel.getProfile();
    }
}
