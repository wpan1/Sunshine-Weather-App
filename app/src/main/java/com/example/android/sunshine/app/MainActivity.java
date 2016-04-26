package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.data.WeatherContract;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

    public static String LOG_CAT = "Sunshine";
    public static String mLocation = null;
    public static boolean mLandscapeLayout = false;
    public static String FORECASTFRAGMENT_TAG = "forecastTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get location from settings
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mLandscapeLayout = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Time dayTime = new Time();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            long dateTime = dayTime.setJulianDay(julianStartDay);
            Uri dataUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(Utility.getPreferredLocation(this), dateTime);
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI_KEY, dataUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, FORECASTFRAGMENT_TAG)
                    .commit();
        } else {
            mLandscapeLayout = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mLandscapeLayout);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.get_location) {
            OpenPrefferedLocationOnMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void OpenPrefferedLocationOnMap() {
        // Get location from settings
        SharedPreferences locationPref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = locationPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        // Create a implicit intent for GoogleMaps
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        // If possible, startActivity
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    @Override
    public void onItemSelected(Uri dataUri) {
        // Check if in landscape mode
        if (mLandscapeLayout) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI_KEY, dataUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, fragment, FORECASTFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(dataUri);
            startActivity(intent);
        }
    }
}
