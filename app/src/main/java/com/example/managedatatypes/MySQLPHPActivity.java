package com.example.managedatatypes;

import androidx.annotation.NonNull;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MySQLPHPActivity extends EdgeToEdgeActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "MySQLPHP";

    public static String jsonDataString = "[" +
            "{\"date\":\"1625564252\",\"name\":\"Henry\",\"email\":\"henry@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Marta\",\"email\":\"marta@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Katie\",\"email\":\"katie@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Teddy\",\"email\":\"teddy@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Wayne\",\"email\":\"wayne@email.com\",\"password\":\"123abc\"}," +
            "{\"date\":\"1625564252\",\"name\":\"Fredy\",\"email\":\"fredy@email.com\",\"password\":\"123abc\"}" +
            "]";

    public static RecyclerView mRecyclerView;

    public static class RetrofitClientInstance {
        private static Retrofit retrofit;
        public static Retrofit getRetrofitInstance() {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(MainActivity.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mysqlphp);

        // INITIALIZE TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbarMySQLPHP);
        setSupportActionBar(toolbar);

        // SET UP THE BASE URL DEPENDING ON WHICH ACTIVITY WE ARE IN, URL IN STRING RESOURCES
        MainActivity.baseUrl = getResources().getString(R.string.mysqlphpurl);

        // SET UP A TAG TO REFERENCE THE ACTIVITY FROM THE RECYCLER ADAPTER
        MainActivity.recycleDelete = getResources().getString(R.string.MySQLPHPActivityRecycle);

        mRecyclerView = findViewById(R.id.my_recycler_view_mysqlphp);

        // SET UP THE SPINNER, OPTIONS CAN BE FOUND IN STRING RESOURCES
        Spinner spinnerMySQLPHP = findViewById(R.id.spinnerMySQLPHP);
        ArrayAdapter<CharSequence> adapterSpinnerMySQLPHP = ArrayAdapter.createFromResource(this,
                R.array.spinnerItemsMySQLPHP, android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerMySQLPHP.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMySQLPHP.setAdapter(adapterSpinnerMySQLPHP);
        spinnerMySQLPHP.setOnItemSelectedListener(this);

    }

    // SET UP THE ONCLICKLISTENER FOR THE SPINNER
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        findViewById(R.id.btnMySQLPHPSpinner).setOnClickListener(view1 -> {
            Runnable[] actions = {
                    () -> handleMySQLPHPSelectAllDialog(getApplicationContext()),
                    this::handleMySQLPHPSelectUserDialog,
                    this::handleMySQLPHPInsertDialog,
                    this::handleMySQLPHPDeleteAllDialog,
                    this::exportMySQLPHPXls
            };
            if (position >= 0 && position < actions.length) {
                actions[position].run();
            } else {
                android.util.Log.w(TAG, "Ignoring unknown PHP/MySQL action: " + position);
            }
        });
    }

    private void exportMySQLPHPXls() {
        try {
            handleMySQLPHPExportXLS();
        } catch (JSONException e) {
            android.util.Log.e(TAG, "Unable to export PHP/MySQL data", e);
            Toast.makeText(this, "Unable to export data", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // THIS METHOD IS CALLED FROM THE RECYCLER VIEW WHEN CLICKED TO UPDATE ENTRY
    public static void handleMySQLPHPUpdateDialog(View view, Context context, String email) {

        Button btnClick = view.findViewById(R.id.btnUpdateDial);
        final EditText nameEdit = view.findViewById(R.id.nameUpdateDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordUpdateDial);
        final TextView titleEdit = view.findViewById(R.id.titleUpdateDial);

        titleEdit.setText(email);

        btnClick.setOnClickListener(view1 -> {

            if (nameEdit.getText().toString().isEmpty() ||
                    passwordEdit.getText().toString().isEmpty()) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.fillallfields), Toast.LENGTH_LONG).show();
            } else {

                //long timeStampLong = System.currentTimeMillis()/1000;
                //String timeStamp = Long.toString(timeStampLong);

                InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
                Call<Void> call = apiService.updateMySQLPHP(nameEdit.getText().toString(),
                                                              email,
                                                              passwordEdit.getText().toString());

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(context, context.getResources().getString(R.string.entryupdated), Toast.LENGTH_LONG).show();

                            // UPDATE THE RECYCLER VIEW WITH THE CHANGES
                            RecyclerAdapter.nameViewUpdate.setText(nameEdit.getText().toString());
                            RecyclerAdapter.passwordViewUpdate.setText(passwordEdit.getText().toString());
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // THIS METHOD IS CALLED FROM THE SPINNER TO POPULATE THE RECYCLERVIEW WITH ALL DB ENTRIES
    private void handleMySQLPHPSelectAllDialog(Context context) {

        InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
        Call<List<MySQLPHPVariables>> call = apiService.selectAllMySQLPHP();
        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<List<MySQLPHPVariables>> call, @NonNull Response<List<MySQLPHPVariables>> response) {

                List<MySQLPHPVariables> posts = response.body();

                Gson gson = new Gson();
                //System.out.println("JSON sent back from Node.js = " + json);

                jsonDataString = gson.toJson(posts);
                addItemsFromJSON();
            }

            @Override
            public void onFailure(@NonNull Call<List<MySQLPHPVariables>> call, @NonNull Throwable throwable) {
                Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(throwable.getMessage());
            }
        });
    }

    // THIS METHOD SELECTS AN ENTRY BASED ON THE DETAILS ENTERED IN THE ALERT DIALOG
    private void handleMySQLPHPSelectUserDialog() {

        View view = getLayoutInflater().inflate(R.layout.dialog_select_user_activity, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view).show();

        Button btnClick = view.findViewById(R.id.btnSelectUserDial);
        final EditText emailEdit = view.findViewById(R.id.emailSelectUserDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordSelectUserDial);

        btnClick.setOnClickListener(view1 -> {

            if (emailEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                Toast.makeText(MySQLPHPActivity.this,
                        getResources().getString(R.string.fillallfields),Toast.LENGTH_LONG).show();
            } else {

                InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
                Call<MongoVariables> call = apiService.selectUserMySQLPHP(emailEdit.getText().toString(),
                        passwordEdit.getText().toString());

                call.enqueue(new Callback<>() {

                    @Override
                    public void onResponse(@NonNull Call<MongoVariables> call, @NonNull Response<MongoVariables> response) {

                        MongoVariables result = response.body();
                        Gson gson = new Gson();
                        String json = gson.toJson(result);
                        //System.out.println("JSON sent back from Node.js = " + json);

                        if (json.equals("{}")) {
                            Toast.makeText(MySQLPHPActivity.this, getResources().getString(R.string.wrongcred),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            jsonDataString = "[" + json + "]";
                            addItemsFromJSON();
                            Toast.makeText(MySQLPHPActivity.this, getResources().getString(R.string.entryfound),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MongoVariables> call, @NonNull Throwable t) {
                        Toast.makeText(MySQLPHPActivity.this, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THIS METHOD REFRESHES THE RECYCLERVIEW TO DISPLAY AN ENTRY WHEN IT IS INSERTED
    public static void refreshMySQLPHPEntry(String email, String password) {

        InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
        Call<MongoVariables> call = apiService.selectUserMySQLPHP(email,password);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<MongoVariables> call, @NonNull Response<MongoVariables> response) {

                MongoVariables result = response.body();
                Gson gson = new Gson();
                String json = gson.toJson(result);
                //System.out.println("JSON sent back from Node.js = " + json);

                if (json.equals("{}")) {
                    Toast.makeText(mRecyclerView.getContext(), mRecyclerView.getContext().getResources().getString(R.string.wrongcred),
                            Toast.LENGTH_LONG).show();
                } else {
                    jsonDataString = "[" + json + "]";
                    addItemsFromJSON();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MongoVariables> call, @NonNull Throwable t) {
                Toast.makeText(mRecyclerView.getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // THIS METHOD INSERTS AN INDIVIDUAL ENTRY
    private void handleMySQLPHPInsertDialog() {

        View view = getLayoutInflater().inflate(R.layout.dialog_insert_activity, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view).show();

        Button btnClick = view.findViewById(R.id.btnInsertDial);
        final EditText nameEdit = view.findViewById(R.id.nameInsertDial);
        final EditText emailEdit = view.findViewById(R.id.emailInsertDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordInsertDial);

        btnClick.setOnClickListener(view1 -> {

            if (nameEdit.getText().toString().isEmpty() ||
                    emailEdit.getText().toString().isEmpty() ||
                    passwordEdit.getText().toString().isEmpty()) {
                Toast.makeText(MySQLPHPActivity.this,
                        getResources().getString(R.string.fillallfields), Toast.LENGTH_LONG).show();
            } else {

                long timeStampLong = System.currentTimeMillis()/1000;
                String timeStamp = Long.toString(timeStampLong);

                InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
                Call<MongoVariables> call = apiService.insertMySQLPHP(timeStamp,
                        nameEdit.getText().toString(),
                        emailEdit.getText().toString(),
                        passwordEdit.getText().toString());

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<MongoVariables> call, @NonNull Response<MongoVariables> response) {

                        MongoVariables result = response.body();
                        Gson gson = new Gson();
                        String json = gson.toJson(result);
                        //System.out.println("JSON sent back from Node.js = " + json);

                        String newstring = json.replace("{\"email\":\"", "").replace("\"}", "");

                        if (newstring.equals(emailEdit.getText().toString())) {
                            Toast.makeText(MySQLPHPActivity.this, getResources().getString(R.string.alreadyexist),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            jsonDataString = "[" + json + "]";
                            addItemsFromJSON();
                            Toast.makeText(MySQLPHPActivity.this, getResources().getString(R.string.entryinsert),
                                    Toast.LENGTH_LONG).show();

                            // UPDATE THE RECYCLER VIEW WITH THE CHANGES
                            refreshMySQLPHPEntry(emailEdit.getText().toString(), passwordEdit.getText().toString());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MongoVariables> call, @NonNull Throwable t) {
                        Toast.makeText(MySQLPHPActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // THIS METHOD DELETES ALL ENTRIES WITH AN ALERT DIALOG WARNING
    private void handleMySQLPHPDeleteAllDialog() {

        new android.app.AlertDialog.Builder(MySQLPHPActivity.this)
                .setMessage("""
                        Delete all entries?

                        Long click on list items to delete individual users""")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {

                    InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
                    Call<Void> call = apiService.deleteAllMySQLPHP();

                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                            Toast.makeText(MySQLPHPActivity.this, "All entries deleted",
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(MySQLPHPActivity.this, t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    // THIS METHOD DELETED AN ENTRY BASED ON THE UNIQUE EMAIL KEY
    public static void handleMySQLPHPDeleteEmailDialog(String name, String email, String password) {

        InterfaceRetrofit apiService = RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
        Call<Void> call = apiService.deleteUserMySQLPHP(name, email, password);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                Toast.makeText(RecyclerAdapter.cText, RecyclerAdapter.cText.getResources().getString(R.string.entrydelete),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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
        return new ArrayList<>(Arrays.asList(usersBuild.toString().split(",")));
    }

    // EXPORT ALL DATA AS XLS FILE

    public void handleMySQLPHPExportXLS() throws JSONException {

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