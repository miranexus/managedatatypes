package com.example.managedatatypes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    // DECLARE ALL THE CALENDAR VARIABLES
    @SuppressLint("ConstantLocale")
    public static final Calendar currentCalendar = Calendar.getInstance(Locale.getDefault());
    @SuppressLint("ConstantLocale")
    public static final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    public static CompactCalendarView compactCalendarView;
    public static ActionBar toolbar;
    private static final String TAG = "Calendar";

    public static String jsonDataString = "";
    public static LayoutInflater inflaterDialog;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View mainTabView = inflater.inflate(R.layout.calender_fragment,container,false);

        // SET UP THE SPINNER, OPTIONS CAN BE FOUND IN STRING RESOURCES
        Spinner spinnerCalendar = mainTabView.findViewById(R.id.spinnerCalendar);
        ArrayAdapter<CharSequence> adapterSpinnerCalendar = ArrayAdapter.createFromResource(requireActivity(),
                R.array.spinnerItemsCalendar, android.R.layout.simple_spinner_dropdown_item);
        adapterSpinnerCalendar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalendar.setAdapter(adapterSpinnerCalendar);

        // SET UP THE ONCLICKLISTENER FOR THE SPINNER
        spinnerCalendar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mainTabView.findViewById(R.id.btnCalendarSpinner).setOnClickListener(v -> {
                    Runnable[] actions = {
                            () -> loadCalendarData(
                                    R.string.SQLiteCalendarRecycle,
                                    null,
                                    CalendarFragment::loadSQLite),
                            () -> loadCalendarData(
                                    R.string.MongoCalendarRecycle,
                                    R.string.mongonodeurl,
                                    CalendarFragment::loadMongo),
                            () -> loadCalendarData(
                                    R.string.MySQLPHPCalendarRecycle,
                                    R.string.mysqlphpurl,
                                    CalendarFragment::loadMySQLPHP)
                    };
                    if (position >= 0 && position < actions.length) {
                        actions[position].run();
                    } else {
                        android.util.Log.w(TAG, "Ignoring unknown calendar data source: " + position);
                    }
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // THESE ADD SOME FEATURES TO THE CALENDAR
        compactCalendarView = mainTabView.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setUseThreeLetterAbbreviation(false);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        compactCalendarView.setIsRtl(false);
        compactCalendarView.displayOtherMonthDays(false);

        // SET UP THE TOOLBAR TO DISPLAY THE MONTH AND YEAR
        toolbar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        // WHEN A DAY IS CLICKED DO THE FOLLOWING
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                // CONVERT HUMAN TME TO TIMESTAMP
                Calendar cal = Calendar.getInstance(Locale.getDefault());
                cal.setTime(dateClicked);
                long calendarMilli = cal.getTimeInMillis()/1000;
                //System.out.println("calendarMilli = " + calendarMilli);

                try {
                    // CONVERT THE DATE STRING ARRAY TO INTEGER ARRAY
                    String[] date = jsonBuilder();
                    int[] dateArray = new int[date.length];
                    for(int di = 0; di < dateArray.length; di++){
                        dateArray[di] = Integer.parseInt(date[di]);
                    }

                    // CHECK IF THE INTEGER ARRAY VALUES FALL WITHIN THE CLICKED DATE AND APPEND THE INDICES
                    StringBuilder dateFallBuild = new StringBuilder();
                    for(int cd = 0; cd < dateArray.length; cd++) {
                        if (dateArray[cd] >= calendarMilli & dateArray[cd] < calendarMilli + 86400) {
                            dateFallBuild
                                    .append(cd)
                                    .append(",");
                        }
                    }

                    // CHECK IF THERE ARE ANY DATA TO ADD TO THE CALENDAR
                    if (!dateFallBuild.toString().isEmpty()) {

                        // CONVERT THE DATE STRING INDICES ARRAY TO AN INTEGER ARRAY
                        String[] index = dateFallBuild.substring(0, dateFallBuild.length() - 1).split("\\s*,\\s*");
                        int[] indexArray = new int[index.length];
                        for(int di = 0; di < indexArray.length; di++){
                            indexArray[di] = Integer.parseInt(index[di]);
                        }

                        // SET UP THE ALERT DIALOG BUILDER TO CONTAIN THE RECYCLER VIEW
                        inflaterDialog = LayoutInflater.from(compactCalendarView.getContext());
                        View viewRecy = inflaterDialog.inflate(R.layout.dialog_recycler_activity, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(viewRecy.getContext());
                        builder.setView(viewRecy).show();

                        // SET UP THE RECYCLER VIEW TO HOLD THE DATA
                        List<Object> viewItems = new ArrayList<>();
                        RecyclerView mRecyclerView = viewRecy.findViewById(R.id.my_recycler_view_recy_dial);
                        mRecyclerView.setHasFixedSize(true);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(inflaterDialog.getContext());
                        mRecyclerView.setLayoutManager(layoutManager);
                        RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter = new RecyclerAdapter
                                (inflaterDialog.getContext(), viewItems);

                        // SELECT ONLY THE VALUES WITH THE ABOVE INDICES FROM THE JSON ARRAY AND PASS TO RECYCLERVIEW
                        JSONArray jsonArray = new JSONArray(jsonDataString);
                        for (int j : indexArray) {
                            JSONObject itemObj = jsonArray.getJSONObject(j);
                            String dateObj = itemObj.getString("date");
                            String nameObj = itemObj.getString("name");
                            String emailObj = itemObj.getString("email");
                            String passwordObj = itemObj.getString("password");
                            RecyclerVariables variables = new RecyclerVariables(
                                    MainActivity.formatDate(dateObj, "yyyy/MM/dd' ('HH:mm:ss)"),
                                    nameObj,
                                    emailObj,
                                    passwordObj);
                            viewItems.add(variables);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    } else {
                        Toast.makeText(compactCalendarView.getContext(), getResources().getString(R.string.noentries),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // CHANGES THE DISPLAYED MONTH AND YEAR WHEN CALENDAR IS SCROLLED LEFT AND RIGHT
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                toolbar.setTitle(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });
        return mainTabView;
    }

    private void loadCalendarData(int recycleMode, Integer baseUrlResourceId, Runnable loader) {
        MainActivity.recycleDelete = getString(recycleMode);
        if (baseUrlResourceId != null) {
            MainActivity.baseUrl = getString(baseUrlResourceId);
        }
        loader.run();
        Toast.makeText(compactCalendarView.getContext(), getString(R.string.addtocalendar),
                Toast.LENGTH_LONG).show();
        compactCalendarView.invalidate();
    }

    // END OF ON CREATE

    // THIS METHOD IS CALLED FROM THE RECYCLER VIEW WHEN CLICKED TO UPDATE ENTRY
    public static void handleSQLiteCalendarUpdateDialog(View view, Context context, String email ) {

        Button btnClick = view.findViewById(R.id.btnUpdateDial);
        final EditText nameEdit = view.findViewById(R.id.nameUpdateDial);
        final EditText passwordEdit = view.findViewById(R.id.passwordUpdateDial);
        final TextView titleEdit = view.findViewById(R.id.titleUpdateDial);

        titleEdit.setText(email);

        SQLiteDatabaseHelper myDBAll = new SQLiteDatabaseHelper(context);

        btnClick.setOnClickListener(view1 -> {
            myDBAll.updateUser(nameEdit.getText().toString(),
                    passwordEdit.getText().toString(),
                    email);

            Toast.makeText(context, context.getResources().getString(R.string.entryupdated),
                    Toast.LENGTH_LONG).show();

            // REFRESH THE CALENDAR WITH DATABASE UPDATES AND ADD CHANGES TO RECYCLERVIEW
            loadSQLite();
            RecyclerAdapter.nameViewUpdate.setText(nameEdit.getText().toString());
            RecyclerAdapter.passwordViewUpdate.setText(passwordEdit.getText().toString());
        });
    }

    // THIS METHOD IS CALLED FROM THE RECYCLER VIEW WHEN CLICKED TO UPDATE ENTRY
    public static void handleMongoCalendarUpdateDialog(View view, Context context, String email) {

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
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.code() == 200) {

                            Toast.makeText(context,
                                    context.getResources().getString(R.string.entryupdated), Toast.LENGTH_LONG).show();

                            // REFRESH THE CALENDAR WITH DATABASE UPDATES AND ADD CHANGES TO RECYCLERVIEW
                            loadMongo();
                            RecyclerAdapter.nameViewUpdate.setText(nameEdit.getText().toString());
                            RecyclerAdapter.passwordViewUpdate.setText(passwordEdit.getText().toString());

                        } else if (response.code() == 400) {
                            Toast.makeText(context,
                                    context.getResources().getString(R.string.alreadyexist), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context, t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THIS METHOD IS CALLED FROM THE RECYCLER VIEW WHEN CLICKED TO UPDATE ENTRY
    public static void handleMySQLPHPCalendarUpdateDialog(View view, Context context, String email) {

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

                InterfaceRetrofit apiService = MySQLPHPActivity.RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
                Call<Void> call = apiService.updateMySQLPHP(nameEdit.getText().toString(),
                        email,
                        passwordEdit.getText().toString());

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                        if(response.isSuccessful()){
                            Toast.makeText(context,context.getResources().getString(R.string.entryupdated), Toast.LENGTH_LONG).show();

                            // REFRESH THE CALENDAR WITH DATABASE UPDATES AND ADD CHANGES TO RECYCLERVIEW
                            loadMySQLPHP();
                            RecyclerAdapter.nameViewUpdate.setText(nameEdit.getText().toString());
                            RecyclerAdapter.passwordViewUpdate.setText(passwordEdit.getText().toString());

                        } else {
                            Toast.makeText(context,context.getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(context,t.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // THIS METHOD IS CALLED FROM THE SPINNER TO POPULATE THE CALENDAR VIEW WITH ALL DB ENTRIES
    public static void loadSQLite() {
        compactCalendarView.removeAllEvents();

        SQLiteDatabaseHelper dbHelperMoodMon = new SQLiteDatabaseHelper(compactCalendarView.getContext());
        List<SQLiteUser> usersList = dbHelperMoodMon.getAllUsers();
        JSONArray users = new JSONArray();
        for (SQLiteUser user : usersList) {
            JSONObject item = new JSONObject();
            try {
                item.put("date", user.getDate());
                item.put("name", user.getName());
                item.put("email", user.getEmail());
                item.put("password", user.getPassword());
                users.put(item);
            } catch (JSONException e) {
                android.util.Log.e(TAG, "Unable to load SQLite calendar data", e);
            }
        }
        jsonDataString = users.toString();
        try {
            addEvents();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // THIS METHOD IS CALLED FROM THE SPINNER TO POPULATE THE CALENDAR VIEW WITH ALL DB ENTRIES
    public static void loadMongo() {
        compactCalendarView.removeAllEvents();
        InterfaceRetrofit api = MainActivity.retrofit.create(InterfaceRetrofit.class);
        Call<List<MongoVariables>> call = api.selectAllMongo();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<MongoVariables>> call, @NonNull Response<List<MongoVariables>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(compactCalendarView.getContext(), "Code: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                List<MongoVariables> posts = response.body();
                Gson gson = new Gson();
                //System.out.println("JSON sent back from Node.js = " + json);
                jsonDataString = gson.toJson(posts);
                try {
                    addEvents();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MongoVariables>> call, @NonNull Throwable t) {
                Toast.makeText(compactCalendarView.getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // THIS METHOD IS CALLED FROM THE SPINNER TO POPULATE THE CALENDAR VIEW WITH ALL DB ENTRIES
    public static void loadMySQLPHP() {
        compactCalendarView.removeAllEvents();

        InterfaceRetrofit apiService = MySQLPHPActivity.RetrofitClientInstance.getRetrofitInstance().create(InterfaceRetrofit.class);
        Call<List<MySQLPHPVariables>> call = apiService.selectAllMySQLPHP();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<MySQLPHPVariables>> call, @NonNull Response<List<MySQLPHPVariables>> response) {
                List<MySQLPHPVariables> posts = response.body();
                Gson gson = new Gson();
                //System.out.println("JSON sent back from Node.js = " + json);
                jsonDataString = gson.toJson(posts);
                try {
                    addEvents();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MySQLPHPVariables>> call, @NonNull Throwable throwable) {
                Toast.makeText(compactCalendarView.getContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                System.out.println(throwable.getMessage());
            }
        });
    }

    // SET TOOLBAR TO CURRENT DATE AND YEAR WHEN CALENDAR IS OPENED ON RESUME

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setTitle(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
    }

    //  THIS METHOD SAVES CODE TO CREATE A DATE ARRAY FOR THE ADD EVENTS METHOD
    private static String[] jsonBuilder() throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonDataString);
        if (jsonArray.length() == 0) {
            return new String[0];
        }
        // GET THE LIST OF ALL NAME ENTRIES AND ADD TO STRING BUILDER
        StringBuilder usersBuild = new StringBuilder();
        for(int nj = 0; nj < jsonArray.length(); nj++){
            JSONObject itemObj = jsonArray.getJSONObject(nj);
            usersBuild
                    .append(itemObj.getString("date"))
                    .append(",");
        }
        usersBuild.setLength(usersBuild.length() - 1);
        return usersBuild.toString().split("\\s*,\\s*");
    }

    // THIS METHOD POPULATES THE CALENDAR WITH THE TIMESTAMPS IN THE DATABASE
    public static void addEvents() throws JSONException {

        currentCalendar.setTime(new Date());

        // GET THE FIRST DAY OF THE CURRENT MONTH WITH THE TIME RIGHT NOW
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = currentCalendar.getTime();

        // GET THE LIST OF TIMESTAMPS FROM THE DATABASE
        String[] timeStampArray = jsonBuilder();
        if (timeStampArray.length == 0) {
            return;
        }
        //System.out.println("timeStampArray = " + Arrays.toString(timeStampArray));

        // SEPARATE THE TIMESTAMPS INTO YEAR, MONTH, DAY AND ADD THEM TO ARRAYS

        StringBuilder yearsStampBuild = new StringBuilder();
        StringBuilder monthStampBuild = new StringBuilder();
        StringBuilder datesStampBuild = new StringBuilder();

        for (String s : timeStampArray) {
            Calendar calendarMilli = Calendar.getInstance(Locale.getDefault());
            long timeFirst = Integer.parseInt(s);
            long timeSecond = timeFirst * 1000;
            calendarMilli.setTimeInMillis(timeSecond);
            yearsStampBuild.append(DateFormat.format("yyyy", calendarMilli).toString()).append(",");
            monthStampBuild.append(DateFormat.format("MM", calendarMilli).toString()).append(",");
            datesStampBuild.append(DateFormat.format("dd", calendarMilli).toString()).append(",");
        }

        String[] yearsArray = yearsStampBuild.substring(0, yearsStampBuild.length() - 1).split("\\s*,\\s*");
        String[] monthArray = monthStampBuild.substring(0, monthStampBuild.length() - 1).split("\\s*,\\s*");
        String[] datesArray = datesStampBuild.substring(0, datesStampBuild.length() - 1).split("\\s*,\\s*");

        //System.out.println("yearsArray = " + Arrays.toString(yearsArray));
        //System.out.println("monthArray = " + Arrays.toString(monthArray));
        //System.out.println("datesArray = " + Arrays.toString(datesArray));

        // LOOP THROUGH THE EVENTS AND ADD THEM TO THE CALENDAR

        for (int i = 0; i < timeStampArray.length; i++) {

            currentCalendar.setTime(firstDayOfMonth);

            // SET YEAR TO POPULATE EVENTS
            currentCalendar.set(Calendar.YEAR, Integer.parseInt(yearsArray[i]));

            // SET E.G. SEPTEMBER (8) AS MONTH TO POPULATE EVENTS
            currentCalendar.set(Calendar.MONTH, Integer.parseInt(monthArray[i]) - 1);

            // SET THE DATE TO ADD THE EVENT E.G. 22ND SEPTEMBER (21)
            currentCalendar.add(Calendar.DATE, Integer.parseInt(datesArray[i]) - 1);

            // GET THE CURRENT TIME IN MILLISECONDS AS A REFERENCE FOR THE EVENTS
            long timeInMillis = currentCalendar.getTimeInMillis();

            // GET EACH TIME STAMP FOR THE EVENTS
            long timeInMillistamp = Long.parseLong(timeStampArray[i]);

            // GET THE LOCAL TIME ZONE TO COMBINE WITH THE TIME STAMP
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",Locale.getDefault());
            sdf.setTimeZone(tz);

            // CREATE A LIST TO ADD EVENTS
            List<Event> events = new ArrayList<>();
            events.add(0, new Event(Color.argb(255, 226, 29, 56), timeInMillis, sdf.format(new Date(timeInMillistamp * 1000))));

            // ADD ALL EVENTS TO THE CALENDAR
            compactCalendarView.addEvents(events);
        }
    }
}
