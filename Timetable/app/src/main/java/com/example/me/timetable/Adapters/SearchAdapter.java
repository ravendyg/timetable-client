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
public class SearchAdapter extends BaseAdapter
{
  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<SearchElement> items;

  public SearchAdapter (Context context, ArrayList<SearchElement> data)
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

  public void addAll (ArrayList<SearchElement> list)
  {
    items.addAll(list);
  }

  @Override
  public long getItemId (int position)
  {
    return position;
  }

  public SearchElement getElement (int position)
  {
    return (SearchElement) getItem(position);
  }

  @Override
  public View getView (int position, View _view, ViewGroup parent)
  {
    View view = _view;
    if (view == null)
    {
      view = inflater.inflate(R.layout.search_item, parent, false);
    }

    SearchElement element = getElement(position);

    ((TextView) view.findViewById(R.id.search_item_text)).setText(element.text);

    return view;
  }
}



































