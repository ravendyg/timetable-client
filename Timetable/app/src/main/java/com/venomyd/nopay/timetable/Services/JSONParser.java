package com.venomyd.nopay.timetable.Services;

import android.util.Log;

import com.venomyd.nopay.timetable.Config;
import com.venomyd.nopay.timetable.DataModels.Lesson;
import com.venomyd.nopay.timetable.DataModels.LessonItem;
import com.venomyd.nopay.timetable.DataModels.ListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;


/**
 * Created by me on 6/11/16.
 */
public class JSONParser
{
  private static final String LOG_TAG = "JSON parser";

  public static ArrayList<ListItem> parseHistory(String str)
  {
    ArrayList<ListItem> history = new ArrayList<>(Arrays.asList(new ListItem[0]));

    if (str != null)
    {
      try
      {
        JSONArray rawHistory = new JSONArray(str);
        for (int i = 0; i < rawHistory.length(); i++ )
        {
          JSONObject temp = rawHistory.getJSONObject(i);
          String id = temp.getString("id");
          String name = temp.getString("name");
          String type = temp.getString("type");
          history.add(new ListItem(id, name, type));
        }
      }
      catch (JSONException err)
      {
        Log.e(LOG_TAG, "parse history", err);
      }
    }

    return history;
  }

  public static String stringifyHistory(ArrayList<ListItem> _history)
  {
    String out = "[";
    for (int i = 0; i < _history.size(); i++)
    {
      String temp = "{\"id\":\"";
      if (i > 0)
      {
         temp = "," + temp;
      }
      ListItem item = _history.get(i);
      temp += item.id + "\",\"name\":\"";
      temp += item.name + "\",\"type\":\"";
      temp += item.type + "\"}";
      out += temp;
    }
    out += "]";
    return out;
  }

  public static ArrayList<Lesson> parseResource(String str, String type)
  {
    ArrayList<Lesson> out = new ArrayList<Lesson>(Arrays.asList(new Lesson[0]));
    Lesson temp;

    try
    {
      int _type = type.equals("groups")
        ? 1
        : type.equals("teachers")
        ? 2
        : 3
        ;
      JSONObject events = (new JSONObject(str)).getJSONObject("events");
      for (int day = 1; day <= 7; day++)
      {
//        int day = 1;
        temp = new Lesson(-1, null, (day - 1) * (Config.bells.length + 1), null, null, null);
        temp.dayName = Config.days[day - 1];
        out.add(temp);
        for (int bell = 0; bell < 7; bell++)
        {
          ArrayList<LessonItem> _items = new ArrayList<>(Arrays.asList(new LessonItem[0]));

          String _bell = Config.bells[bell],
                 _longBell1 = Config.longBells[bell*2],
                 _longBell2 = Config.longBells[bell*2 + 1];
          if (events.has(day + "|" + bell))
          {
            JSONArray ev = events.getJSONArray(day + "|" + bell);
            for (int j = 0; j < 3; j++)
            {
              if (ev.isNull(j) || ev.getString(j).equals("[]"))
              {
                _items.add(null);
              }
              else
              {
                JSONObject evUnit = ev.getJSONObject(j);
                ArrayList<ListItem> links = new ArrayList<>(Arrays.asList(new ListItem[0]));
                ListItem _lIt;
                String name = evUnit.getString("name");
                String line2 = "", line3 = "";
                if (type.equals("places"))
                {
                  if (evUnit.has("teacherName"))
                  {
                    line2 = evUnit.getString("teacherName");
                  }
                  if (evUnit.has("teacherId"))
                  {
                    _lIt = new ListItem(evUnit.getString("teacherId"), line2, "teachers");
                    links.add(_lIt);
                  }
                }
                else if (evUnit.has("placeId"))
                {
                  line2 = evUnit.getString("placeId");
                  _lIt = new ListItem(line2, line2, "places");
                  links.add(_lIt);
                }
                if (type.equals("groups"))
                {
                  if (evUnit.has("teacherName"))
                  {
                    line3 = evUnit.getString("teacherName");
                  }
                  if (evUnit.has("teacherId"))
                  {
                    _lIt = new ListItem(evUnit.getString("teacherId"), line3, "teachers");
                    links.add(_lIt);
                  }
                }
                else if (evUnit.has("groups"))
                {
                  JSONArray _groups = evUnit.getJSONArray("groups");
                  int len = _groups.length();
                  line3 = reduceGroupsList(_groups);
                  for (int i = 0; i < len; i++)
                  {
                    String _group = _groups.getString(i);
                    _lIt = new ListItem(_group, _group, "groups");
                    links.add(_lIt);
                  }
                }
                _items.add(new LessonItem(name, line2, line3, links));
              }
            }
            // if bells overloaded
          }
          temp = new Lesson(_type, _items, (day - 1) * (Config.bells.length + 1) + bell + 1,
                  _bell, _longBell1, _longBell2);
          out.add(temp);
        }
      }
    }
    catch (JSONException err)
    {
      Log.e(LOG_TAG, "parse resource", err);
    }

    return out;
  }

