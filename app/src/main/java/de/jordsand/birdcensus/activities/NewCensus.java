package de.jordsand.birdcensus.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Spinner;

import de.jordsand.birdcensus.R;

public class NewCensus extends AppCompatActivity {
    private Spinner windDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_census);

        windDirection = (Spinner) findViewById(R.id.spin_wind_direction);
    }
}
