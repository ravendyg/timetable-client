package com.venomyd.timetable.DataModels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by me on 4/03/17.
 */

public class Lesson implements Serializable
{
  private static final int POSITION_HEADER = -1;
  private static final int POSITION_EVERY = 0;
  private static final int POSITION_ODD = 1;
  private static final int POSITION_EVEN = 2;
  private static final int POSITION_BOTH = 3;
  private static final int POSITION_EMPTY = 4;

//  public String id;
  public int type;
  public int position;

  public ArrayList<LessonItem> items;
  public String time;
  public String timeLine1;
  public String timeLine2;

  public String dayName = null;
  public boolean open = false;
  public int counter;

  public Lesson(
//          String _id,
          int _type, ArrayList<LessonItem> _items, int _counter,
                String _time, String _timeLine1, String _timeLine2)
  {
//    id = _id;
    type = _type;
    items = _items;
    counter = _counter;
    time = _time;
    timeLine1 = _timeLine1;
    timeLine2 = _timeLine2;

    if (_items == null)
    {
      position = POSITION_HEADER;
    }
    else if (_items.size() == 0)
    {
      position = POSITION_EMPTY;
    }
    else if (_items.get(0) != null)
    {
      position = POSITION_EVERY;
    }
    else if (_items.get(2) == null)
    {
      position = POSITION_ODD;
    }
    else if (_items.get(1) == null)
    {
      position = POSITION_EVEN;
    }
    else
    {
      position = POSITION_BOTH;
    }
  }
}

