package com.venomyd.nopay.timetable;

import java.lang.reflect.Array;

/**
 * Created by me on 4/03/17.
 */

public class Config
{
  public static final String[] bells = {"9:00", "10:50", "12:40", "14:30", "16:20", "18:10", "20:00"};
  public static final String[] longBells = {
          "9.00 - 9.45", "9.50 - 10.35",
          "10.50 - 11.35", "11.40 - 12.25",
          "12.40 - 13.25", "13.30 - 14.15",
          "14.30 - 15.15", "15.20 - 16.05",
          "16.20 - 17.05", "17.10 - 17.55",
          "18.10 - 18.55", "19.00 - 19.45"
  };
  public static final String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};

  public static final long updateValidFor = 1000 * 60 * 60 * 6;
}
