package com.example.inm5151.activityJava.groups.singleGroup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Ranking extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)
    ArrayList<Integer> goals = new ArrayList<>();
    ArrayList<Integer> assists = new ArrayList<>();
    ArrayList<String> name_participant = new ArrayList<>();
    private int goal_value = 0;
    private int pass_value = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        System.out.println(getIntent().getStringExtra("group_name"));
        System.out.println(getIntent().getStringExtra("group_id"));

        String groupId = getIntent().getStringExtra("group_id");

        paramgroup(groupId);
    }

    private void paramgroup(String groupId) {
        System.out.println("group call");
        HashMap<String, String> map = new HashMap<>();

        map.put("group_id", groupId);

        Call<GroupResult> call = retrofitInterface.executeGetPointsGroup(map);
        call.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                System.out.println(response.code() + "Successful request group");

                if (response.code() == 200) {
                    String ptsvalue = response.body().getGroups();
                    JSONParser parser = new JSONParser();
                    JSONObject parsedpoints = null;
                    try {
                        parsedpoints = (JSONObject) parser.parse(ptsvalue);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    JSONArray arraybuts = (JSONArray) parsedpoints.get("goal_value");
                    JSONArray arraypasses = (JSONArray) parsedpoints.get("pass_value");
                    goal_value = Integer.parseInt(arraybuts.get(0).toString().substring(14, arraybuts.get(0).toString().length() - 1 ));
                    pass_value = Integer.parseInt(arraypasses.get(0).toString().substring(14, arraypasses.get(0).toString().length() - 1 ));

                    listParticipants(groupId);
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                //Toast.makeText(Participants.this, t.getMessage(),
                //      Toast.LENGTH_LONG).show();
                System.out.println("Failure");
            }
        });
    }

    private void listParticipants(String groupId) {
        System.out.println("participants");
        HashMap<String, String> map = new HashMap<>();

        map.put("group_id", groupId);

        Call<GroupResult> call = retrofitInterface.executeGetParticipants(map);

        call.enqueue(new Callback<GroupResult>() {
            @Override
            public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                System.out.println(response.code() + "Successful request participant");

                if (response.code() == 200) {
                    String participants = response.body().getGroups();

                    try {
                        getPoints(participants);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (response.code() == 404) {
                    //Nothing
                }
            }

            @Override
            public void onFailure(Call<GroupResult> call, Throwable t) {
                //Toast.makeText(Participants.this, t.getMessage(),
                  //      Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPoints(String participants) throws ParseException {
        System.out.println("Points");
        JSONParser parser = new JSONParser();
        JSONObject parsedParticipants = (JSONObject) parser.parse(participants);
        JSONArray arrayParticipants = (JSONArray) parsedParticipants.get("Participants");

        for(int i = 0; i < Objects.requireNonNull(arrayParticipants).size(); i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("group_id", getIntent().getStringExtra("group_id"));
            map.put("participant_name", arrayParticipants.get(i).toString());
            Call<GroupResult> call = retrofitInterface.executeGetPoints(map);

            call.enqueue(new Callback<GroupResult>() {
                @Override
                public void onResponse(Call<GroupResult> call, Response<GroupResult> response) {
                    System.out.println(response.code() + "Successful request points");
                    if (response.code() == 200) {
                        String pts = response.body().getGroups();
                        try {
                            totpts(pts, participants);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() == 404) {
                        //Nothing
                    }
                }

                @Override
                public void onFailure(Call<GroupResult> call, Throwable t) {
                    //Toast.makeText(Participants.this, t.getMessage(),
                    //      Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void totpts(String pts, String participants) throws ParseException {
        JSONParser parsere = new JSONParser();
        JSONObject parsedParticipants = (JSONObject) parsere.parse(participants);
        JSONArray arrayParticipants = (JSONArray) parsedParticipants.get("Participants");
        JSONParser parser = new JSONParser();
        JSONObject parsedpoints = (JSONObject) parser.parse(pts);
        JSONArray arraybuts = (JSONArray) parsedpoints.get("goals");
        JSONArray arraypasses = (JSONArray) parsedpoints.get("pass");
        JSONArray arrayname = (JSONArray) parsedpoints.get("name");
        String but = arraybuts.get(0).toString().substring(8, arraybuts.get(0).toString().length() - 1 );
        String passes = arraypasses.get(0).toString().substring(10, arraypasses.get(0).toString().length() - 1 );
        int buts = Integer.parseInt(but);
        int passe = Integer.parseInt(passes);
        name_participant.add(arrayname.get(0).toString().substring(13, arrayname.get(0).toString().length() - 2 ));
        goals.add(buts);
        assists.add(passe);
        if( assists.size() == Objects.requireNonNull(arrayParticipants).size()) {
            initTable(participants);
        }
    }

    private void initTable(String participants) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject parsedParticipants = (JSONObject) parser.parse(participants);
        JSONArray arrayParticipants = (JSONArray) parsedParticipants.get("Participants");
        for (int i = 0; i < goals.size(); i++){
            goals.set(i, goals.get(i) * goal_value);
            assists.set(i, assists.get(i) * pass_value);
            goals.set(i, goals.get(i) + assists.get(i));
        }
        int temp;
        String temp1;
        for (int i = 0; i < goals.size(); i++) {
            for (int j = i+1; j < goals.size(); j++) {
                if((goals.get(i) < goals.get(j))){
                    temp1 = name_participant.get(i);
                    name_participant.set(i, name_participant.get(j));
                    name_participant.set(j, temp1);
                    temp = goals.get(i);
                    goals.set(i, goals.get(j));
                    goals.set(j, temp);
                }
            }
        }

        //create table
        TableLayout stk = (TableLayout) findViewById(R.id.table_ranking);
        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText(" Place ");
        tv0.setTextColor(Color.BLACK);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText(" Participants ");
        tv1.setTextColor(Color.BLACK);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(" Points ");
        tv2.setTextColor(Color.BLACK);
        tbrow0.addView(tv2);
        stk.addView(tbrow0);
        for (int i = 0; i < Objects.requireNonNull(arrayParticipants).size(); i++) {
            TableRow tbrow = new TableRow(this);
            TextView t1v = new TextView(this);
            t1v.setText(i+1 + ".");
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);
            TextView t2v = new TextView(this);
            t2v.setText(name_participant.get(i));
            t2v.setTextColor(Color.BLACK);
            t2v.setGravity(Gravity.CENTER);
            tbrow.addView(t2v);
            TextView t3v = new TextView(this);
            t3v.setText(goals.get(i).toString());
            t3v.setTextColor(Color.BLACK);
            t3v.setGravity(Gravity.CENTER);
            tbrow.addView(t3v);
            stk.addView(tbrow);
        }
    }
}
