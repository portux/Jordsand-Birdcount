package de.jordsand.birdcensus.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.ExistingSpeciesException;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.services.BirdCountService;
import de.jordsand.birdcensus.services.SimpleBirdCountService;

/**
 * Adds a new species to the database
 * @author Rico Bergmann
 */
public class NewSpecies extends AppCompatActivity {
    private BirdCountService birdCountService;
    private boolean mBound;

    private EditText name;
    private EditText scientific;
    private EditText count;

    private String areaCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_species);

        name = (EditText) findViewById(R.id.name);
        scientific = (EditText) findViewById(R.id.scientific);
        count = (EditText) findViewById(R.id.count);

        areaCode = getIntent().getStringExtra("area_code");
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SimpleBirdCountService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_species, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_species:
                if (saveSpecies()) {
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Persists the species according to the data in the fields
     * @return whether the species was saved successfully
     */
    private boolean saveSpecies() {
        String newName = firstLetterToUppercase(name.getText().toString());
        String newScientificName = firstLetterToUppercase(scientific.getText().toString());
        Species species = null;
        try {
            species = birdCountService.addNewSpecies(newName, newScientificName);
        } catch (ExistingSpeciesException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.existing_species)
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    })
                    .setTitle(R.string.error)
                    .setIcon(R.drawable.ic_warning)
                    .setCancelable(false);
            builder.show();
            return false;
        }

        int selectedCount;
        try {
            selectedCount = Integer.parseInt(count.getText().toString());
        } catch (NumberFormatException e) {
            selectedCount = 1;
        }
        birdCountService.addSightingToCurrentBirdCount(areaCode, species, selectedCount);
        return true;
    }

    /**
     * Converts the first letter of a string to uppercase
     * @param str the string to convert
     * @return the converted string
     */
    private String firstLetterToUppercase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstLetter = Character.toUpperCase(str.charAt(0));
        return firstLetter + str.substring(1);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}
