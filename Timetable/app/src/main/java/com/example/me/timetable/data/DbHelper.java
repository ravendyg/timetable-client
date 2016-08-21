package com.example.me.timetable.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by me on 21/08/16.
 */
public class DbHelper extends SQLiteOpenHelper
{
  public static class dataEntry implements BaseColumns
  {
    public static final String TABLE_NAME = "time_table";

    public static final String TIME = "time";

    public static final String DAY = "day";

    public static final String PLACE = "place";

    public static final String NAME = "name";

    public static final String GROUP = "group_id";

    public static final String PERSON = "person";

    public static final String PERSON_ID = "person_id";

    public static final String FULL_NAME = "full_name";

    public static final String STATUS = "status";

    public static final String TIMESTAMP = "timestamp";
  }

  public static class timeEntry implements BaseColumns
  {
    public static final String TABLE_NAME = "timestamp_table";

    public static final String COUNTER = "counter";

    public static final String TIMESTAMP = "timestamp";
  }

  private static final int DB_VERSION = 3;

  static final String DB_NAME = "timetable.db";

  public DbHelper (Context context)
  {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate (SQLiteDatabase db)
  {
    final String CREATE_TABLE_TABLE =
      "CREATE TABLE " + dataEntry.TABLE_NAME + " (" +
        dataEntry._ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        dataEntry.DAY         + " INTEGER NOT NULL, " +
        dataEntry.TIME        + " TEXT NOT NULL, " +
        dataEntry.PLACE       + " TEXT NOT NULL, " +
        dataEntry.NAME        + " TEXT NOT NULL, " +
        dataEntry.GROUP       + " TEXT NOT NULL, " +
        dataEntry.PERSON      + " TEXT NOT NULL, " +
        dataEntry.PERSON_ID   + " INTEGER NOT NULL, " +
        dataEntry.FULL_NAME   + " TEXT NOT NULL, " +
        dataEntry.STATUS      + " INTEGER NOT NULL, " +
        dataEntry.TIMESTAMP   + " INTEGER NOT NULL, " +

        "UNIQUE (" +
          dataEntry.DAY + ", "  + dataEntry.TIME + ", " +
          dataEntry.PLACE + ", " + dataEntry.TIMESTAMP +
        ") ON CONFLICT REPLACE);";

    final String CREATE_STAMP_TABLE =
      "CREATE TABLE " + timeEntry.TABLE_NAME + " (" +
        timeEntry._ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        timeEntry.COUNTER     + " INTEGER NOT NULL, " +
        timeEntry.TIMESTAMP   + " INTEGER NOT NULL, " +
        "UNIQUE (" + timeEntry.COUNTER + ") ON CONFLICT REPLACE);";

    db.execSQL(CREATE_STAMP_TABLE);
    db.execSQL(CREATE_TABLE_TABLE);

//    Log.d("sql", CREATE_TABLE_TABLE);
  }

  @Override
  public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + dataEntry.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + timeEntry.TABLE_NAME);
    onCreate(db);
  }
}

































