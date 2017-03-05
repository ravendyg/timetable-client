package com.venomyd.nopay.timetable.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.venomyd.nopay.timetable.DataModels.Lesson;
import com.venomyd.nopay.timetable.DataModels.LessonItem;
import com.venomyd.nopay.timetable.R;
import com.venomyd.nopay.timetable.data.PeriodsService;

import java.util.ArrayList;

/**
 * Created by me on 24/08/16.
 */
public class RowAdapter extends BaseAdapter
{
  private static final int TYPE_MAX_COUNT = 50;

  private static final int TYPE_ITEM_GROUP = 1;
  private static final int TYPE_ITEM_TEACHER = 2;
  private static final int TYPE_ITEM_PLACE = 3;

  private static final int POSITION_HEADER = -1;
  private static final int POSITION_EVERY = 0;
  private static final int POSITION_ODD = 1;
  private static final int POSITION_EVEN = 2;
  private static final int POSITION_BOTH = 3;
  private static final int POSITION_EMPTY = 4;

  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<Lesson> items;
  private String [] times = PeriodsService.getTimes();

  public RowAdapter (Context context, ArrayList<Lesson> data)
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

  public void clear()
  {
    items.clear();
    notifyDataSetChanged();
  }

  public void addAll(ArrayList<Lesson> list)
  {
    items.addAll(list);
  }

  @Override
  public long getItemId (int position)
  {
    return position;
  }

  public Lesson getElement (int position)
  {
    return (Lesson) getItem(position);
  }

  @Override
  public View getView (int position, View _view, ViewGroup parent)
  {
    View view = _view;

    Lesson element = getElement(position);

    if (view == null)
    {
      switch (element.position)
      {
        case POSITION_EVERY:
          view = inflater.inflate(R.layout.row_item, parent, false);
          ( (TextView) view.findViewById(R.id.item_day)).setText("");
        break;

        case POSITION_ODD:
          view = inflater.inflate(R.layout.row_item, parent, false);
//          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.odd);
        break;

        case POSITION_EVEN:
          view = inflater.inflate(R.layout.row_item, parent, false);
//          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.even);
        break;

        case POSITION_BOTH:
          view = inflater.inflate(R.layout.row_double_item, parent, false);
        break;

        case POSITION_EMPTY:
          view = inflater.inflate(R.layout.empty_row_item, parent, false);
        break;

        case POSITION_HEADER:
          view = inflater.inflate(R.layout.row_header, parent, false);
      }
    }

    LessonItem _item = null;
    switch (element.position)
    {
      case POSITION_EVERY:
        _item = element.items.get(0);
      case POSITION_ODD:
        _item = _item == null ? element.items.get(1) : _item;
      case POSITION_EVEN:
        _item = _item == null ? element.items.get(2) : _item;

        String line3;
        if (element.type == TYPE_ITEM_GROUP)
        {
          line3 = _item.line3;
        }
        else
        {
          line3 = "reduce groups";
        }
        ((TextView) view.findViewById(R.id.row_time)).setText(element.time);
        ((TextView) view.findViewById(R.id.name)).setText(_item.name);
        ((TextView) view.findViewById(R.id.line2)).setText(_item.line2);
        ((TextView) view.findViewById(R.id.line3)).setText(line3);

        if (element.position == POSITION_ODD )
        {
          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.odd);
        }
        else if (element.position == POSITION_EVEN)
        {
          ( (TextView) view.findViewById(R.id.item_day)).setText(R.string.even);
        }
      break;

      case POSITION_BOTH:
        LessonItem _item1 = element.items.get(1);
        LessonItem _item2 = element.items.get(2);
        String line31, line32;
        if (element.type == TYPE_ITEM_GROUP)
        {
          line31 = _item1.line3;
          line32 = _item2.line3;
        }
        else
        {
          line31 = "reduce groups 1";
          line32 = "reduce groups 2";
        }

        ((TextView) view.findViewById(R.id.row_time)).setText(element.time);
        ((TextView) view.findViewById(R.id.name1)).setText(_item1.name);
        ((TextView) view.findViewById(R.id.line21)).setText(_item1.line2);
        ((TextView) view.findViewById(R.id.line31)).setText(line31);
        ((TextView) view.findViewById(R.id.name2)).setText(_item2.name);
        ((TextView) view.findViewById(R.id.line22)).setText(_item2.line2);
        ((TextView) view.findViewById(R.id.line32)).setText(line32);
      break;

      case POSITION_HEADER:
        ((TextView) view.findViewById(R.id.row_header)).setText(element.dayName);
      break;

      default:
        ((TextView) view.findViewById(R.id.row_time)).setText(element.time);

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
    Lesson element = getElement(position);

    return element.type * 10 + element.position;
  }
}



































