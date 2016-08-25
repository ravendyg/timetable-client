package com.example.me.timetable.Adapters;

import java.io.Serializable;

/**
 * Created by me on 24/08/16.
 */
public class SearchElement implements Serializable, Comparable<SearchElement>
{
  public String text;
  public String type;
  public int id;

  public SearchElement (String _text, String _type, int _id)
  {
    text = _text;
    type = _type;
    id = _id;
  }

  public int compareTo (SearchElement el)
  {
    return text.compareTo(el.text);
  }
}
