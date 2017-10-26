package de.jordsand.birdcensus.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.core.SpeciesRepository;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteMonitoringAreaRepository;
import de.jordsand.birdcensus.database.repositories.SQLiteSpeciesRepository;
import de.jordsand.birdcensus.database.repositories.setup.DatabaseInflater;
import de.jordsand.birdcensus.services.OfflineMapSetupService;
import de.jordsand.birdcensus.services.OsmDroidOfflineMapSetupService;
import de.jordsand.birdcensus.services.SimpleBirdCountService;

/**
 * Main activity.
 * <p>
 * The user may either start/continue a bird count or show a past one.
 * </p>
 * @author Rico Bergmann
 */
public class Overview extends AppCompatActivity {
    private static final String TAG = Overview.class.getSimpleName();
    private static final int RQ_NEW_CENSUS = 111;
    private static final int RQ_CONTINUE_CENSUS = 222;

    private SimpleBirdCountService birdCountService;
    private boolean mBound = false;

    private Button showData;
    private Button newCensus;
    private Button continueCensus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        showData = (Button) findViewById(R.id.show_data);
        showData.setOnClickListener(new ShowDataBtnOnClickListener());
        newCensus = (Button) findViewById(R.id.start_census);
        newCensus.setOnClickListener(new StartCensusBtnOnClickListener());
        continueCensus = (Button) findViewById(R.id.continue_census);
        continueCensus.setOnClickListener(new ContinueCensusBtnOnClickListener());
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, SimpleBirdCountService.class);
        if (!SimpleBirdCountService.isRunning()) {
            startService(intent);
        }
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (mBound && birdCountService.isBirdCountOngoing()) {
            continueCensus.setEnabled(true);
            newCensus.setEnabled(false);
        } else {
            continueCensus.setEnabled(false);
            newCensus.setEnabled(true);
        }

        if (isFirstStart()) {
            BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
            MonitoringAreaRepository areaRepo = new SQLiteMonitoringAreaRepository(openHandler.getWritableDatabase());
            SpeciesRepository speciesRepo = new SQLiteSpeciesRepository(openHandler.getWritableDatabase());
            initFirstStart(speciesRepo, areaRepo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_NEW_CENSUS || requestCode == RQ_CONTINUE_CENSUS) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("terminated", false)) {
                    Toast.makeText(this, R.string.census_terminated, Toast.LENGTH_SHORT).show();
                } else if (data.getBooleanExtra("aborted", false)) {
                    Toast.makeText(this, R.string.census_aborted, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * @return {@code true} if the app was started for the first time ever, {@code false} otherwise
     */
    private boolean isFirstStart() {
        final String FIRST_START_KEY = "first_start";

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean firstStart = preferences.getBoolean(FIRST_START_KEY, true);

        if (firstStart) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_START_KEY, false);
            editor.apply();
        }

        return firstStart;
    }

    /**
     * Setup specifically for the first start. It will inflate the repositories.
     * @param speciesRepo the species repository
     * @param areaRepo the monitoring area repository
     */
    private void initFirstStart(SpeciesRepository speciesRepo, MonitoringAreaRepository areaRepo) {
        try {
            InputStream managementIndicatorSpeciesXML = getAssets().open("xml/management_indicator_species.xml");
            InputStream monitoringAreasXML = getAssets().open("xml/monitoring_areas.xml");
            DatabaseInflater inflater = DatabaseInflater.fromXML(managementIndicatorSpeciesXML, monitoringAreasXML, speciesRepo, areaRepo);
            inflater.inflate();

            OfflineMapSetupService offlineMapSetupService = new OsmDroidOfflineMapSetupService();
            InputStream offlineMapData = getAssets().open("Schleimuendung.zip");
            offlineMapSetupService.setup(offlineMapData, Environment.getExternalStorageDirectory().getPath() + File.separator + "osmdroid" + File.separator + "Schleimuendung.zip");

        } catch (IOException e) {
            Log.e(TAG, "Unable to open assets: " + e);
        }
    }

    /**
     * Connection to the {@link de.jordsand.birdcensus.services.BirdCountService}
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SimpleBirdCountService.BirdCountBinder binder = (SimpleBirdCountService.BirdCountBinder) service;
            birdCountService = binder.getService();
            mBound = true;

            if (birdCountService.isBirdCountOngoing()) {
                continueCensus.setEnabled(true);
                newCensus.setEnabled(false);
            } else {
                continueCensus.setEnabled(false);
                newCensus.setEnabled(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    /**
     * Handler for showing a past bird count
     */
    private class ShowDataBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent censusSelection = new Intent();
            censusSelection.setClass(Overview.this, CensusSelection.class);
            startActivity(censusSelection);
        }
    }

    /**
     * Handler for starting a new bird count
     */
    private class StartCensusBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent createCensus = new Intent(Overview.this, NewCensus.class);
            startActivityForResult(createCensus, RQ_NEW_CENSUS);
        }
    }

    /**
     * Handler for continuing an ongoing bird count
     */
    private class ContinueCensusBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent continueCensus = new Intent(Overview.this, AreaSelectionMap.class);
            startActivityForResult(continueCensus, RQ_CONTINUE_CENSUS);
        }
    }

}
