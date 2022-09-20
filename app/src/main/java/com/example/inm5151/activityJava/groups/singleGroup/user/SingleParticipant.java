package com.example.inm5151.activityJava.groups.singleGroup.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class SingleParticipant extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_participant);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        String groupName = getIntent().getStringExtra("group_name");
        String groupId = getIntent().getStringExtra("group_id");
        String participantName = getIntent().getStringExtra("participant_name");

        showConfirmationButton(participantName, groupId);
        setPageTitle(participantName);
        listPlayers(participantName, groupId);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void showConfirmationMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleParticipant.this);

        builder.setTitle("Success!");
        builder.setMessage("Your players have been successfully set!");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void confirmPlayers(String participant, String groupId) {
        //Check for maximum players
        HashMap<String, String> map = new HashMap<>();

        map.put("participant_name", participant);
        map.put("group_id", groupId);

        Call<PlayersResult> call = retrofitInterface.executeConfirmPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    showConfirmationMessage();
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(SingleParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConfirmationButton(String participant, String groupId) {
        String currentParticipant = ((UserInformation) this.getApplication()).getUsername();
        Button confirmButton = findViewById(R.id.single_participant_confirm);

        if(!currentParticipant.equals(participant)) {
            confirmButton.setVisibility(View.INVISIBLE);
        } else {
            confirmButton.setOnClickListener(v -> confirmPlayers(participant, groupId));
        }
    }

    private void setPageTitle(String participantName) {
        TextView textView = findViewById(R.id.single_participant_title);
        textView.setText(participantName);
    }

    private void showCannotDeleteMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleParticipant.this);

        builder.setTitle("Error");
        builder.setMessage("You cannot delete players when you have already confirmed your selection.\n" +
                "Speak with the group administrator to resolve the problem.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void sendDeletePlayerRequest(String player, String userId, String groupId) {
        HashMap<String, String> map = new HashMap<>();

        map.put("player_name", player);
        map.put("group_id", groupId);
        map.put("user_id", userId);

        Call<PlayersResult> call = retrofitInterface.executeDeletePlayer(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    String players = response.body().getResult();
                    System.out.println("Player deleted");

                    finish();
                    refreshPage();
                } else if (response.code() == 201) {
                    showCannotDeleteMessage();
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(SingleParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshPage() {
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void showDeleteConfirmation(String player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SingleParticipant.this);
        String userId = ((UserInformation) this.getApplication()).getId();
        String groupId = getIntent().getStringExtra("group_id");

        builder.setTitle("Delete " + player);
        builder.setMessage("Are you sure you want to delete " + player + " from your list?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeletePlayerRequest(player, userId, groupId);
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private String formatContent(JSONObject player) {
        String minutes = player.get("time_on_ice").toString().replaceAll(":.{2}$", "");
        return "Team:                 " + player.get("team_name").toString() + "\n" +
                "Jersey number: " + player.get("jersey_number").toString() + "\n" +
                "Position:             " + player.get("position").toString() + "\n" +
                "Time on ice:       " + minutes + " minutes\n" +
                "Games played:   " + player.get("games").toString() + "\n" +
                "Goals:                  " + player.get("goals").toString() + "\n" +
                "Assists:               " + player.get("assists").toString() + "\n" +
                "Shots:                  " + player.get("shots").toString() + "\n";
    }

    private void openExchangePage(String playerName, String currentParticipant) {
        Intent intent = new Intent(this, UserExchange.class);
        intent.putExtra("group_name", getIntent().getStringExtra("group_name"));
        intent.putExtra("group_id", getIntent().getStringExtra("group_id"));
        intent.putExtra("player_name", playerName);
        intent.putExtra("participant_name", currentParticipant);
        startActivity(intent);
    }

    private void addTradeButton(AlertDialog.Builder builder, String currentParticipant, String playerName) {
        HashMap<String, String> map = new HashMap<>();

        map.put("participant_name", currentParticipant);
        map.put("player_name", playerName);
        map.put("group_id", getIntent().getStringExtra("group_id"));

        Call<PlayersResult> call = retrofitInterface.executeTradablePlayer(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    System.out.println("Creating button");
                    builder.setPositiveButton("Exchange", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openExchangePage(playerName, currentParticipant);

                            dialog.dismiss();
                        }
                    });

                    builder.show();
                } else if (response.code() == 400) {
                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(SingleParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPlayerPage(String player) throws ParseException{
        JSONParser parser = new JSONParser();
        JSONObject parsed_players = (JSONObject) parser.parse(player);
        JSONArray array_players = (JSONArray) parsed_players.get("Players");
        JSONObject playerObject = (JSONObject) array_players.get(0);

        String playerName = playerObject.get("name").toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(SingleParticipant.this);
        builder.setTitle(playerName);
        builder.setMessage(formatContent(playerObject));
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        String currentUser = ((UserInformation) this.getApplication()).getUsername();
        String currentParticipant = getIntent().getStringExtra("participant_name");

        if(currentParticipant.equals(currentUser)) {
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showDeleteConfirmation(playerName);
                    dialog.dismiss();
                }
            });

            builder.show();
        } else {
            addTradeButton(builder, currentParticipant, playerName);
        }
    }

    private void sendGetPlayerInfoRequest(String player) {
        HashMap<String, String> map = new HashMap<>();

        map.put("name", player);

        Call<PlayersResult> call = retrofitInterface.executeGetPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    try {
                        openPlayerPage(response.body().getResult());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(SingleParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createButton(int i, String player) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.single_participant_scroll_view);

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
        group_button.setOnClickListener(v -> sendGetPlayerInfoRequest(player));

        //add button to the layout
        layout.addView(group_button);
    }

    private void showPlayers(String players) throws ParseException{
        JSONParser parser = new JSONParser();
        JSONObject parsedPlayers = (JSONObject) parser.parse(players);
        JSONArray arrayPlayers = (JSONArray) parsedPlayers.get("Players");

        for (int i = 0; i < Objects.requireNonNull(arrayPlayers).size(); i++) {
            System.out.println(arrayPlayers.get(i));
            createButton(i, arrayPlayers.get(i).toString());
        }
    }

    private void listPlayers(String participantName, String groupId) {
        System.out.println("Fetching players from" + participantName);
        HashMap<String, String> map = new HashMap<>();

        map.put("group_id", groupId);
        map.put("participant_name", participantName);

        Call<PlayersResult> call = retrofitInterface.executeGetParticipantPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    String players = response.body().getResult();

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
                Toast.makeText(SingleParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
