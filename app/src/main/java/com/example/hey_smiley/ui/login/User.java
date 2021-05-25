package com.example.hey_smiley.ui.login;

public class User {

    private String id;
    private String username;
    private float img;
    private String status;
    private String email;

    public User(String id, String username, float img, String status, String email) {
        this.id = id;
        this.username = username;
        this.img = img;
        this.status = status;
        this.email = email;
    }

    public User() {

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

    public float getImg() {
        return img;
    }

    public void setImageURL(float img) { this.img = img; }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getInfo()
    {
        return email + username + id +status + img;
    }
}
