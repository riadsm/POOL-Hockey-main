package com.example.inm5151.activityJava.groups.singleGroup;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.activityJava.Invitations;
import com.example.inm5151.retrofit.AllUsersResult;
import com.example.inm5151.retrofit.InvitationResult;
import com.example.inm5151.retrofit.RetrofitInterface;
import com.example.inm5151.user.UserInformation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InviteParticipant extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_participant);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
        sendGetAllUsersRequest();
    }

    private void startListeners() {
        //Invite
        Button signUpButton = findViewById(R.id.invite_participant_button);
        signUpButton.setOnClickListener(v -> sendInvite());
    }

    private void sendInvite() {
        AutoCompleteTextView usernameText = findViewById(R.id.invite_participant_text);
        String username = usernameText.getText().toString();
        TextView warning = findViewById(R.id.invite_participant_warning);
        String currentUser = ((UserInformation) this.getApplication()).getUsername();

        if(username.equals("") || username.equals(currentUser)) {
            warning.setVisibility(View.VISIBLE);

            showInvalidCredentials(warning);
        } else {
            String groupId = getIntent().getStringExtra("group_id");
            sendGroupInviteRequest(username, groupId);
        }
    }

    private void showInvalidCredentials(TextView warning) {
        warning.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
                warning.startAnimation(fadeOut);
                fadeOut.setDuration(1200);
                fadeOut.setFillAfter(false);
                warning.setVisibility(View.INVISIBLE);
            }
        }, 3000);
    }

    private void sendGroupInviteRequest(String username, String groupId) {
        HashMap<String, String> map = new HashMap<>();

        map.put("username", username);
        map.put("groupId", groupId);

        Call<InvitationResult> call = retrofitInterface.executeInviteGroup(map);

        call.enqueue(new Callback<InvitationResult>() {
            @Override
            public void onResponse(Call<InvitationResult> call, Response<InvitationResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    showConfirmationMessage(username);
                } else if (response.code() == 404) {
                    TextView warning = findViewById(R.id.invite_participant_warning);
                    warning.setText(R.string.user_not_exist);
                    warning.setVisibility(View.VISIBLE);
                    showInvalidCredentials(warning);
                } else if(response.code() == 201) {
                    TextView warning = findViewById(R.id.invite_participant_warning);
                    warning.setText(R.string.user_already_invited);
                    warning.setVisibility(View.VISIBLE);
                    showInvalidCredentials(warning);
                } else if(response.code() == 202) {
                    TextView warning = findViewById(R.id.invite_participant_warning);
                    warning.setText(R.string.user_already_in_group);
                    warning.setVisibility(View.VISIBLE);
                    showInvalidCredentials(warning);
                }
            }

            @Override
            public void onFailure(Call<InvitationResult> call, Throwable t) {
                Toast.makeText(InviteParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConfirmationMessage(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InviteParticipant.this);
        builder.setTitle("Success");
        builder.setMessage("The user " + username + " has been invited to your group!");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing
            }
        });

        builder.show();
    }

    private void sendGetAllUsersRequest() {
        Call<AllUsersResult> call = retrofitInterface.executeGetAllUsers();

        call.enqueue(new Callback<AllUsersResult>() {
            @Override
            public void onResponse(Call<AllUsersResult> call, Response<AllUsersResult> response) {
                System.out.println(response.code() + " Successful request");

                if (response.code() == 200) {
                    showSuggestions(response.body().getUsers());
                } else if (response.code() == 404) {
                    //Gestion d'erreur
                }
            }

            @Override
            public void onFailure(Call<AllUsersResult> call, Throwable t) {
                Toast.makeText(InviteParticipant.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuggestions(String users) {
        try {
            setAdapter(responseToArray(users));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> responseToArray(String users) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_groups = (JSONObject) parser.parse(users);
        JSONArray usersJson = (JSONArray) parsed_groups.get("Users");

        ArrayList<String> usersArray = new ArrayList<>();

        for (int i = 0; i < Objects.requireNonNull(usersJson).size(); i++) {
            usersArray.add(usersJson.get(i).toString());
        }

        return usersArray;
    }

    private void setAdapter(ArrayList<String> users) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, users);
        AutoCompleteTextView textView = findViewById(R.id.invite_participant_text);
        textView.setAdapter(adapter);
    }
}

