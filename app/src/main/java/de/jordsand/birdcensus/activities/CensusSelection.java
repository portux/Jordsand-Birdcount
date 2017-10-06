package de.jordsand.birdcensus.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteBirdCountRepository;

/**
 * Displaying a list of all past bird counts
 * @author Rico Bergmann
 */
public class CensusSelection extends AppCompatActivity {
    private Resources res;
    private ListView list;
    private CensusAdapter adapter;
    private EditText search;
    private SQLiteBirdCountRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_census_selection);

        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        repo = new SQLiteBirdCountRepository(openHandler.getReadableDatabase());

        res = getResources();
        list = (ListView) findViewById(R.id.census_selection);
        adapter = new CensusAdapter(this, repo.findAll());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new CensusSelectionOnClickListener());

        search = (EditText) findViewById(R.id.search_date);
        search.addTextChangedListener(new SearchTextWatcher());
    }

    /**
     * Adapter for displaying a census
     */
    private class CensusAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater inflater;

        private List<Pair<String, BirdCount>> availableBirdCounts;
        private List<BirdCount> adaptedBirdCounts;

        CensusAdapter(Context ctx, Iterable<BirdCount> birdCounts) {
            inflater = LayoutInflater.from(ctx);
            this.availableBirdCounts = new LinkedList<>();
            this.adaptedBirdCounts = new ArrayList<>();
            for (BirdCount bc : birdCounts) {
                this.availableBirdCounts.add(new Pair<>(formatDate(bc.getStartTime()), bc));
                this.adaptedBirdCounts.add(bc);
            }
        }

        @Override
        public int getCount() {
            return adaptedBirdCounts.size();
        }

        @Override
        public Object getItem(int i) {
            return adaptedBirdCounts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.census_list, parent, false);
                holder = new ViewHolder();
                holder.startDate = convertView.findViewById(R.id.census_start_date);
                holder.observer = convertView.findViewById(R.id.census_observer);
                holder.totalSpecies = convertView.findViewById(R.id.census_total_species);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BirdCount birdCount = (BirdCount) getItem(position);
            holder.startDate.setText(formatDate(birdCount.getStartTime()));
            holder.observer.setText(birdCount.getObserverName());
            holder.totalSpecies.setText(res.getString(R.string.total_species_count, birdCount.getDifferentSpeciesCount(), birdCount.getTotalObservedSpeciesCount()));
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();
                    adaptedBirdCounts.clear();

                    for (Pair<String, BirdCount> bcp : availableBirdCounts) {
                        if (bcp.first.contains(charSequence)) {
                            adaptedBirdCounts.add(bcp.second);
                        }
                    }

                    results.count = adaptedBirdCounts.size();
                    results.values = adaptedBirdCounts;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    notifyDataSetChanged();
                }
            };
        }

        String formatDate(Date date) {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
            return format.format(date);
        }

        class ViewHolder {
            TextView startDate, observer, totalSpecies;
        }
    }

    /**
     * Handler for click events on a specific census
     */
    private class CensusSelectionOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent censusDisplay = new Intent(CensusSelection.this, CensusDisplay.class);
            censusDisplay.putExtra("census_start_date", ((BirdCount)adapter.getItem(position)).getStartTime().getTime());
            startActivity(censusDisplay);
        }
    }

    /**
     * Handler for the date search field
     */
    private class SearchTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            adapter.getFilter().filter(editable.toString());
        }
    }

}
