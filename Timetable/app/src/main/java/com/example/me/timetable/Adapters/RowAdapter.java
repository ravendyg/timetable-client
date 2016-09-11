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
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by me on 24/08/16.
 */
public class RowAdapter extends BaseAdapter
{
  private static final int TYPE_MAX_COUNT = 6;

  private static final int TYPE_ITEM = 0;
  private static final int TYPE_ITEM_LEFT = 1;
  private static final int TYPE_ITEM_RIGHT = 2;
  private static final int TYPE_ITEM_DOUBLE = 3;
  private static final int TYPE_EMPTY = 4;
  private static final int TYPE_SEPARATOR = 5;

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
    notifyDataSetChanged();
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
          ( (TextView) view.findViewById(R.id.item_day)).setText("");
        break;

        case TYPE_ITEM_LEFT:
          view = inflater.inflate(R.layout.row_item, parent, false);
//          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.odd);
        break;

        case TYPE_ITEM_RIGHT:
          view = inflater.inflate(R.layout.row_item, parent, false);
//          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.even);
        break;

        case TYPE_ITEM_DOUBLE:
          view = inflater.inflate(R.layout.row_double_item, parent, false);
        break;

        case TYPE_SEPARATOR:
          view = inflater.inflate(R.layout.row_header, parent, false);
        break;

        default:
          view = inflater.inflate(R.layout.empty_row_item, parent, false);
      }
    }

    String person1, person2;
    String timeLabel = "";
    if ( element.time != -1 )
    {
      timeLabel = times[element.time];
      if ( timeLabel.length() == 4 )
      {
        timeLabel = "0" + timeLabel;
      }
    }


    switch (type)
    {
      case TYPE_ITEM:
      case TYPE_ITEM_LEFT:
        person1 = reduceGroups(element.person[0]);
        ((TextView) view.findViewById(R.id.row_time)).setText(timeLabel);
        ((TextView) view.findViewById(R.id.item_title)).setText(element.title[0]);
        ((TextView) view.findViewById(R.id.item_content_place)).setText(element.place[0]);
        ((TextView) view.findViewById(R.id.item_content_person)).setText(person1);

        if ( type == TYPE_ITEM_LEFT )
        {
          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.odd);
        }
      break;

      case TYPE_ITEM_RIGHT:
        person2 = reduceGroups(element.person[1]);
        ((TextView) view.findViewById(R.id.row_time)).setText(timeLabel);
        ((TextView) view.findViewById(R.id.item_title)).setText(element.title[1]);
        ((TextView) view.findViewById(R.id.item_content_place)).setText(element.place[1]);
        ((TextView) view.findViewById(R.id.item_content_person)).setText(person2);

        ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.even);
      break;

      case TYPE_ITEM_DOUBLE:
        person1 = reduceGroups(element.person[0]);
        person2 = reduceGroups(element.person[1]);
        ((TextView) view.findViewById(R.id.row_time)).setText(timeLabel);
        ((TextView) view.findViewById(R.id.odd_item_title)).setText(element.title[0]);
        ((TextView) view.findViewById(R.id.odd_item_content_place)).setText(element.place[0]);
        ((TextView) view.findViewById(R.id.odd_item_content_person)).setText(person1);
        ((TextView) view.findViewById(R.id.even_item_title)).setText(element.title[1]);
        ((TextView) view.findViewById(R.id.even_item_content_place)).setText(element.place[1]);
        ((TextView) view.findViewById(R.id.even_item_content_person)).setText(person2);
      break;

      case TYPE_SEPARATOR:
        ((TextView) view.findViewById(R.id.row_header)).setText(element.title[0]);
      break;

      default:
        ((TextView) view.findViewById(R.id.row_time)).setText(timeLabel);

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

    int out;

    switch (element.type)
    {
      case 0:
        out = TYPE_ITEM;
      break;

      case 1:
        out = TYPE_ITEM_LEFT;
      break;

      case 2:
        out = TYPE_ITEM_RIGHT;
      break;

      case 3:
        out = TYPE_ITEM_DOUBLE;
      break;

      default:
        out = TYPE_EMPTY;
    }

    // rows without data - either headers or just empty
    int type = position % (1 + PeriodsService.getTimes().length);
    return type == 0 ? TYPE_SEPARATOR : out;
  }

  private String reduceGroups(String input)
  {
    if ( !input.matches(".*[0-9]+.*") )
    {
      return input;
    }
    String [] options = input.split(",");
    ArrayList<String> result = new ArrayList<String>();

    for (int i = 0; i < options.length; i++)
    {
      options[i] = options[i].replaceAll("(^\\s|\\s$)", "");
      options[i] = options[i].replaceAll("\\..*$", "");
    }
    for (int i = 0; i < options.length; i++)
    {
      if ( result.size() == 0 )
      {
        result.add( options[i] );
      }
      else
      {
        boolean notFound = true;
        for (int j = 0; j < result.size(); j++)
        {
          if ( result.get(j).equals(options[i]) )
          {
            notFound = false;
            break;
          }
        }
        if ( notFound )
        {
          result.add( options[i] );
        }
      }
    }
    String output = "";
    for (int j = result.size() - 1; j >= 0 ; j--)
    {
      if ( result.get(j).length() > 0 )
      {
        output += result.get(j) + ", ";
      }
    }
    output = output.replaceAll("\\,\\s$", "");
    return output;
  }
}



































