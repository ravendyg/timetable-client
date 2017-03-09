package com.venomyd.timetable.DataModels;

import java.io.Serializable;

/**
 * Created by me on 26/02/17.
 */

public class ListItem implements Serializable
{
  public String id;
  public String name;
  public String type = null;

  public ListItem(String _id, String _name, String _type)
  {
    id = _id;
    name = _name;
    type = _type;
  }
}
