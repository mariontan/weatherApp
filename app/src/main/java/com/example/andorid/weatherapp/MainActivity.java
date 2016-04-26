package com.example.andorid.weatherapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> weekForecast = new ArrayList<String>();
    private ArrayAdapter<String> listAdapter;
    WeatherApplication weatherApplication = new WeatherApplication();
    //String city = "Manila";
    @Override
    public void onStart(){
        super.onStart();
        downloadWeather();
        listAdapter.notifyDataSetChanged();

    }
    //save data when app stops
    @Override
    public void onStop(){
        //saving weather data from the past days
        String oldForecast=weatherApplication.getWeatherData(weatherApplication.getCityName());
        String newForecast = "";
        //storing of old data
        String oldData[] =oldForecast.split("\n");

        //checking if new data is in old data
        for (String oldFrcst : oldData) {
            if (weekForecast.contains(oldFrcst)==false) {
                Log.i("INFO", "Found message: " + oldFrcst);
                weekForecast.add(oldFrcst);
            }
        }

        for(String forecst:weekForecast){
            newForecast+=forecst;
            newForecast+="\n";
        }
        weatherApplication.saveWeatherData(newForecast,weatherApplication.getCityName());
        super.onStop();
    }
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView cityName = (TextView) findViewById(R.id.text_city);
        cityName.setText(weatherApplication.getCityName());

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, weekForecast);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    //For searching different cities
    public void searchCity(View view){
        TextView cityName = (TextView) findViewById(R.id.text_city);
        EditText searchCity = (EditText) findViewById(R.id.edit_search);
        //city = searchCity.getText().toString();
        weatherApplication.setCityName(searchCity.getText().toString());
        searchCity.setText("");
        cityName.setText(weatherApplication.getCityName());
        downloadWeather();
    }
    public void useLocation(){


    }

    private void downloadWeather(){
        AsyncTask<String,Void, String[]> dlTsk = new AsyncTask<String, Void, String[]>() {
            //gets the name of the app
            private final String LOG_TAG = MainActivity.class.getSimpleName();
            private String getReadableDateString(long time) {
                // Because the API returns a unix timestamp (measured in seconds),
                // it must be converted to milliseconds in order to be converted to valid date.
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(time);
            }
            private String formatTemps(double high, double low, double day, String unitType) {
                if(unitType.equalsIgnoreCase("imperial")){
                    high = high*1.8 + 32;
                    low = low*1.8 + 32;
                    day = day*1.8 + 32;
                }
                else if(unitType.equalsIgnoreCase("kelvin")){
                    high = high + 273.15;
                    low = low + 273.15;
                    day = day + 273.15;
                }
                else{
                    high = high;
                    low = low;
                    day = day;
                }

                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);
                long roundedDay = Math.round(day);

                String highLowStr = roundedHigh + "/" + roundedLow + "(" + roundedDay + ")";
                return highLowStr;
            }
            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // Names of the JSON objects used for extraction
                final String OWM_LIST = "list";

                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWN_HUMIDITY = "humidity";

                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWN_DAY = "day";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                // OWM returns daily forecasts based upon the local time of the city that is being
                // asked for, which means that we need to know the GMT offset to translate this data
                // properly.

                // Since this data is also sent in-order and the first day is always the
                // current day, we're going to take advantage of that to get a nice
                // normalized UTC date for all of our weather.

                Time dayTime = new Time();
                dayTime.setToNow();

                // we start at the day returned by local time. Otherwise this is a mess.
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                // now we work exclusively in UTC
                dayTime = new Time();

                String[] resultStrs = new String[numDays];
                for (int i = 0; i < weatherArray.length(); i++) {
                    String day;
                    String description;
                    String highAndLow;
                    String humidity;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherArray.getJSONObject(i);

                    // The date/time is returned as a long.  We need to convert that
                    // into something human-readable, since most people won't read "1400356800" as
                    // "this saturday".
                    long dateTime;
                    // Cheating to convert this to UTC time, which is what we want anyhow
                    dateTime = dayTime.setJulianDay(julianStartDay + i);
                    day = getReadableDateString(dateTime);

                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);
                    double dayTemp = temperatureObject.getDouble(OWN_DAY);

                    // Displays the humidity of the day
                    humidity = dayForecast.getString(OWN_HUMIDITY);

                    /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String unitType = prefs.getString(getString(R.string.pref_units_key),
                            getString(R.string.pref_metric));*/

                    highAndLow = formatTemps(high, low, dayTemp, getString(R.string.unit_default));
                    //Where the data is assembled
                    resultStrs[i] = day + " - " + highAndLow + " - " + description + " - " + humidity + "%";
                }

                for (String s : resultStrs) {
                    Log.v(LOG_TAG, "Forecast entry: " + s);
                }
                return resultStrs;

            }
            @Override
            protected String[] doInBackground (String...params){
                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String forecastJsonStr = null;

                int numDays = Integer.parseInt(getString(R.string.days_default));

                try {
                    /***http://openweathermap.org/current for seeing other data     http://openweathermap.org/weather-data#16days***/
                    //to be able to return new cities
                    Uri builtUri = Uri.parse(getString(R.string.forecast_base_url)).buildUpon()
                            .appendQueryParameter(getString(R.string.query_param), params[0])
                            .appendQueryParameter(getString(R.string.format_param), getString(R.string.format_default))
                            .appendQueryParameter(getString(R.string.units_param), getString(R.string.unit_default))
                            .appendQueryParameter(getString(R.string.days_param), getString(R.string.days_default))
                            .appendQueryParameter(getString(R.string.appid_param), getString(R.string.appid_default))
                            .build();

                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are available at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=a43ecc59eb6dc14d800bd5f635923bb9");

                    URL url = new URL(builtUri.toString());
                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        Log.e(LOG_TAG,"Error no data received check connection or city");
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                    Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    forecastJsonStr = null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            //returns the paresed data from openweate=her api
                            return getWeatherDataFromJson(forecastJsonStr, numDays);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
            //onPostExecute knows already which String[] result we are taking about 
            @Override
            protected void onPostExecute(String[] result) {
                if(result != null){
                    listAdapter.clear();
                    for(String dayForecastStr : result){
                        listAdapter.add(dayForecastStr);
                    }

                    //set the current temperature for the day
                    TextView textHigh = (TextView) findViewById(R.id.text_high);
                    TextView textLow = (TextView) findViewById(R.id.text_low);
                    TextView textCur = (TextView) findViewById(R.id.text_temp);

                    //listAdapter.getItem(0)will have the values for today
                    textHigh.setText("High:"+listAdapter.getItem(0).substring(listAdapter.getItem(0).indexOf("- ")+
                                        1,listAdapter.getItem(0).lastIndexOf("/")));
                    textLow.setText("Low:"+listAdapter.getItem(0).substring(listAdapter.getItem(0).lastIndexOf("/")+
                                        1,listAdapter.getItem(0).lastIndexOf("(")));
                    textCur.setText(listAdapter.getItem(0).substring(listAdapter.getItem(0).lastIndexOf("(")+
                                        1, listAdapter.getItem(0).lastIndexOf(")")));
                }
            }
        };
        dlTsk.execute(weatherApplication.getCityName());
    }
}
