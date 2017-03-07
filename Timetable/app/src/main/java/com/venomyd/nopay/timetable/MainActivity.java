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
import java.util.Collection;


public class MainActivity extends AppCompatActivity
{
  private final String LOG_TAG = this.getClass().getSimpleName();

  private final int HISTORY_SIZE = 10;

  private SearchAdapter searchAdapter;

  private ArrayList<ListItem> searchResult = new ArrayList<ListItem>(Arrays.asList(new ListItem[0]));

  ListView searchList;

  private EditText searchInput;

  private Button clearSearchInput;

  private BroadcastReceiver mainReceiver;

  private ArrayList<ListItem> history = null, list = null;

  private long searchListVersion = 0;

  private boolean initialized = false;



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

            updateHistory(element);

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
      TextView view = (TextView) findViewById(R.id.search_bar_auto);

      if (!initialized)
      {
        findViewById(R.id.loading_message).setVisibility(View.GONE);
        findViewById(R.id.loading_spinner).setVisibility(View.GONE);
        findViewById(R.id.search_view).setVisibility(View.VISIBLE);

        initialized = true;

        if (history.size() == 0)
        {
          view.requestFocus();
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
      }

      filterSearchResults(view.getText().toString());
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
            long version = intent.getLongExtra("version", 0);
            boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
            ArrayList<ListItem> chunck = (ArrayList<ListItem>) intent.getSerializableExtra("searchList");

            if (list == null || (forceUpdate && searchListVersion != version))
            {
              list = chunck;
              searchListVersion = version;
            }
            else
            {
              list.addAll(chunck);
            }
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
    intent.putExtra("type", "main");
    sendBroadcast(intent);
    unregisterReceiver(mainReceiver);
  }

  public void clearSearchInput (View btn)
  {
    searchInput.setText("");
    btn.setVisibility(View.GONE);
  }

  public void updateHistory(ListItem el)
  {
    if (history == null)
    {
      return;
    }

    for (int i = 0; i < history.size(); i++)
    {
      if (history.get(i).id.equals(el.id))
      {
        if (i == 0)
        {
          return;
        }
        history.remove(i);
      }
    }
    history.add(0, el);
    int size = history.size();
    if (size > HISTORY_SIZE)
    {
      history.remove(size - 1);
    }

    Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
    intent.putExtra("event", "new-history");
    intent.putExtra("history", history);
    sendBroadcast(intent);
  }



  private void filterSearchResults(String input)
  {
    searchResult.clear();

    if (input.length() == 0)
    { // show last searches
      if (history != null)
      {
        searchResult.addAll(history);
      }
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
}







































