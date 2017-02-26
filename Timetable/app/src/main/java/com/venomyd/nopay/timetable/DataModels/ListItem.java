package com.venomyd.nopay.timetable.DataModels;

import java.io.Serializable;

/**
 * Created by me on 26/02/17.
 */

public class ListItem implements Serializable
{
  public String id;
  public String name;

  public ListItem(String _id, String _name)
  {
    id = _id;
    name = _name;
  }
}
