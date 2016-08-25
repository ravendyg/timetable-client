package com.example.me.timetable.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.me.timetable.R;

import java.util.ArrayList;

/**
 * Created by me on 24/08/16.
 */
public class RowAdapter extends BaseAdapter
{
  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<RowElement> items;

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
    if (view == null)
    {
      view = inflater.inflate(R.layout.row_item, parent, false);
    }

    RowElement element = getElement(position);

    ((TextView) view.findViewById(R.id.row_time)).setText(element.time);
    ((TextView) view.findViewById(R.id.row_title)).setText(element.title);
    ((TextView) view.findViewById(R.id.row_place)).setText(element.place);
    ((TextView) view.findViewById(R.id.row_person)).setText(element.person);

    return view;
  }
}



































