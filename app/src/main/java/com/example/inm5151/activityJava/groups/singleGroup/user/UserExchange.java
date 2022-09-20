package com.example.inm5151.activityJava.groups.singleGroup.user;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.PlayersResult;
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

public class UserExchange extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_exchange);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        String groupName = getIntent().getStringExtra("group_name");
        String groupId = getIntent().getStringExtra("group_id");
        String participantName = getIntent().getStringExtra("participant_name");
        String playerName = getIntent().getStringExtra("player_name");
        String currentUser = ((UserInformation) this.getApplication()).getUsername();

        listTradablePlayers(groupId, currentUser);
    }

    private void listTradablePlayers(String groupId, String currentUser) {
        HashMap<String, String> map = new HashMap<>();

        map.put("group_id", groupId);
        map.put("participant_name", currentUser);

        Call<PlayersResult> call = retrofitInterface.executeGetUserTradablePlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    String players = response.body().getResult();
                    System.out.println(players);

                    try {
                        showPlayers(players);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(UserExchange.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPlayers(String players) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsedPlayers = (JSONObject) parser.parse(players);
        JSONArray arrayPlayers = (JSONArray) parsedPlayers.get("Players");

        for (int i = 0; i < Objects.requireNonNull(arrayPlayers).size(); i++) {
            System.out.println(arrayPlayers.get(i));
            createButton(i, arrayPlayers.get(i).toString());
        }
    }

    private void createButton(int i, String player) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.exchange_scroll_view);

        Button group_button = new Button(this);
        group_button.setText(Objects.requireNonNull(player));
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
        group_button.setOnClickListener(v -> showConfirmationMessage(player));

        //add button to the layout
        layout.addView(group_button);
    }

    private void showConfirmationMessage(String playerName) {
        String otherParticipant = getIntent().getStringExtra("participant_name");
        String otherPlayer = getIntent().getStringExtra("player_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(UserExchange.this);
        builder.setTitle("Exchange");
        builder.setMessage("Are you sure you want to trade your player " + playerName + " for " +
                otherParticipant + "'s player " + otherPlayer + " ?");

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendExchangeRequest(playerName);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void sendExchangeRequest(String sourcePlayerName) {
        String currentUser = ((UserInformation) this.getApplication()).getUsername();
        String otherParticipant = getIntent().getStringExtra("participant_name");
        String otherPlayer = getIntent().getStringExtra("player_name");
        String groupId = getIntent().getStringExtra("group_id");
        String groupName = getIntent().getStringExtra("group_name");

        HashMap<String, String> map = new HashMap<>();

        map.put("source_user", currentUser);
        map.put("target_user", otherParticipant);
        map.put("group_id", groupId);
        map.put("group_name", groupName);
        map.put("source_player_name", sourcePlayerName);
        map.put("target_player_name", otherPlayer);

        Call<PlayersResult> call = retrofitInterface.executeExchangePlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    showExchangeConfirmation();
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(UserExchange.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showExchangeConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserExchange.this);
        builder.setTitle("Success!");
        builder.setMessage("The exchange request was sent.");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
