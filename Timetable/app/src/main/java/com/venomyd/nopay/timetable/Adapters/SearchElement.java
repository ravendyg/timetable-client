package com.venomyd.nopay.timetable.Adapters;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by me on 24/08/16.
 */
public class SearchElement implements Serializable
{
  public String text;
  public int id;
  public String type = "1";


  public SearchElement(String _text, int _id)
  {
    text = _text;
    id = _id;
  }

  @Override
  public String toString ()
  {
    return text;
  }
}
