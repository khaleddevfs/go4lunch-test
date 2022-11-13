package com.example.go4lunch.models;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public class  User {

    private String uid;

    private String username;

    @Nullable
    private String urlPicture;

    private ArrayList<String> choice;

    @Nullable
    private boolean isNotificationOn;


    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String username, @Nullable String urlPicture, ArrayList<String> choice, @Nullable boolean isNotificationOn) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.choice = choice;
        this.isNotificationOn = isNotificationOn;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }

    public String getUsername() { return username; }

    @Nullable
    public String getUrlPicture() { return urlPicture; }

    public ArrayList<String> getChoice() {
        return choice;
    }

    @Nullable
    public boolean isNotificationOn() {
        return isNotificationOn;
    }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }

    public void setUid(String uid) { this.uid = uid; }

    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }

    public void setChoice(ArrayList<String> choice) {
        this.choice = choice;
    }

    public void setNotificationOn(@Nullable boolean notificationOn) {isNotificationOn = notificationOn;}


}




