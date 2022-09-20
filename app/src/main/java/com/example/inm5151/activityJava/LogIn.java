package com.example.inm5151.activityJava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.LoginResult;
import com.example.inm5151.retrofit.RetrofitInterface;
import com.example.inm5151.user.UserInformation;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogIn extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }

    private void startListeners() {
        //Sign up
        Button signUpButton = findViewById(R.id.main_sign_up);
        signUpButton.setOnClickListener(v -> openSignUp());

        //Sign in
        Button signInButton = findViewById(R.id.main_sign_in);
        signInButton.setOnClickListener(v -> signIn());
    }

    private void openSignUp() {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    private void signIn() {
        EditText username = findViewById(R.id.main_username);
        EditText password = findViewById(R.id.main_password);

        validateInputs(username.getText().toString(), password.getText().toString());
    }

    private void validateInputs(String username, String password) {
        TextView warning = findViewById(R.id.credentials_warning);

        if(username.equals("") || password.equals("")) {
            warning.setVisibility(View.VISIBLE);

            showInvalidCredentials(warning);
        } else {
            sendLoginInRequest(username, password);
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

    private void sendLoginInRequest(String username, String password) {
        HashMap<String, String> map = new HashMap<>();

        System.out.println(username + " and " + password);
        map.put("username", username);
        map.put("password", password);

        Call<LoginResult> call = retrofitInterface.executeLogin(map);

        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    setUserInformations(username, response.body().getId());
                    openHomepage();

                } else if (response.code() == 404) {
                    TextView warning = findViewById(R.id.credentials_warning);
                    warning.setVisibility(View.VISIBLE);
                    showInvalidCredentials(warning);
                }
            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                Toast.makeText(LogIn.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openHomepage() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }

    private void setUserInformations(String username, String id) {
        ((UserInformation) this.getApplication()).setUsername(username);
        ((UserInformation) this.getApplication()).setId(id);
    }
}