package de.jordsand.birdcensus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.BirdCountRepository;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.WatchList;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteBirdCountRepository;

/**
 * The details display for a past {@link BirdCount}
 * @author Rico Bergmann
 */
public class CensusDisplayDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_CENSUS_START_DATE = "census_start_date";
    private static final int APPROX_SPECIES_PER_AREA = 5;

    private ListView observationList;

    private BirdCountRepository birdCountRepo;
    private BirdCount birdCount;

    public CensusDisplayDetailsFragment() { ; }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CensusDisplayDetailsFragment newInstance(Date startDate) {
        CensusDisplayDetailsFragment fragment = new CensusDisplayDetailsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CENSUS_START_DATE, startDate.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context ctx = getActivity() == null ? getContext() : getActivity();
        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(ctx);
        birdCountRepo = new SQLiteBirdCountRepository(openHandler.getReadableDatabase());
        Date startDate = new Date(getArguments().getLong(ARG_CENSUS_START_DATE));
        birdCount = birdCountRepo.findByStartDate(startDate);

        View rootView = inflater.inflate(R.layout.fragment_census_display_details, container, false);

        observationList = rootView.findViewById(R.id.census_display_observations);
        observationList.setAdapter(new ObservationListAdapter(ctx, birdCount));

        return rootView;
    }

    private class ObservationListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private List<DetailListItem> list;

        ObservationListAdapter(Context ctx, BirdCount birdCount) {
            inflater = LayoutInflater.from(ctx);

            Map<Species, Integer> observationSummary = birdCount.getObservationSummary();
            Map<MonitoringArea, WatchList> observations = birdCount.getObservedSpecies();

            list = new ArrayList<>(observations.size() + APPROX_SPECIES_PER_AREA * observations.size());
            list.add(new SummarySectionItem());

            for (Species species : observationSummary.keySet()) {
                list.add(new SummaryItem(species, observationSummary.get(species)));
            }

            for (MonitoringArea area : observations.keySet()) {
                Map<Species, Integer> observedSpecies = observations.get(area).getObservedSpeciesMap();
                list.add(new AreaSectionItem(area));
                for (Species species : observedSpecies.keySet()) {
                    list.add(new StandardItem(species, observedSpecies.get(species)));
                }
            }

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DetailListItem item = (DetailListItem) getItem(position);

            switch (item.getItemType()) {
                case SUMMARY_SECTION:
                case AREA_SECTION:
                    inflateSection(item, convertView, parent);
                    break;
                case SUMMARY_ITEM:
                case STANDARD_ITEM:
                    inflateEntry(item, convertView, parent);
                    break;
            }

            return convertView;

        }

        /**
         * Inflates a section title within the list
         * @param item the title
         * @param convertView the view
         * @param parent the view group the given item resides in
         */
        private void inflateSection(DetailListItem item, View convertView, ViewGroup parent) {
            SectionViewHolder sectionHolder;
            if (convertView == null || !(convertView.getTag() instanceof SectionViewHolder)) {
                convertView = inflater.inflate(R.layout.species_list_monitoring_area, parent, false);
                sectionHolder = new SectionViewHolder();
                sectionHolder.title = (TextView) convertView.findViewById(R.id.observation_area);
                convertView.setTag(sectionHolder);
            } else {
                sectionHolder = (SectionViewHolder) convertView.getTag();
            }

            if (item.getItemType() == DetailListItem.ItemType.AREA_SECTION) {
                MonitoringArea area = (MonitoringArea) item.getData();
                sectionHolder.title.setText(area.getName());
            } else {
                sectionHolder.title.setText(R.string.census_overview);
            }
        }

        /**
         * Inflates an entry within the list
         * @param item the entry
         * @param convertView the view
         * @param parent the view group the given item resides in
         */
        private void inflateEntry(DetailListItem item, View convertView, ViewGroup parent) {
            SpeciesViewHolder speciesHolder;
            if (convertView == null || !(convertView.getTag() instanceof SpeciesViewHolder)) {
                convertView = inflater.inflate(R.layout.observation_list, parent, false);
                speciesHolder = new SpeciesViewHolder();
                speciesHolder.name = convertView.findViewById(R.id.observation_name);
                speciesHolder.scientific = convertView.findViewById(R.id.observation_scientific);
                speciesHolder.count = convertView.findViewById(R.id.observation_count);
                convertView.setTag(convertView);
            } else {
                speciesHolder = (SpeciesViewHolder) convertView.getTag();
            }
            Pair<Species, Integer> observation = (Pair<Species, Integer>) item.getData();

            speciesHolder.name.setText(observation.first.getName());
            speciesHolder.count.setText(String.format(Locale.GERMANY, "%d", observation.second));

            if (observation.first.hasScientificName()) {
                speciesHolder.scientific.setVisibility(View.VISIBLE);
                speciesHolder.scientific.setText(observation.first.getScientificName());
            } else {
                speciesHolder.scientific.setVisibility(View.GONE);
            }
        }

        class SectionViewHolder {
            TextView title;
        }

        class SpeciesViewHolder {
            TextView name, scientific, count;
        }
    }

    /**
     * Base class for all items that may be used in the list view
     */
    private static abstract class DetailListItem {
        enum ItemType {
            SUMMARY_SECTION,
            SUMMARY_ITEM,
            AREA_SECTION,
            STANDARD_ITEM
        }

        private ItemType itemType;

        DetailListItem(ItemType itemType) {
            this.itemType = itemType;
        }

        ItemType getItemType() {
            return itemType;
        }

        public abstract Object getData();
    }

    /**
     * The summary title
     */
    private class SummarySectionItem extends DetailListItem {

        SummarySectionItem() {
            super(ItemType.SUMMARY_SECTION);
        }

        @Override
        public Object getData() {
            return ItemType.SUMMARY_SECTION;
        }
    }

    /**
     * A normal summary entry
     */
    private class SummaryItem extends DetailListItem {
        private Pair<Species, Integer> item;

        SummaryItem(Species species, int count) {
            super(ItemType.SUMMARY_ITEM);
            item = new Pair<>(species, count);
        }

        @Override
        public Object getData() {
            return item;
        }
    }

    /**
     * A monitoring area title
     */
    private class AreaSectionItem extends DetailListItem {
        private MonitoringArea area;

        AreaSectionItem(MonitoringArea area) {
            super(ItemType.AREA_SECTION);
            this.area = area;
        }

        @Override
        public Object getData() {
            return area;
        }
    }

    /**
     * A normal observation entry for a monitoring area
     */
    private class StandardItem extends DetailListItem {
        private Pair<Species, Integer> item;

        StandardItem(Species species, int count) {
            super(ItemType.STANDARD_ITEM);
            item = new Pair<>(species, count);
        }

        @Override
        public Object getData() {
            return item;
        }
    }
}
