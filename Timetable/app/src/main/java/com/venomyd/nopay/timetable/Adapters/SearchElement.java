package com.venomyd.nopay.timetable.Adapters;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by me on 24/08/16.
 */
public class SearchElement implements Serializable, Comparable<SearchElement>
{
  public String text;
  public String type;
  public int id;
  public int fav;

  public static class CustomComparator implements Comparator<SearchElement>
  {
    @Override
    public int compare(SearchElement o1, SearchElement o2) {
      if ( o1.fav > o2.fav )
      {
        return -1;
      }
      else if ( o1.fav < o2.fav )
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }
  }

  public SearchElement (String _text, String _type, int _id, int _fav)
  {
    text = _text;
    type = _type;
    id = _id;
    fav = _fav;
  }

  public int compareTo (SearchElement el)
  {
    return text.compareTo(el.text);
  }

  @Override
  public String toString ()
  {
    return text;
  }
}
