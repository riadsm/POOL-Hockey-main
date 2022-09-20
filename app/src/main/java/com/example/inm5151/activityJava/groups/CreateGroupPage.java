package com.example.inm5151.activityJava.groups;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.GroupResult;
import com.example.inm5151.retrofit.RetrofitInterface;
import com.example.inm5151.user.UserInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateGroupPage extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
    }

    private void startListeners() {
        Button addGroupButton = findViewById(R.id.create_group_button);
        addGroupButton.setOnClickListener(v -> createGroup());
    }


    private void createGroup() {
        EditText groupName = findViewById(R.id.create_group_name);
        EditText goal = findViewById(R.id.create_group_goalpts);
        EditText pass = findViewById(R.id.create_group_passpts);
        EditText prize = findViewById(R.id.create_group_prize);

        validateInput(groupName.getText().toString(), goal.getText().toString(), pass.getText().toString(), prize.getText().toString());
    }

    private void validateInput(String name, String goal, String pass, String prize) {
        TextView warning = findViewById(R.id.create_group_warning);

        if(name.equals("") || goal.equals("") || pass.equals("") || !goal.matches("[0-9]+") || !pass.matches("[0-9]+")) {
            warning.setVisibility(View.VISIBLE);
            showInvalidCredentials(warning);
        } else {
            sendCreateRequest(name, goal, pass, prize);
        }
    }

    private void showInvalidCredentials(TextView warning) {
        warning.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
                warning.startAnimation(fadeOut);
                fadeOut.setDuration(2000);
                fadeOut.setFillAfter(false);
                warning.setVisibility(View.INVISIBLE);
            }
        }, 5);
    }

    private void sendCreateRequest(String name, String goal, String pass, String prize) {
        String username = ((UserInformation) this.getApplication()).getUsername();
        String user_id = ((UserInformation) this.getApplication()).getId();

        HashMap<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("group_name", name);
        map.put("user_id", user_id);
        map.put("goal_value", goal);
        map.put("pass_value", pass);
        map.put("prize_value", prize);

        Call<GroupResult> call = retrofitInterface.create_group(map);

        call.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {

                if (response.code() == 200) {
                    showMessage(name);
                } else if (response.code() == 404) {
                    TextView warning = findViewById(R.id.create_group_warning);
                    showInvalidCredentials(warning);
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                Toast.makeText(CreateGroupPage.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMessage(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupPage.this);
        builder.setTitle("Group");
        builder.setMessage("Your group " + name + " has been created");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.show();

        // Clear text
        ((EditText)findViewById(R.id.create_group_name)).getText().clear();
    }
}
