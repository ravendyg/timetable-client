package com.venomyd.nopay.timetable.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.venomyd.nopay.timetable.DataModels.ListItem;
import com.venomyd.nopay.timetable.R;

import java.util.ArrayList;

/**
 * Created by me on 24/08/16.
 */
public class SearchAdapter extends BaseAdapter
{
  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<ListItem> items;

  private ArrayList<ListItem> fileredItems;

  public SearchAdapter(Context context, ArrayList<ListItem> data)
  {
    ctx = context;
    items = data;
    fileredItems = data;
    inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount()
  {
    return fileredItems.size();
  }

  @Override
  public Object getItem(int position)
  {
    return fileredItems.get(position);
  }

  public void clear()
  {
    items.clear();
    fileredItems.clear();
    notifyDataSetChanged();
  }

  public void addAll(ArrayList<ListItem> list)
  {
    if (list.size() > 1)
    {
      items.addAll(list);
    }
    else if (list.size() == 1)
    {
      items.add(list.get(0));
    }
    notifyDataSetChanged();
  }

  @Override
  public long getItemId (int position)
  {
    return position;
  }

  public ListItem getElement (int position)
  {
    return (ListItem) getItem(position);
  }

  @Override
  public View getView (int position, View _view, ViewGroup parent)
  {
    View view = _view;
    if (view == null)
    {
      view = inflater.inflate(R.layout.search_item, parent, false);
    }

    ListItem element = getElement(position);

    ((TextView) view.findViewById(R.id.search_item_text)).setText(element.name);

    return view;
  }
}



































