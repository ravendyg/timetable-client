package com.venomyd.nopay.timetable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.venomyd.nopay.timetable.Adapters.RowAdapter;
import com.venomyd.nopay.timetable.Adapters.RowElement;
import com.venomyd.nopay.timetable.Adapters.SearchElement;
import com.venomyd.nopay.timetable.DataModels.EventList;
import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.Services.DataProvider;
import com.venomyd.nopay.timetable.data.DbHelper;
import com.venomyd.nopay.timetable.data.DbHelper.dataEntry;
import com.venomyd.nopay.timetable.data.PeriodsService;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by me on 25/08/16.
 */
public class TableActivity extends AppCompatActivity
{
  private SearchElement element;

  private RowAdapter rowAdapter;

  private ArrayList<RowElement> list = new ArrayList<RowElement>(Arrays.asList(new RowElement[0]));

  private ListView tableList;

  private int daysLength = PeriodsService.getDays().length;
  private int timesLength = PeriodsService.getTimes().length;

  private ArrayList<String> times = new ArrayList<String>(Arrays.asList(PeriodsService.getTimes() ));

  private RowElement [] items = new RowElement[daysLength * (1 + timesLength)];

  private BroadcastReceiver mainReceiver;

  private ListItem item;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_table);

    item = (ListItem) getIntent().getSerializableExtra("data");
    requestData(item);
    setHeader(item.name);

//    LinearLayout table = new LinearLayout(this);
//
//    element = (SearchElement) getIntent().getSerializableExtra("data");
//
//    ActionBar bar = getSupportActionBar();
//    bar.setTitle(element.text);
//    bar.setDisplayHomeAsUpEnabled(false);
//
//    rowAdapter = new RowAdapter(this, list);
//
//    tableList = (ListView) findViewById(R.id.table_list);
//    tableList.setAdapter(rowAdapter);
//
//    for (int j = 0; j < daysLength * (1 + timesLength); j++)
//    {
//      int time = j % (1 + timesLength) - 1;
//      items[j] =
//        new RowElement (
//          time,
//          time == -1 ? PeriodsService.getDays()[j / (1 + timesLength)] : ""
//        );
//    }

//    refreshList();

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
          if (eventType.equals("resource"))
          {
            setTable((ArrayList<EventList>) intent.getSerializableExtra("list"));
//            history = (ArrayList<ListItem> ) intent.getSerializableExtra("history");
          }
        }
      };
    }
    registerReceiver(mainReceiver, new IntentFilter("timetable_table_activity"));

    if (!isServiceRunning(DataProvider.class))
    {
      startService(new Intent(this, DataProvider.class));
    }
    else
    {
      Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
      intent.putExtra("event", "activity-online");
      intent.putExtra("type", "table");
      sendBroadcast(intent);
    }
  }

  private void requestData(ListItem item)
  {
    Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
    intent.putExtra("event", "data-request");
    intent.putExtra("item", item);
    sendBroadcast(intent);
  }

  private void setHeader(String name)
  {
    ActionBar bar = getSupportActionBar();
    bar.setTitle(name);
  }

  private void setTable(ArrayList<EventList> data)
  {
    if (data != null)
    {
      // rewrite RowAdapter to accept EventLists
      // rowAdapter = new RowAdapter(this, data);
      tableList = (ListView) findViewById(R.id.table_list);
      // tableList.setAdapter(rowAdapter);
      findViewById(R.id.loading_spinner).setVisibility(View.GONE);
    }
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
//  private void refreshList ()
//  {
//    ArrayList<RowElement> matching = new ArrayList<RowElement>(Arrays.asList(new RowElement[0]));
//
//    DbHelper mDbHelper = new DbHelper(this);
//    SQLiteDatabase db = mDbHelper.getReadableDatabase();
//
//    String queryString;
//
//    if (element.type.equals("group") )
//    {
//      queryString =
//        "SELECT " + dataEntry.TIME + ", " + dataEntry.NAME + ", " + dataEntry.PLACE + ", " + dataEntry.PERSON +
//                ", " + dataEntry.DAY + ", " + dataEntry.POSITION +
//        " FROM " + dataEntry.TABLE_NAME +
//        " WHERE " + dataEntry.GROUP + " LIKE '" + element.text + "'" +
//        " ORDER BY " + dataEntry.TIME + " ASC " +
//        ";";
//    }
//    else
//    {
//      queryString =
//            "SELECT " + dataEntry.TIME + ", " + dataEntry.NAME +", " + dataEntry.PLACE + ", " + dataEntry.GROUP +
//                  ", " + dataEntry.DAY + ", " + dataEntry.POSITION +
//            " FROM " + dataEntry.TABLE_NAME +
//            " WHERE " + dataEntry.PERSON_ID + "='" + element.id + "'" +
//            " ORDER BY " + dataEntry.TIME + " ASC, " + dataEntry.GROUP + " DESC" +
//        ";";
//    }
//
//    Cursor cursor = db.rawQuery(queryString, null);
//
//    while (cursor.moveToNext())
//    {
//      int timePointer = times.indexOf( cursor.getString(0) );
//      // day * number of rows per day + time
//      items[cursor.getInt(4) * (1 + timesLength) + timePointer + 1]
//      .addElement(
//          timePointer,
//          cursor.getString(1),
//          cursor.getString(2),
//          cursor.getString(3),
//          cursor.getInt(5)
//        );
//    }
//
//    list.addAll(
//      new ArrayList<RowElement>(Arrays.asList(items))
//    );
//
//    cursor.close();
//  }
}
