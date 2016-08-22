package com.example.me.timetable;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by me on 21/08/16.
 */
public class HttpService
{
  private final String tag = "http service";

  private final String SYNC_API_URL = "http://192.168.1.157:3011/sync";

  public String getSync (String time, long timestamp)
  {
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    String dataSrt = "" ;

      try
      {
        Uri builtUri = Uri.parse(SYNC_API_URL).buildUpon()
                .appendQueryParameter("time", time)
                .appendQueryParameter("timestamp", "" + timestamp)
                .build();

        URL url = new URL(builtUri.toString());

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        InputStream input = connection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        if (input == null)
        {
          return dataSrt;
        }

        reader = new BufferedReader(new InputStreamReader(input));

        String line;
        while ((line = reader.readLine()) != null)
        {
          buffer.append(line + "\n");
        }

        if (buffer.length() == 0)
        {
          return dataSrt;
        }

        dataSrt = buffer.toString();
      }
      catch (IOException e)
      {
        Log.e(tag, "error", e);
      }
      finally
      {
        if (connection != null)
        {
          connection.disconnect();
        }
        if (reader != null)
        {
          try
          {
            reader.close();
          } catch (final IOException e)
          {
            Log.e(tag, "closing stream", e);
          }
        }
      }

    return dataSrt;
  }
}














































