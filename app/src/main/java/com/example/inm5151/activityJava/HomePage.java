package com.example.inm5151.activityJava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.activityJava.groups.GroupListPage;
import com.example.inm5151.activityJava.news.News;
import com.example.inm5151.activityJava.settings.Settings;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        startListeners();
    }

    private void startListeners() {
        //Settings
        Button settingsButton = findViewById(R.id.homepage_settings);
        settingsButton.setOnClickListener(v -> openSettings());

        //Groups
        Button groupsButton = findViewById(R.id.homepage_groups);
        groupsButton.setOnClickListener(v -> openGroups());

        //Invitations
        Button invitationsButton = findViewById(R.id.homepage_invitations);
        invitationsButton.setOnClickListener(v -> openInvitations());

        //News
        Button newsButton = findViewById(R.id.homepage_news);
        newsButton.setOnClickListener(v -> openNews());
    }

    private void openSettings() {
        System.out.println("Settings");
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void openGroups() {
        System.out.println("Groups");
        Intent intent = new Intent(this, GroupListPage.class);
        startActivity(intent);
    }

    private void openInvitations() {
        System.out.println("Invitations");
        Intent intent = new Intent(this, Invitations.class);
        startActivity(intent);
    }

    private void openNews() {
        System.out.println("News");
        Intent intent = new Intent(this, News.class);
        startActivity(intent);
    }
}
