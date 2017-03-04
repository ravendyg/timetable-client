package com.venomyd.nopay.timetable.DataModels;

import java.util.ArrayList;

/**
 * Created by me on 4/03/17.
 */

public class EventList
{
  public String id;
  public String name;
  public String line2;  // place / place / teacher
  public String line3;  // teacher / groups / groups
  public ArrayList<ListItem> links;
  public String time = null;
  public String timeLine1 = null;
  public String timeLine2 = null;

  public EventList(String _id, String _name, String _line2, String _line3, ArrayList<ListItem> _links)
  {
    id = _id;
    name = _name;
    line2= _line2;
    line3= _line3;
    links = _links;
  }
}
