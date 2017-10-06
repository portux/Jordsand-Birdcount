package de.jordsand.birdcensus.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteSpeciesRepository;
import de.jordsand.birdcensus.fragments.SelectSpeciesCount;
import de.jordsand.birdcensus.services.SimpleBirdCountService;

/**
 * Shows a list of all species available, enabling the user to add new sightings
 * @author Rico Bergmann
 */
public class AddSighting extends AppCompatActivity implements SelectSpeciesCount.OnSpeciesCountSelectedListener {
    private static final String SELECT_COUNT_TAG = "SpeciesCountFragment";
    private static final int RQ_SPECIES_COUNT = 777;
    private static final int RQ_NEW_SPECIES = 888;

    private SimpleBirdCountService birdCountService;
    private boolean mBound;

    private ListView list;
    private SpeciesAdapter adapter;
    private Button newSpecies;
    private EditText search;
    private SQLiteSpeciesRepository repo;

    private String monitoringArea;
    private Species selectedSpecies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sighting);

        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        repo = new SQLiteSpeciesRepository(openHandler.getReadableDatabase());

        newSpecies = (Button) findViewById(R.id.new_species);
        newSpecies.setOnClickListener(new NewSpeciesOnClickListener());

        list = (ListView) findViewById(R.id.species);
        adapter = new SpeciesAdapter(this, repo.findAll());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new SpeciesListOnClickListener());

        search = (EditText) findViewById(R.id.search_species);
        search.addTextChangedListener(new SearchTextWatcher());

        monitoringArea = getIntent().getStringExtra("area");
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
        inflater.inflate(R.menu.menu_add_sighting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_sighting_terminate_census:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.terminate_bird_count)
                        .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                birdCountService.terminateBirdCount();
                                Intent intent = new Intent();
                                intent.putExtra("terminated", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setTitle(R.string.save)
                        .setIcon(R.drawable.ic_info);
                builder.show();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSpeciesCountSelected(int count) {
        birdCountService.addSightingToCurrentBirdCount(monitoringArea, selectedSpecies, count);
        selectedSpecies = null;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_NEW_SPECIES && resultCode == RESULT_OK) {
            Toast.makeText(this, "Species added", Toast.LENGTH_SHORT).show();
            adapter.setData(repo.findAll());
            adapter.notifyDataSetChanged();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SimpleBirdCountService.BirdCountBinder binder = (SimpleBirdCountService.BirdCountBinder) service;
            birdCountService = binder.getService();
            mBound = true;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    /**
     * Adapter for the list of available species
     */
    private class SpeciesAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater inflater;

        private List<Species> availableSpecies;
        private List<Species> adaptedList;

        SpeciesAdapter(Context ctx, Iterable<Species> species) {
            inflater = LayoutInflater.from(ctx);
            setData(species);
        }

        void setData(Iterable<Species> species) {
            this.availableSpecies = new LinkedList<>();
            for (Species s : species) {
                this.availableSpecies.add(s);
            }
            this.adaptedList = new ArrayList<>(this.availableSpecies);
        }

        @Override
        public int getCount() {
            return adaptedList.size();
        }

        @Override
        public Object getItem(int i) {
            return adaptedList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.species_list, parent, false);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.species_name);
                holder.scientific = convertView.findViewById(R.id.species_scientific_name);
                holder.currentCount = convertView.findViewById(R.id.species_observed_count);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Species species = (Species) getItem(position);
            holder.name.setText(species.getName());
            if (species.hasScientificName()) {
                holder.scientific.setVisibility(View.VISIBLE);
                holder.scientific.setText(species.getScientificName());
            } else {
                holder.scientific.setVisibility(View.GONE);
            }
            holder.currentCount.setVisibility(View.VISIBLE);
            if (mBound) {
                if (birdCountService.getCurrentBirdCount().wasObservedIn(species, monitoringArea)) {
                    holder.currentCount.setText(String.format(Locale.GERMANY, "%d", birdCountService.getCurrentBirdCount().getObservedCountOf(species, monitoringArea)));
                } else {
                    holder.currentCount.setVisibility(View.GONE);
                }
            } else {
                holder.currentCount.setText(R.string.unknown);
            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    adaptedList.clear();

                    CharSequence firstLetterUppercaseConstraint;
                    if (constraint.length() > 0) {
                        char firstLetter = Character.toUpperCase(constraint.charAt(0));
                        firstLetterUppercaseConstraint = firstLetter + constraint.toString().substring(1);
                    } else {
                        firstLetterUppercaseConstraint = constraint;
                    }

                    for (Species species : availableSpecies) {
                        if (species.getName().contains(constraint) || species.getName().contains(firstLetterUppercaseConstraint)) {
                            adaptedList.add(species);
                        } else if (species.hasScientificName()) {
                            if (species.getScientificName().contains(constraint) || species.getScientificName().contains(firstLetterUppercaseConstraint)) {
                                adaptedList.add(species);
                            }
                        }
                    }

                    results.count = adaptedList.size();
                    results.values = adaptedList;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }

        class ViewHolder {
            TextView name, scientific, currentCount;
        }
    }

    /**
     * Handler for the click on a species
     */
    private class SpeciesListOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectedSpecies = (Species) adapter.getItem(position);
            DialogFragment dialog = new SelectSpeciesCount();
            dialog.setTargetFragment(null, RQ_SPECIES_COUNT);
            dialog.show(getSupportFragmentManager(), SELECT_COUNT_TAG);
        }
    }

    /**
     * Handler for the click on the 'New species' button
     */
    private class NewSpeciesOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent createSpecies = new Intent(AddSighting.this, NewSpecies.class);
            createSpecies.putExtra("area_code", monitoringArea);
            startActivityForResult(createSpecies, RQ_NEW_SPECIES);
        }
    }

    /**
     * Handler for the species search field
     */
    private class SearchTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence newText, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            adapter.getFilter().filter(editable.toString());
        }
    }
}
