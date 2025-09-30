package com.sifat.bcsbnagla.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String token;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getToken() { return token; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setToken(String token) { this.token = token; }
}
