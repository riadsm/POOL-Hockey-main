package com.example.inm5151.activityJava;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.InvitationResult;
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

public class Invitations extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    private String groupInvitations;
    private String exchangeInvitations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        String userId = ((UserInformation) this.getApplication()).getId();
        getGroupInvitations(userId);
    }

    //Refresh page when group is created
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void getGroupInvitations(String userId) {
        System.out.println("Fetching group invitations");
        HashMap<String, String> map = new HashMap<>();

        map.put("user_id", userId);

        Call<InvitationResult> call = retrofitInterface.executeInvitationsList(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + " Successful request");

                if (response.code() == 200) {
                    groupInvitations = response.body().getResult();
                    getExchangeInvitations(userId);
                } else if (response.code() == 404) {
                    showInvitationsError();
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(Invitations.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getExchangeInvitations(String userId) {
        System.out.println("Fetching exchange invitations");
        HashMap<String, String> map = new HashMap<>();

        map.put("user_id", userId);

        Call<InvitationResult> call = retrofitInterface.executeExchangeInvitations(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + " Successful request");

                if (response.code() == 200) {
                    exchangeInvitations = response.body().getResult();

                    try {
                        showInvitations();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    showInvitationsError();
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(Invitations.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showInvitationsError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Invitations.this);
        builder.setTitle("Error");
        builder.setMessage("There was an error with invitations fetching. Please try again.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();
    }

    private void showInvitations() throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_groups = (JSONObject) parser.parse(groupInvitations);
        JSONObject parsed_exchanges = (JSONObject) parser.parse(exchangeInvitations);

        JSONArray groupInvitationsArray = (JSONArray) parsed_groups.get("Invitations");
        int groupsLength = Objects.requireNonNull(groupInvitationsArray).size();

        JSONArray exchangeInvitationsArray = (JSONArray) parsed_exchanges.get("Invitations");
        int exchangesLength = Objects.requireNonNull(exchangeInvitationsArray).size();

        if(groupsLength == 0 && exchangesLength == 0) {
            showNoInvitations();
            return;
        }

        showGroupInvitations(groupInvitationsArray, groupsLength);
        showExchangeInvitations(exchangeInvitationsArray, exchangesLength);
    }

    private void showGroupInvitations(JSONArray array, int length) {
        for (int i = 0; i < length; i++) {
            createButton(i, (JSONObject) array.get(i));
        }
    }

    private void showExchangeInvitations(JSONArray array, int length) {
        for (int i = 0; i < length; i++) {
            createExchangeButton(i, (JSONObject) array.get(i));
        }
    }

    private void showNoInvitations() {
        //Image source: https://dribbble.com/shots/5822231-Empty-state-Inbox-empty
        LinearLayout layout = findViewById(R.id.invitations_scroll_view);
        ImageView emptyInboxImage = new ImageView(this);
        emptyInboxImage.setImageResource(R.drawable.blank_inbox_email);
        layout.addView(emptyInboxImage);
    }

    private void createButton(int i, JSONObject invitations) {
        String groupName = Objects.requireNonNull(invitations.get("group_name")).toString();

        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.invitations_scroll_view);

        Button group_button = new Button(this);
        group_button.setText(groupName);
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
        group_button.setOnClickListener(v -> openPopUp(invitations));

        //add button to the layout
        layout.addView(group_button);
    }

    private void openPopUp(JSONObject invitations) {
        String groupName = Objects.requireNonNull(invitations.get("group_name")).toString();
        String groupId = Objects.requireNonNull(invitations.get("group_id")).toString();
        String invitationId = Objects.requireNonNull(invitations.get("id")).toString();
        String userId = ((UserInformation) this.getApplication()).getId();

        AlertDialog.Builder builder = new AlertDialog.Builder(Invitations.this);
        builder.setTitle("Invitation");
        builder.setMessage("Do you want to join the group " + groupName + "?");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptInvitation(invitationId, groupId, userId);
                finish();
                refreshPage();
            }
        });

        builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rejectInvitation(invitationId);
                finish();
                refreshPage();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
        int red = getResources().getColor(R.color.red);
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(red);
        int green = getResources().getColor(R.color.green);
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(green);

        builder.setCancelable(true);
    }

    private void refreshPage() {
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void acceptInvitation(String invitationId, String groupId, String userId) {
        System.out.println("Accepting invite");
        HashMap<String, String> map = new HashMap<>();

        map.put("invitation_id", invitationId);
        map.put("group_id", groupId);
        map.put("user_id", userId);

        Call<InvitationResult> call = retrofitInterface.executeAcceptInvitation(map);

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
                Toast.makeText(Invitations.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rejectInvitation(String invitationId) {
        System.out.println("Declining invite");
        HashMap<String, String> map = new HashMap<>();

        map.put("invitation_id", invitationId);

        Call<InvitationResult> call = retrofitInterface.executeDeclineInvitation(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    System.out.println("Declined");
                } else if (response.code() == 404) {
                    //Error
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(Invitations.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void createExchangeButton(int i, JSONObject invitations) {
        String groupName = Objects.requireNonNull(invitations.get("group_name")).toString();

        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.invitations_scroll_view);

        Button group_button = new Button(this);
        group_button.setText(groupName + " exchange");
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
        group_button.setOnClickListener(v -> openExchangePage(invitations));

        //add button to the layout
        layout.addView(group_button);
    }

    private void openExchangePage(JSONObject exchange) {
        Intent intent = new Intent(this, ExchangeSummary.class);

        intent.putExtra("src_player_name", exchange.get("src_player_name").toString());
        intent.putExtra("trg_user_id", exchange.get("trg_user_id").toString());
        intent.putExtra("group_id", exchange.get("group_id").toString());
        intent.putExtra("group_name", exchange.get("group_name").toString());
        intent.putExtra("trg_player_name", exchange.get("trg_player_name").toString());
        intent.putExtra("src_user_id", exchange.get("src_user_id").toString());
        intent.putExtra("invitation_id", exchange.get("id").toString());

        startActivity(intent);
    }
}
