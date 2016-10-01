package com.venomyd.nopay.timetable.data;

/**
 * Created by me on 27/08/16.
 */
public class PeriodsService
{
  private static final String [] times = new String [] {"9:00", "10:50", "12:40", "14:30", "16:20", "18:10", "20:00"};
  private static final String [] days = new String [] {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};

  public static final String [] getTimes ()
  {
    return times;
  }

  public static final String [] getDays ()
  {
    return days;
  }
}
