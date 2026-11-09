package com.example.managedatatypes;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.TypedValue;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.sundeepk.compactcalendarview.domain.Event;

import org.junit.Test;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void calendarDisplaysEventsAndChangesMonth() throws InterruptedException {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.calendarMain)).perform(click());
            Calendar date = Calendar.getInstance();
            date.set(2025, Calendar.JUNE, 15, 12, 0, 0);
            date.set(Calendar.MILLISECOND, 0);
            scenario.onActivity(activity -> {
                CalendarFragment.compactCalendarView.setCurrentDate(date.getTime());
                CalendarFragment.compactCalendarView.addEvent(new Event(0xff00ff00, date.getTimeInMillis(), "test"));
                assertEquals(1, CalendarFragment.compactCalendarView.getEvents(date.getTime()).size());
            });
            onView(withId(R.id.compactcalendar_view)).perform(swipeLeft());
            Thread.sleep(1000);
            scenario.onActivity(activity -> assertNotEquals(
                    Calendar.JUNE, monthOf(CalendarFragment.compactCalendarView.getFirstDayOfCurrentMonth())));
        }
    }

    private int monthOf(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    @Test
    public void navigationBarAppearanceMatchesDayAndNightThemes() {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1);
        assertNavigationBarAppearance(Configuration.UI_MODE_NIGHT_NO, true);
        assertNavigationBarAppearance(Configuration.UI_MODE_NIGHT_YES, false);
    }

    private void assertNavigationBarAppearance(int nightMode, boolean expectedLightNavigationBar) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightMode;
        Context themedContext = context.createConfigurationContext(configuration);
        themedContext.setTheme(R.style.Theme_LetsRetrofit_NoActionBar);
        TypedValue value = new TypedValue();
        assertTrue(themedContext.getTheme().resolveAttribute(
                android.R.attr.windowLightNavigationBar, value, true));
        assertEquals(expectedLightNavigationBar, value.data != 0);
    }

    @Test
    public void backClosesCalendarThenShowsExitConfirmation() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.calendarMain)).perform(click());
            onView(withId(R.id.spinnerCalendar)).check(matches(isDisplayed()));
            pressBack();
            onView(withId(R.id.calendarMain)).check(matches(isDisplayed()));
            pressBack();
            onView(withText("Are you sure you want to exit?")).check(matches(isDisplayed()));
            onView(withText("No")).perform(click());
        }
    }

    @Test
    public void deletingRowRemovesOnlySelectedEntry() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(context);
        String email = UUID.randomUUID() + "@example.test";
        helper.insertUser("123", "test-user", email, "secret");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                MainActivity.recycleDelete = activity.getString(R.string.SQLiteActivityRecycle);
                List<Object> rows = new ArrayList<>();
                rows.add(new RecyclerVariables("123", "test-user", email, "secret"));
                rows.add(new RecyclerVariables("123", "remaining-user", "other@example.test", "secret"));
                RecyclerView recyclerView = new RecyclerView(activity);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                recyclerView.setAdapter(new RecyclerAdapter(activity, rows));
                activity.setContentView(recyclerView);
            });

            onView(withText(email)).perform(longClick());
            onView(withText("Yes")).perform(click());
            onView(withText("remaining-user")).check(matches(isDisplayed()));
            assertNull(helper.getUserByEmail(email));
        } finally {
            String id = helper.getUserFieldByEmail(email, SQLiteContract.SaveUsersTable._ID);
            if (id != null) {
                helper.deleteUser(Integer.valueOf(id));
            }
            helper.close();
        }
    }

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