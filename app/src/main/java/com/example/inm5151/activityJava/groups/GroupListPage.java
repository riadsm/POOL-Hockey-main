package com.example.inm5151.activityJava.groups;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inm5151.R;
import com.example.inm5151.activityJava.LogIn;
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

public class GroupListPage extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list_page);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
        listGroups();
    }

    //Refresh page when group is created
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void startListeners() {
        ImageButton addGroupButton = findViewById(R.id.add_group_button);
        addGroupButton.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        Intent intent = new Intent(this, CreateGroupPage.class);
        startActivity(intent);
    }

    private void listGroups() {
        String username = ((UserInformation) this.getApplication()).getUsername();
        getGroups(username);
    }

    private void getGroups(String username) {
        System.out.println("Fetching groups");
        HashMap<String, String> map = new HashMap<>();

        map.put("username", username);

        Call<GroupResult> call = retrofitInterface.executeGroup(map);

        call.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    String groups = response.body().getGroups();
                    try {
                        showGroups(groups);
                    } catch (ParseException e) {
                        showGroupError();
                    }
                } else if (response.code() == 404) {
                    showGroupError();
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                Toast.makeText(GroupListPage.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showGroupError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupListPage.this);
        builder.setTitle("Error");
        builder.setMessage("There was an error with group fetching. Please try again.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GroupListPage.this, LogIn.class);
                startActivity(intent);
                finish();
            }
        });

        builder.show();
    }

    private void showGroups(String groups) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsed_groups = (JSONObject) parser.parse(groups);
        JSONArray array_groups = (JSONArray) parsed_groups.get("Groups");

        for (int i = 0; i < Objects.requireNonNull(array_groups).size(); i++) {
            System.out.println(array_groups.get(i));
            createButton(i, (JSONObject) array_groups.get(i));
        }
    }

    private void createButton(int i, JSONObject group) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.group_scroll_view);

        Button group_button = new Button(this);
        group_button.setText(Objects.requireNonNull(group.get("name")).toString());
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
        group_button.setOnClickListener(v -> openGroupPage(group));

        //add button to the layout
        layout.addView(group_button);
    }

    private void openGroupPage(JSONObject group) {
        System.out.println(group);
        Intent intent = new Intent(this, GroupPage.class);
        intent.putExtra("group_name", Objects.requireNonNull(group.get("name")).toString());
        intent.putExtra("group_id", Objects.requireNonNull(group.get("id")).toString());
        startActivity(intent);
    }
}
