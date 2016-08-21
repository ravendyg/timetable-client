package com.example.me.timetable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by me on 21/08/16.
 */
public class EventsArrayAdapter extends ArrayAdapter<EventElement>
{
  private static class ViewHolder
  {
    private ListView itemView;
  }

  public EventsArrayAdapter (Context contect, int resourceId, ArrayList<EventElement> items)
  {
    super(contect, resourceId, items);
  }

//  public View getView (int position, View convertView, ViewGroup parent)
//  {
//    if (convertView == null)
//    {
//      convertView =
//        LayoutInflater
//        .from(this.getContext())
//        .inflate(R.layout.)
//      ;
//    }
//  }
}
