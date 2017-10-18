package de.jordsand.birdcensus.database.repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.BirdCountRepository;
import de.jordsand.birdcensus.core.Location;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.WatchList;
import de.jordsand.birdcensus.core.WeatherData;
import de.jordsand.birdcensus.core.WeatherData.*;
import de.jordsand.birdcensus.database.BirdCountContract;
import de.jordsand.birdcensus.util.DateConverter;

/**
 * Repository to read and write {@link BirdCount} instances from and to a SQLite database
 * @author Rico Bergmann
 */
public class SQLiteBirdCountRepository implements BirdCountRepository {
    private static final int APPROX_SPECIES_PER_AREA = 4;

    private SQLiteDatabase db;
    private QueryAssistant queryAssistant;
    private DateConverter dateConverter;

    public SQLiteBirdCountRepository(SQLiteDatabase db) {
        this.db = db;
        this.queryAssistant = new QueryAssistant();
        this.dateConverter = new DateConverter();
    }

    @Override
    public Long save(BirdCount instance) {
        BirdCountToSQLiteConverter converter = new BirdCountToSQLiteConverter();
        long id = db.insert(BirdCountContract.BirdCount.TABLE_NAME, null, converter.extractBirdCountTableData(instance));
        for (ContentValues values : converter.extractObservationTableData(instance)) {
            db.insert(BirdCountContract.ObservedSpecies.TABLE_NAME, null, values);
        }
        return id;
    }

    @Nullable
    @Override
    public BirdCount findByStartDate(@NonNull Date startDate) {
        SQLiteToBirdCountConverter converter = new SQLiteToBirdCountConverter();
        return converter.loadBirdCountFrom(startDate);
    }

