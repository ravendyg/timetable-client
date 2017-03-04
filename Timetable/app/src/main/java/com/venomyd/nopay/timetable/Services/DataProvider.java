package com.venomyd.nopay.timetable.Services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.venomyd.nopay.timetable.Config;
import com.venomyd.nopay.timetable.DataModels.EventList;
import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.R;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by me on 26/02/17.
 */

public class DataProvider extends Service
{
  private static final String LOG_TAG = "Timetable data service";
  private static final long LIVE_WITHOUT_ACTIVITY = 1000 * 60 * 5;
  private static final long SYNC_VALID_FOR = 1000 * 60 * 60 * 6;

  private String apiKey;
  private int activityCount = 0;

  private Handler countDownHandler;
  private Runnable countDownRunnableLife;
  private boolean dataLoaded = false;

  private ArrayList<ListItem> history = null, searchList = null;

  private HashMap<String, ArrayList<EventList>> resources = new HashMap<>();
  private HashMap<String, String> resourceTsp = new HashMap<>();

  private SharedPreferences pref;

  private BroadcastReceiver mainReceiver;

  private BroadcastReceiver networkStateReceiver = null;
  boolean waitSearchListUpdate = false;
  long searchListTsp = 0;

  private ArrayList<ListItem> itemsToUpdate = new ArrayList<>(Arrays.asList(new ListItem[0]));
  private ArrayList<Long> itemsToUpdateTsp = new ArrayList<Long>();