  public static long getLastUpdateTsp(String str)
  {
    long out = 0;
    try
    {
      JSONObject temp = new JSONObject(str);
      out = temp.getLong("tsp");
    }
    catch (JSONException err)
    {
      Log.e(LOG_TAG, "parse search list", err);
    }
    return out;
  }

  public static ArrayList<ListItem> parseSearchList(String str)
  {
    ArrayList<ListItem> searchList = new ArrayList<>(Arrays.asList(new ListItem[0]));

    if (str != null)
    {
      try
      {
        JSONObject rawList = new JSONObject(str);

        JSONArray temp = rawList.getJSONArray("groups");
        for (int i = 0; i < temp.length(); i++ )
        {
          String id = temp.getJSONObject(i).getString("groupId");
          String name = temp.getJSONObject(i).getString("name");
          searchList.add(new ListItem(id, name, "groups"));
        }

        temp = rawList.getJSONArray("places");
        for (int i = 0; i < temp.length(); i++ )
        {
          String id = temp.getJSONObject(i).getString("placeId");
          String name = temp.getJSONObject(i).getString("name");
          searchList.add(new ListItem(id, name, "places"));
        }

        temp = rawList.getJSONArray("teachers");
        for (int i = 0; i < temp.length(); i++ )
        {
          String id = temp.getJSONObject(i).getString("teacherId");
          String name = temp.getJSONObject(i).getString("name");
          searchList.add(new ListItem(id, name, "teachers"));
        }

        Log.e(LOG_TAG, "sorted");
      }
      catch (JSONException err)
      {
        Log.e(LOG_TAG, "parse search list", err);
      }
    }

    return searchList;
  }

  public static long getSearchListTsp(String str)
  {
    long tsp = 0;
    if (str != null)
    {
      try
      {
        tsp = (new JSONObject(str)).getLong("tsp");
      }
      catch (JSONException err)
      {
        Log.e(LOG_TAG, "parse search list tsp", err);
      }
    }

    return tsp;
  }

  private static String reduceGroupsList(JSONArray groups)
  {
    HashMap<String , String> gr = new HashMap<>();
    String out = "";
    int len = groups.length();
    try
    {
      for (int i = 0; i < len; i++)
      {
        String raw = groups.getString(i);
        String key = raw.replace(".*\\.[0-9]$", "");
        if (gr.containsKey(key))
        {
          gr.put(key, key);
        }
        else
        {
          gr.put(key, raw);
        }
        gr.put(key, raw);
      }
      Iterator<String> it = gr.values().iterator();
      while (it.hasNext())
      {
        len--;
        out += it.next();
        if (len > 0)
        {
          out += ", ";
        }
      }
    }
    catch (JSONException err)
    {
      Log.e(LOG_TAG, "parse groups array", err);
    }
    return out;
  }

}

