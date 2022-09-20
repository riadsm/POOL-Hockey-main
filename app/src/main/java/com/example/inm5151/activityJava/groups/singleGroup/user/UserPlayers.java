package com.example.inm5151.activityJava.groups.singleGroup.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;

public class UserPlayers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_players);

        System.out.println(getIntent().getStringExtra("group_name"));
        System.out.println(getIntent().getStringExtra("group_id"));
    }
}
