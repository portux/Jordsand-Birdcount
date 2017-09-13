package de.jordsand.birdcensus.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CalendarView;

import de.jordsand.birdcensus.R;

public class CensusSelection extends AppCompatActivity {
    private CalendarView calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_census_selection);

        calendar = (CalendarView) findViewById(R.id.calendar);


    }

}
