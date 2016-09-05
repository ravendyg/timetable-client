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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

  private SearchAdapter adapter;

  private SearchAdapter favoritesAdapter;

  private ArrayList<SearchElement> storage = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private ArrayList<SearchElement> favorites = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  ListView favoritesList;

  private int refreshed = 0;

  private int minFav = 0, maxFav = 0;

  private DbHelper mDbHelper;
  private SQLiteDatabase db;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    long now = (new Date()).getTime();

    mDbHelper = new DbHelper(this);
    db = mDbHelper.getWritableDatabase();

    favoritesList = (ListView) this.findViewById(R.id.list_view);

    favoritesAdapter = new SearchAdapter(this, favorites);
    favoritesList.setAdapter(favoritesAdapter);

    adapter = new SearchAdapter(this, storage);

    if ( isOnline() )
    {
      long timestamp = getTimestamp();
      new SyncData(now, timestamp).execute();
    }
    else
    {
      refreshList();
    }

    favoritesList.setOnItemClickListener(
      new AdapterView.OnItemClickListener()
      {
        @Override
        public void onItemClick (AdapterView<?> adapterView, View view, int position, long id)
        {
          SearchElement element = favoritesAdapter.getElement(position);

          Intent tableIntent = new Intent(MainActivity.this, TableActivity.class);
          tableIntent.putExtra("data", element);
          startActivity(tableIntent);
        }
    }
    );
  }

  @Override
  public void onResume ()
  {
    super.onResume();
    if (refreshed > 0)
    {
      refreshList();
    }
  }

  @Override
  public void onDestroy ()
  {
    super.onDestroy();
    db.close();
  }

  private class SyncData extends AsyncTask<URL, Void, String>
  {
    private final String tag = this.getClass().getSimpleName();

    private long now, timestamp;

    public SyncData (long nowT, long tmsp)
    {
      now = nowT;
      timestamp = tmsp;
    }

    protected String doInBackground (URL... urls)
    {
      String syncData;
      syncData = new HttpService().getSync(timestamp);

      EventElement[] output;

      try
      {
        String type = ResponseParser.getType(syncData);
        output = ResponseParser.getElements(syncData);
        storeChanges(output, type);
        setTimestamp(now);
      } catch (JSONException e)
      {
        Log.e(tag, "response is empty", e);
      }
      return "";
    }

    protected void onPostExecute (String time)
    {
      refreshList();
    }
  }

  private void refreshList ()
  {
    storage.clear();
    favorites.clear();

    String queryString =
      "SELECT " + groupEntry.NAME + ", " + groupEntry.FAVORITE +
      " FROM " + groupEntry.TABLE_NAME + ";";
    Cursor cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      SearchElement element = new SearchElement(cursor.getString(0), "group", 0, cursor.getInt(1));
      storage.add(element);

      if ( cursor.getInt(1) > 0 )
      {
        favorites.add(element);
        if ( minFav ==0 || element.fav < minFav )
        {
          minFav = element.fav;
        }
        if ( element.fav > maxFav)
        {
          maxFav = element.fav;
        }
      }
    }

    cursor.close();

    queryString =
      "SELECT " + personEntry.FULL_NAME + ", " + personEntry.PERSON_ID + ", " + personEntry.FAVORITE +
      " FROM " + personEntry.TABLE_NAME + ";";
    cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      SearchElement element = new SearchElement(cursor.getString(0), "person", cursor.getInt(1), cursor.getInt(2));
      storage.add(element);

      if ( cursor.getInt(2) > 0 )
      {
        favorites.add(element);
        if ( minFav ==0 || element.fav < minFav )
        {
          minFav = element.fav;
        }
        if ( element.fav > maxFav)
        {
          maxFav = element.fav;
        }
      }
    }

    cursor.close();

    adapter.notifyDataSetChanged();
    favoritesAdapter.notifyDataSetChanged();


    findViewById(R.id.loading_message).setVisibility(View.GONE);
    findViewById(R.id.loading_spinner).setVisibility(View.GONE);

    findViewById(R.id.search_view).setVisibility(View.VISIBLE);

    AutoCompleteTextView view = (AutoCompleteTextView) findViewById(R.id.search_bar_auto);
    view.setAdapter(adapter);

    view.requestFocus();
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT);

    view.setOnItemClickListener(
      new AdapterView.OnItemClickListener()
      {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
        {
          AutoCompleteTextView vw = (AutoCompleteTextView) findViewById(R.id.search_bar_auto);
          vw.setText("");

          SearchElement element = (SearchElement) adapterView.getItemAtPosition(position);

          if ( element.fav == 0 )
          {
            ContentValues cv = new ContentValues();

            if (element.type.equals("group"))
            {
              cv.put(groupEntry.FAVORITE, ++maxFav);

              db.update(
                  groupEntry.TABLE_NAME,
                  cv,
                  groupEntry.NAME + "=?",
                  new String[] {element.text}
              );
            }
            else
            {
              cv.put(personEntry.FAVORITE, ++maxFav);

              db.update(
                      personEntry.TABLE_NAME,
                      cv,
                      personEntry.PERSON_ID + "=?",
                      new String[] {""+element.id}
              );
            }

            if ( maxFav - minFav > 2 )
            {
              cv.put(groupEntry.FAVORITE, 0);

              db.update(
                  groupEntry.TABLE_NAME,
                  cv,
                  groupEntry.FAVORITE + "=?",
                  new String[] {""+minFav}
              );

              cv.put(personEntry.FAVORITE, 0);

              db.update(
                      personEntry.TABLE_NAME,
                      cv,
                      personEntry.FAVORITE + "=?",
                      new String[] {""+minFav}
              );

              minFav++;
            }
          }

          Intent tableIntent = new Intent(MainActivity.this, TableActivity.class);
          tableIntent.putExtra("data", element);
          startActivity(tableIntent);
        }
      }
    );

    refreshed = 1;
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
    String queryString = "SELECT " + DbHelper.timeEntry.TIMESTAMP + " FROM " + DbHelper.timeEntry.TABLE_NAME +
      " WHERE " + DbHelper.timeEntry.COUNTER + " = " + 1 + ";";

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
    ContentValues values = new ContentValues();
    values.put(DbHelper.timeEntry.COUNTER, 1);
    values.put(DbHelper.timeEntry.TIMESTAMP, now);

    db.insert(DbHelper.timeEntry.TABLE_NAME, null, values);
  }

  private void storeChanges (EventElement [] data, String type)
  {
    // connect to location db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    if (data.length == 0)
    {
      return;
    }

    db.beginTransaction();

    try
    {
      if ( type.equals("new") )
      { // remove all previous data
        String query = "DELETE FROM " + dataEntry.TABLE_NAME + ";";
        db.rawQuery(query, null);
        query = "DELETE FROM " + personEntry.TABLE_NAME + ";";
        db.rawQuery(query, null);
        query = "DELETE FROM " + groupEntry.TABLE_NAME + ";";
        db.rawQuery(query, null);
      }

      for (int k = 0; k < data.length; k++)
      {
        try
        {
          if (data[k].status == 1)
          {
            insertNewEvent(db, data[k]);
            insertNewGroup(db, data[k]);
            insertNewPerson(db, data[k]);
          }
          else
          {
            String query =
              "DELETE FROM " + dataEntry.TABLE_NAME +
                " WHERE " +
                  dataEntry.TIME + "=" + data[k].time +
                  dataEntry.DAY + "=" + data[k].day +
                  dataEntry.PLACE + "=" + data[k].place +
              ";";
            db.rawQuery(query, null);
          }
        } catch (Exception e)
        {
          Log.e(tag, "inserting event into db", e);
        }
      }
      db.setTransactionSuccessful();
    }
    catch (Exception e)
    {
      Log.e("general insert", "", e);
    }
    finally
    {
      db.endTransaction();
    }
  }

  private void insertNewEvent (SQLiteDatabase db, EventElement data)
  {
    ContentValues event = new ContentValues();
    event.put(dataEntry.TIME, data.time);
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

    db.insertWithOnConflict(dataEntry.TABLE_NAME, null, event,  SQLiteDatabase.CONFLICT_REPLACE);
  }

  private void insertNewPerson (SQLiteDatabase db, EventElement data)
  {
    ContentValues person = new ContentValues();
    person.put(DbHelper.personEntry.PERSON_ID, data.personId);
    person.put(DbHelper.personEntry.FULL_NAME, data.fullName);

    db.insertWithOnConflict(DbHelper.personEntry.TABLE_NAME, null, person,  SQLiteDatabase.CONFLICT_REPLACE);
  }

  private void insertNewGroup (SQLiteDatabase db, EventElement data)
  {
    ContentValues group = new ContentValues();
    group.put(groupEntry.NAME, data.group);

    db.insertWithOnConflict(groupEntry.TABLE_NAME, null, group, SQLiteDatabase.CONFLICT_REPLACE);
  }
}







































