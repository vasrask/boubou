package com.vasrask.boubou;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.vasrask.boubou.utils.LocaleHelper;
import com.vasrask.boubou.views.UserViewModel;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private Spinner languageSpinner;
    private Switch notificationsSwitch;
    private TextView appVersionText;
    private String selectedLanguageCode;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applySavedLocale(this);

        setContentView(R.layout.activity_settings);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        languageSpinner = findViewById(R.id.languageSpinner);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        appVersionText = findViewById(R.id.appVersionText);

        mAuth = FirebaseAuth.getInstance();

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupLanguageSpinner();
        setupNotificationsSwitch();
        displayAppVersion();
    }

    private void setupLanguageSpinner() {
        String[] languages = {"English", "Ελληνικά"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String savedCode = prefs.getString("app_lang", "en");
        int savedIndex = savedCode.equals("en") ? 0 : 1;
        languageSpinner.setSelection(savedIndex);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (selected.equals("Ελληνικά")) {
                    selectedLanguageCode = "el";
                } else {
                    selectedLanguageCode = "en";
                }
                if (!selectedLanguageCode.equals(prefs.getString("app_lang", "en"))) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("app_lang", selectedLanguageCode);
                    editor.apply();

                    LocaleHelper.applySavedLocale(SettingsActivity.this);
                    recreate();
                    userViewModel.updateLanguage(selectedLanguageCode);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void setupNotificationsSwitch() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("notifications_enabled", true);
        notificationsSwitch.setChecked(enabled);

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
    }


    private void displayAppVersion() {
        try {
            String version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            appVersionText.setText("App version " + version);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
