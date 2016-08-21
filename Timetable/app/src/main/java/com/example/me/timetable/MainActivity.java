package com.example.me.timetable;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.me.timetable.data.DbHelper;

import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
  private final String tag = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String syncData = "not connected";

    if ( isOnline() )
    {
      new SyncData().execute();
    }
  }

  private class SyncData extends AsyncTask<URL, Void, String []>
  {
    private final String tag = this.getClass().getSimpleName();

    private long now;

    protected String [] doInBackground (URL... urls)
    {
      now = (new Date()).getTime();
      long timestamp = getTimestamp();

      String syncData [] = new String[1];
      syncData[0] = HttpService.getSync("9:00", timestamp);

      return syncData;
    }

    protected void onPostExecute (String [] syncData)
    {
      for (int i = 0; i < syncData.length; i++)
      {
        Log.e(tag, syncData[i]);
      }

      setTimestamp(now);
    }
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
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getReadableDatabase();

    String queryString = "SELECT " + DbHelper.timeEntry.TIMESTAMP + " FROM " + DbHelper.timeEntry.TABLE_NAME +
      " WHERE " + DbHelper.timeEntry.COUNTER + " = 1;";

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
    // connect to db in write mode
    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(DbHelper.timeEntry.COUNTER, 1);
    values.put(DbHelper.timeEntry.TIMESTAMP, now);

    db.insert(DbHelper.timeEntry.TABLE_NAME, null, values);

    db.close();
  }
}







































