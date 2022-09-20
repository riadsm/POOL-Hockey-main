package com.example.inm5151.activityJava.groups.singleGroup.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;

public class User extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        System.out.println(getIntent().getStringExtra("group_name"));
        System.out.println(getIntent().getStringExtra("group_id"));

        startListeners();
    }

    private void startListeners() {
        //Manage
        Button manageButton = findViewById(R.id.user_page_manage);
        manageButton.setOnClickListener(v -> openManage());

        //Players
        Button playersButton = findViewById(R.id.user_page_players);
        playersButton.setOnClickListener(v -> openPlayers());
    }

    private void openManage() {
        Intent intent = new Intent(this, Manage.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }

    private void openPlayers() {
        Intent intent = new Intent(this, UserPlayers.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }
}
