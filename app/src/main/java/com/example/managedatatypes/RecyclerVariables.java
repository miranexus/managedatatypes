package com.example.managedatatypes;

public class RecyclerVariables {

    private final String date;
    private final String name;
    private final String email;
    private final String password;

    public RecyclerVariables(String date, String name, String email, String password) {
        this.date = date;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getDate() {
        return date;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
}
