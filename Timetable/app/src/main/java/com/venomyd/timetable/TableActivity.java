package com.venomyd.timetable;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.venomyd.timetable.Adapters.RowAdapter;
import com.venomyd.timetable.Adapters.RowElement;
import com.venomyd.timetable.Adapters.SearchElement;
import com.venomyd.timetable.DataModels.Lesson;
import com.venomyd.timetable.DataModels.ListItem;
import com.venomyd.timetable.Services.DataProvider;
import com.venomyd.timetable.data.PeriodsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.os.Handler;
import android.widget.TextView;

/**
 * Created by me on 25/08/16.
 */
public class TableActivity extends AppCompatActivity
{
  private SearchElement element;

  private RowAdapter rowAdapter;

  private ArrayList<Lesson> data;
  private ArrayList<Lesson> list = new ArrayList<>(Arrays.asList(new Lesson[0]));

  private ListView tableList;

  private int daysLength = PeriodsService.getDays().length;
  private int timesLength = PeriodsService.getTimes().length;

  private ArrayList<String> times = new ArrayList<String>(Arrays.asList(PeriodsService.getTimes() ));

  private RowElement [] items = new RowElement[daysLength * (1 + timesLength)];

  private BroadcastReceiver mainReceiver;

  private ListItem item;

  private ArrayList<TextView> tabs = new ArrayList<>(Arrays.asList(new TextView[0]));

  int day = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;


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
    rowAdapter = new RowAdapter(this, list);
    tableList.setAdapter(rowAdapter);

    tabs.add((TextView) findViewById(R.id.day0));
    tabs.add((TextView) findViewById(R.id.day1));
    tabs.add((TextView) findViewById(R.id.day2));
    tabs.add((TextView) findViewById(R.id.day3));
    tabs.add((TextView) findViewById(R.id.day4));
    tabs.add((TextView) findViewById(R.id.day5));
    tabs.add((TextView) findViewById(R.id.day6));

    for (int i = 0; i < tabs.size(); i++)
    {
      final TextView tab = tabs.get(i);
      tab.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          int id = v.getId();
          for (int j = 0; j < tabs.size(); j++)
          {
            if (id == tabs.get(j).getId())
            {
              day = j;
              updateDayView();
              break;
            }
          }
        }
      });
    }

    updateDayView();
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
            String id = intent.getStringExtra("id");
            if (item.id.equals(id))
            {
              setTable((ArrayList<Lesson>) intent.getSerializableExtra("list"));
            }
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
      Intent intent = new Intent("com.venomyd.timetable.data.service");
      intent.putExtra("event", "activity-online");
      intent.putExtra("type", "table");
      sendBroadcast(intent);
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();

    Intent intent = new Intent("com.venomyd.timetable.data.service");
    intent.putExtra("event", "activity-offline");
    intent.putExtra("type", "table");
    sendBroadcast(intent);
    unregisterReceiver(mainReceiver);
  }

  private void updateDayView()
  {
    if (data != null)
    {
      list.clear();
      for (int i = day * Config.bells.length;
           i < Math.min((day+1) * Config.bells.length, data.size()); i++)
      {
        list.add(data.get(i));
      }
      rowAdapter.notifyDataSetChanged();
    }

    for (int i = 0; i < tabs.size(); i++)
    {
      if (day != i)
      {
        tabs.get(i).setBackgroundColor(this.getResources().getColor(R.color.tabBg));
      }
      else
      {
        tabs.get(i).setBackgroundColor(this.getResources().getColor(R.color.selectedTab));
      }
    }
  }

  private void requestData(ListItem item)
  {
    Intent intent = new Intent("com.venomyd.timetable.data.service");
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
      data = _data;
      updateDayView();
    }
    findViewById(R.id.loading_spinner).setVisibility(View.GONE);
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
