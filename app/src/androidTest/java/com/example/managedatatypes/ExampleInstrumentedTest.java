package com.example.managedatatypes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.managedatatypes", appContext.getPackageName());
    }

    @Test
    public void sqliteDatabaseOpensAndSeedsInitialRows() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String testDatabaseName = "sqlite-activation-test.db";
        testContext.deleteDatabase(testDatabaseName);

        SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(testContext, testDatabaseName);
        SQLiteDatabase database = helper.getReadableDatabase();
        try (Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + SQLiteContract.SaveUsersTable.TABLE_USERS, null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(4, cursor.getInt(0));
        } finally {
            helper.close();
            testContext.deleteDatabase(testDatabaseName);
        }
    }

    @Test
    public void sqliteCrudHandlesEmptyTablesAndMissingUsers() {
        Context testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String testDatabaseName = "sqlite-crud-test.db";
        testContext.deleteDatabase(testDatabaseName);

        SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(testContext, testDatabaseName);
        try {
            assertEquals("mary", helper.getUserFieldByEmail(
                    "mary@email.com", SQLiteContract.SaveUsersTable.COLUMN_NAME));
            assertNull(helper.getUserFieldByEmail(
                    "missing@email.com", SQLiteContract.SaveUsersTable.COLUMN_NAME));

            helper.deleteAllUsers();
            assertTrue(helper.getAllUsers().isEmpty());
            assertNull(helper.getUserFieldByEmail(
                    "mary@email.com", SQLiteContract.SaveUsersTable.COLUMN_NAME));

            helper.insertUser("123", "O'Brien", "o'hara@example.com", "secret");
            helper.updateUser("Updated", "new-secret", "o'hara@example.com");
            SQLiteUser updatedUser = helper.getUserByEmail("o'hara@example.com");
            assertNotNull(updatedUser);
            assertEquals("Updated", updatedUser.getName());
            assertEquals("new-secret", updatedUser.getPassword());

            String userId = helper.getUserFieldByEmail(
                    "o'hara@example.com", SQLiteContract.SaveUsersTable._ID);
            helper.deleteUser(Integer.valueOf(userId));
            assertNull(helper.getUserByEmail("o'hara@example.com"));
        } finally {
            helper.close();
            testContext.deleteDatabase(testDatabaseName);
        }
    }
}