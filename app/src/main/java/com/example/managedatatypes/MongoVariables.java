package com.example.managedatatypes;

// WHENEVER WE SUCCESSFULLY LOGIN THE SERVER WILL SEND THE NAME AND EMAIL AND WE WILL STORE THAT DATA IN THIS CLASS
// WE WILL NOT BE DOING THIS MANUALLY AS THE GSON CONVERTER WILL AUTOMATICALLY CONVERT THE DATA INTO A JAVA OBJECT

public class MongoVariables {

    // THIS CLASS WILL HAVE STRING MEMBER VARIABLES "name" AND "email"
    // WE CAN ADD A SERIALIZED NAME ANNOTATION TO TELL GSON CONVERTER TO STORE THE DATA WITH KEY "@SerializedName("name");"
    // IF "@SerializedName("name");" AND "private String name;" HAVE THE SAME "name" WE DON'T NEED IT
    private final String date;
    private final String name;
    private final String email;
    private final String password;

    public MongoVariables(String date, String name, String email, String password) {
        this.date = date;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // WE ADD GETTER METHODS FOR THE VALUES ABOVE SO WE CAN LATER GET THEM OUT OF THE POST OBJECTS TO DISPLAY
    // RIGHT CLICK - GENERATE - GETTER - SELECT ALL - OK
    // WITH RETROFIT WE WILL LATER RETRIEVE THE POST ARRAY FROM THE REST API
    // THE GSON CONVERTER WILL TURN THESE POST JSON OBJECTS INTO POST JAVA OBJECTS

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






