package com.example.me.timetable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by me on 25/08/16.
 */
public class TableActivity extends AppCompatActivity
{
  private SearchElement element;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_table);

    element = (SearchElement) getIntent().getSerializableExtra("data");

    TextView tempName = (TextView) findViewById(R.id.temp_name);
    tempName.setText( element.text );
  }
}
