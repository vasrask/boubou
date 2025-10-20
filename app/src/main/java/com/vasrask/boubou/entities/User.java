package com.vasrask.boubou.entities;


import androidx.annotation.NonNull;

import java.util.Map;

public class User {
    private String id;
    private String username;
    private String email;
    private Map<String, BabyActivity> babyActivites;
    public User() {}


    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public User(String id, String username, String email, Map<String, BabyActivity> babyActivities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.babyActivites = babyActivities;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, BabyActivity> getBabyActivites() {
        return babyActivites;
    }

    public void setBabyActivites(Map<String, BabyActivity> babyActivites) {
        this.babyActivites = babyActivites;
    }


    public void removeBabyActivity(BabyActivity babyActivity) {
        if (this.babyActivites != null) {
            this.babyActivites.remove(babyActivity.getId());
        }
    }

    public void addBabyActivity(BabyActivity babyActivity, String category) {
        if (this.babyActivites != null) {
            this.babyActivites.put(category, babyActivity);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", babyActivites=" + babyActivites +
                '}';
    }
}
