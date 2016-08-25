package com.example.me.timetable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.me.timetable.Adapters.RowAdapter;
import com.example.me.timetable.Adapters.RowElement;
import com.example.me.timetable.Adapters.SearchElement;
import com.example.me.timetable.data.DbHelper;
import com.example.me.timetable.data.DbHelper.dataEntry;

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

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_table);

    element = (SearchElement) getIntent().getSerializableExtra("data");

    ActionBar bar = getSupportActionBar();
    bar.setTitle(element.text);
    bar.setDisplayHomeAsUpEnabled(false);

    rowAdapter = new RowAdapter(this, list);

    tableList = (ListView) findViewById(R.id.table_list);
    tableList.setAdapter(rowAdapter);

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
        " FROM " + dataEntry.TABLE_NAME +
        " WHERE " + dataEntry.GROUP + "='" + element.text + "';";
    }
    else
    {
      queryString =
        "SELECT " + dataEntry.TIME + ", " + dataEntry.NAME +", " + dataEntry.PLACE + ", " + dataEntry.PERSON +
        " FROM " + dataEntry.TABLE_NAME +
        " WHERE " + dataEntry.PERSON_ID + "='" + element.id + "';";
    }

    Cursor cursor = db.rawQuery(queryString, null);

    while (cursor.moveToNext())
    {
      RowElement item = new RowElement(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));

      list.add(item);
    }

    cursor.close();
  }
}
