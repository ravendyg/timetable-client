package com.example.me.timetable;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import com.example.me.timetable.NetworkStateReceiver.NetworkStateReceiverListener;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity
{
  private final String tag = this.getClass().getSimpleName();
//
//  private SearchAdapter adapter;

  private SearchAdapter searchAdapter;

  private ArrayList<SearchElement> storage = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private ArrayList<SearchElement> favorites = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private ArrayList<SearchElement> searchResult = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  ListView searchList;

  private EditText searchInput;

  private Button clearSearchInput;

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


    searchAdapter = new SearchAdapter(this, searchResult);

    searchList = (ListView) this.findViewById(R.id.list_view);
    searchList.setAdapter(searchAdapter);
//    searchAdapter = new SearchAdapter(this, favorites);
    searchList.setOnItemClickListener(
        new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick (AdapterView<?> adapterView, View view, int position, long id)
          {
            SearchElement element = searchAdapter.getElement(position);

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

              if ( maxFav - minFav >= 5 )
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

    searchInput = (EditText) findViewById(R.id.search_bar_auto);

    searchInput.addTextChangedListener(
      new TextWatcher()
      {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable)
        {
          filterSearchResults( editable.toString() );
        }
      }
    );

    clearSearchInput = (Button) findViewById(R.id.clear_search);


    long timestamp = getTimestamp();

    if ( isOnline() )
    {
      new SyncData(now, timestamp).execute();
    }
    else if ( timestamp > 0 )
    {
      refreshList();
    }
    else
    {
//      waitForConnection();
      ( (TextView) findViewById(R.id.loading_message) ).setText( getString( R.string.no_internet_message) );
      NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();
      networkStateReceiver.addListener( new ConnectionListener() );
      this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

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
        if ( output != null )
        {
          storeChanges(output, type);
          setTimestamp(now);
        }
      }
      catch (JSONException e)
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

    Collections.sort(favorites, new SearchElement.CustomComparator());

    findViewById(R.id.loading_message).setVisibility(View.GONE);
    findViewById(R.id.loading_spinner).setVisibility(View.GONE);

    findViewById(R.id.search_view).setVisibility(View.VISIBLE);

    TextView view = (TextView) findViewById(R.id.search_bar_auto);

    filterSearchResults( view.getText().toString() );  // serch results on start or reload

    view.requestFocus();
    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT);

    refreshed = 1;
  }

  private void filterSearchResults ( String input )
  {
    searchResult.clear();

    if ( input.length() == 0 )
    { // show last searches
      searchResult.addAll( favorites );
    }
    else
    {
      for (SearchElement el: storage)
      {
        if ( el.text.toLowerCase().matches( "(.*)" + input.toLowerCase() + "(.*)" ) )
        {
          searchResult.add(el);
        }
      }

      clearSearchInput.setVisibility(View.VISIBLE);
    }

    searchAdapter.notifyDataSetChanged();
  }

  public void clearSearchInput (View btn)
  {
    searchInput.setText("");
    btn.setVisibility(View.GONE);
  }

  private boolean isOnline() {
    ConnectivityManager cm =
            (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    int permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

    return netInfo != null && netInfo.isConnectedOrConnecting() && permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  public class ConnectionListener implements NetworkStateReceiverListener {
    /* ... */
    private NetworkStateReceiver networkStateReceiver;

    @Override
    public void networkAvailable() {
      long now = (new Date()).getTime();
      ( (TextView) findViewById(R.id.loading_message) ).setText( getString( R.string.loading_message) );
      new SyncData(now, 0).execute();
    }

    @Override
    public void networkUnavailable() {
    }
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
        db.delete(dataEntry.TABLE_NAME, null, null);
        db.delete(personEntry.TABLE_NAME, null, null);
        db.delete(groupEntry.TABLE_NAME, null, null);
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
            db.delete(
              dataEntry.TABLE_NAME,
              dataEntry.TIME + "=? AND " + dataEntry.DAY + "=? AND " + dataEntry.GROUP + "=?",
              new String[] {data[k].time, ""+data[k].day, data[k].group}
            );
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
    String queryString =
      "SELECT * FROM " + personEntry.TABLE_NAME + " WHERE " + personEntry.PERSON_ID + "=" + data.personId + ";";
    Cursor cursor = db.rawQuery(queryString, null);

    int fav = 0;
    boolean replace = true;

    if ( cursor.getCount() > 0 )
    {
      cursor.moveToFirst();
      int id = cursor.getInt(0);
      String name = cursor.getString(1);
      fav = cursor.getInt(2);

      if ( !name.equals(data.fullName) )
      {
        replace = false;
      }
    }

    cursor.close();

    if ( replace )
    {
      ContentValues person = new ContentValues();
      person.put(personEntry.PERSON_ID, data.personId);
      person.put(personEntry.FULL_NAME, data.fullName);
      person.put(personEntry.FAVORITE, fav);

      db.insertWithOnConflict(DbHelper.personEntry.TABLE_NAME, null, person, SQLiteDatabase.CONFLICT_REPLACE);
    }
  }

  private void insertNewGroup (SQLiteDatabase db, EventElement data)
  {
    String queryString =
            "SELECT * FROM " + groupEntry.TABLE_NAME + " WHERE " + groupEntry.NAME + "='" + data.group + "';";
    Cursor cursor = db.rawQuery(queryString, null);

    if ( cursor.getCount() == 0 )
    {
      ContentValues group = new ContentValues();
      group.put(groupEntry.NAME, data.group);

      db.insertWithOnConflict(groupEntry.TABLE_NAME, null, group, SQLiteDatabase.CONFLICT_REPLACE);
    }

    cursor.close();
  }
}







































