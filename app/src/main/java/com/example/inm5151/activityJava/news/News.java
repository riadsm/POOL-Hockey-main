package com.example.inm5151.activityJava.news;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.inm5151.R;
import com.example.inm5151.retrofit.RetrofitInterface;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class News extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        fetchNewsData();
    }

    private void fetchNewsData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://site.api.espn.com/apis/site/v2/sports/hockey/nhl/news";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            parseData(response);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                }
        });

        queue.add(stringRequest);
    }

    private void parseData(String data) throws ParseException {
        JSONParser parser = new JSONParser();
        org.json.simple.JSONObject parsedData = (JSONObject) parser.parse(data);
        JSONArray arrayArticles = (JSONArray) parsedData.get("articles");

        for (int i = 0; i < Objects.requireNonNull(arrayArticles).size(); i++) {
            JSONObject article = (JSONObject) arrayArticles.get(i);
            createArticle(i, article);
        }
    }

    private void createArticle(int i, JSONObject article) {
        //Screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int button_width = (int) (0.8 * width);

        LinearLayout layout = findViewById(R.id.news_scroll_view);

        Button button = new Button(this);
        button.setText(Objects.requireNonNull(article.get("headline")).toString());
        button.setId(i);
        button.setWidth(button_width);
        button.setBackgroundResource(R.drawable.group_button_style);
        button.setTextColor(Color.parseColor("#000000"));

        //Set the margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 50, 0, 0);
        button.setLayoutParams(params);
        button.setPadding(10, 0, 10, 0);

        //Set listener for each button
        JSONObject links = (JSONObject) article.get("links");
        JSONObject web = (JSONObject) Objects.requireNonNull(links).get("web");
        String url = Objects.requireNonNull(Objects.requireNonNull(web).get("href")).toString();

        button.setOnClickListener(v -> openArticle(url));

        //add button to the layout
        layout.addView(button);
    }

    private void openArticle(String url) {
        System.out.println(url);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
