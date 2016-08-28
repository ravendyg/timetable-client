package com.example.me.timetable.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

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

    public static final String POSITION = "position";

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

  public static class groupEntry implements BaseColumns
  {
    public static final String TABLE_NAME = "groups";

    public static final String NAME = "name";
  }

  public static class personEntry implements  BaseColumns
  {
    public static final String TABLE_NAME = "people";

    public static final String PERSON_ID = "person_id";

    public static final String FULL_NAME = "full_name";
  }

  private static final int DB_VERSION = 38;

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
        dataEntry.TIME        + " INTEGER NOT NULL, " +
        dataEntry.PLACE       + " TEXT NOT NULL, " +
        dataEntry.NAME        + " TEXT NOT NULL, " +
        dataEntry.GROUP       + " TEXT NOT NULL, " +
        dataEntry.POSITION    + " INTEGER NOT NULL, " +
        dataEntry.PERSON      + " TEXT NOT NULL, " +
        dataEntry.PERSON_ID   + " INTEGER NOT NULL, " +
        dataEntry.FULL_NAME   + " TEXT NOT NULL, " +
        dataEntry.STATUS      + " INTEGER NOT NULL, " +
        dataEntry.TIMESTAMP   + " INTEGER NOT NULL, " +

        "UNIQUE (" +
          dataEntry.DAY + ", "  + dataEntry.TIME + ", " +
          dataEntry.PLACE + ", " + dataEntry.GROUP +
        ") ON CONFLICT REPLACE);";

    final String CREATE_STAMP_TABLE =
      "CREATE TABLE " + timeEntry.TABLE_NAME + " (" +
        timeEntry._ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        timeEntry.COUNTER     + " INTEGER NOT NULL, " +
        timeEntry.TIMESTAMP   + " INTEGER NOT NULL, " +
        "UNIQUE (" + timeEntry.COUNTER + ") ON CONFLICT REPLACE);";

    final String CREATE_GROUPS_TABLE =
      "CREATE TABLE " + groupEntry.TABLE_NAME + " (" +
        groupEntry._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        groupEntry.NAME + " TEXT NOT NULL, " +
        "UNIQUE (" + groupEntry.NAME + ") ON CONFLICT REPLACE);";

    final String CREATE_PEOPLE_TABLE =
      "CREATE TABLE " + personEntry.TABLE_NAME + " (" +
        personEntry._ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        personEntry.PERSON_ID   + " INTEGER NOT NULL, " +
        personEntry.FULL_NAME   + " TEXT NOT NULL, " +
        "UNIQUE (" + personEntry.PERSON_ID + ") ON CONFLICT REPLACE);";

    db.execSQL(CREATE_STAMP_TABLE);
    db.execSQL(CREATE_TABLE_TABLE);
    db.execSQL(CREATE_GROUPS_TABLE);
    db.execSQL(CREATE_PEOPLE_TABLE);
  }

  @Override
  public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + dataEntry.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + timeEntry.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + groupEntry.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + personEntry.TABLE_NAME);
    onCreate(db);
  }
}

































