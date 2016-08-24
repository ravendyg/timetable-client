package com.example.me.timetable;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by me on 21/08/16.
 */
public class ResponseParser
{
  public static EventElement [] getElements (String rawJson)
    throws JSONException
  {
    EventElement [] result;
    try
    {
      JSONArray list = new JSONObject(rawJson).getJSONArray("changes");
      result = new EventElement[list.length()];

      for (int i = 0; i < list.length(); i++)
      {
        JSONObject elem = list.getJSONObject(i);
        result[i] =
          new EventElement(
            elem.getInt("d"),
            elem.getString("p"),
            elem.getString("g"),
            elem.getString("n"),
            elem.getString("pn"),
            elem.getInt("pi"),
            elem.getString("f"),
            elem.getInt("s"),
            elem.getLong("ts")
          );

      }
    }
    catch (JSONException e)
    {
      Log.e("parser", "read data", e);
      result = new EventElement[0];
    }

    return result;
  }

  public static String getType (String rawJson)
    throws JSONException
  {
    String type = "new";
    try
    {
      type = new JSONObject(rawJson).getString("flag");
    }
    catch (JSONException e)
    {
      Log.e("parser", "get response type", e);
    }

    return type;
  }
}
