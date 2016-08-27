package com.example.me.timetable.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.me.timetable.R;
import com.example.me.timetable.data.PeriodsService;

import java.util.ArrayList;

/**
 * Created by me on 24/08/16.
 */
public class RowAdapter extends BaseAdapter
{
  private static final int TYPE_MAX_COUNT = 2;

  private static final int TYPE_ITEM = 0;
  private static final int TYPE_SEPARATOR = 1;

  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<RowElement> items;
  private String [] times = PeriodsService.getTimes();

  public RowAdapter (Context context, ArrayList<RowElement> data)
  {
    ctx = context;
    items = data;
    inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount ()
  {
    return items.size();
  }

  @Override
  public Object getItem (int position)
  {
    return items.get(position);
  }

  public void clear ()
  {
    items.clear();
  }

  public void addAll (ArrayList<RowElement> list)
  {
    items.addAll(list);
  }

  @Override
  public long getItemId (int position)
  {
    return position;
  }

  public RowElement getElement (int position)
  {
    return (RowElement) getItem(position);
  }

  @Override
  public View getView (int position, View _view, ViewGroup parent)
  {
    View view = _view;

    int type = getItemViewType(position);

    RowElement element = getElement(position);

    if (view == null)
    {
      switch (type)
      {
        case TYPE_ITEM:
          view = inflater.inflate(R.layout.row_item, parent, false);
        break;

        case TYPE_SEPARATOR:
          view = inflater.inflate(R.layout.row_header, parent, false);
        break;
      }
    }

    switch (type)
    {
      case TYPE_ITEM:
        ((TextView) view.findViewById(R.id.row_time)).setText(times[element.time]);
        ((TextView) view.findViewById(R.id.row_title)).setText(element.title);
        ((TextView) view.findViewById(R.id.row_place)).setText(element.place);
        ((TextView) view.findViewById(R.id.row_person)).setText(element.person);
        break;

      case TYPE_SEPARATOR:
        ((TextView) view.findViewById(R.id.row_header)).setText(element.title);
        break;
    }

    return view;
  }

  @Override
  public int getViewTypeCount() {
    return TYPE_MAX_COUNT;
  }

  @Override
  public int getItemViewType(int position) {
    int type = position % (1 + PeriodsService.getTimes().length);
    return type == 0 ? TYPE_SEPARATOR : TYPE_ITEM;
  }
}



































