package com.example.me.timetable;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.me.timetable.Adapters.EventElement;
import com.example.me.timetable.Adapters.SearchAdapter;
import com.example.me.timetable.Adapters.SearchElement;
import com.example.me.timetable.data.DbHelper;
import com.example.me.timetable.data.DbHelper.dataEntry;
import com.example.me.timetable.data.DbHelper.personEntry;
import com.example.me.timetable.data.DbHelper.groupEntry;
import com.example.me.timetable.data.PeriodsService;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity
{
  private final String tag = this.getClass().getSimpleName();

  private EditText searchBar;

  private SearchAdapter searchAdapter;

  private ArrayList<SearchElement> storage = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private ArrayList<SearchElement> searchResults = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private String [] times = PeriodsService.getTimes();

  private int timesLength = times.length;

  private TextView loader;

  ListView searchList;

  private int counter = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String syncData = "not connected";
    long now = (new Date()).getTime();

    if ( isOnline() )
    {
      for (int i = 0; i < timesLength; i++)
      {
        long timestamp = getTimestamp(i);
        new SyncData(i, now, timestamp).execute();
      }
    }
    else
    {
      refreshList();
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

    searchList = (ListView) this.findViewById(R.id.list_view);

    searchAdapter = new SearchAdapter(this, searchResults);
    searchList.setAdapter(searchAdapter);

    searchList.setOnItemClickListener(
      new AdapterView.OnItemClickListener()
      {
        @Override
        public void onItemClick (AdapterView<?> adapterView, View view, int position, long id)
        {
          SearchElement element = searchAdapter.getElement(position);

          Intent tableIntent = new Intent(MainActivity.this, TableActivity.class);
          tableIntent.putExtra("data", element);
          startActivity(tableIntent);
        }
    }
    );

    loader = ((TextView) findViewById(R.id.loading_percent));
    updateLoader("0%");
  }

  private void performSearch ()
  {
    SearchElement value;
    String input = searchBar.getText().toString();

    searchResults.clear();

    // no empty search input
    if (input.length() > 0)
    {
      for (int i = 0; i < storage.size(); i++)
      {
        value = storage.get(i);
        if (value.text.toLowerCase().contains(input.toLowerCase()))
        {
          searchResults.add(value);
        }
      }

      Collections.sort(searchResults);
      searchAdapter.notifyDataSetChanged();
    }
  }

  private class SyncData extends AsyncTask<URL, Void, String>
  {
    private final String tag = this.getClass().getSimpleName();

    private long now, timestamp;

    private int time;

    public SyncData (int timeData, long nowT, long tmsp)
    {
      time = timeData;
      now = nowT;
      timestamp = tmsp;
    }

    protected String doInBackground (URL... urls)
    {
      String syncData;
      syncData = new HttpService().getSync(times[time], timestamp);

      EventElement[] output;

      int resultLength = 0;
      try
      {
        String type = ResponseParser.getType(syncData);
        output = ResponseParser.getElements(syncData);
        storeChanges(time, output, type);
        setTimestamp(now, time);
      } catch (JSONException e)
      {
        Log.e(tag, "response is empty", e);
      }
      return "";
    }

    protected void onPostExecute (String time)
    {
      counter++;

      updateLoader( (counter * 100 / timesLength) + "%" );

      if (counter == timesLength)
      {
        refreshList();
      }
    }
  }

  private void updateLoader (String val)
  {
    loader.setText(val);
  }

  private void refreshList ()
  {
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getReadableDatabase();

    String queryString = "SELECT " + groupEntry.NAME + " FROM " + groupEntry.TABLE_NAME + ";";
    Cursor cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      SearchElement element = new SearchElement(cursor.getString(0), "group", 0);
      storage.add(element);
    }

    cursor.close();

    queryString =
      "SELECT " + personEntry.FULL_NAME + ", " + personEntry.PERSON_ID +
      " FROM " + personEntry.TABLE_NAME + ";";
    cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      SearchElement element = new SearchElement(cursor.getString(0), "person", cursor.getInt(1));
      storage.add(element);
    }

    cursor.close();

    findViewById(R.id.loading_message).setVisibility(View.GONE);
    findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    findViewById(R.id.loading_percent).setVisibility(View.GONE);
    findViewById(R.id.search_view).setVisibility(View.VISIBLE);

    TextView view = (TextView) this.findViewById(R.id.search_bar);
    view.requestFocus();
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT);
  }

  private boolean isOnline() {
    ConnectivityManager cm =
            (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    int permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

    return netInfo != null && netInfo.isConnectedOrConnecting() && permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  private long getTimestamp (int counter)
  {
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getReadableDatabase();

    String queryString = "SELECT " + DbHelper.timeEntry.TIMESTAMP + " FROM " + DbHelper.timeEntry.TABLE_NAME +
      " WHERE " + DbHelper.timeEntry.COUNTER + " = " + counter + ";";

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

  private void setTimestamp (long now, int counter)
  {
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(DbHelper.timeEntry.COUNTER, counter);
    values.put(DbHelper.timeEntry.TIMESTAMP, now);

    db.insert(DbHelper.timeEntry.TABLE_NAME, null, values);

    db.close();
  }

  private void storeChanges (int time, EventElement [] data, String type)
  {
    // connect to location db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    for (int k = 0; k < data.length; k++)
    {
      try
      {
        if (data[k].status == 1)
        {
          long res = insertNewEvent(db, data[k], time);
          Log.e("insert", ""+res);

          ContentValues person = new ContentValues();
          person.put(DbHelper.personEntry.PERSON_ID, data[k].personId);
          person.put(DbHelper.personEntry.FULL_NAME, data[k].fullName);
          db.insert(DbHelper.personEntry.TABLE_NAME, null, person);

          ContentValues group = new ContentValues();
          group.put(DbHelper.groupEntry.NAME, data[k].group);
          db.insert(DbHelper.groupEntry.TABLE_NAME, null, group);
        }
        else
        {
          String query =
            "DELETE FROM " + dataEntry.TABLE_NAME +
              " WHERE " +
                dataEntry.TIME + "=" + time +
                dataEntry.DAY + "=" + data[k].day +
                dataEntry.PLACE + "=" + data[k].place +
            ";";
          db.rawQuery(query, null);
        }
      }
      catch (Exception e)
      {
        Log.e(tag, "inserting event into db", e);
      }
    }
  }

  private long insertNewEvent (SQLiteDatabase db, EventElement data, int time)
  {
    ContentValues event = new ContentValues();
    event.put(dataEntry.TIME, time);
    event.put(dataEntry.DAY, data.day);
    event.put(dataEntry.PLACE, data.place);
    event.put(dataEntry.NAME, data.name);
    event.put(dataEntry.GROUP, data.group);
    event.put(dataEntry.PERSON, data.person);
    event.put(dataEntry.PERSON_ID, data.personId);
    event.put(dataEntry.FULL_NAME, data.fullName);
    event.put(dataEntry.POSITION, data.position);
    event.put(dataEntry.STATUS, data.status);
    event.put(dataEntry.TIMESTAMP, data.timestamp);

    long out = db.insert(dataEntry.TABLE_NAME, null, event);
    return out;
  }
}







































