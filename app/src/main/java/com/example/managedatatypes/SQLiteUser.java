package com.example.managedatatypes;

public class SQLiteUser {

    private String date;
    private String name;
    private String email;
    private String password;

    public SQLiteUser() {
    }
    public SQLiteUser(String date, String name, String email, String password) {
        this.date = date;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
