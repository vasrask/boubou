package com.vasrask.boubou;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.vasrask.boubou.entities.BabyActivityType;
import com.vasrask.boubou.entities.FeedingType;
import com.vasrask.boubou.utils.LocaleHelper;
import com.vasrask.boubou.views.HomeViewModel;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private HomeViewModel homeViewModel;
    private String currentLanguage;
    private FirebaseAuth mAuth;
    private static final String TAG = "DashboardActivity";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentLanguage = PreferenceManager.getDefaultSharedPreferences(this).getString("app_lang", "en");
        LocaleHelper.applySavedLocale(this);

        BabyActivityType.init(this);
        FeedingType.init(this);
        setContentView(R.layout.activity_dashboard);

        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView username = header.findViewById(R.id.header_name);
        TextView email = header.findViewById(R.id.header_email);

        homeViewModel.getUserProfile().observe(this, user -> {
            username.setText(user.getUsername());
            email.setText(user.getEmail());
        });
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = prefs.getString("app_lang", "en");
        if (!lang.equals(currentLanguage)) {
            currentLanguage = lang;
            LocaleHelper.applySavedLocale(this);
            recreate();
        }
    }
}