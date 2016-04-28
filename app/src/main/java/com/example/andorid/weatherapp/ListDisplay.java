package com.example.andorid.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ListDisplay extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_display);
        Intent intent = getIntent();
        String suggestions = "";
        String forecast = intent.getStringExtra(Intent.EXTRA_TEXT);

        String day = forecast.substring(0, 10);
        String high = forecast.substring(forecast.indexOf("- ") +
                1, forecast.lastIndexOf("/"));
        String low = forecast.substring(forecast.lastIndexOf("/")+
                1,forecast.lastIndexOf("("));
        String cur = forecast.substring(forecast.lastIndexOf("(")+
                1, forecast.lastIndexOf(")"));
        String weather = forecast.substring(forecast.lastIndexOf(") - ")
                +4,forecast.lastIndexOf(" - "));
        String humidity = forecast.substring(forecast.lastIndexOf(" - ")+3);

        TextView txvDay = (TextView) findViewById(R.id.Day);
        txvDay.setText("Day: "+day);
        TextView txvHigh = (TextView) findViewById(R.id.high);
        txvHigh.setText("High: "+high);
        TextView txvLow = (TextView) findViewById(R.id.low);
        txvLow.setText("Low: "+low);
        TextView txvCur = (TextView) findViewById(R.id.curTemp);
        txvCur.setText("Current: "+cur);
        TextView txvWeat = (TextView) findViewById(R.id.weather);
        txvWeat.setText("Weather: "+weather);
        TextView txvHumid = (TextView) findViewById(R.id.humidity);
        txvHumid.setText("Humidity: " + humidity);

        //add suggestions
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
