package com.venomyd.timetable.DataModels;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by me on 5/03/17.
 */

public class LessonItem implements Serializable
{
  public String name;
  public String line2;  // place / place / teacher
  public String line3;  // teacher / groups / groups
  public ArrayList<ListItem> links;

  public LessonItem(String _name, String _line2, String _line3, ArrayList<ListItem> _links)
  {
    name = _name;
    line2= _line2;
    line3= _line3;
    links = _links;
  }
}
