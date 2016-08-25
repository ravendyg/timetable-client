package com.example.me.timetable.Adapters;

/**
 * Created by me on 21/08/16.
 */
public class EventElement
{
  public int day;
  public String place;
  public String name;
  public String group;
  public String person;
  public int personId;
  public String fullName;
  public int status;
  public long timestamp;

  public EventElement (
    int newDay,
    String newPlace,
    String newGroup,
    String newName,
    String newPerson,
    int newPersonId,
    String newFullName,
    int newStatus,
    long newTimestamp
  )
  {
    day = newDay;
    place= newPlace;
    group = newGroup;
    status = newStatus;
    timestamp = newTimestamp;

    if (status == 1)
    {
      name = newName;
      person = newPerson;
      personId = newPersonId;
      fullName = newFullName;
    }
    else
    {
      name = "";
      person = "";
      personId = 0;
      fullName = "";
    }
  }
}
