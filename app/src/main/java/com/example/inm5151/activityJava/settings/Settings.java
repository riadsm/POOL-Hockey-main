package com.example.inm5151.activityJava.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        startListeners();
    }

    private void startListeners() {
        //General
        Button generalButton = findViewById(R.id.settings_general);
        generalButton.setOnClickListener(v -> openGeneralSettings());

        //Account
        Button accountButton = findViewById(R.id.settings_account);
        accountButton.setOnClickListener(v -> openAccountSettings());
    }

    private void openGeneralSettings() {
        System.out.println("General settings");
        Intent intent = new Intent(this, GeneralSettings.class);
        startActivity(intent);
    }

    private void openAccountSettings() {
        System.out.println("Account settings");
        Intent intent = new Intent(this, AccountSettings.class);
        startActivity(intent);
    }
}
