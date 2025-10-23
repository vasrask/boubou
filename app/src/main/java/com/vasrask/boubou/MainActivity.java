package com.vasrask.boubou;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.vasrask.boubou.utils.LocaleHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.applySavedLocale(this);
        setContentView(R.layout.activity_main);
    }
}