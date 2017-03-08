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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.venomyd.nopay.timetable.Adapters.SearchAdapter;
import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.Services.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity
{
  private final String LOG_TAG = this.getClass().getSimpleName();

  private final int HISTORY_SIZE = 10;

  private SearchAdapter searchAdapter;
  private ArrayAdapter _searchAdapter = null;

  private ArrayList<ListItem> searchResult = new ArrayList<ListItem>(Arrays.asList(new ListItem[0]));
  private ArrayList<String> _names;
  private ArrayList<String> _history = null;
  private ArrayList<String> _searchResult = new ArrayList<>(Arrays.asList(new String[0]));
  private HashMap<String, ListItem> namesToIds = new HashMap<>();;
  private HashMap<String, ArrayList<String>> searchHistory;

  ListView searchList;

  private ProgressBar spinner;

  private EditText searchInput;
  private String searchQuery = "";
  private Button clearSearchInputBtn;

  private BroadcastReceiver mainReceiver;

  private ArrayList<ListItem> history = null, list = null;

  private long searchListVersion = 0;

  private boolean initialized = false;



  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    _searchAdapter = new ArrayAdapter(this, R.layout.search_item, _searchResult);
    searchList = (ListView) this.findViewById(R.id.list_view);
    searchList.setAdapter(_searchAdapter);
    spinner = (ProgressBar) findViewById(R.id.loading_spinner);
    searchInput = (EditText) findViewById(R.id.search_bar_auto);
    clearSearchInputBtn = (Button) findViewById(R.id.clear_search);

    searchList.setOnItemClickListener(
        new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick (AdapterView<?> adapterView, View view, int position, long id)
          {
            String name = (String) _searchAdapter.getItem(position);
            ListItem element = namesToIds.get(name);

            updateHistory(element);

            Intent tableIntent = new Intent(MainActivity.this, TableActivity.class);
            tableIntent.putExtra("data", element);
            startActivity(tableIntent);
          }
        }
    );

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
          if (editable.toString().length() == 0)
          {
            clearSearchInputBtn.setVisibility(View.GONE);
          }
          startFilterSearchResults(editable.toString());
        }
      }
    );


  }

  public void tryInit()
  {
    if (history != null && _names != null)
    {
      TextView view = (TextView) findViewById(R.id.search_bar_auto);

      if (!initialized)
      {
        spinner.setVisibility(View.GONE);
        findViewById(R.id.search_view).setVisibility(View.VISIBLE);

        initialized = true;

        if (history.size() == 0)
        {
          view.requestFocus();
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }

        startFilterSearchResults(view.getText().toString());
      }
    }
  }

  @Override
  public void onResume ()
  {
    super.onResume();

    if (_history != null && _searchAdapter != null && searchInput.getText().toString().length() == 0)
    {
      _searchResult.clear();
      Iterator<String> _items = _history.iterator();
      while (_items.hasNext())
      {
        _searchResult.add(_items.next());
      }
      _searchAdapter.notifyDataSetChanged();
    }
    if (mainReceiver == null)
    {
      mainReceiver = new BroadcastReceiver()
      {
        @Override
        public void onReceive(Context context, Intent intent)
        {
          String eventType = intent.getStringExtra("event");
          if (history == null && eventType.equals("history"))
          {
            history = (ArrayList<ListItem> ) intent.getSerializableExtra("history");
            _history = new ArrayList<>(Arrays.asList(new String[0]));
            Iterator<ListItem> items = history.iterator();
            while (items.hasNext())
            {
              ListItem temp = items.next();
              _history.add(temp.name);
              namesToIds.put(temp.name, temp);
            }
            tryInit();
          }
          else if (eventType.equals("searchList"))
          {
            long version = intent.getLongExtra("version", 0);
            boolean forceUpdate = intent.getBooleanExtra("forceUpdate", false);
            ArrayList<ListItem> chunk = (ArrayList<ListItem>) intent.getSerializableExtra("searchList");

            if (_names == null || (forceUpdate && searchListVersion != version))
            {
              _names = new ArrayList<>(Arrays.asList(new String[0]));
              searchHistory = new HashMap<>();
              searchHistory.put("", _names);
            }
            Iterator<ListItem> items = chunk.iterator();
            while (items.hasNext())
            {
              ListItem temp = items.next();
              _names.add(temp.name);
              namesToIds.put(temp.name, temp);
            }
            searchHistory.put("", _names);
            searchListVersion = version;
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

  public void clearSearchInput(View view)
  {
    searchInput.setText("");
    clearSearchInputBtn.setVisibility(View.GONE);
    startFilterSearchResults("");
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

    _history = new ArrayList<>(Arrays.asList(new String[0]));
    Iterator<ListItem> items = history.iterator();
    while (items.hasNext())
    {
      ListItem temp = items.next();
      _history.add(temp.name);
    }

    Intent intent = new Intent("com.venomyd.nopay.timetable.data.service");
    intent.putExtra("event", "new-history");
    intent.putExtra("history", history);
    sendBroadcast(intent);
  }


  private void startFilterSearchResults(final String input)
  {
    if (input.length() == 0)
    { // show last searches
      if (history != null && history.size() != _searchResult.size())
      {
        _searchResult.clear();
        _searchResult.addAll(_history);
        _searchAdapter.notifyDataSetChanged();
      }
    }
    else
    {
      searchList.setVisibility(View.GONE);
      spinner.setVisibility(View.VISIBLE);
      filterSearchResults(input);
    }
  }

  private void filterSearchResults(final String input)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        _searchResult.clear();

        if (searchHistory.containsKey(input))
        {
          _searchResult.addAll(searchHistory.get(input));
        }
        else
        {
          String temp = input;
          while (temp.length() > 0)
          {
            if (searchHistory.containsKey(temp))
            {
              break;
            }
            temp = temp.substring(0, temp.length() - 1);
          }
          ArrayList<String> tempList = searchHistory.get(temp);
          ArrayList<String> narrowerList = new ArrayList<>(Arrays.asList(new String[0]));
          for (String el : tempList)
          {
            if (el.toLowerCase().matches("(.*)" + input.toLowerCase() + "(.*)"))
            {
              narrowerList.add(el);
            }
          }
          searchHistory.put(input, narrowerList);
          _searchResult.addAll(narrowerList);
        }

        String newInput = searchInput.getText().toString();
        if (newInput.equals(input))
        {
          runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              if (input.length() > 0)
              {
                clearSearchInputBtn.setVisibility(View.VISIBLE);
              }
              else
              {
                clearSearchInputBtn.setVisibility(View.GONE);
              }
              _searchAdapter.notifyDataSetChanged();
              searchList.setVisibility(View.VISIBLE);
              spinner.setVisibility(View.GONE);
            }
          });
        }
        else
        {
          filterSearchResults(newInput);
        }
      }
    }).start();
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







































