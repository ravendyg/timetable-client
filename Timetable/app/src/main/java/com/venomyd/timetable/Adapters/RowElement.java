package com.venomyd.timetable.Adapters;

/**
 * Created by me on 25/08/16.
 */
public class RowElement
{
  public int time;
  public String [] title = new String [] {"", ""};
  public String [] place = new String [] {"", ""};
  public String [] person = new String [] {"", ""};
  public int type = 0;  // -1 - plug, 0 - every week, 1 - uneven, 2 - event, 3 - switching

  public RowElement (int _time, String _title)
  {
    addElement(_time, _title, "", "", -1);
  }

  public void addElement (int _time, String _title, String _place, String _person, int position)
  {
    int index =
      position == 1 || position == 2
        ? position - 1
        : 0
      ;

    time = _time;

    title[index] = _title;
    place[index] = _place;
    person[index] = person[index].equals("") ? _person : person[index] + ", " + _person;

    if (position == -1)
    {
      type = -1;
    }
    else if (position == 0)
    {
      type = 0;
    }
    else if (position == 1 || position == 2)
    {
      if (type == 0 || type == -1)
      {
        type = position;
      }
      else if ( type != position )
      {
        type = 3;
      }
    }
  }
}
