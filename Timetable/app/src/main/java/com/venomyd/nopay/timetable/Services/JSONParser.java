package com.venomyd.nopay.timetable.Services;

import android.util.Log;

import com.venomyd.nopay.timetable.DataModels.ListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;


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
          history.add(new ListItem(id, name));
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
      temp += item.name + "\"}";
      out += temp;
    }
    out += "]";
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
          String tempItem = temp.getString(i);
          searchList.add(new ListItem(tempItem, tempItem));
        }

        temp = rawList.getJSONArray("places");
        for (int i = 0; i < temp.length(); i++ )
        {
          String tempItem = temp.getString(i);
          searchList.add(new ListItem(tempItem, tempItem));
        }

        JSONArray _temp = rawList.getJSONArray("teachers");
        for (int i = 0; i < _temp.length(); i++ )
        {
          String id = _temp.getJSONObject(i).getString("teacherId");
          String name = _temp.getJSONObject(i).getString("name");
          searchList.add(new ListItem(id, name));
        }
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

//  public static long getTimestamp(JSONObject input)
//  {
//    long timestamp = 0;
//    try
//    {
//      timestamp = input.getLong("timestamp");
//    }
//    catch (JSONException err)
//    {
//    }
//
//    return timestamp;
//  }
//
//  public static ArrayList<WayGroup> getWayGroups(JSONObject input)
//          throws JSONException
//  {
//    ArrayList <WayGroup> wayGroups = new ArrayList<>(Arrays.asList(new WayGroup[0]));
//
//    JSONArray routes = input.getJSONArray("routes");
//
//    for (int i = 0; i < routes.length(); i++)
//    {
//      try
//      {
//        JSONObject rawRoute = routes.getJSONObject(i);
//        int type = rawRoute.getInt("type");
//
//        WayGroup group = new WayGroup(type);
//
//        JSONArray ways = rawRoute.getJSONArray("ways");
//        for (int j = 0; j < ways.length(); j++)
//        {
//          JSONObject way = ways.getJSONObject(j);
//          group.addWay( new Way(way) );
//        }
//
//        wayGroups.add(group);
//      }
//      catch (JSONException err)
//      {
//        Log.e(LOG_TAG, "error", err);
//      }
//    }
//
//    return wayGroups;
//  }
//
//  public static HashMap<String, StopInfo> extractStops(JSONObject input)
//  {
//    HashMap<String, StopInfo> out = new HashMap<String, StopInfo>();
//    Iterator<String> stopIds = input.keys();
//    while (stopIds.hasNext())
//    {
//      try
//      {
//        String _id = stopIds.next();
//        JSONObject temp = input.getJSONObject(_id);
//
//        StopInfo stopInfo = new StopInfo(_id, Double.parseDouble(temp.getString("lat")), Double.parseDouble(temp.getString("lng")));
//        if (temp.has("n"))
//        {
//          stopInfo.setName(temp.getString("n"));
//        }
//        if (temp.has("vehicles"))
//        {
//          Iterator<String> vehicles = temp.getJSONObject("vehicles").keys();
//          while (vehicles.hasNext())
//          {
//            stopInfo.setBus(vehicles.next());
//          }
//        }
//
//        out.put(_id, stopInfo);
//      }
//      catch (JSONException err)
//      {
//        Log.e(LOG_TAG, "error", err);
//      }
//    }
//
//    return out;
//  }
//
//  public static HashMap<String, HashSet<String>> extractBusStops(JSONObject input)
//  {
//    HashMap<String, HashSet<String>> out = new HashMap<String, HashSet<String>>();
//    Iterator<String> vehicles = input.keys();
//    while (vehicles.hasNext())
//    {
//      try
//      {
//        String _id = vehicles.next();
//        JSONObject stopsHolder = input.getJSONObject(_id);
//        HashSet<String> stop = new HashSet<String>();
//        Iterator<String> stops = stopsHolder.keys();
//        while (stops.hasNext())
//        {
//          String stopsCode = stops.next();
//          stop.add(stopsCode);
//        }
//        out.put(_id, stop);
//      }
//      catch (JSONException err)
//      {
//        Log.e(LOG_TAG, "error", err);
//      }
//    }
//
//    return out;
//  }
//
//  public static ArrayList<GeoPoint> parseRoutePoints(JSONArray points)
//          throws JSONException
//  {
//    ArrayList<GeoPoint> out = new ArrayList<>(Arrays.asList(new GeoPoint[0]));
//    for (int i = 0; i < points.length(); i++)
//    {
//      JSONObject point = points.getJSONObject(i);
//      out.add(new GeoPoint(Double.parseDouble(point.getString("lat")), Double.parseDouble(point.getString("lng"))));
//    }
//    return out;
//  }
}