  public void onCreate()
  {
    super.onCreate();

    countDownHandler = new Handler();
    pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


    apiKey = pref.getString("apiKey", null);
    if (apiKey == null)
    {
      apiKey = String.valueOf(Math.random()).substring(2);
      SharedPreferences.Editor editor = pref.edit();
      editor.putString("apiKey", apiKey);
      editor.commit();
    }

    networkStateReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if (ni != null)
        {
          if (waitSearchListUpdate)
          {
            syncSearchList();
          }
          int position = itemsToUpdate.size() - 1;
          while (position >= 0)
          {
            ListItem _item = itemsToUpdate.get(position);
            itemsToUpdate.remove(position);
            long tsp = itemsToUpdateTsp.get(position);
            itemsToUpdateTsp.remove(position);
            updateResource(_item, tsp);
            position = itemsToUpdate.size() - 1;
          }
        }
      }
    };
    registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));


    loadData();


    if (mainReceiver == null)
    {
      mainReceiver = new BroadcastReceiver()
      {
        @Override
        public void onReceive(Context context, Intent intent)
        {
          String eventType = intent.getStringExtra("event");
          if (eventType.equals("activity-offline"))
          {
            activityCount--;
            if (activityCount == 0)
            {
              startFinalCountdown();
            }
          }
          else if (eventType.equals("activity-online"))
          {
            activityCount++;
            if (countDownRunnableLife != null)
            {
              countDownHandler.removeCallbacks(countDownRunnableLife);
              countDownRunnableLife = null;
            }
            // if activity connected to already running service, provide it with data
            if (dataLoaded)
            {
              if (intent.getStringExtra("type").equals("main"))
              {
                sendDataToMain();
              }
            }
            else
            {
              loadData();
            }
          }
          else if (eventType.equals("data-request"))
          {
            ListItem item = (ListItem) intent.getSerializableExtra("item");
            long tsp = 0;
            String fileName = "resource_" + item.id;
            ArrayList<EventList> resource = null;

            if (resources.containsValue(item.id))
            {
              resource = resources.get(item.id);
              tsp = Long.parseLong(resourceTsp.get(item.id));
            }
            else if (FileAPI.isFileExists(getBaseContext(), fileName))
            {
              String resourceStr = FileAPI.readFile(getBaseContext(), fileName);
              resource = JSONParser.parceResource(resourceStr);
              tsp = JSONParser.getLastUpdateTsp(resourceStr);
              resources.put(item.id, resource);
              resourceTsp.put(item.id, "" + tsp);
            }

            if (resource != null)
            {
              sendResource(resource, false);
            }
            if (System.currentTimeMillis() - tsp > Config.updateValidFor)
            {
              if (isOnline())
              {
                updateResource(item, tsp);
              }
              else
              {
                itemsToUpdate.add(item);
                itemsToUpdateTsp.add(tsp);
              }
            }
          }
          else if (eventType.equals("new-history"))
          {
            updateHistory((ArrayList<ListItem>) intent.getSerializableExtra("history"));
          }
        }
      };
    }
    registerReceiver(mainReceiver, new IntentFilter("com.venomyd.nopay.timetable.data.service"));
  }

  public IBinder onBind(Intent intent)
  {
    Log.e(LOG_TAG, "onBind");
    return new Binder();
  }

  public void onDestroy()
  {
    super.onDestroy();
    if (networkStateReceiver != null)
    {
      unregisterReceiver(networkStateReceiver);
    }
    if (networkStateReceiver != null)
    {
      unregisterReceiver(networkStateReceiver);
    }
  }

  private String ajax(String url)
  {
    String out = "";

    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try
    {
      URL syncUrl = new URL(url);

      connection = (HttpURLConnection) syncUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      connection.connect();

      InputStream input = connection.getInputStream();
      StringBuffer buffer = new StringBuffer();

      if (input != null) {
        reader = new BufferedReader(new InputStreamReader(input));

        String line;
        while ((line = reader.readLine()) != null) {
          buffer.append(line + "\n");
        }

        out = buffer.toString();
      }
    }
    catch (SocketTimeoutException e)
    {
      Log.e(LOG_TAG, "error", e);
    }
    catch (IOException e)
    {
      Log.e(LOG_TAG, "error", e);
    }
    finally
    {
      if (connection != null) {
        connection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "closing stream", e);
        }
      }
    }

    return out;
  }

  private void updateResource(final ListItem item, final long _tsp)
  {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try
        {
          String url = getString(R.string.base_url) +
                  "sync/" + item.type +
                  "?tsp=" + _tsp +
                  "&id=" + URLEncoder.encode(item.id, "utf-8") +
                  "&api_key=" + apiKey;

          String response = ajax(url);

          if (response.length() > 0)
          {
            ArrayList<EventList> resource = JSONParser.parceResource(response);
            long tsp = JSONParser.getLastUpdateTsp(response);
            resources.put(item.id, resource);
            resourceTsp.put(item.id, "" + tsp);
            sendResource(resource, true);

            String filename = "resource_" + item.id;
            FileAPI.writeFile(getBaseContext(), filename, response);
          }
        }
        catch (UnsupportedEncodingException err)
        {
          Log.e(LOG_TAG, "parse id", err);
        }
      }
    }).start();
  }

  private void startFinalCountdown()
  {
    countDownRunnableLife = new Runnable()
    {
      @Override
      public void run()
      {
        stopSelf();
      }
    };
    countDownHandler.postDelayed(countDownRunnableLife, LIVE_WITHOUT_ACTIVITY);
  }

  private void loadData()
  {
    loadHistory();
    loadSearchList();
  }

  private void loadHistory()
  {
    String fileName = "history";
    String historyStr = null;
    if (FileAPI.isFileExists(getBaseContext(), fileName))
    {
      historyStr = FileAPI.readFile(getBaseContext(), fileName);
    }
    history = JSONParser.parseHistory(historyStr);

    sendHistory();
  }

  private void updateHistory(ArrayList<ListItem> _history)
  {
    if (_history == null)
    {
      return;
    }
    history = _history;
    String historyStr = JSONParser.stringifyHistory(_history);
    String filename = "history";
    FileAPI.writeFile(getBaseContext(), filename, historyStr);
  }

  private void loadSearchList()
  {
    long now = System.currentTimeMillis();
    searchListTsp = loadSearchListFromDisk();
    long lastListSync = pref.getLong("searchListSync", 0);
    if (isOnline() && now - lastListSync > SYNC_VALID_FOR)
    {
      syncSearchList();
    }
    else if (now - lastListSync > SYNC_VALID_FOR)
    {
      waitSearchListUpdate = true;
    }
  }

  private long loadSearchListFromDisk()
  {
    String fileName = "list";
    String listStr = null;
    if (FileAPI.isFileExists(getBaseContext(), fileName))
    {
      listStr = FileAPI.readFile(getBaseContext(), fileName);
    }

    searchList = JSONParser.parseSearchList(listStr);
    sendSearchList();

    return JSONParser.getSearchListTsp(listStr);
  }

  private void sendResource(ArrayList<EventList> list, boolean forceUpdate)
  {
    Intent intent = new Intent("timetable_table_activity");
    intent.putExtra("event", "resource");
    intent.putExtra("list", list);
    intent.putExtra("forceUpdate", forceUpdate);
    getBaseContext().sendBroadcast(intent);
  }

  private void sendHistory()
  {
    Intent intent = new Intent("timetable_main_activity");
    intent.putExtra("event", "history");
    intent.putExtra("history", history);
    getBaseContext().sendBroadcast(intent);
  }

  private void sendSearchList()
  {
    Intent intent = new Intent("timetable_main_activity");
    intent.putExtra("event", "searchList");
    intent.putExtra("searchList", searchList);
    getBaseContext().sendBroadcast(intent);
  }

  private void sendDataToMain()
  {
    sendHistory();
  }

  private void syncSearchList()
  {
    new Thread(new Runnable() {
      @Override
      public void run()
      {
        waitSearchListUpdate = false;
        long now = System.currentTimeMillis();

        String url = getString(R.string.base_url) +
                "lists?" +
                "tsp=" + searchListTsp +
                "&api_key=" + apiKey;

        String syncWebStr = ajax(url);

        if (syncWebStr.length() > 0)
        {
          dataLoaded = true;
          searchList = JSONParser.parseSearchList(syncWebStr);
          sendSearchList();

          FileAPI.writeFile(getBaseContext(), "list", syncWebStr);

          SharedPreferences.Editor editor = pref.edit();
          editor.putLong("searchListSync", now);
          editor.commit();
        }
      }
    }).start();
  }

  private boolean isOnline() {
    ConnectivityManager cm =
            (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    int permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

    return netInfo != null && netInfo.isConnectedOrConnecting() && permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

}
