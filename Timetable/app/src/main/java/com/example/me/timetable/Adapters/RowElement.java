package com.example.me.timetable.Adapters;

/**
 * Created by me on 25/08/16.
 */
public class RowElement
{
  public int time;
  public String title;
  public String place;
  public String person;

  public RowElement (int _time, String _title, String _place, String _person)
  {
    time = _time;
    title = _title;
    place = _place;
    person =_person;
  }
}
