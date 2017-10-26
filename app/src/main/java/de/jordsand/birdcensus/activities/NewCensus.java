package de.jordsand.birdcensus.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.WeatherData;
import de.jordsand.birdcensus.services.SimpleBirdCountService;
import de.jordsand.birdcensus.services.SimpleBirdCountService.BirdCountBinder;

/**
 * Starts a new census.
 * <p>
 * All metadata has to be entered at this point
 * </p>
 * @author Rico Bergmann
 */
public class NewCensus extends AppCompatActivity {
    private static final int RQ_NEW_CENSUS = 444;

    private SimpleBirdCountService birdCountService;
    private boolean mBound = false;

    private EditText startTime;
    private EditText observer;
    private EditText waterGauge;
    private EditText windStrength;
    private Spinner windDirection;
    private Spinner precipitation;
    private Spinner visibility;
    private Spinner glaciationLvl;
    private Button createBirdCount;

    private Date startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_census);

        setUpUI();
        startDate = new Date();
        startTime.setText(formatDate(startDate));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SimpleBirdCountService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_NEW_CENSUS && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("terminated", false) || data.getBooleanExtra("aborted", false)) {
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    private void setUpUI() {
        startTime = (EditText) findViewById(R.id.edit_start);
        observer = (EditText) findViewById(R.id.edit_observer_name);
        waterGauge = (EditText) findViewById(R.id.edit_water_gauge);
        windStrength = (EditText) findViewById(R.id.edit_wind_strength);

        windDirection = (Spinner) findViewById(R.id.spin_wind_direction);
        ArrayAdapter<CharSequence> windDirAdapter = ArrayAdapter.createFromResource(this, R.array.wind_dirs, android.R.layout.simple_spinner_item);
        windDirAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        windDirection.setAdapter(windDirAdapter);

        precipitation = (Spinner) findViewById(R.id.spin_precipitation);
        ArrayAdapter<CharSequence> precipitationAdapter = ArrayAdapter.createFromResource(this, R.array.precipitation, android.R.layout.simple_spinner_item);
        precipitationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        precipitation.setAdapter(precipitationAdapter);

        visibility = (Spinner) findViewById(R.id.spin_visibility);
        ArrayAdapter<CharSequence> visibilityAdapter = ArrayAdapter.createFromResource(this, R.array.visibility, android.R.layout.simple_spinner_item);
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibility.setAdapter(visibilityAdapter);

        glaciationLvl = (Spinner) findViewById(R.id.spin_glaciation_lvl);
        ArrayAdapter<CharSequence> glaciationLvlAdapter = ArrayAdapter.createFromResource(this, R.array.glaciation_level, android.R.layout.simple_spinner_item);
        glaciationLvlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        glaciationLvl.setAdapter(glaciationLvlAdapter);

        createBirdCount = (Button) findViewById(R.id.btn_go);
        createBirdCount.setOnClickListener(new StartNewBirdCountOnClickListener());
    }

    private WeatherData fetchWeatherData() {
        Double wg;
        try {
            wg = Double.valueOf(waterGauge.getText().toString());
        } catch (NumberFormatException e) {
            wg = null;
        }

        Integer ws;
        try {
            ws = Integer.valueOf(windStrength.getText().toString());
        } catch (NumberFormatException e) {
            ws = null;
        }

        int selectedWindDirection = windDirection.getSelectedItemPosition() - 1;
        WeatherData.WindDirection wd;
        if (selectedWindDirection >= 0) {
            wd = WeatherData.WindDirection.values()[selectedWindDirection];
        } else {
            wd = null;
        }

        WeatherData.Precipitation pre = WeatherData.Precipitation.values()[precipitation.getSelectedItemPosition()];
        WeatherData.Visibility vis = WeatherData.Visibility.values()[visibility.getSelectedItemPosition()];
        WeatherData.GlaciationLevel glaclvl = WeatherData.GlaciationLevel.values()[glaciationLvl.getSelectedItemPosition()];

        return new WeatherData(wg, ws, wd, pre, vis, glaclvl);
    }

    private String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMANY);
        return format.format(date);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BirdCountBinder binder = (BirdCountBinder) service;
            birdCountService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    private class StartNewBirdCountOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            initBirdCount();
            Intent areaSelection = new Intent(NewCensus.this, AreaSelectionMap.class);
            startActivityForResult(areaSelection, RQ_NEW_CENSUS);
        }

        private void initBirdCount() {
            WeatherData weatherData = fetchWeatherData();
            String observerName = observer.getText().toString();
            birdCountService.startBirdCount(startDate, observerName, weatherData);
        }
    }
}