    @Override
    public boolean exists(Long censusId) {
        String[] projection = {
                BirdCountContract.BirdCount._ID
        };
        String selection = BirdCountContract.BirdCount._ID + " = ?";
        String[] selectionArgs = { censusId.toString() };

        Cursor cursor = db.query(
                BirdCountContract.BirdCount.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public boolean exists(@NonNull Date startDate) {
        long id = queryAssistant.fetchCensusId(startDate);
        return id != -1L;
    }

    @NonNull
    @Override
    public Iterable<BirdCount> findByObserver(@NonNull String observerName) {
        String[] projection = {
                BirdCountContract.BirdCount.COLUMN_NAME_START_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_END_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION,
                BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION,
                BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY,
                BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL,
                BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER
        };
        String selection = BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER + " CONTAINS ?";
        String[] selectionArgs = { observerName };

        Cursor matchingBirdCounts = db.query(
                BirdCountContract.BirdCount.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        final int CENSUS_ID_IDX = matchingBirdCounts.getColumnIndexOrThrow(BirdCountContract.BirdCount._ID);
        List<BirdCount> birdCounts = new ArrayList<>(matchingBirdCounts.getCount());
        SQLiteToBirdCountConverter converter = new SQLiteToBirdCountConverter();

        while (matchingBirdCounts.moveToNext()) {
            long censusID = matchingBirdCounts.getLong(CENSUS_ID_IDX);
            Cursor observationData = converter.loadObservationDataFor(censusID);
            birdCounts.add(converter.buildBirdCount(matchingBirdCounts, observationData));
            observationData.close();
        }

        matchingBirdCounts.close();

        return birdCounts;
    }

    @Override
    public BirdCount findOne(Long censusId) {
        String[] projection = {
                BirdCountContract.BirdCount._ID,
                BirdCountContract.BirdCount.COLUMN_NAME_START_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_END_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION,
                BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION,
                BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY,
                BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL,
                BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER
        };
        String selection = BirdCountContract.BirdCount._ID + " = ?";
        String[] selectionArgs = { censusId.toString() };

        Cursor birdCountData = db.query(
                BirdCountContract.BirdCount.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (!birdCountData.moveToFirst()) {
            return null;
        }

        SQLiteToBirdCountConverter converter = new SQLiteToBirdCountConverter();
        Cursor observationData = converter.loadObservationDataFor(censusId);

        BirdCount birdCount = converter.buildBirdCount(birdCountData, observationData);
        birdCountData.close();
        return birdCount;
    }

    @Override
    public Iterable<BirdCount> findAll() {
        String[] projection = {
                BirdCountContract.BirdCount._ID,
                BirdCountContract.BirdCount.COLUMN_NAME_START_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_END_TIME,
                BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH,
                BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION,
                BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION,
                BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY,
                BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL,
                BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER
        };

        Cursor rawBirdCounts = db.query(
                BirdCountContract.BirdCount.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        final int CENSUS_ID_IDX = rawBirdCounts.getColumnIndexOrThrow(BirdCountContract.BirdCount._ID);
        int resultSize = rawBirdCounts.getCount();
        List<BirdCount> birdCounts = new ArrayList<>(resultSize);
        SQLiteToBirdCountConverter converter = new SQLiteToBirdCountConverter();

        while (rawBirdCounts.moveToNext()) {
            long censusID = rawBirdCounts.getLong(CENSUS_ID_IDX);
            Cursor observationData = converter.loadObservationDataFor(censusID);
            birdCounts.add(converter.buildBirdCount(rawBirdCounts, observationData));
            observationData.close();
        }
        rawBirdCounts.close();

        return birdCounts;
    }

    @Override
    public boolean remove(Long censusId) {
        throw new UnsupportedOperationException("Existing bird counts may not be removed!");
    }

    /**
     * Helper-class for quickly accessing certain fields of the database
     */
    private class QueryAssistant {

        /**
         * Queries for the ID of a certain species
         * @param species the species to look up
         * @return the associated primary key or {@code -1} if non exists
         */
        long fetchSpeciesId(Species species) {
            return species.hasScientificName()
                    ? fetchSpeciesIdByScientificName(species)
                    : fetchSpeciesIdByName(species);
        }

        /**
         * Queries for the ID of a certain bird count
         * @param census the bird count to look up
         * @return the associated primary key or {@code -1 } if non exists
         */
        long fetchCensusId(BirdCount census) {
            return fetchCensusId(census.getStartTime());
        }

        /**
         * Queries for the ID of a certain bird count
         * @param startTime the bird count's start date
         * @return the associated primary key or {@code -1 } if non exists
         */
        long fetchCensusId(Date startTime) {
            String[] projection = {
                    BirdCountContract.BirdCount._ID
            };
            String selection = BirdCountContract.BirdCount.COLUMN_NAME_START_TIME + " = ?";
            String[] selectionArgs = { dateConverter.formatDate(startTime) };

            Cursor cursor = db.query(
                    BirdCountContract.BirdCount.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (!cursor.moveToFirst()) {
                return -1L;
            }
            long censusId = cursor.getLong(cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount._ID));
            cursor.close();
            return censusId;
        }

        /**
         * Performs the ID-lookup for species with scientific name
         * @param species the species to query for
         * @return the associated primary key or {@code -1} if non exists
         */
        private long fetchSpeciesIdByScientificName(Species species) {
            String[] projection = {
                    BirdCountContract.Species._ID
            };
            String selection = BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME + " = ?";
            String[] selectionArgs = { species.getScientificName() };

            Cursor cursor = db.query(
                    BirdCountContract.Species.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            if (!cursor.moveToFirst()) {
                return -1L;
            }
            long speciesId = cursor.getLong(cursor.getColumnIndexOrThrow(BirdCountContract.Species._ID));
            cursor.close();
            return speciesId;
        }

        /**
         * Performs the ID-lookup for species without scientific name
         * @param species the species to query for
         * @return the associated primary key or {@code -1} if non exists
         */
        private long fetchSpeciesIdByName(Species species) {
            String[] projection = {
                    BirdCountContract.Species._ID,
                    BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME
            };
            String selection = BirdCountContract.Species.COLUMN_NAME_NAME + " = ?";
            String[] selectionArgs = { species.getName() };

            Cursor cursor = db.query(
                    BirdCountContract.Species.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            final int SCIENTIFIC_NAME_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME);

            while (cursor.moveToNext()){
                boolean scientificNameUnset = cursor.isNull(SCIENTIFIC_NAME_IDX) || cursor.getString(SCIENTIFIC_NAME_IDX).isEmpty();
                if (scientificNameUnset) {
                    long speciesId = cursor.getLong(cursor.getColumnIndexOrThrow(BirdCountContract.Species._ID));
                    cursor.close();
                    return speciesId;
                }
            }
            cursor.close();
            return -1L;
        }

    }

    /**
     * Helper-class to prepare a {@link BirdCount} instance for being persisted
     */
    private class BirdCountToSQLiteConverter {

        /**
         * Prepares the data for the BirdCount-table itself
         * @param birdCount the bird count to prepare
         * @return the representation of the bird count
         * @see de.jordsand.birdcensus.database.BirdCountContract.BirdCount
         */
        ContentValues extractBirdCountTableData(BirdCount birdCount) {
            if (!birdCount.isTerminated()) {
                throw new IllegalStateException("Bird count is not yet terminated");
            }
            ContentValues values = new ContentValues();
            String start = dateConverter.formatDate(birdCount.getStartTime());
            values.put(BirdCountContract.BirdCount.COLUMN_NAME_START_TIME, start);
            String end = dateConverter.formatDate(birdCount.getEndTime());
            values.put(BirdCountContract.BirdCount.COLUMN_NAME_END_TIME, end);
            WeatherData weather = birdCount.getWeatherInfo();
            values.put(BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE, weather.getWaterGauge());
            values.put(BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH, weather.getWindStrength());
            if (weather.getWindDirection() != null) values.put(BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION, weather.getWindDirection().ordinal());
            if (weather.getPrecipitation() != null) values.put(BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION, weather.getPrecipitation().ordinal());
            if (weather.getVisibility() != null) values.put(BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY, weather.getVisibility().ordinal());
            if (weather.getGlaciationLevel() != null) values.put(BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL, weather.getGlaciationLevel().ordinal());
            values.put(BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER, birdCount.getObserverName());
            return values;
        }

        /**
         * Prepares the data for the Observation-table
         * @param birdCount the bird count to prepare
         * @return the representation of the observations
         * @see de.jordsand.birdcensus.database.BirdCountContract.ObservedSpecies
         */
        Iterable<ContentValues> extractObservationTableData(BirdCount birdCount) {
            if (!birdCount.isTerminated()) {
                throw new IllegalStateException("Bird count is not yet terminated");
            }
            Set<MonitoringArea> areas = birdCount.getObservedSpecies().keySet();
            List<ContentValues> values = new ArrayList<>(areas.size() * areas.size());
            for (MonitoringArea area : areas) {
                WatchList watchList = birdCount.getObservedSpecies().get(area);
                for (Pair<Species, Integer> entry : watchList) {
                    ContentValues valuesToAdd = new ContentValues();
                    valuesToAdd.put(BirdCountContract.ObservedSpecies.COLUMN_NAME_AREA, area.getCode());
                    long speciesId = fetchSpeciesIdFor(entry);
                    valuesToAdd.put(BirdCountContract.ObservedSpecies.COLUMN_NAME_SPECIES, speciesId);
                    long censusId = fetchBirdCountIdFor(birdCount);
                    valuesToAdd.put(BirdCountContract.ObservedSpecies.COLUMN_NAME_CENSUS, censusId);
                    valuesToAdd.put(BirdCountContract.ObservedSpecies.COLUMN_NAME_COUNT, entry.second);
                    values.add(valuesToAdd);
                }
            }
            return values;
        }

        /**
         * Wrapper-method for {@link QueryAssistant#fetchSpeciesId(Species)}
         * @throws SpeciesNotPersistedException if the species is not part of the database yet
         */
        long fetchSpeciesIdFor(Pair<Species, Integer> entry) {
            long speciesId = queryAssistant.fetchSpeciesId(entry.first);
            if (speciesId < 0L) {
                throw new SpeciesNotPersistedException("For species: " + entry.first);
            }
            return speciesId;
        }

        /**
         * Wrapper-method for {@link QueryAssistant#fetchCensusId(BirdCount)}
         * @throws BirdCountNotPersistedException if the census is not part of the database yet
         */
        long fetchBirdCountIdFor(BirdCount birdCount) {
            long censusId = queryAssistant.fetchCensusId(birdCount);
            if (censusId < 0L) {
                throw new BirdCountNotPersistedException("For bird count: " + birdCount);
            }
            return censusId;
        }

    }

    /**
     * Helper-class to restore a {@link BirdCount} instance from the database.
     * Due to the separated table-layout this involves a bit of work:
     * <ul>
     *     <li>Firstly, all required data about the bird count and all sightings will be fetched</li>
     *     <li>Second, the required {@link MonitoringArea}s and {@link WatchList}s will be re-initiated</li>
     *     <li>Third, the watchlists will be associated with their corresponding areas</li>
     *     <li>And finally all the pieces will be put together</li>
     * </ul>
     */
    private class SQLiteToBirdCountConverter {

        /**
         * Performs the conversion. This mainly involves delegating to other methods
         * @param startTime the start time of the bird count to convert
         * @return the restored bird count object
         */
        BirdCount loadBirdCountFrom(Date startTime) {
            Cursor birdCountData = loadBirdCountDataFrom(startTime);
            Cursor observationData = loadObservationDataFrom(startTime);

            return buildBirdCount(birdCountData, observationData);
        }

        /**
         * Connects the database-rows for the bird count with those for the observations
         * @param birdCountData the cursor for the bird count data
         * @param observationData the cursor for the observation data
         * @return the new bird count
         */
        BirdCount buildBirdCount(Cursor birdCountData, Cursor observationData) {
            final int START_IDX = birdCountData.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_START_TIME);
            final int END_IDX = birdCountData.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_END_TIME);
            final int OBSERVER_IDX = birdCountData.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER);

            // repositioning if necessary
            if (birdCountData.getPosition() < 0) {
                birdCountData.moveToFirst();
            }

            Date startTime = dateConverter.retrieveDate(birdCountData.getString(START_IDX));
            Date endTime = dateConverter.retrieveDate(birdCountData.getString(END_IDX));
            WeatherData weatherData = fetchWeatherDataFrom(birdCountData);
            String observer = birdCountData.getString(OBSERVER_IDX);
            Map<MonitoringArea, WatchList> observedSpecies = rebuildObservedSpecies(observationData);

            return new BirdCount(startTime, endTime, observer, weatherData, observedSpecies);
        }

        /**
         * Loads the direct fields of the bird count (start- and end-time, weather-info and observer)
         * @param startTime the start time of the bird count to load
         * @return a cursor for the result-set
         */
        Cursor loadBirdCountDataFrom(Date startTime) {
            String[] projection = {
                    BirdCountContract.BirdCount.COLUMN_NAME_START_TIME,
                    BirdCountContract.BirdCount.COLUMN_NAME_END_TIME,
                    BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE,
                    BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH,
                    BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION,
                    BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION,
                    BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY,
                    BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL,
                    BirdCountContract.BirdCount.COLUMN_NAME_OBSERVER
            };
            String selection = BirdCountContract.BirdCount.COLUMN_NAME_START_TIME + " = ?";
            String[] selectionArgs = { dateConverter.formatDate(startTime) };

            return db.query(
                    BirdCountContract.BirdCount.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
        }

        /**
         * Extracts the weather-data
         * @param cursor the result-set to use
         * @return the data
         */
        WeatherData fetchWeatherDataFrom(Cursor cursor) {
            if (cursor.getPosition() < 0 && !cursor.moveToFirst()) {
                return null;
            }

            final int WATER_GAUGE_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_WATER_GAUGE);
            final int WIND_STRENGTH_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_WIND_STRENGTH);
            final int WIND_DIRECTION_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_WIND_DIRECTION);
            final int PRECIPITATION_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_PRECIPITATION);
            final int VISIBILITY_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_VISIBILITY);
            final int GLACIATION_IDX = cursor.getColumnIndexOrThrow(BirdCountContract.BirdCount.COLUMN_NAME_GLACIATION_LEVEL);

            double waterGauge = cursor.getDouble(WATER_GAUGE_IDX);
            int windStrength = cursor.getInt(WIND_STRENGTH_IDX);
            WindDirection windDirection = WindDirection.values()[cursor.getInt(WIND_DIRECTION_IDX)];
            Precipitation precipitation = Precipitation.values()[cursor.getInt(PRECIPITATION_IDX)];
            Visibility visibility = Visibility.values()[cursor.getInt(VISIBILITY_IDX)];
            GlaciationLevel glaciationLevel = GlaciationLevel.values()[cursor.getInt(GLACIATION_IDX)];

            return new WeatherData(waterGauge, windStrength, windDirection, precipitation, visibility, glaciationLevel);
        }

