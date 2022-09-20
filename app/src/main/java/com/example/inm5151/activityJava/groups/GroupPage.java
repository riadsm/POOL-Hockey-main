package com.example.inm5151.activityJava.groups;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.activityJava.groups.singleGroup.Participants;
import com.example.inm5151.activityJava.groups.singleGroup.AvailablePlayers;
import com.example.inm5151.activityJava.groups.singleGroup.Ranking;

public class GroupPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);

        System.out.println(getIntent().getStringExtra("group_name"));
        System.out.println(getIntent().getStringExtra("group_id"));

        startListeners();
    }

    private void startListeners() {
        //Participants
        Button participantsButton = findViewById(R.id.group_participants);
        participantsButton.setOnClickListener(v -> openParticipants());

        //Ranking
        Button rankingButton = findViewById(R.id.group_ranking);
        rankingButton.setOnClickListener(v -> openRanking());

        //Players
        Button playersButton = findViewById(R.id.group_players);
        playersButton.setOnClickListener(v -> openPlayers());
    }

    private void openParticipants() {
        Intent intent = new Intent(this, Participants.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }

    private void openRanking() {
        Intent intent = new Intent(this, Ranking.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }

    private void openPlayers() {
        Intent intent = new Intent(this, AvailablePlayers.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }
}
