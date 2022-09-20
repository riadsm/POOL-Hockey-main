package com.example.inm5151.activityJava.groups.singleGroup;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
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

@RequiresApi(api = Build.VERSION_CODES.M)
public class AvailablePlayers extends AppCompatActivity {

    private static final int MAX_PLAYERS_SIZE = 30;

    private boolean searchFilled;

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchFilled = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_players);

        System.out.println(getIntent().getStringExtra("group_name"));
        System.out.println(getIntent().getStringExtra("group_id"));

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
        sendFirstPlayersRequest();
    }

    //Refresh page on search, get refreshpage() animation from invitations.java
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void sendFirstPlayersRequest() {
        Call<PlayersResult> call = retrofitInterface.executeGetFirstPlayers();

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    try {
                        showPlayers(response.body().getResult());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(AvailablePlayers.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPlayers(String groups) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_players = (JSONObject) parser.parse(groups);
        JSONArray array_players = (JSONArray) parsed_players.get("Players");
        int size = Objects.requireNonNull(array_players).size();

        for (int i = 0; i < Math.min(size, MAX_PLAYERS_SIZE); i++) {
            System.out.println(array_players.get(i));
            createButton(i, (JSONObject) array_players.get(i));
        }
    }

    private void refreshPlayers(String groups) throws ParseException {
        clearButtons();

        JSONParser parser = new JSONParser();
        JSONObject parsed_players = (JSONObject) parser.parse(groups);
        JSONArray array_players = (JSONArray) parsed_players.get("Players");
        int size = Objects.requireNonNull(array_players).size();

        for (int i = 0; i < Math.min(size, MAX_PLAYERS_SIZE); i++) {
            System.out.println(array_players.get(i));
            createButton(i, (JSONObject) array_players.get(i));
        }
    }

    private void clearButtons() {
        if(searchFilled) {
            LinearLayout layout = findViewById(R.id.available_players_view);
            layout.removeAllViews();
        }
    }

    private void createButton(int i, JSONObject player) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.available_players_view);

        Button group_button = new Button(this);
        group_button.setText(Objects.requireNonNull(player.get("name")).toString());
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
        group_button.setOnClickListener(v -> openPlayerPage(player));

        //add button to the layout
        layout.addView(group_button);
    }

    private void showAddPlayerConfirmation(String player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AvailablePlayers.this);

        builder.setTitle("Success!");
        builder.setMessage("The player " + player + " has been added to your list!");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showErrorPopUp(String error){
        AlertDialog.Builder builder = new AlertDialog.Builder(AvailablePlayers.this);

        builder.setTitle("Oops!");
        builder.setMessage(error);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void sendAddPlayerRequest(String player) {
        HashMap<String, String> map = new HashMap<>();

        map.put("user_id", ((UserInformation) this.getApplication()).getId());
        map.put("group_id", getIntent().getStringExtra("group_id"));
        map.put("name", player);

        Call<PlayersResult> call = retrofitInterface.executeAddPlayer(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    showAddPlayerConfirmation(player);
                } else if (response.code() == 201) {
                    showErrorPopUp(response.body().getResult());
                }   else if (response.code() == 202) {
                    showErrorPopUp(response.body().getResult());
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(AvailablePlayers.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPlayerPage(JSONObject player) {
        //Change font for better alignment
        AlertDialog.Builder builder = new AlertDialog.Builder(AvailablePlayers.this);

        builder.setTitle(player.get("name").toString());
        builder.setMessage(formatContent(player));
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Add to my list", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendAddPlayerRequest(player.get("name").toString());
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

    private void startListeners() {
        ImageButton addGroupButton = findViewById(R.id.available_players_button);
        addGroupButton.setOnClickListener(v -> getSearchPlayers());
    }

    private void getSearchPlayers() {
        EditText playerNameText = findViewById(R.id.available_players_text);
        String playerName = playerNameText.getText().toString();

        if(!playerName.equals("")) {
            searchFilled = true;
            sendGetPlayersRequest(playerName);
        } else {
            clearButtons();
            sendFirstPlayersRequest();
            searchFilled = false;
        }
    }

    private void sendGetPlayersRequest(String playerName) {
        HashMap<String, String> map = new HashMap<>();

        map.put("name", playerName);

        Call<PlayersResult> call = retrofitInterface.executeGetPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    try {
                        refreshPlayers(response.body().getResult());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(AvailablePlayers.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
