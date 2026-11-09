package com.example.managedatatypes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "userdetails.db";
    private static final int DATABASE_VERSION = 1;

    // THESE VALUES ARE TO INSERT INTO THE DATABASE WHEN THE APP IS FIRST INSTALLED

    long timeStampLong = System.currentTimeMillis()/1000;

    private final String[] dateArray = {
            Long.toString(timeStampLong-86400),
            Long.toString(timeStampLong-172800),
            Long.toString(timeStampLong-259200),
            Long.toString(timeStampLong-345600)};

    private final String[] nameArray = {
            "mary",
            "john",
            "paul",
            "jane"};

    private final String[] emailArray = {
            "mary@email.com",
            "john@email.com",
            "paul@email.com",
            "jane@email.com"};

    public SQLiteDatabaseHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    SQLiteDatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    // THIS METHOD WILL BE CALLED THE FIRST TIME WE ACCESS THIS DATABASE
    // THIS IS WHERE WE CREATE THE INITIAL DATABASE

    @Override
    public void onCreate(SQLiteDatabase dbDeclare) {

        // THE MoodContract CLASS VARIABLES ARE PASSED IN HERE
        // THIS onCreate METHOD WILL ONLY BE CALLED THE FIRST TIME WE ACCESS THIS DATABASE

        final String sqlCreateUsersTable = "CREATE TABLE " +
                SQLiteContract.SaveUsersTable.TABLE_USERS + " ( " +
                SQLiteContract.SaveUsersTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SQLiteContract.SaveUsersTable.COLUMN_DATE + " TEXT, " +
                SQLiteContract.SaveUsersTable.COLUMN_NAME + " TEXT, " +
                SQLiteContract.SaveUsersTable.COLUMN_EMAIL + " TEXT, " +
                SQLiteContract.SaveUsersTable.COLUMN_PASSWORD + " TEXT " +
                ")";
        dbDeclare.execSQL(sqlCreateUsersTable);

        // WE ALSO WANT TO FILL THE DATABASE WITH QUESTION RIGHT AWAY WITH THE FOLLOWING METHOD

        fillUsersTable(dbDeclare);
    }

    // IF WE EVER DECIDE TO MAKE CHANGES TO THIS DATABASE I.E. ADD ANOTHER COLUMN WE CAN'T JUST ADD TO onCreate
    // WE NEED TO TELL THE APP TO DO SO USING onUpgrade METHOD - CHANGE "private static final int DATABASE_VERSION = 2;"
    // WE ALSO DEFINE IN onUpgrade HOW WE WANT TO UPGRADE THE DATABASE - DELETE OLD TABLE AND CALL onCreate AGAIN
    // WHEN THE DATABASE HAS BEEN CHANGED IT WILL DROP EXISTING TABLE AND CREATE A NEW ONE
    // OR YOU CAN JUST DELETE THE APP FROM YOUR PHONE AND REINSTALL
    // THIS PROCESS IS ONLY DONE IF THE APP IS ALREADY IN PRODUCTION

    @Override
    public void onUpgrade(SQLiteDatabase dbUpgrade, int oldVersion, int newVersion) {
        dbUpgrade.execSQL("DROP TABLE IF EXISTS " + SQLiteContract.SaveUsersTable.TABLE_USERS);
        onCreate(dbUpgrade);
    }

    // IF ALL ENTRIES IN THE DATABASE HAVE BEEN REMOVED IN THE CALENDAR AND THE APP HAS BEEN REINSTALLED
    // THE DATABASE VERSION IS REVERTED TO VERSION NUMBER 1
    @Override
    public void onDowngrade(SQLiteDatabase dbDowngrade, int oldVersion, int newVersion) {
        dbDowngrade.setVersion(oldVersion);
    }

    // THESE ARE REFERENCED IN THE GETTER AND SETTER METHODS IN THE CLASS "MoodChanges.java"

    public void insertUser(String date, String name, String email, String password) throws SQLException {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_DATE, date);
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_NAME, name);
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_EMAIL, email);
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_PASSWORD, password);
        database.insertOrThrow(SQLiteContract.SaveUsersTable.TABLE_USERS, null, cv);
    }

    // THESE ARE REFERENCED IN THE GETTER AND SETTER METHODS IN THE CLASS "SQLiteUser.java"
    // FILL THE DATABASE WITH DUMMY DATA THE FIRST TIME THE APP IS INSTALLED

    private void fillUsersTable(SQLiteDatabase database) {
        for (int x = 0; x < emailArray.length; x ++) {
            SQLiteUser user = new SQLiteUser(dateArray[x], nameArray[x], emailArray[x], "123abc");
            addInitialUsers(database, user);
        }
    }

    private void addInitialUsers(SQLiteDatabase database, SQLiteUser user) {
        ContentValues cv = new ContentValues();
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_DATE, user.getDate());
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_NAME, user.getName());
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_EMAIL, user.getEmail());
        cv.put(SQLiteContract.SaveUsersTable.COLUMN_PASSWORD, user.getPassword());
        database.insertOrThrow(SQLiteContract.SaveUsersTable.TABLE_USERS, null, cv);
    }

    // WE HAVE ALREADY SAVED THE DATA IN OUR DATABASE BUT WE HAVE NO WAY TO RETRIEVE THE DATA YET
    // WE WILL MAKE THE FOLLOWING METHOD PUBLIC AS WE WANT TO ACCESS IT FROM ANOTHER CLASS
    // THE RETURN TYPE IS LIST OF "SQLiteUser" OBJECT

    public List<SQLiteUser> getAllUsers() {
        List<SQLiteUser> usersList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        try (Cursor dbCursor = database.query(
                SQLiteContract.SaveUsersTable.TABLE_USERS,
                new String[] {
                        SQLiteContract.SaveUsersTable.COLUMN_DATE,
                        SQLiteContract.SaveUsersTable.COLUMN_NAME,
                        SQLiteContract.SaveUsersTable.COLUMN_EMAIL,
                        SQLiteContract.SaveUsersTable.COLUMN_PASSWORD
                },
                null, null, null, null, SQLiteContract.SaveUsersTable._ID + " ASC")) {
            while (dbCursor.moveToNext()) {
                SQLiteUser user = new SQLiteUser();
                user.setDate(dbCursor.getString(dbCursor.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_DATE)));
                user.setName(dbCursor.getString(dbCursor.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_NAME)));
                user.setEmail(dbCursor.getString(dbCursor.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_EMAIL)));
                user.setPassword(dbCursor.getString(dbCursor.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_PASSWORD)));

                // ALL THE RETRIEVED QUESTIONS ARE ADDED TO "usersList" ARRAY LIST

                usersList.add(user);
            }
        }
        return usersList;
    }

    public void deleteUser(Integer id) throws SQLException {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(SQLiteContract.SaveUsersTable.TABLE_USERS,
                SQLiteContract.SaveUsersTable._ID + "=?", new String[] { String.valueOf(id) });
    }

    public String getUserFieldByEmail(String email, String column) {
        List<String> validColumns = Arrays.asList(
                SQLiteContract.SaveUsersTable._ID,
                SQLiteContract.SaveUsersTable.COLUMN_DATE,
                SQLiteContract.SaveUsersTable.COLUMN_NAME,
                SQLiteContract.SaveUsersTable.COLUMN_EMAIL,
                SQLiteContract.SaveUsersTable.COLUMN_PASSWORD);
        if (!validColumns.contains(column)) {
            throw new IllegalArgumentException("Unknown user column: " + column);
        }

        SQLiteDatabase database = getReadableDatabase();
        try (Cursor result = database.query(
                SQLiteContract.SaveUsersTable.TABLE_USERS,
                new String[] { column },
                SQLiteContract.SaveUsersTable.COLUMN_EMAIL + "=?",
                new String[] { email },
                null, null, null, "1")) {
            if (result.moveToFirst()) {
                return result.getString(result.getColumnIndexOrThrow(column));
            }
        }
        return null;
    }

    public SQLiteUser getUserByEmail(String email) {
        SQLiteDatabase database = getReadableDatabase();
        try (Cursor result = database.query(
                SQLiteContract.SaveUsersTable.TABLE_USERS,
                new String[] {
                        SQLiteContract.SaveUsersTable.COLUMN_DATE,
                        SQLiteContract.SaveUsersTable.COLUMN_NAME,
                        SQLiteContract.SaveUsersTable.COLUMN_EMAIL,
                        SQLiteContract.SaveUsersTable.COLUMN_PASSWORD
                },
                SQLiteContract.SaveUsersTable.COLUMN_EMAIL + "=?",
                new String[] { email },
                null, null, null, "1")) {
            if (result.moveToFirst()) {
                return new SQLiteUser(
                        result.getString(result.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_DATE)),
                        result.getString(result.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_NAME)),
                        result.getString(result.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_EMAIL)),
                        result.getString(result.getColumnIndexOrThrow(SQLiteContract.SaveUsersTable.COLUMN_PASSWORD)));
            }
        }
        return null;
    }

    public void deleteAllUsers() throws SQLException {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(SQLiteContract.SaveUsersTable.TABLE_USERS, null, null);
    }

    public void updateUser(String name, String password, String email) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteContract.SaveUsersTable.COLUMN_NAME, name);
        values.put(SQLiteContract.SaveUsersTable.COLUMN_PASSWORD, password);
        database.update(SQLiteContract.SaveUsersTable.TABLE_USERS, values,
                SQLiteContract.SaveUsersTable.COLUMN_EMAIL + "=?", new String[] { email });
    }
}
