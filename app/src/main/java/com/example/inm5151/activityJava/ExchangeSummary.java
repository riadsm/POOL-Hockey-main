package com.example.inm5151.activityJava;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.InvitationResult;
import com.example.inm5151.retrofit.PlayersResult;
import com.example.inm5151.retrofit.RetrofitInterface;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExchangeSummary extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public

    private String sourceParticipantName;
    private JSONObject sourcePlayer;
    private JSONObject targetPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_summary);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
        showTitle();
        getSourcePlayer();
    }

    private void startListeners() {
        Button acceptButton = findViewById(R.id.summary_accept);
        acceptButton.setOnClickListener(v -> showConfirmationAccept());

        Button declineButton = findViewById(R.id.summary_decline);
        declineButton.setOnClickListener(v -> showConfirmationDecline());
    }

    private void showConfirmationAccept() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeSummary.this);
        builder.setTitle("Exchange");
        builder.setMessage("Are you sure you want to proceed with the exchange?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptExchange();
                finish();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
        builder.setCancelable(true);
    }

    private void showConfirmationDecline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ExchangeSummary.this);
        builder.setTitle("Exchange");
        builder.setMessage("Are you sure you want to decline the exchange?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                declineExchange();
                finish();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
        builder.setCancelable(true);
    }

    private void acceptExchange() {
        System.out.println("Accepting invite");
        HashMap<String, String> map = new HashMap<>();

        map.put("invitation_id", getIntent().getStringExtra("invitation_id"));
        map.put("src_user_id", getIntent().getStringExtra("src_user_id"));
        map.put("trg_user_id", getIntent().getStringExtra("trg_user_id"));
        map.put("src_player_name", getIntent().getStringExtra("src_player_name"));
        map.put("trg_player_name", getIntent().getStringExtra("trg_player_name"));
        map.put("group_id", getIntent().getStringExtra("group_id"));

        Call<InvitationResult> call = retrofitInterface.executeAcceptExchange(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    System.out.println("Accepted");
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(ExchangeSummary.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void declineExchange() {
        System.out.println("Declining invite");
        HashMap<String, String> map = new HashMap<>();

        map.put("invitation_id", getIntent().getStringExtra("invitation_id"));
        map.put("src_user_id", getIntent().getStringExtra("src_user_id"));
        map.put("trg_user_id", getIntent().getStringExtra("trg_user_id"));
        map.put("src_player_name", getIntent().getStringExtra("src_player_name"));
        map.put("trg_player_name", getIntent().getStringExtra("trg_player_name"));
        map.put("group_id", getIntent().getStringExtra("group_id"));

        Call<InvitationResult> call = retrofitInterface.executeDeclineExchange(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    System.out.println("Accepted");
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(ExchangeSummary.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showTitle() {
        HashMap<String, String> map = new HashMap<>();

        map.put("user_id", getIntent().getStringExtra("src_user_id"));

        Call<InvitationResult> call = retrofitInterface.executeGetUsername(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    sourceParticipantName = response.body().getResult();
                    setTitle();
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(ExchangeSummary.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setTitle() {
        TextView textView = findViewById(R.id.exchange_summary_title);
        textView.setText(sourceParticipantName + "'s proposition");
    }

    private void getSourcePlayer() {
        HashMap<String, String> map = new HashMap<>();

        map.put("name", getIntent().getStringExtra("src_player_name"));

        Call<PlayersResult> call = retrofitInterface.executeGetPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    try {
                        parseSourcePlayer(response.body().getResult());
                        getTargetPlayer();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(ExchangeSummary.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseSourcePlayer(String player) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_player = (JSONObject) parser.parse(player);

        JSONArray array = (JSONArray) parsed_player.get("Players");
        sourcePlayer = (JSONObject) array.get(0);
    }

    private void getTargetPlayer() {
        HashMap<String, String> map = new HashMap<>();

        map.put("name", getIntent().getStringExtra("trg_player_name"));

        Call<PlayersResult> call = retrofitInterface.executeGetPlayers(map);

        call.enqueue(new Callback<PlayersResult>() {
            @Override
            public void onResponse(Call<PlayersResult> call, Response<PlayersResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    try {
                        parseTargetPlayer(response.body().getResult());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    showExchange();
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<PlayersResult> call, Throwable t) {
                Toast.makeText(ExchangeSummary.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseTargetPlayer(String player) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_player = (JSONObject) parser.parse(player);

        JSONArray array = (JSONArray) parsed_player.get("Players");
        targetPlayer = (JSONObject) array.get(0);
    }

    private void showExchange() {
        System.out.println(sourcePlayer);
        System.out.println(targetPlayer);

        addText(sourceParticipantName + " offers " + sourcePlayer.get("name"));
        showPlayerStats(sourcePlayer);
        addText("For your player " + targetPlayer.get("name"));
        showPlayerStats(targetPlayer);
    }

    @SuppressLint({"SetTextI18n"})
    private void addText(String text) {
        LinearLayout layout = findViewById(R.id.exchange_summary_scroll_view);

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(20);
        textView.setTextColor(getResources().getColor(R.color.black));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 50, 0, 20);

        textView.setLayoutParams(params);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        layout.addView(textView);
    }

    private void showPlayerStats(JSONObject player) {
        LinearLayout layout = findViewById(R.id.exchange_summary_scroll_view);
        LinearLayout newLayout = new LinearLayout(getApplicationContext());
        newLayout.setBackgroundResource(R.drawable.group_button_style);

        TextView textView = new TextView(this);
        textView.setText(formatContent(player));
        textView.setTextSize(15);
        textView.setTextColor(getResources().getColor(R.color.black));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 0);

        textView.setLayoutParams(params);

        newLayout.addView(textView);
        layout.addView(newLayout);
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
}
