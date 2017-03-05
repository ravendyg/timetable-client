package com.venomyd.nopay.timetable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.venomyd.nopay.timetable.Adapters.RowAdapter;
import com.venomyd.nopay.timetable.Adapters.RowElement;
import com.venomyd.nopay.timetable.Adapters.SearchElement;
import com.venomyd.nopay.timetable.DataModels.Lesson;
import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.Services.DataProvider;
import com.venomyd.nopay.timetable.data.PeriodsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by me on 25/08/16.
 */
public class TableActivity extends AppCompatActivity
{
  private SearchElement element;

  private RowAdapter rowAdapter;

  private ArrayList<Lesson> data;
  private ArrayList<Lesson> list = null;

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

    ActionBar bar = getSupportActionBar();
    bar.setDisplayHomeAsUpEnabled(false);

    item = (ListItem) getIntent().getSerializableExtra("data");
    requestData(item);
    setHeader(item.name);

    tableList = (ListView) findViewById(R.id.table_list);
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
            setTable((ArrayList<Lesson>) intent.getSerializableExtra("list"));
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

  private void setTable(ArrayList<Lesson> _data)
  {
    if (_data != null)
    {
      int day = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5
              +1
      ) % 7;
      data = _data;
      ArrayList<Lesson> newList = new ArrayList<>(Arrays.asList(new Lesson[0]));

      if (list != null)
      {
        for (int oldListPointer = 0, newListPointer = 0;
              oldListPointer < list.size() && newListPointer < _data.size(); oldListPointer++)
        {
          Lesson oldItem = list.get(oldListPointer);
          newList.add(_data.get(newListPointer));
          if (oldItem.position == -1 && !oldItem.open)
          {
            newListPointer += 8;
          }
          else
          {
            newListPointer++;
          }
        }
      }
      else
      {
        int listDay = -1;
        for (int newListPointer = 0; newListPointer < _data.size(); )
        {
          Lesson _item = _data.get(newListPointer);
          newList.add(_item);
          if (_item.position == -1)
          {
            listDay++;
            if (day != listDay)
            {
              newListPointer += 8;
            }
            else
            {
              newListPointer++;
            }
          }
          else
          {
            newListPointer++;
          }
        }
      }
      list = newList;

      rowAdapter = new RowAdapter(this, list);
      tableList.setAdapter(rowAdapter);
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
}
