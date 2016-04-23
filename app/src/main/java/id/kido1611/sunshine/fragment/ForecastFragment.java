package id.kido1611.sunshine.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import id.kido1611.sunshine.DetailActivity;
import id.kido1611.sunshine.R;
import id.kido1611.sunshine.SettingsActivity;

/**
 * Created by Ahmad on 4/23/2016.
 */
public class ForecastFragment extends Fragment {

    private static boolean DEBUG = false;

    public ForecastFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private ArrayAdapter<String> mForecastAdapter;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                Intent i = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(pos));
                startActivity(i);
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            updateWeather();
            return true;
        }else if(id==R.id.action_settings){
            Intent i = new Intent(getActivity(), SettingsActivity.class);
            startActivity(i);
            return true;
        }else if(id==R.id.action_map){
            openPreferredLocationMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationMap(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPrefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(geoLocation);

        if(i.resolveActivity(getActivity().getPackageManager())!=null){
            startActivity(i);
        }
    }

    private void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Toast.makeText(getActivity(), "lokasi : "+location, Toast.LENGTH_SHORT).show();
        weatherTask.execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private String getReadableDateString(long time){
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low, String unitType){
            if(unitType.equals(getString(R.string.pref_units_imperial))){
                high = (high  *1.8)+32;
                low = (low *1.8)+32;
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            return roundedHigh + "/"+roundedLow;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {

            final String OWM_LIST = "list";
            final String OWM_WEAHTER = "weather";
            final String OWM_TEMPERATUR = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            final String OWM_CITY = "city";
            final String OWM_CITY_NAME = "name";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityObject = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityObject.getString(OWM_CITY_NAME);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            SharedPreferences sharefPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharefPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));

            String[] resultStr = new String[numDays];
            for(int i=0;i<weatherArray.length();i++){
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEAHTER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATUR);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low, unitType);

                resultStr[i] = cityName+" - "+day+" - "+description+" - "+highAndLow;
            }
            return resultStr;
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length==0){
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";
                final String APPID_DATA = "e2b075d68c39dc43e16995653fcd6fd0";

                Uri builtUri;
                if(DEBUG){
                    builtUri = Uri.parse("http://192.168.1.180/sunshine/ambarawa.php").buildUpon().build();
                }else{
                    builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .appendQueryParameter(APPID_PARAM, APPID_DATA)
                            .build();
                }

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(inputStream==null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine())!= null){
                    buffer.append(line+"\n");
                }

                if(buffer.length()==0){

                }
                forecastJsonStr = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
                if(reader!=null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {

            if(strings!=null){
                mForecastAdapter.clear();
                for(String dayForecast : strings){
                    mForecastAdapter.add(dayForecast);
                }
                mForecastAdapter.notifyDataSetChanged();
            }

        }
    }
}
