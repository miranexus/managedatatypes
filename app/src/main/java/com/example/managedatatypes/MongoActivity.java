package com.example.managedatatypes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MongoActivity extends EdgeToEdgeActivity implements AdapterView.OnItemSelectedListener{

    public static final String TAG = "Mongo";

    public static String jsonDataString = "[" +
            "{\"date\":\"1625564252\",\"name\":\"Henry\",\"email\":\"henry@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Marta\",\"email\":\"marta@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Katie\",\"email\":\"katie@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Teddy\",\"email\":\"teddy@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Wayne\",\"email\":\"wayne@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Fredy\",\"email\":\"fredy@email.com\",\"password\":\"123abc\"}" +
            "]";

    public static RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mongo);

        // INITIALIZE TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarMongo);
        setSupportActionBar(toolbar);

        // SET UP THE BASE URL DEPENDING ON WHICH ACTIVITY WE ARE IN, URL IN STRING RESOURCES
        MainActivity.baseUrl = getResources().getString(R.string.mongonodeurl);
        wakeUpHerokuServerIfConfigured();

        // SET UP A TAG TO REFERENCE THE ACTIVITY FROM THE RECYCLER ADAPTER
        MainActivity.recycleDelete = getResources().getString(R.string.MongoActivityRecycle);

        mRecyclerView = findViewById(R.id.my_recycler_view_mongo);

        // SET UP THE SPINNER, OPTIONS CAN BE FOUND IN STRING RESOURCES
        Spinner spinnerMongo = findViewById(R.id.spinnerMongo);
        ArrayAdapter<CharSequence> adapterSpinnerMongo = ArrayAdapter.createFromResource(this,
                R.array.spinnerItemsMongo, android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerMongo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMongo.setAdapter(adapterSpinnerMongo);
        spinnerMongo.setOnItemSelectedListener(this);
    }

    private void wakeUpHerokuServerIfConfigured() {
        Uri serverUri = Uri.parse(MainActivity.baseUrl);
        String host = serverUri.getHost();
        if ("https".equalsIgnoreCase(serverUri.getScheme())
                && host != null
                && host.toLowerCase(Locale.ROOT).endsWith(".herokuapp.com")) {
            wakeUpMongoServer();
        }
    }

    // Heroku may suspend an idle app; this health check is only used for configured Heroku URLs.
    private void wakeUpMongoServer() {
        InterfaceRetrofit api = MainActivity.retrofit.create(InterfaceRetrofit.class);
        Call<List<MongoVariables>> call = api.checkExistsMongo();
        call.enqueue(new Callback<List<MongoVariables>>() {
            @Override
            public void onResponse(Call<List<MongoVariables>> call, Response<List<MongoVariables>> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Heroku wake-up check returned HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MongoVariables>> call, Throwable error) {
                Log.w(TAG, "Heroku wake-up check failed", error);
            }
        });
    }

    // SET UP THE ONCLICKLISTENER FOR THE SPINNER
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        findViewById(R.id.btnMongoSpinner).setOnClickListener(view1 -> {
            Runnable[] actions = {
                    this::handleMongoSelectAllDialog,
                    this::handleMongoSelectUserDialog,
                    this::handleMongoInsertDialog,
                    this::handleMongoDeleteAllDialog,
                    this::exportMongoXls
            };
            if (position >= 0 && position < actions.length) {
                actions[position].run();
            } else {
                android.util.Log.w(TAG, "Ignoring unknown MongoDB action: " + position);
            }
        });
    }

    private void exportMongoXls() {
        try {
            handleMongoExportXLS();
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Unable to export MongoDB data", e);
            Toast.makeText(this, "Unable to export data", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // THIS METHOD IS CALLED FROM THE RECYCLER VIEW WHEN CLICKED TO UPDATE ENTRY
    public static void handleMongoUpdateDialog(View view, Context context, String email) {

        Button btnClick = view.findViewById(R.id.btnUpdateDial);
        final EditText nameEdit = view.findViewById(R.id.nameUpdateDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordUpdateDial);
        final TextView titleEdit = view.findViewById(R.id.titleUpdateDial);

        titleEdit.setText(email);

        btnClick.setOnClickListener(view1 -> {

            if (nameEdit.getText().toString().equals("") ||
                    passwordEdit.getText().toString().equals("")) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.fillallfields), Toast.LENGTH_LONG).show();
            } else {

                HashMap<String, String> map = new HashMap<>();

                //long timeStampLong = System.currentTimeMillis()/1000;
                //String timeStamp = Long.toString(timeStampLong);

                //map.put("date", timeStamp);
                map.put("name", nameEdit.getText().toString());
                map.put("email", email);
                map.put("password", passwordEdit.getText().toString());

                Call<Void> call = MainActivity.interfaceRetrofit.updateMongo(map);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.code() == 200) {

                            Toast.makeText(context,
                                    context.getResources().getString(R.string.entryupdated), Toast.LENGTH_LONG).show();

                            // UPDATE THE RECYCLER VIEW WITH THE CHANGES
                            RecyclerAdapter.nameViewUpdate.setText(nameEdit.getText().toString());
                            RecyclerAdapter.passwordViewUpdate.setText(passwordEdit.getText().toString());

                        } else if (response.code() == 400) {
                            Toast.makeText(context,
                                    context.getResources().getString(R.string.alreadyexist), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THIS METHOD IS CALLED FROM THE SPINNER TO POPULATE THE RECYCLERVIEW WITH ALL DB ENTRIES
    private void handleMongoSelectAllDialog() {

        // WITH THE INSTANCE OF RETROFIT "retrofit "WE CAN CREATE OUR "RetrofitInterface"
        InterfaceRetrofit api = MainActivity.retrofit.create(InterfaceRetrofit.class);

        // TO EXECUTE OUR NETWORK REQUEST WE USE THE "Call" OBJECT FROM EARLIER
        // WE INTRODUCE A "Call" OBJECT - "Call<T> (retrofit)"
        // WE ADD "RetrofitInterface" AND WE CAN CALL OUR "getPosts()" METHOD IN "InterfaceRetrofit.java"
        // RETROFIT CREATES THE IMPLEMENTATION FOR THIS METHOD
        // THEN WE EXECUTE THIS CALL AND GET OUR RESPONSE BACK
        Call<List<MongoVariables>> call = api.selectAllMongo();

        // WE ADD "call.enqueue" BUT WE DON'T ADD "call.execute" BECAUSE THIS METHOD WILL RUN SYNCHRONOUSLY
        // THIS MEANS IT WILL BE EXECUTED ON WHICHEVER THREAD WE ARE ON AND WE ARE ON THE MAIN THREAD
        // IF WE TRY TO DO NETWORK OPERATIONS ON THE MAIN THREAD WE WILL GET AN EXCEPTION AND FREEZE APP
        // INSTEAD WE EXECUTE IT ON THE BACKGROUND THREAD AND RETROFIT PROVIDES THIS WITH "enqueue" METHOD
        // IMPLEMENT THE METHODS WITH "new Callback<List<MongoVariables>>()"
        call.enqueue(new Callback<List<MongoVariables>>() {

            @Override
            // THIS METHOD WILL BE TRIGGERED WHEN WE GET A RESPONSE FROM THE SERVER
            // HOWEVER THIS DOESN'T MEAN THAT OUR REQUEST WAS SUCCESSFUL
            // THE SERVER COULD RESPOND WITH A HTTP 404 CODE THE DATA WERE NOT THERE
            public void onResponse(Call<List<MongoVariables>> call, Response<List<MongoVariables>> response) {
                // IF THE RESPONSE WAS NOT SUCCESSFUL SET THE RESPONSE TO THE HTTP RESPONSE CODE
                if (!response.isSuccessful()) {
                    Toast.makeText(mRecyclerView.getContext(), "Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // THE CODE HERE WILL ONLY BE CALLED IF OUR CALL WAS SUCCESSFUL
                // IF THE RESPONSE WAS SUCCESSFUL THE HTTP CODE IS BETWEEN 200 AND 300
                // GET A LIST OF POSTS "response.body()" - DATA FROM WEBSERVICE
                // WE THEN WANT TO DISPLAY IT IN OUR TEXT VIEW
                List<MongoVariables> posts = response.body();

                Gson gson = new Gson();
                String json = gson.toJson(posts);
                //System.out.println("JSON sent back from Node.js = " + json);

                jsonDataString = json;
                addItemsFromJSON();
            }

            @Override
            public void onFailure(Call<List<MongoVariables>> call, Throwable t) {
                Toast.makeText(mRecyclerView.getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // THIS METHOD SELECTS AN ENTRY BASED ON THE DETAILS ENTERED IN THE ALERT DIALOG
    private void handleMongoSelectUserDialog() {

        // WE CREATE AN OBJECT OF THE "View" CLASS
        // USING THE LAYOUT INFLATER INFLATE "dialog_select_user_activity.xml" INSIDE THIS VIEW
        View view = getLayoutInflater().inflate(R.layout.dialog_select_user_activity, null);

        // NOW CREATE AN OBJECT OF "AlertDialog.Builder"
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // SET THE VIEW OF THE "AlertDialog.Builder" TO THE VIEW THAT WE JUST INFLATED
        builder.setView(view).show();

        // SET THE COMPONENTS FOR "dialog_select_user_activity.xml"
        Button btnClick = view.findViewById(R.id.btnSelectUserDial);
        final EditText emailEdit = view.findViewById(R.id.emailSelectUserDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordSelectUserDial);

        // WHEN THE LOG IN BUTTON IS CLICKED WE WILL SEND THE EMAIL AND PASSWORD TO THE SERVER
        // TO SEND THE DATA TO THE SERVER WE NEED  AN OBJECT OF RETROFIT CLASS IN MainActivity.java "private Retrofit retrofit;"
        // WE ALSO NEED AN OBJECT OF THE RETROFIT INTERFACE IN MainActivity.java "private InterfaceRetrofit interfaceRetrofit;"
        btnClick.setOnClickListener(view1 -> {

            if (emailEdit.getText().toString().equals("") || passwordEdit.getText().toString().equals("")){
                Toast.makeText(MongoActivity.this,
                        getResources().getString(R.string.fillallfields), Toast.LENGTH_LONG).show();
            } else {

                // CREATE AN OBJECT OF "HashMap" WHERE KEY AND VALUE TYPE WILL BE STRINGS "<String, String>"
                HashMap<String, String> map = new HashMap<>();

                // PUT THE "email" AND "password" INSIDE THIS MAP
                map.put("email", emailEdit.getText().toString());
                map.put("password", passwordEdit.getText().toString());

                // CREATE AN OBJECT OF THE "Call" CLASS WHICH WILL HOLD THE "MongoVariables"
                // REFER THIS TO THE CALL OBJECT RETURNED BY THE retrofitInterface WHICH EXPECTS OBJECT OF "map" CLASS
                Call<MongoVariables> call = MainActivity.interfaceRetrofit.selectUserMongo(map);

                // TO EXECUTE THE HTTP REQUEST USE THE "call.enqueue" METHOD WHICH EXPECTS A CALLBACK LISTENER
                call.enqueue(new Callback<MongoVariables>() {

                    // THE "onResponse" METHOD IS CALLED WHEN THE SERVER RESPONDS TO OUR HTTP REQUEST
                    // THE FOLLOWING IS REFERENCED IN THE App.js NODE.JS FILE
                    @Override
                    public void onResponse(Call<MongoVariables> call, Response<MongoVariables> response) {

                        if (response.code() == 200) {

                            // EXTRACT THE NAME FROM THE RESPONSE WITH AN OBJECT OF "MongoVariables"
                            // GO TO THE "MongoVariables" AND CREATE GETTERS
                            MongoVariables result = response.body();

                            Gson gson = new Gson();
                            String json = gson.toJson(result);
                            //System.out.println("JSON sent back from Node.js = " + json);

                            Toast.makeText(MongoActivity.this, getResources().getString(R.string.entryfound),
                                    Toast.LENGTH_LONG).show();

                            // IF "(response.code() == 200)" CREATE AN OBJECT OF "AlertDialog.Builder"
                            // THE TITLE OF THE BUILDER WILL BE THE NAME OF THE LOGGED IN USER
                            //AlertDialog.Builder builder1 = new AlertDialog.Builder(MongoActivity.this);
                            // INSIDE THE "setTitle" METHOD SHOW THE NAME OF THE LOGGED IN USER
                            //assert result != null;
                            //builder1.setTitle(result.getName());
                            // THE MESSAGE OF THE BUILDER WILL CONTAIN THE EMAIL OF THE LOGGED IN USER
                            //builder1.setMessage(result.getEmail());
                            // SHOW THE ALERT DIALOG
                            //builder1.show();

                            jsonDataString = "[" + json + "]";
                            addItemsFromJSON();

                        } else if (response.code() == 404) {
                            Toast.makeText(MongoActivity.this, getResources().getString(R.string.wrongcred),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    // 034 - THE "onFailure" METHOD IS CALLED WHEN THE HTTP REQUEST FAILS
                    @Override
                    public void onFailure(Call<MongoVariables> call, Throwable t) {
                        Toast.makeText(MongoActivity.this, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THIS METHOD REFRESHES THE RECYCLERVIEW TO DISPLAY AN ENTRY WHEN IT IS INSERTED
    public static void refreshMongoEntry(String email, String password) {

        HashMap<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("password", password);
        Call<MongoVariables> call = MainActivity.interfaceRetrofit.selectUserMongo(map);

        call.enqueue(new Callback<MongoVariables>() {

            @Override
            public void onResponse(Call<MongoVariables> call, Response<MongoVariables> response) {

                if (response.code() == 200) {
                    MongoVariables result = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(result);
                    //System.out.println("JSON sent back from Node.js = " + json);

                    jsonDataString = "[" + json + "]";
                    addItemsFromJSON();
                } else if (response.code() == 404) {
                    Toast.makeText(mRecyclerView.getContext(), mRecyclerView.getContext().getResources().getString(R.string.wrongcred),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MongoVariables> call, Throwable t) {
                Toast.makeText(mRecyclerView.getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // THIS METHOD INSERTS AN INDIVIDUAL ENTRY
    private void handleMongoInsertDialog() {

        // FOLLOW THE SAME PROCESS AS IN "handleLoginDialog" FOR "signup_dialog.xml"

        View view = getLayoutInflater().inflate(R.layout.dialog_insert_activity, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view).show();

        Button btnClick = view.findViewById(R.id.btnInsertDial);
        final EditText nameEdit = view.findViewById(R.id.nameInsertDial);
        final EditText emailEdit = view.findViewById(R.id.emailInsertDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordInsertDial);

        btnClick.setOnClickListener(view1 -> {

            if (nameEdit.getText().toString().equals("") ||
                    emailEdit.getText().toString().equals("") ||
                    passwordEdit.getText().toString().equals("")) {
                Toast.makeText(MongoActivity.this,
                        getResources().getString(R.string.fillallfields), Toast.LENGTH_LONG).show();
            } else {

                HashMap<String, String> map = new HashMap<>();

                long timeStampLong = System.currentTimeMillis()/1000;
                String timeStamp = Long.toString(timeStampLong);

                map.put("date", timeStamp);
                map.put("name", nameEdit.getText().toString());
                map.put("email", emailEdit.getText().toString());
                map.put("password", passwordEdit.getText().toString());

                // CREATE AN OBJECT OF "Call" CLASS
                // WHEN WE SIGN UP THE SERVER WILL NOT BE SENDING ANY NAME OR EMAIL SO THE CALL OBJECT WILL BE "Void"
                Call<Void> call = MainActivity.interfaceRetrofit.insertMongo(map);

                // TO EXECUTE THE HTTP REQUEST USE THE "call.enqueue" METHOD WHICH EXPECTS A CALLBACK LISTENER
                // ADD THE FOLLOWING TO THE MANIFEST "<uses-permission android:name="android.permission.INTERNET" />"
                // ALSO ADD THIS FOR ANDROID 10 "android:usesCleartextTraffic="true""
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        // 041 - IF "(response.code() == 200)" THEN WE SUCCESSFULLY SIGNED UP
                        if (response.code() == 200) {
                            Toast.makeText(MongoActivity.this,
                                    getResources().getString(R.string.entryinsert), Toast.LENGTH_LONG).show();
                            // UPDATE THE RECYCLER VIEW WITH THE CHANGES
                            refreshMongoEntry(emailEdit.getText().toString(), passwordEdit.getText().toString());
                        } else if (response.code() == 400) {
                            Toast.makeText(MongoActivity.this,
                                    getResources().getString(R.string.alreadyexist), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MongoActivity.this, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THIS METHOD DELETES ALL ENTRIES WITH AN ALERT DIALOG WARNING
    private void handleMongoDeleteAllDialog() {

        new android.app.AlertDialog.Builder(MongoActivity.this)
                .setMessage("Delete all entries?" + "\n\n" + "Long click on list items to delete individual users")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {

                    InterfaceRetrofit api = MainActivity.retrofit.create(InterfaceRetrofit.class);
                    Call<Void> call = api.deleteAllMongo();

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {

                            Toast.makeText(MongoActivity.this, "All entries deleted",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MongoActivity.this, t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    Toast.makeText(MongoActivity.this, "All entries Deleted",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // THIS METHOD DELETED AN ENTRY BASED ON THE UNIQUE EMAIL KEY
    public static void handleMongoDeleteEmailDialog(String name, String email, String password) {

        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("password", password);
        Call<Void> call = MainActivity.interfaceRetrofit.deleteUserMongo(map);

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                Toast.makeText(RecyclerAdapter.cText, RecyclerAdapter.cText.getResources().getString(R.string.entrydelete),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RecyclerAdapter.cText, t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // IF ANY CHANGES HAVE BEEN MADE UPDATE THE JSON CONTAINING THE DATA AND ADD TO THE RECYCLERVIEW
    public static void addItemsFromJSON() {
        try {
            JSONArray jsonArray = new JSONArray(jsonDataString);

            // SET UP THE RECYCLER VIEW TO HOLD THE DATA
            List<Object> viewItems = new ArrayList<>();
            mRecyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mRecyclerView.getContext());
            mRecyclerView.setLayoutManager(layoutManager);
            RecyclerView.Adapter mAdapter = new RecyclerAdapter(mRecyclerView.getContext(), viewItems);

            for (int i=0; i<jsonArray.length(); ++i) {
                JSONObject itemObj = jsonArray.getJSONObject(i);
                String date = itemObj.getString("date");
                String name = itemObj.getString("name");
                String email = itemObj.getString("email");
                String password = itemObj.getString("password");
                RecyclerVariables variables = new RecyclerVariables(
                        MainActivity.formatDate(date,"yyyy/MM/dd' ('HH:mm:ss)"),
                        name,
                        email,
                        password);
                viewItems.add(variables);
                mRecyclerView.setAdapter(mAdapter);
            }

        } catch (JSONException e) {
            Log.d(TAG, "addItemsFromJSON: ", e);
        }
    }

    // THIS METHOD SAVES CODE TO CREATE NAME, EMAIL, AND PASSWORD ARRAYS IN THE EXPORT XLS METHOD
    private List<String> jsonBuilder(String column) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonDataString);
        StringBuilder usersBuild = new StringBuilder();
        for (int ub = 0; ub < jsonArray.length(); ub++) {
            JSONObject itemObj = jsonArray.getJSONObject(ub);
            usersBuild
                    .append(itemObj.getString(column))
                    .append(",");
        }
        usersBuild.setLength(usersBuild.length() - 1);
        List<String> nameValues = new ArrayList<>(Arrays.asList(usersBuild.toString().split(",")));
        return nameValues;
    }

    // EXPORT ALL DATA AS XLS FILE

    public void handleMongoExportXLS() throws JSONException {

        Workbook exWorkBook = new HSSFWorkbook();
        Sheet exSheet = exWorkBook.createSheet("Export");

        // SET SOME CELL FORMATTING STYLES
        CellStyle cellYellow = exWorkBook.createCellStyle();
        cellYellow.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellYellow.setBorderTop(BorderStyle.THIN);
        cellYellow.setBorderBottom(BorderStyle.THIN);
        cellYellow.setBorderLeft(BorderStyle.THIN);
        cellYellow.setBorderRight(BorderStyle.THIN);
        //cellYellow.setAlignment(HorizontalAlignment.CENTER);

        CellStyle borderThin = exWorkBook.createCellStyle();
        borderThin.setBorderTop(BorderStyle.THIN);
        borderThin.setBorderBottom(BorderStyle.THIN);
        borderThin.setBorderLeft(BorderStyle.THIN);
        borderThin.setBorderRight(BorderStyle.THIN);

        CellStyle centerBorder = exWorkBook.createCellStyle();
        centerBorder.setBorderTop(BorderStyle.THIN);
        centerBorder.setBorderBottom(BorderStyle.THIN);
        centerBorder.setBorderLeft(BorderStyle.THIN);
        centerBorder.setBorderRight(BorderStyle.THIN);

        // GET THE CURRENT TIMESTAMP CONVERT IT TO HUMAN READABLE AND ADD TO FILE NAME AND TITLE
        long timeStampLong = System.currentTimeMillis();
        String timeStamp = Long.toString(timeStampLong);
        String fileName = TAG + "_" + MainActivity.formatDate(timeStamp,"yyyy-MM-dd'_'HH.mm.ss");
        String fileString = fileName + ".xls";

        // SET WIDTH OF FIRST FIVE COLUMNS
        exSheet.setColumnWidth(0,(10*500));
        exSheet.setColumnWidth(1,(10*500));
        exSheet.setColumnWidth(2,(10*500));
        exSheet.setColumnWidth(3,(10*500));
        exSheet.setColumnWidth(4,(10*500));

        // DECLARE FIRST ROWS FOR STUDENT INFO
        Row row01 = exSheet.createRow(0);

        // SET TITLE FOR STUDENT INFO
        Cell row01coll01 = row01.createCell(0);
        row01coll01.setCellValue("Name");
        row01coll01.setCellStyle(cellYellow);
        Cell row01coll02 = row01.createCell(1);
        row01coll02.setCellValue("Email");
        row01coll02.setCellStyle(cellYellow);
        Cell row01coll03 = row01.createCell(2);
        row01coll03.setCellValue("Password");
        row01coll03.setCellStyle(cellYellow);
        Cell row01coll04 = row01.createCell(3);
        row01coll04.setCellValue("Date");
        row01coll04.setCellStyle(cellYellow);
        Cell row01coll05 = row01.createCell(4);
        row01coll05.setCellValue("Time");
        row01coll05.setCellStyle(cellYellow);

        File file = new File(getFilesDir(), fileString);
        FileOutputStream outputStream =null;

        JSONArray jsonArray = new JSONArray(jsonDataString);

        // GET THE LIST OF ALL DATE ENTRIES AND ADD TO STRING BUILDER
        StringBuilder dateUsersBuild = new StringBuilder();
        for(int dj = 0; dj < jsonArray.length(); dj++){
            JSONObject dateitemObj = jsonArray.getJSONObject(dj);
            dateUsersBuild
                    .append(MainActivity.formatDate(dateitemObj.getString("date"),"dd/MM/yyyy"))
                    .append(",");
        }
        dateUsersBuild.setLength(dateUsersBuild.length() - 1);

        // GET THE LIST OF ALL TIME ENTRIES AND ADD TO STRING BUILDER
        StringBuilder timeUsersBuild = new StringBuilder();
        for(int dj = 0; dj < jsonArray.length(); dj++){
            JSONObject timeitemObj = jsonArray.getJSONObject(dj);
            timeUsersBuild
                    .append(MainActivity.formatDate(timeitemObj.getString("date"),"HH:mm:ss"))
                    .append(",");
        }
        timeUsersBuild.setLength(timeUsersBuild.length() - 1);

        List<String> nameValues = jsonBuilder("name");
        List<String> emaiValues = jsonBuilder("email");
        List<String> passValues = jsonBuilder("password");
        List<String> dateValues = new ArrayList<>(Arrays.asList(dateUsersBuild.toString().split(",")));
        List<String> timeValues = new ArrayList<>(Arrays.asList(timeUsersBuild.toString().split(",")));

        // SET UP AND EXPORT THE DATA
        for (int av = 0; av < dateValues.size(); av ++) {

            Row rowAV = exSheet.createRow(1 + av);

            Cell cellNV = rowAV.createCell(0);
            cellNV.setCellValue(nameValues.get(av));
            cellNV.setCellStyle(borderThin);

            Cell cellEV = rowAV.createCell(1);
            cellEV.setCellValue(emaiValues.get(av));
            cellEV.setCellStyle(borderThin);

            Cell cellPV = rowAV.createCell(2);
            cellPV.setCellValue(passValues.get(av));
            cellPV.setCellStyle(centerBorder);

            Cell cellDV = rowAV.createCell(3);
            cellDV.setCellValue(dateValues.get(av));
            cellDV.setCellStyle(borderThin);

            Cell cellTV = rowAV.createCell(4);
            cellTV.setCellValue(timeValues.get(av));
            cellTV.setCellStyle(borderThin);
        }

        try {
            outputStream = new FileOutputStream(file);
            exWorkBook.write(outputStream);
            //Toast.makeText(getApplicationContext(),"Export Successful",Toast.LENGTH_LONG).show();
            Context context = getApplicationContext();
            Uri path = FileProvider.getUriForFile(context, MainActivity.packageName + ".fileprovider", file);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/xls");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(fileIntent);

        } catch (java.io.IOException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(),"Export Error",Toast.LENGTH_LONG).show();
            try {
                assert outputStream != null;
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}