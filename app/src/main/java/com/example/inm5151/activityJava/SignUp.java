package com.example.inm5151.activityJava;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inm5151.R;
import com.example.inm5151.retrofit.RetrofitInterface;
import com.example.inm5151.retrofit.SignUpResult;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignUp extends AppCompatActivity {

    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;
    private String BASE_URL = "http://10.0.2.2:8000"; // Change url if making it public (for emulator)
    // private String BASE_URL = "http://192.168.2.206:8000"; // Change url if making it public (for real device)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        startListeners();
    }

    private void startListeners() {
        //Sign up
        Button signUpButton = findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        EditText username = findViewById(R.id.sign_up_username);
        EditText email = findViewById(R.id.sign_up_email);
        EditText password = findViewById(R.id.sign_up_password);

        validateInputs(username.getText().toString(), email.getText().toString(), password.getText().toString());
    }

    private void validateInputs(String username, String email, String password) {
        TextView missing_username_warning = findViewById(R.id.missing_username_warning);
        TextView missing_email_warning = findViewById(R.id.missing_email_warning);
        TextView missing_password_warning = findViewById(R.id.missing_password_warning);

        boolean isValid = true;

        if(username.equals("")) {
            missing_username_warning.setVisibility(View.VISIBLE);
            showInvalidCredentials(missing_username_warning);
            isValid = false;
        }

        if(email.equals("")) {
            missing_email_warning.setVisibility(View.VISIBLE);
            showInvalidCredentials(missing_email_warning);
            isValid = false;
        }

        if(password.equals("")) {
            missing_password_warning.setVisibility(View.VISIBLE);
            showInvalidCredentials(missing_password_warning);
            isValid = false;
        }

        if(isValid) {
            sendSignUpRequest(username, email, password);
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

    private void sendSignUpRequest(String username, String email, String password) {
        HashMap<String, String> map = new HashMap<>();

        System.out.println("Sign up: " + username + " , " + email + " , " + password);
        map.put("username", username);
        map.put("email", email);
        map.put("password", password);

        Call<SignUpResult> call = retrofitInterface.executeSignup(map);

        call.enqueue(new Callback<SignUpResult>() {
            @Override
            public void onResponse(Call<SignUpResult> call, Response<SignUpResult> response) {
                System.out.println(response.code() + "Successful request");

                if (response.code() == 200) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                    builder.setTitle("Signed up");
                    builder.setMessage("Signed up successfully");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    builder.show();

                    // Clear text
                    ((EditText)findViewById(R.id.sign_up_username)).getText().clear();
                    ((EditText)findViewById(R.id.sign_up_email)).getText().clear();
                    ((EditText)findViewById(R.id.sign_up_password)).getText().clear();

                } else if (response.code() == 201) {
                    TextView used_username_warning = findViewById(R.id.used_username_warning);
                    TextView used_email_warning = findViewById(R.id.used_email_warning);
                    SignUpResult result = response.body();

                    if(result.getUsername().equals("invalid")) {
                        used_username_warning.setVisibility(View.VISIBLE);
                        showInvalidCredentials(used_username_warning);
                    }

                    if(result.getEmail().equals("invalid")) {
                        used_email_warning.setVisibility(View.VISIBLE);
                        showInvalidCredentials(used_email_warning);
                    }
                }
            }

            @Override
            public void onFailure(Call<SignUpResult> call, Throwable t) {
                Toast.makeText(SignUp.this, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}