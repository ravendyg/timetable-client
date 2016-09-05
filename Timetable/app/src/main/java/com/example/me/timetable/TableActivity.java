package com.example.me.timetable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.me.timetable.Adapters.RowAdapter;
import com.example.me.timetable.Adapters.RowElement;
import com.example.me.timetable.Adapters.SearchElement;
import com.example.me.timetable.data.DbHelper;
import com.example.me.timetable.data.DbHelper.dataEntry;
import com.example.me.timetable.data.PeriodsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by me on 25/08/16.
 */
public class TableActivity extends AppCompatActivity
{
  private SearchElement element;

  private RowAdapter rowAdapter;

  private ArrayList<RowElement> list = new ArrayList<RowElement>(Arrays.asList(new RowElement[0]));

  private ListView tableList;

  private int daysLength = PeriodsService.getDays().length;
  private int timesLength = PeriodsService.getTimes().length;

  private ArrayList<String> times = new ArrayList<String>(Arrays.asList(PeriodsService.getTimes() ));

  private RowElement [] items = new RowElement[daysLength * (1 + timesLength)];

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_table);

    LinearLayout table = new LinearLayout(this);

    element = (SearchElement) getIntent().getSerializableExtra("data");

    ActionBar bar = getSupportActionBar();
    bar.setTitle(element.text);
    bar.setDisplayHomeAsUpEnabled(false);

    rowAdapter = new RowAdapter(this, list);

    tableList = (ListView) findViewById(R.id.table_list);
    tableList.setAdapter(rowAdapter);

    for (int j = 0; j < daysLength * (1 + timesLength); j++)
    {
      int time = j % (1 + timesLength) - 1;
      items[j] =
        new RowElement (
          time,
          time == -1 ? PeriodsService.getDays()[j / (1 + timesLength)] : ""
        );
    }

    refreshList();

  }

  private void refreshList ()
  {
    ArrayList<RowElement> matching = new ArrayList<RowElement>(Arrays.asList(new RowElement[0]));

    DbHelper mDbHelper = new DbHelper(this);
    SQLiteDatabase db = mDbHelper.getReadableDatabase();

    String queryString;

    if (element.type.equals("group") )
    {
      queryString =
        "SELECT " + dataEntry.TIME + ", " + dataEntry.NAME + ", " + dataEntry.PLACE + ", " + dataEntry.PERSON +
                ", " + dataEntry.DAY + ", " + dataEntry.POSITION +
        " FROM " + dataEntry.TABLE_NAME +
        " WHERE " + dataEntry.GROUP + " LIKE '" + element.text + "'" +
        " ORDER BY " + dataEntry.TIME + " ASC " +
        ";";
    }
    else
    {
      queryString =
            "SELECT " + dataEntry.TIME + ", " + dataEntry.NAME +", " + dataEntry.PLACE + ", " + dataEntry.GROUP +
                  ", " + dataEntry.DAY + ", " + dataEntry.POSITION +
            " FROM " + dataEntry.TABLE_NAME +
            " WHERE " + dataEntry.PERSON_ID + "='" + element.id + "'" +
            " ORDER BY " + dataEntry.TIME + " ASC, " + dataEntry.GROUP + " DESC" +
        ";";
    }

    Cursor cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      int timePointer = times.indexOf( cursor.getString(0) );
      // day * number of rows per day + time
      items[cursor.getInt(4) * (1 + timesLength) + timePointer + 1]
      .addElement(
          timePointer,
          cursor.getString(1),
          cursor.getString(2),
          cursor.getString(3),
          cursor.getInt(5)
        );
    }

    list.addAll(
      new ArrayList<RowElement>(Arrays.asList(items))
    );

    cursor.close();
  }
}
