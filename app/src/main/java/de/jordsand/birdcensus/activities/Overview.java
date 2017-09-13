package de.jordsand.birdcensus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import de.jordsand.birdcensus.R;

public class Overview extends AppCompatActivity {
    private Button showData;
    private Button newCensus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        if (isFirstStart()) {
            initFirstStart();
        }

        showData = (Button) findViewById(R.id.show_data);
        showData.setOnClickListener(new ShowDataBtnOnClickListener());
        newCensus = (Button) findViewById(R.id.start_census);
        newCensus.setOnClickListener(new StartCensusBtnOnClickListener());
    }

    private boolean isFirstStart() {
        final String FIRST_START_KEY = "first_start";

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean firstStart = preferences.getBoolean(FIRST_START_KEY, true);

        if (firstStart) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_START_KEY, false);
            editor.commit();
        }

        return firstStart;
    }

    private void initFirstStart() {
        // TODO inflate database
        // TODO if central Service will be used, move to it
    }

    private class ShowDataBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent censusSelection = new Intent();
            censusSelection.setClass(getApplicationContext(), CensusSelection.class);
            startActivity(censusSelection);
        }
    }

    private class StartCensusBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent createCensus = new Intent(getApplicationContext(), NewCensus.class);
            startActivity(createCensus);
        }
    }

}
