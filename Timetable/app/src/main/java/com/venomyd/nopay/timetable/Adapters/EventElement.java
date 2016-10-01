package com.venomyd.nopay.timetable.Adapters;

/**
 * Created by me on 21/08/16.
 */
public class EventElement
{
  public int day;
  public String time;
  public String place;
  public String name;
  public String group;
  public String person;
  public int personId;
  public String fullName;
  public int position;
  public int status;
  public long timestamp;

  public EventElement (
    int newDay,
    String newTime,
    String newPlace,
    String newGroup,
    String newName,
    String newPerson,
    int newPersonId,
    String newFullName,
    int newPosition,
    int newStatus,
    long newTimestamp
  )
  {
    day = newDay;
    time = newTime;
    place = newPlace;
    group = newGroup;
    status = newStatus;
    timestamp = newTimestamp;

    if (status == 1)
    {
      name = newName;
      person = newPerson;
      personId = newPersonId;
      fullName = newFullName;
      position = newPosition;
    }
    else
    {
      name = "";
      person = "";
      personId = 0;
      fullName = "";
      position = 0;
    }
  }
}