        /**
         * Loads all sightings of a specific bird count
         * @param startTime the start time of the bird count to fetch
         * @return a cursor for the result-set
         */
        Cursor loadObservationDataFrom(Date startTime) {
            long censusId = queryAssistant.fetchCensusId(startTime);
            return loadObservationDataFor(censusId);
        }

        Cursor loadObservationDataFor(Long censusID) {
            String[] projection = {
                    BirdCountContract.ObservedSpecies.COLUMN_NAME_AREA,
                    BirdCountContract.ObservedSpecies.COLUMN_NAME_SPECIES,
                    BirdCountContract.ObservedSpecies.COLUMN_NAME_CENSUS,
                    BirdCountContract.ObservedSpecies.COLUMN_NAME_COUNT
            };
            String selection = BirdCountContract.ObservedSpecies.COLUMN_NAME_CENSUS + " = ?";
            String[] selectionArgs = { censusID.toString() };
            return db.query(
                    BirdCountContract.ObservedSpecies.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
        }

        /**
         * Constructs the necessary {@link MonitoringArea}s and {@link WatchList}s, as well as
         * their connections
         * @param resultSet the raw data to use
         * @return the associated objects
         */
        Map<MonitoringArea, WatchList> rebuildObservedSpecies(Cursor resultSet) {
            Map<MonitoringArea, WatchList> mapping = new HashMap<>(resultSet.getCount());
            Map<String, MonitoringArea> areas = fetchRelevantMonitoringAreas(resultSet);
            Map<String, WatchList> watchlists = fetchRelevantWatchlists(resultSet);

            for (String areaCode : areas.keySet()) {
                mapping.put(areas.get(areaCode), watchlists.get(areaCode));
            }

            return mapping;
        }

        /**
         * Constructs the necessary {@link MonitoringArea}s and maps them to their location
         * @param resultSet the raw data to use
         * @return the re-initiated objects (as area code -> monitoring area)
         */
        Map<String, MonitoringArea> fetchRelevantMonitoringAreas(Cursor resultSet) {
            HashSet<String> areaCodes = new HashSet<>(resultSet.getCount() / APPROX_SPECIES_PER_AREA);

            if (resultSet.moveToFirst()) {
                do {
                    String code = resultSet.getString(
                            resultSet.getColumnIndexOrThrow(BirdCountContract.ObservedSpecies.COLUMN_NAME_AREA));
                    areaCodes.add(code);
                } while(resultSet.moveToNext());
            }

            List<MonitoringArea> areas = fetchAssociatedMonitoringAreas(areaCodes);

            Map<String, MonitoringArea> result = new HashMap<>(areas.size());
            for (MonitoringArea a : areas) {
                result.put(a.getCode(), a);
            }
            return result;
        }

        /**
         * Constructs the necessary {@link WatchList}s and maps them to their location
         * @param resultSet the raw data to use
         * @return the re-initiated objects (as area code -> corresponding watchlist)
         */
        Map<String, WatchList> fetchRelevantWatchlists(Cursor resultSet) {
            Map<String, List<Pair<Species, Integer>>> subsets = partitionObservationsByArea(resultSet);
            Map<String, WatchList> result = new HashMap<>(subsets.size());
            for (String areaCode : subsets.keySet()) {
                WatchList watchList = new WatchList(convertSpeciesRepresentation(subsets.get(areaCode)));
                result.put(areaCode, watchList);
            }
            return result;
        }

        /**
         * Builds the {@link MonitoringArea} objects for the given codes
         * @param codes the area codes to query for
         * @return the objects
         */
        List<MonitoringArea> fetchAssociatedMonitoringAreas(Iterable<String> codes) {
            List<MonitoringArea> areas = new LinkedList<>();
            for (String c : codes) {
                areas.add(fetchAssociatedMonitoringArea(c));
            }
            return areas;
        }

        /**
         * Reloads the {@link MonitoringArea} objects with the given code
         * @param code the area code to query for
         * @return the monitoring area, or {@code null} if no association exists
         */
        MonitoringArea fetchAssociatedMonitoringArea(String code) {
            String[] projection = {
                    BirdCountContract.MonitoringArea.COLUMN_NAME_CODE,
                    BirdCountContract.MonitoringArea.COLUMN_NAME_NAME,
                    BirdCountContract.MonitoringArea.COLUMN_NAME_LAT,
                    BirdCountContract.MonitoringArea.COLUMN_NAME_LON
            };
            String selection = BirdCountContract.MonitoringArea.COLUMN_NAME_CODE + " = ?";
            String[] selectionArgs = { code };

            Cursor cursor = db.query(
                    BirdCountContract.MonitoringArea.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (!cursor.moveToFirst()) {
                return null;
            }
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_NAME)
            );
            double lat = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_LAT)
            );
            double lon = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_LON)
            );
            cursor.close();
            return new MonitoringArea(name, code, new Location(lat, lon));
        }

        /**
         * Divides the raw result set of the database query into more manageable parts for each location
         * (as area code -> "raw watchlist")
         * @param resultSet the raw data
         * @return the rebuild objects
         */
        Map<String, List<Pair<Species, Integer>>> partitionObservationsByArea(Cursor resultSet) {
            final int AREA_IDX = resultSet.getColumnIndexOrThrow(BirdCountContract.ObservedSpecies.COLUMN_NAME_AREA);
            final int SPECIES_CODE_IDX = resultSet.getColumnIndexOrThrow(BirdCountContract.ObservedSpecies.COLUMN_NAME_SPECIES);
            final int COUNT_IDX = resultSet.getColumnIndexOrThrow(BirdCountContract.ObservedSpecies.COLUMN_NAME_COUNT);

            Map<String, List<Pair<Species, Integer>>> result = new HashMap<>(resultSet.getCount() / APPROX_SPECIES_PER_AREA);

            if (resultSet.moveToFirst()) {
                do {
                    String areaCode = resultSet.getString(AREA_IDX);
                    long speciesId = resultSet.getLong(SPECIES_CODE_IDX);
                    Species species = fetchAssociatedSpecies(speciesId);
                    int count = resultSet.getInt(COUNT_IDX);
                    Pair<Species, Integer> pairToAdd = new Pair<>(species, count);

                    if (result.containsKey(areaCode)) {
                        result.get(areaCode).add(pairToAdd);
                    } else {
                        List<Pair<Species, Integer>> newList = new LinkedList<>();
                        newList.add(pairToAdd);
                        result.put(areaCode, newList);
                    }

                } while (resultSet.moveToNext());
            }

            return result;
        }

        /**
         * Converts tuples of {@code (Species, Integer)} into a mapping {@code Species -> Integer}
         * @param raw the list to convert
         * @return the converted list
         */
        Map<Species, Integer> convertSpeciesRepresentation(List<Pair<Species, Integer>> raw) {
            Map<Species, Integer> result = new HashMap<>(raw.size());
            for (Pair<Species, Integer> entry : raw) {
                result.put(entry.first, entry.second);
            }
            return result;
        }

        /**
         * Loads the species with the given ID
         * @param id the ID to query for
         * @return the associated species, or {@code null} if not association exists
         */
        Species fetchAssociatedSpecies(long id) {
            String[] projection = {
                    BirdCountContract.Species.COLUMN_NAME_NAME,
                    BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME
            };
            String selection = BirdCountContract.Species._ID + " = ?";
            String[] selectionArgs = { Long.toString(id) };

            Cursor cursor = db.query(
                    BirdCountContract.Species.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (!cursor.moveToFirst()) {
                return null;
            }

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(BirdCountContract.Species.COLUMN_NAME_NAME)
            );
            String scientificName = cursor.getString(
                    cursor.getColumnIndexOrThrow(BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME)
            );
            cursor.close();
            return new Species(name, scientificName);
        }
    }
}
