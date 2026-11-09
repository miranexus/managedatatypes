package com.example.managedatatypes;

import com.google.gson.annotations.SerializedName;

public class MySQLPHPVariables {
    @SerializedName("date")
    private String date;
    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private boolean message;

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
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean isMessage() {
        return message;
    }
    public void setMessage(boolean message) {
        this.message = message;
    }
}
