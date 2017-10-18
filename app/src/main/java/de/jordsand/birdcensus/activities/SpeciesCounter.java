package de.jordsand.birdcensus.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import de.jordsand.birdcensus.R;

import static android.view.KeyEvent.ACTION_DOWN;

/**
 * Displays a counter for the number of instances of a species seen.
 * @author Rico Bergmann
 */
public class SpeciesCounter extends AppCompatActivity {
    private static final int MIN_SPECIES_COUNT = 1;
    private static final int COUNTER_LARGE_STEP = 10;

    private EditText countView;
    private Counter counter;
    private Button increase;
    private Button increase10;
    private Button decrease;
    private Button decrease10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_counter);

        countView = (EditText) findViewById(R.id.counter);
        increase = (Button) findViewById(R.id.increase_counter);
        increase10 = (Button) findViewById(R.id.increase_counter_10);
        decrease = (Button) findViewById(R.id.decrease_counter);
        decrease10 = (Button) findViewById(R.id.decrease_counter_10);

        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter.increaseCounter();
            }
        });

        increase10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter.increaseCounterBy(COUNTER_LARGE_STEP);
            }
        });

        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter.decreaseCounter();
            }
        });

        decrease10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter.decreaseCounterBy(COUNTER_LARGE_STEP);
            }
        });

        counter = new Counter(countView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_species_counter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish_species_counter:
                if (!counter.isValid()) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                Intent resultData = new Intent();
                resultData.putExtra("count", counter.getCounter());
                setResult(RESULT_OK, resultData);
                finish();
                return true;
            case R.id.abort_species_counter:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (action != ACTION_DOWN) {
            return false;
        }
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                counter.increaseCounter();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                counter.decreaseCounter();
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    /**
     * The backing "model" for the counter
     */
    private class Counter {
        private int counter;
        private EditText view;

        /**
         * Constructor
         * @param view the edit text of the view
         */
        Counter(EditText view) {
            this.counter = 0;
            this.view = view;
            this.view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    updateCounter(editable.toString());
                }
            });
        }

        /**
         * @return the counter's value
         */
        int getCounter() {
            return counter;
        }

        /**
         * @return {@code true} if the counters state is valid
         */
        boolean isValid() {
            return counter >= MIN_SPECIES_COUNT;
        }

        /**
         * Increases the counter
         */
        void increaseCounter() {
            counter++;
            updateView(counter);
        }

        /**
         * Increases the counter
         * @param val the value to add
         */
        void increaseCounterBy(int val) {
            counter += val;
            updateView(counter);
        }

        /**
         * Decreases the counter
         */
        void decreaseCounter() {
            if (counter > MIN_SPECIES_COUNT) {
                counter--;
                updateView(counter);
            }
        }

        /**
         * Decreases the counter
         * @param val the value to subtract
         */
        void decreaseCounterBy(int val) {
            if (counter - val > MIN_SPECIES_COUNT) {
                counter -= val;
                updateView(counter);
            }
        }

        /**
         * Sets the counter to the given value
         * @param count the new value
         */
        private void updateCounter(String count) {
            try {
                counter = Integer.parseInt(count);
            } catch (NumberFormatException e) {
                counter = 0;
            }
        }

        /**
         * Synchronizes view and counter
         * @param count the count to display
         */
        private void updateView(int count) {
            view.setText(String.format(Locale.GERMANY, "%d", count));
        }
    }

}
