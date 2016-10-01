package com.venomyd.nopay.timetable.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.venomyd.nopay.timetable.R;

import java.util.ArrayList;

/**
 * Created by me on 24/08/16.
 */
public class SearchAdapter extends BaseAdapter// implements Filterable
{
  private Context ctx;
  private LayoutInflater inflater;
  private ArrayList<SearchElement> items;

  private ArrayList<SearchElement> fileredItems;

  public SearchAdapter (Context context, ArrayList<SearchElement> data)
  {
    ctx = context;
    items = data;
    fileredItems = data;
    inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount ()
  {
    return fileredItems.size();
  }

  @Override
  public Object getItem (int position)
  {
    return fileredItems.get(position);
  }

  public void clear ()
  {
    items.clear();
    fileredItems.clear();
    notifyDataSetChanged();
  }

  public void addAll (ArrayList<SearchElement> list)
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
/*
  @Override
  public Filter getFilter ()
  {
    return new Filter ()
    {
      @Override
      protected FilterResults performFiltering (CharSequence constraint)
      {
        FilterResults results = new FilterResults();

        if(constraint == null || constraint.length() == 0)
        {
          results.values = new ArrayList<SearchElement>(Arrays.asList(new SearchElement[0]));
          results.count = 0;
        }
        else
        {
          ArrayList<SearchElement> filterRes = new ArrayList<SearchElement>();

          for (SearchElement elem : items)
          {
            if (elem.text.toLowerCase().matches( "(.*)" + constraint.toString().toLowerCase() + "(.*)"))
            {
              filterRes.add(elem);
            }
          }

          results.values = filterRes;
          results.count = filterRes.size();
        }

        return results;
      }

      @Override
      protected void publishResults (CharSequence constraint, FilterResults res)
      {
        fileredItems = (ArrayList<SearchElement>)res.values;
        notifyDataSetChanged();
      }
    };
  }
  */
}



































