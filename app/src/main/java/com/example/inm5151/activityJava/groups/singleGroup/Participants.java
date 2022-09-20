package com.example.inm5151.activityJava.groups.singleGroup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.activityJava.groups.GroupListPage;
import com.example.inm5151.activityJava.groups.GroupPage;
import com.example.inm5151.activityJava.groups.singleGroup.user.SingleParticipant;
import com.example.inm5151.activityJava.groups.singleGroup.user.User;
import com.example.inm5151.retrofit.GroupResult;
import com.example.inm5151.retrofit.RetrofitInterface;
import com.example.inm5151.user.UserInformation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Participants extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        String groupName = getIntent().getStringExtra("group_name");
        String groupId = getIntent().getStringExtra("group_id");

        startListeners();
        setPageTitle(groupName);
        listParticipants(groupId);
    }

    private void setPageTitle(String groupName) {
        TextView textView = findViewById(R.id.participants_title);
        textView.setText(groupName);
    }

    private void startListeners() {
        //Invite
        ImageButton inviteButton = findViewById(R.id.add_participant_button);
        inviteButton.setOnClickListener(v -> openInvite());
    }

    private void openInvite() {
        Intent intent = new Intent(this, InviteParticipant.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        startActivity(intent);
    }

    private void showParticipants(String participants) throws ParseException{
        JSONParser parser = new JSONParser();
        JSONObject parsedParticipants = (JSONObject) parser.parse(participants);
        JSONArray arrayParticipants = (JSONArray) parsedParticipants.get("Participants");

        for (int i = 0; i < Objects.requireNonNull(arrayParticipants).size(); i++) {
            System.out.println(arrayParticipants.get(i));
            createButton(i, arrayParticipants.get(i).toString());
        }
    }

    private void openParticipantPage(String participant) {
        Intent intent = new Intent(this, SingleParticipant.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        intent.putExtra("participant_name", participant);
        startActivity(intent);
    }

    private void createButton(int i, String participant) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.participants_scroll_view);

        Button group_button = new Button(this);
        group_button.setText(Objects.requireNonNull(participant));
        group_button.setId(i);
        group_button.setWidth(button_width);
        group_button.setBackgroundResource(R.drawable.group_button_style);
        group_button.setTextColor(Color.parseColor("#000000"));

        //Set the margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 50, 0, 0);
        group_button.setLayoutParams(params);

        //Set listener for each button
        group_button.setOnClickListener(v -> openParticipantPage(participant));

        //add button to the layout
        layout.addView(group_button);
    }

    private void listParticipants(String groupId) {
        System.out.println("Fetching participants");
        HashMap<String, String> map = new HashMap<>();

        map.put("group_id", groupId);

        Call<GroupResult> call = retrofitInterface.executeGetParticipants(map);

        call.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    String participants = response.body().getGroups();

                    try {
                        showParticipants(participants);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                Toast.makeText(Participants.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
