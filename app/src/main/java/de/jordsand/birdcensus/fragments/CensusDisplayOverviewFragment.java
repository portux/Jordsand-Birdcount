package de.jordsand.birdcensus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.BirdCountRepository;
import de.jordsand.birdcensus.core.WeatherData;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteBirdCountRepository;
import de.jordsand.birdcensus.util.WeatherToStringMapper;

/**
 * The overview display for a past {@link BirdCount}
 */
public class CensusDisplayOverviewFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_CENSUS_START_DATE = "census_start_date";

    private TextView censusDuration;
    private TextView censusObserver;
    private TextView weatherWaterGauge;
    private TextView weatherWind;
    private TextView weatherPrecipitation;
    private TextView weatherVisibility;
    private TextView weatherGlaciationLevel;
    private TextView differentSpeciesCount;
    private TextView totalObservationsCount;

    private BirdCountRepository birdCountRepo;
    private BirdCount birdCount;

    public CensusDisplayOverviewFragment() {}

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CensusDisplayOverviewFragment newInstance(Date startDate) {
        CensusDisplayOverviewFragment fragment = new CensusDisplayOverviewFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CENSUS_START_DATE, startDate.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context ctx = getActivity() == null ? getContext() : getActivity();
        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(ctx);
        birdCountRepo = new SQLiteBirdCountRepository(openHandler.getReadableDatabase());
        Date startDate = new Date(getArguments().getLong(ARG_CENSUS_START_DATE));
        birdCount = birdCountRepo.findByStartDate(startDate);

        View rootView = inflater.inflate(R.layout.fragment_census_display_overview, container, false);

        connectViews(rootView);
        setUpViewData();

        return rootView;
    }

    /**
     * Connects the text views to their corresponding XML elements
     * @param root the view containing the text fields
     */
    private void connectViews(View root) {
        censusDuration = root.findViewById(R.id.census_display_duration);
        censusObserver = root.findViewById(R.id.census_display_observer);
        weatherWaterGauge = root.findViewById(R.id.census_display_water_gauge);
        weatherWind = root.findViewById(R.id.census_display_wind);
        weatherPrecipitation = root.findViewById(R.id.census_display_precipitation);
        weatherVisibility = root.findViewById(R.id.census_display_visibility);
        weatherGlaciationLevel = root.findViewById(R.id.census_display_glaciation_lvl);
        differentSpeciesCount = root.findViewById(R.id.census_display_different_species);
        totalObservationsCount = root.findViewById(R.id.census_display_total_sightings);
    }

    /**
     * Initializes the text views
     */
    private void setUpViewData() {
        censusDuration.setText(renderDuration());
        censusObserver.setText(birdCount.getObserverName());
        renderWeatherData(birdCount.getWeatherInfo());
        differentSpeciesCount.setText(String.format(Locale.GERMANY, "%d", birdCount.getDifferentSpeciesCount()));
        totalObservationsCount.setText(String.format(Locale.GERMANY, "%d", birdCount.getTotalObservedSpeciesCount()));
    }

    /**
     * Initializes the weather text views
     * @param weatherData the data to use
     */
    private void renderWeatherData(WeatherData weatherData) {
        WeatherToStringMapper mapper = new WeatherToStringMapper(getResources());

        if (weatherData.getWaterGauge() != null) {
            weatherWaterGauge.setText(String.format(Locale.GERMANY, "%.2f", weatherData.getWaterGauge()));
        } else {
            weatherWaterGauge.setText(R.string.field_unset);
        }

        String windStrength;
        String windDirection;
        if (weatherData.getWindStrength() != null) {
            windStrength = String.format(Locale.GERMANY, "%d", weatherData.getWindStrength());
        } else {
            windStrength = "";
        }
        if (weatherData.getWindDirection() != null) {
            windDirection = mapper.convertWindDirection(weatherData.getWindDirection());
        } else {
            windDirection = "";
        }
        if (windStrength.equals("") && windDirection.equals("")) {
            weatherWind.setText(R.string.field_unset);
        } else {
            weatherWind.setText(getString(R.string.census_overview_wind, windStrength, windDirection));
        }

        if (weatherData.getPrecipitation() != null) {
            weatherPrecipitation.setText(mapper.convertPrecipitation(weatherData.getPrecipitation()));
        } else {
            weatherPrecipitation.setText(R.string.field_unset);
        }

        if (weatherData.getVisibility() != null) {
            weatherVisibility.setText(mapper.convertVisibility(weatherData.getVisibility()));
        } else {
            weatherVisibility.setText(R.string.field_unset);
        }

        if (weatherData.getGlaciationLevel() != null) {
            weatherGlaciationLevel.setText(mapper.convertGlaciationLevel(weatherData.getGlaciationLevel()));
        } else {
            weatherGlaciationLevel.setText(R.string.field_unset);
        }
    }

    /**
     * Pretty prints a bird counts duration
     * @return the pretty printed string
     */
    private String renderDuration() {
        Date start = birdCount.getStartTime();
        Date end = birdCount.getEndTime();
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        SimpleDateFormat completeFormat = new SimpleDateFormat("dd.MM.yyy HH:mm", Locale.GERMANY);
        if (dateOnlyFormat.format(start).equals(dateOnlyFormat.format(end))) {
            return getString(R.string.census_overview_duration, completeFormat.format(start), timeOnlyFormat.format(end));
        } else {
            return getString(R.string.census_overview_duration, completeFormat.format(start), completeFormat.format(end));
        }
    }
}
