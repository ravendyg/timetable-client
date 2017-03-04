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


import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.R;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

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

  private ArrayList<ListItem> history, searchList = null;

  private SharedPreferences pref;

  private BroadcastReceiver mainReceiver;

  private BroadcastReceiver networkStateReceiver = null;

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
            if (countDownRunnableLife != null) {
              countDownHandler.removeCallbacks(countDownRunnableLife);
              countDownRunnableLife = null;
            }
            // if activity connected to already running service, provide it with data
            if (dataLoaded)
            {
              sendDataToMain();
            }
            else
            {
              loadData();
            }
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

  private void loadSearchList()
  {
    final long now = System.currentTimeMillis();
    final long tsp = loadSearchListFromDisk();
    long lastListSync = pref.getLong("searchListSync", 0);
    if (isOnline() && now - lastListSync > SYNC_VALID_FOR)
    {
      syncSearchList(now, tsp);
    }
    else if (now - lastListSync > SYNC_VALID_FOR)
    {
      networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo ni = manager.getActiveNetworkInfo();
          if (ni != null)
          {
            syncSearchList(now, tsp);
            unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
          }
        }
      };
      registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
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

  private void syncSearchList(final long now, final long tsp)
  {
    new Thread(new Runnable() {
      @Override
      public void run() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        String syncWebStr = "";
        try
        {
          URL syncUrl = new URL(
                  getString(R.string.base_url) +
                          "lists?" +
                          "tsp=" + tsp +
                          "&api_key=" + apiKey
          );

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

            syncWebStr = buffer.toString();
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
