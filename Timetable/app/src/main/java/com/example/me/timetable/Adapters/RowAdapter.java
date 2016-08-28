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
import java.util.regex.Pattern;

/**
 * Created by me on 24/08/16.
 */
public class RowAdapter extends BaseAdapter
{
  private static final int TYPE_MAX_COUNT = 3;

  private static final int TYPE_ITEM = 0;
  private static final int TYPE_DOUBLE_ITEM = 2;
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

        case TYPE_DOUBLE_ITEM:
          view = inflater.inflate(R.layout.row_double_item, parent, false);
          break;

        case TYPE_SEPARATOR:
          view = inflater.inflate(R.layout.row_header, parent, false);
        break;
      }
    }

    String person1, person2;
    switch (type)
    {
      case TYPE_ITEM:
        person1 = reduceGroups(element.person[0]);
        ((TextView) view.findViewById(R.id.row_time)).setText(times[element.time]);
        ((TextView) view.findViewById(R.id.row_title)).setText(element.title[0]);
        ((TextView) view.findViewById(R.id.row_place)).setText(element.place[0]);
        ((TextView) view.findViewById(R.id.row_person)).setText(person1);
      break;

      case TYPE_DOUBLE_ITEM:
        person1 = reduceGroups(element.person[0]);
        person2 = reduceGroups(element.person[1]);
        ((TextView) view.findViewById(R.id.row_time)).setText(times[element.time]);
        ((TextView) view.findViewById(R.id.row_title1)).setText(element.title[0]);
        ((TextView) view.findViewById(R.id.row_place1)).setText(element.place[0]);
        ((TextView) view.findViewById(R.id.row_person1)).setText(person1);
        ((TextView) view.findViewById(R.id.row_title2)).setText(element.title[1]);
        ((TextView) view.findViewById(R.id.row_place2)).setText(element.place[1]);
        ((TextView) view.findViewById(R.id.row_person2)).setText(person2);
      break;

      case TYPE_SEPARATOR:
        ((TextView) view.findViewById(R.id.row_header)).setText(element.title[0]);
      break;
    }

    return view;
  }

  @Override
  public int getViewTypeCount()
  {
    return TYPE_MAX_COUNT;
  }

  @Override
  public int getItemViewType(int position)
  {
    RowElement element = getElement(position);

    // rows with data
    switch (element.type)
    {
      case 0:
        return TYPE_ITEM;
      case 1:
      case 2:
      case 3:
        return TYPE_DOUBLE_ITEM;
    }

    // rows without data - either headers or just empty
    int type = position % (1 + PeriodsService.getTimes().length);
    return type == 0 ? TYPE_SEPARATOR : TYPE_ITEM;
  }

  private String reduceGroups(String input)
  {
    String [] options = input.split(",");
    for (int i = 0; i < options.length; i++)
    {
      options[i] = options[i].replaceAll("(^\\s|\\s$)", "");
    }
    for (int i = 0; i < options.length - 1; i++)
    {
      for (int j = i + 1; j < options.length; j++)
      {
        if (options[i].length() == 0)
        {
          break;
        }
        if ( options[i].replaceAll("\\.[0-9]{1,}", "").equals( options[j].replaceAll("\\.[0-9]{1,}", "") ) )
        {
          options[i] = options[i].replaceAll("\\.[0-9]{1,}", "");
          options[j] = "";
          break;
        }
      }
    }
    String output = "";
    for (int j = options.length - 1; j >= 0; j--)
    {
      if ( options[j].length() > 0 )
      {
        output += options[j] + ", ";
      }
    }
    output = output.replaceAll("\\,\\s$", "");
    return output;
  }
}



































