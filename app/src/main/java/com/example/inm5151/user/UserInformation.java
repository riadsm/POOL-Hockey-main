package com.example.inm5151.user;

import android.app.Application;

/**
 * Contains all the informations regarding the user. These variables are accessible
 * throughout the whole application.
 */
public class UserInformation extends Application {

    private String username;
    private String email;
    private String id;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }
}
