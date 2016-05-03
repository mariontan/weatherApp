package com.example.andorid.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.List;

public class CurrentLocation extends AppCompatActivity {
    String lat = "";
    String lon = "";
    private ArrayAdapter<String> listAdapter;
    List<String> stationforecast = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        getLatLon();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationforecast);
        ListView listView = (ListView) findViewById(R.id.weatherList);
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_current_location, menu);
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

    public void getLatLon() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                lat = String.valueOf(location.getLatitude());
                lon = String.valueOf(location.getLongitude());
                TextView txvLat = (TextView) findViewById(R.id.Lat);
                TextView txvLon = (TextView) findViewById(R.id.Lon);
                txvLat.setText(lat);
                txvLon.setText(lon);
                downloadCurWeather(lat, lon);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        downloadCurWeather(lat,lon);
    }
    public void downloadCurWeather(final String lat,final String lon ){
        AsyncTask<String,Void, String[]> dlTsk = new AsyncTask<String, Void, String[]>() {
            //gets the name of the app
            private final String LOG_TAG = CurrentLocation.class.getSimpleName();
            private String formatTemps(double temp, String unitType) {
                if(unitType.equalsIgnoreCase("imperial")){
                    temp = temp*1.8 + 32;
                }
                else if(unitType.equalsIgnoreCase("kelvin")){
                    temp = temp + 273.15;
                }
                else{
                    temp = temp;
                }

                double roundedTemp = Math.round(temp);

                String temperature = String.valueOf(roundedTemp);
                return temperature;
            }
            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // Names of the JSON objects used for extraction
                final String OWM_LIST = "list";

                final String OWM_WEATHER = "last";

                final String OWM_STATION = "station";
                final String OWM_DESCRIPTION = "main";

                final String OWM_NAME = "name";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_HUMIDITY = "humidity";
                final String OWM_PRESSURE = "pressure";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
                String[] resultStrs = new String[numDays];
                for (int i = 0; i < 7; i++) {
                    //Log.v(LOG_TAG,"forecast"+weatherArray.getJSONObject(i).toString());
                    String name;
                    Double temp;
                    Double pressure;
                    Double humidity;
                    JSONObject locationForecast = weatherArray.getJSONObject(i);

                    JSONObject stationObj = locationForecast.getJSONObject(OWM_STATION);
                    name = stationObj.getString(OWM_NAME);

                    JSONObject lastObj = locationForecast.getJSONObject(OWM_WEATHER);
                    JSONObject mainObj = lastObj.getJSONObject(OWM_DESCRIPTION);
                    temp = mainObj.getDouble(OWM_TEMPERATURE);
                    humidity = mainObj.getDouble(OWM_HUMIDITY);
                    pressure = mainObj.getDouble(OWM_PRESSURE);
                    resultStrs[i]= "sation name: "+ name+" "+ formatTemps(temp-273.15,"")+" "+humidity+"%"+" "+pressure;
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
                    Uri builtUri = Uri.parse(getString(R.string.forecast_location_url)).buildUpon()
                            .appendQueryParameter(getString(R.string.latitude), lat)
                            .appendQueryParameter(getString(R.string.longhitude),lon)
                            .appendQueryParameter(getString(R.string.days_param), getString(R.string.days_default))
                            .appendQueryParameter(getString(R.string.appid_param), getString(R.string.appid_default))
                            .build();

                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are available at OWM's forecast API page, at
                    URL url = new URL(builtUri.toString());
                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    Log.i(LOG_TAG,builtUri.toString());
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
                    forecastJsonStr = " {\"list\":"+buffer.toString()+"}";
                    Log.v(LOG_TAG, "Forecast JSON current location String: " + forecastJsonStr);
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
                }
            }
        };
        dlTsk.execute();
    }

}
