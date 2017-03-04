package com.venomyd.nopay.timetable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.venomyd.nopay.timetable.Adapters.SearchAdapter;
import com.venomyd.nopay.timetable.Adapters.SearchElement;
import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.Services.DataProvider;


import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity
{
  private final String LOG_TAG = this.getClass().getSimpleName();
//
//  private SearchAdapter adapter;

  private SearchAdapter searchAdapter;

  private ArrayList<SearchElement> storage = new ArrayList<>(Arrays.asList(new SearchElement[0]));
  private ArrayList<SearchElement> favorites = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));

  private ArrayList<ListItem> searchResult = new ArrayList<ListItem>(Arrays.asList(new ListItem[0]));

  ListView searchList;

  private EditText searchInput;

  private Button clearSearchInput;

  private int refreshed = 0;

  private int minFav = 0, maxFav = 0;

  private BroadcastReceiver mainReceiver;

  private ArrayList<ListItem> history = null, list = null;



  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

        searchAdapter = new SearchAdapter(this, searchResult);
    searchList = (ListView) this.findViewById(R.id.list_view);
    searchList.setAdapter(searchAdapter);
    searchList.setOnItemClickListener(
        new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick (AdapterView<?> adapterView, View view, int position, long id)
          {
            ListItem element = searchAdapter.getElement(position);

//            if ( element.fav <= maxFav )
//            {
//              ContentValues cv = new ContentValues();
//
//              if (element.type.equals("group"))
//              {
//                cv.put(groupEntry.FAVORITE, ++maxFav);
//
//                db.update(
//                        groupEntry.TABLE_NAME,
//                        cv,
//                        groupEntry.NAME + "=?",
//                        new String[] {element.text}
//                );
//              }
//              else
//              {
//                cv.put(personEntry.FAVORITE, ++maxFav);
//
//                db.update(
//                        personEntry.TABLE_NAME,
//                        cv,
//                        personEntry.PERSON_ID + "=?",
//                        new String[] {""+element.id}
//                );
//              }
//
//              if ( maxFav - minFav >= 5 )
//              {
//                cv.put(groupEntry.FAVORITE, 0);
//
//                db.update(
//                        groupEntry.TABLE_NAME,
//                        cv,
//                        groupEntry.FAVORITE + "=?",
//                        new String[] {""+minFav}
//                );
//
//                cv.put(personEntry.FAVORITE, 0);
//
//                db.update(
//                        personEntry.TABLE_NAME,
//                        cv,
//                        personEntry.FAVORITE + "=?",
//                        new String[] {""+minFav}
//                );
//
//                minFav++;
//              }
//            }

            Intent tableIntent = new Intent(MainActivity.this, TableActivity.class);
            tableIntent.putExtra("data", element.id);
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
          filterSearchResults(editable.toString());
        }
      }
    );

    clearSearchInput = (Button) findViewById(R.id.clear_search);
  }

  public void tryInit()
  {
    if (history != null && list != null)
    {
      findViewById(R.id.loading_message).setVisibility(View.GONE);
      findViewById(R.id.loading_spinner).setVisibility(View.GONE);

      findViewById(R.id.search_view).setVisibility(View.VISIBLE);

      TextView view = (TextView) findViewById(R.id.search_bar_auto);

      filterSearchResults(view.getText().toString());

      if (history.size() == 0)
      {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput( view, InputMethodManager.SHOW_IMPLICIT);
      }
    }
  }

  @Override
  public void onResume ()
  {
    super.onResume();
    if (mainReceiver == null)
    {
      mainReceiver = new BroadcastReceiver()
      {
        @Override
        public void onReceive(Context context, Intent intent)
        {
          String eventType = intent.getStringExtra("event");
          if (eventType.equals("history"))
          {
            history = (ArrayList<ListItem> ) intent.getSerializableExtra("history");
            tryInit();
          }
          else if (eventType.equals("searchList"))
          {
            list = (ArrayList<ListItem> ) intent.getSerializableExtra("searchList");
            tryInit();
          }
        }
      };
    }
    registerReceiver(mainReceiver, new IntentFilter("timetable_main_activity"));

    if (!isServiceRunning(DataProvider.class))
    {
      startService(new Intent(this, DataProvider.class));
    }
    else
    {
      Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
      intent.putExtra("event", "activity-online");
      sendBroadcast(intent);
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();

    Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
    intent.putExtra("event", "activity-offline");
    sendBroadcast(intent);
    unregisterReceiver(mainReceiver);
  }

  public void clearSearchInput (View btn)
  {
    searchInput.setText("");
    btn.setVisibility(View.GONE);
  }



  private void filterSearchResults(String input)
  {
    searchResult.clear();

    if (input.length() == 0)
    { // show last searches
      searchResult.addAll(history);
    }
    else
    {
      for (ListItem el: list)
      {
        if (el.name.toLowerCase().matches( "(.*)" + input.toLowerCase() + "(.*)" ))
        {
          searchResult.add(el);
        }
      }

      clearSearchInput.setVisibility(View.VISIBLE);
    }

    searchAdapter.notifyDataSetChanged();
  }

  private boolean isServiceRunning(Class<?> serviceClass)
  {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }




//
//  private long getTimestamp ()
//  {
//    String queryString = "SELECT " + DbHelper.timeEntry.TIMESTAMP + " FROM " + DbHelper.timeEntry.TABLE_NAME +
//      " WHERE " + DbHelper.timeEntry.COUNTER + " = " + 1 + ";";
//
//    Cursor cursor = db.rawQuery(queryString, null);
//
//    long output;
//    if (cursor.getCount() > 0)
//    {
//      cursor.moveToFirst();
//      output = cursor.getLong(0);
//    }
//    else
//    {
//      output = 0;
//    }
//    cursor.close();
//
//    return output;
//  }
//
//  private void setTimestamp (long now)
//  {
//    ContentValues values = new ContentValues();
//    values.put(DbHelper.timeEntry.COUNTER, 1);
//    values.put(DbHelper.timeEntry.TIMESTAMP, now);
//
//    db.insert(DbHelper.timeEntry.TABLE_NAME, null, values);
//  }
//
//  private long storeChanges (EventElement [] data, String type)
//  {
//    // connect to location db in write mode
//    DbHelper mDbHelper = new DbHelper(this);
//    SQLiteDatabase db = mDbHelper.getWritableDatabase();
//
//    long newTimestamp = -1;
//
//    if (data.length == 0)
//    {
//      return newTimestamp;
//    }
//
//    db.beginTransaction();
//
//    try
//    {
//      if ( type.equals("new") )
//      { // remove all previous data
//        db.delete(dataEntry.TABLE_NAME, null, null);
//        db.delete(personEntry.TABLE_NAME, null, null);
//        db.delete(groupEntry.TABLE_NAME, null, null);
//      }
//
//      for (int k = 0; k < data.length; k++)
//      {
//        try
//        {
//          if (data[k].status == 1)
//          {
//            insertNewEvent(db, data[k]);
//            insertNewGroup(db, data[k]);
//            insertNewPerson(db, data[k]);
//          }
//          else
//          {
//            db.delete(
//              dataEntry.TABLE_NAME,
//              dataEntry.TIME + "=? AND " + dataEntry.DAY + "=? AND " + dataEntry.GROUP + "=?",
//              new String[] {data[k].time, ""+data[k].day, data[k].group}
//            );
//          }
//
//          if ( data[k].timestamp > newTimestamp )
//          {
//            newTimestamp = data[k].timestamp;
//          }
//        }
//        catch (Exception e)
//        {
//          Log.e(tag, "inserting event into db", e);
//        }
//      }
//      db.setTransactionSuccessful();
//    }
//    catch (Exception e)
//    {
//      Log.e("general insert", "", e);
//    }
//    finally
//    {
//      db.endTransaction();
//    }
//
//    return newTimestamp;
//  }
//
//  private void insertNewEvent (SQLiteDatabase db, EventElement data)
//  {
//    ContentValues event = new ContentValues();
//    event.put(dataEntry.TIME, data.time);
//    event.put(dataEntry.DAY, data.day);
//    event.put(dataEntry.PLACE, data.place);
//    event.put(dataEntry.NAME, data.name);
//    event.put(dataEntry.GROUP, data.group);
//    event.put(dataEntry.PERSON, data.person);
//    event.put(dataEntry.PERSON_ID, data.personId);
//    event.put(dataEntry.FULL_NAME, data.fullName);
//    event.put(dataEntry.POSITION, data.position);
//    event.put(dataEntry.STATUS, data.status);
//    event.put(dataEntry.TIMESTAMP, data.timestamp);
//
//    db.insertWithOnConflict(dataEntry.TABLE_NAME, null, event,  SQLiteDatabase.CONFLICT_REPLACE);
//  }
//
//  private void insertNewPerson (SQLiteDatabase db, EventElement data)
//  {
//    String queryString =
//      "SELECT * FROM " + personEntry.TABLE_NAME + " WHERE " + personEntry.PERSON_ID + "=" + data.personId + ";";
//    Cursor cursor = db.rawQuery(queryString, null);
//
//    int fav = 0;
//    boolean replace = true;
//
//    if ( cursor.getCount() > 0 )
//    {
//      cursor.moveToFirst();
//      int id = cursor.getInt(0);
//      String name = cursor.getString(1);
//      fav = cursor.getInt(2);
//
//      if ( !name.equals(data.fullName) )
//      {
//        replace = false;
//      }
//    }
//
//    cursor.close();
//
//    if ( replace )
//    {
//      ContentValues person = new ContentValues();
//      person.put(personEntry.PERSON_ID, data.personId);
//      person.put(personEntry.FULL_NAME, data.fullName);
//      person.put(personEntry.FAVORITE, fav);
//
//      db.insertWithOnConflict(DbHelper.personEntry.TABLE_NAME, null, person, SQLiteDatabase.CONFLICT_REPLACE);
//    }
//  }
//
//  private void insertNewGroup (SQLiteDatabase db, EventElement data)
//  {
//    String queryString =
//            "SELECT * FROM " + groupEntry.TABLE_NAME + " WHERE " + groupEntry.NAME + "='" + data.group + "';";
//    Cursor cursor = db.rawQuery(queryString, null);
//
//    if ( cursor.getCount() == 0 )
//    {
//      ContentValues group = new ContentValues();
//      group.put(groupEntry.NAME, data.group);
//
//      db.insertWithOnConflict(groupEntry.TABLE_NAME, null, group, SQLiteDatabase.CONFLICT_REPLACE);
//    }
//
//    cursor.close();
//  }
}







































