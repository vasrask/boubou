package com.vasrask.boubou;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.FeedingType;
import com.vasrask.boubou.utils.LocaleHelper;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "DashboardActivity";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applySavedLocale(this);
        BabyActivityType.init(this);
        FeedingType.init(this);
        setContentView(R.layout.activity_dashboard);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        NavController navController = navHostFragment.getNavController();

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        return true;
    }

    private void logoutUser() {
        mAuth = FirebaseAuth.getInstance();

        mAuth.signOut();
        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logoutUser();
            return true;
        }
        return false;
    }

}