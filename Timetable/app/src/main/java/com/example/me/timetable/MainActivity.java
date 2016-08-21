package com.example.me.timetable;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.me.timetable.data.DbHelper;
import com.example.me.timetable.data.DbHelper.dataEntry;

import org.json.JSONException;

import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
  private final String tag = this.getClass().getSimpleName();

  private EditText searchBar;

  private ArrayAdapter<String> eventsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String syncData = "not connected";

    if ( isOnline() )
    {
      new SyncData("9:00").execute();
    }

    searchBar = (EditText) findViewById(R.id.search_bar);
    searchBar.setOnEditorActionListener(
      new EditText.OnEditorActionListener()
      {
        @Override
        public boolean onEditorAction (TextView view, int actionId, KeyEvent event)
        {
          if (actionId == EditorInfo.IME_ACTION_DONE)
          {
            performSearch();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            return true;
          }
          return false;
        }
      }
    );
  }

  private void performSearch ()
  {
    Log.e(tag, searchBar.getText().toString());
  }

  private class SyncData extends AsyncTask<URL, Void, String>
  {
    private final String tag = this.getClass().getSimpleName();

    private long now;

    private String time;

    public SyncData (String newTime)
    {
      time = newTime;
    }

    protected String doInBackground (URL... urls)
    {
      now = (new Date()).getTime();
      long timestamp = getTimestamp();

      String syncData;
      syncData = HttpService.getSync(time, timestamp);

      EventElement [] output;

      int resultLength = 0;
      try
      {
        output = ResponseParser.getElements(syncData);
        storeChanges(time, output);
        setTimestamp(now);
      }
      catch (JSONException e)
      {
        Log.e(tag, "response is empty", e);
      }
      return "";
    }

    protected void onPostExecute (String q)
    {
      Log.e(tag, q);
    }
  }

  private boolean isOnline() {
    ConnectivityManager cm =
            (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    int permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

    return netInfo != null && netInfo.isConnectedOrConnecting() && permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  private long getTimestamp ()
  {
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getReadableDatabase();

    String queryString = "SELECT " + DbHelper.timeEntry.TIMESTAMP + " FROM " + DbHelper.timeEntry.TABLE_NAME +
      " WHERE " + DbHelper.timeEntry.COUNTER + " = 1;";

    Cursor cursor = db.rawQuery(queryString, null);

    long output;
    if (cursor.getCount() > 0)
    {
      cursor.moveToFirst();
      output = cursor.getLong(0);
    }
    else
    {
      output = 0;
    }
    cursor.close();

    return output;
  }

  private void setTimestamp (long now)
  {
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(DbHelper.timeEntry.COUNTER, 1);
    values.put(DbHelper.timeEntry.TIMESTAMP, now);

    db.insert(DbHelper.timeEntry.TABLE_NAME, null, values);

    db.close();
  }

  private void storeChanges (String time, EventElement [] data)
  {
    // connect to location db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    for (int k = 0; k < data.length; k++)
    {
      try
      {
        ContentValues event = new ContentValues();
        event.put(dataEntry.TIME, time);
        event.put(dataEntry.DAY, data[k].day);
        event.put(dataEntry.PLACE, data[k].place);
        event.put(dataEntry.NAME, data[k].name);
        event.put(dataEntry.GROUP, data[k].group);
        event.put(dataEntry.PERSON, data[k].person);
        event.put(dataEntry.PERSON_ID, data[k].personId);
        event.put(dataEntry.FULL_NAME, data[k].fullName);
        event.put(dataEntry.STATUS, data[k].status);
        event.put(dataEntry.TIMESTAMP, data[k].timestamp);

        db.insert(dataEntry.TABLE_NAME, null, event);
      }
      catch (Exception e)
      {
        Log.e(tag, "inserting event into db", e);
      }
    }
  }
}







































