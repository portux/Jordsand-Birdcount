package de.jordsand.birdcensus.database;

import android.provider.BaseColumns;

/**
 * Collection of the data schema and most crucial management operations for the involved tables
 * @author Rico Bergmann
 */
public class BirdCountContract {

    @SuppressWarnings("unused")
    private BirdCountContract() {}

    public static class BirdCount implements BaseColumns {
        public static final String TABLE_NAME = "bird_count";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_WATER_GAUGE = "water_gauge";
        public static final String COLUMN_NAME_WIND_STRENGTH = "wind_strength";
        public static final String COLUMN_NAME_WIND_DIRECTION = "wind_direction";
        public static final String COLUMN_NAME_PRECIPITATION = "precipitation";
        public static final String COLUMN_NAME_VISIBILITY = "visibility";
        public static final String COLUMN_NAME_GLACIATION_LEVEL = "glaciation_level";
        public static final String COLUMN_NAME_OBSERVER = "observer";
    }
    public static final String BIRD_COUNT_TABLE_CREATE =
            "CREATE TABLE " + BirdCount.TABLE_NAME + " (" +
                    BirdCount._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BirdCount.COLUMN_NAME_START_TIME + " TEXT UNIQUE NOT NULL, " +
                    BirdCount.COLUMN_NAME_END_TIME + " TEXT NOT NULL, " +
                    BirdCount.COLUMN_NAME_WATER_GAUGE + " REAL, " +
                    BirdCount.COLUMN_NAME_WIND_STRENGTH + " INTEGER, " +
                    BirdCount.COLUMN_NAME_WIND_DIRECTION + " INTEGER, " +
                    BirdCount.COLUMN_NAME_PRECIPITATION + " INTEGER, " +
                    BirdCount.COLUMN_NAME_VISIBILITY + " INTEGER, " +
                    BirdCount.COLUMN_NAME_GLACIATION_LEVEL + " INTEGER, " +
                    BirdCount.COLUMN_NAME_OBSERVER + " TEXT)";
    public static final String BIRD_COUNT_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + BirdCount.TABLE_NAME;

    public static class MonitoringArea {
        public static final String TABLE_NAME = "monitoring_area";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LON = "longitude";
        // public static final String COLUMN_NAME_IS_STATION = "station";
    }
    public static final String MONITORING_AREA_TABLE_CREATE =
            "CREATE TABLE " + MonitoringArea.TABLE_NAME + " (" +
                    MonitoringArea.COLUMN_NAME_CODE + " TEXT PRIMARY KEY, " +
                    MonitoringArea.COLUMN_NAME_NAME + " TEXT UNIQUE NOT NULL, " +
                    MonitoringArea.COLUMN_NAME_LAT + " FLOAT, " +
                    MonitoringArea.COLUMN_NAME_LON + " FLOAT)";
    public static final String MONITORING_AREA_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + MonitoringArea.TABLE_NAME;

    public static class Species implements BaseColumns {
        public static final String TABLE_NAME = "species";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SCIENTIFIC_NAME = "scientific_name";
        // public static final String COLUMN_NAME_BELONGING = "belonging";
    }
    public static final String SPECIES_TABLE_CREATE =
            "CREATE TABLE " + Species.TABLE_NAME + " (" +
                    Species._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Species.COLUMN_NAME_SCIENTIFIC_NAME + " TEXT UNIQUE DEFAULT NULL, " +
                    Species.COLUMN_NAME_NAME + " TEXT NOT NULL)";
    public static final String SPECIES_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + Species.TABLE_NAME;

    public static class ObservedSpecies {
        public static final String TABLE_NAME = "observation";
        public static final String COLUMN_NAME_AREA = "area";
        public static final String COLUMN_NAME_SPECIES = "species";
        public static final String COLUMN_NAME_CENSUS = "census";
        public static final String COLUMN_NAME_COUNT = "count";
    }
    public static final String OBSERVATION_TABLE_CREATE =
            "CREATE TABLE " + ObservedSpecies.TABLE_NAME + " (" +
                    ObservedSpecies.COLUMN_NAME_AREA + " TEXT, " +
                    ObservedSpecies.COLUMN_NAME_SPECIES + " INTEGER, " +
                    ObservedSpecies.COLUMN_NAME_CENSUS + " INTEGER, " +
                    ObservedSpecies.COLUMN_NAME_COUNT + " INTEGER, " +
                    "PRIMARY KEY (" +
                    ObservedSpecies.COLUMN_NAME_AREA + ", " +
                    ObservedSpecies.COLUMN_NAME_SPECIES + ", " +
                    ObservedSpecies.COLUMN_NAME_CENSUS + "), " +
                    "FOREIGN KEY (" +
                    ObservedSpecies.COLUMN_NAME_AREA + ") REFERENCES " +
                    MonitoringArea.TABLE_NAME + "(" +
                    MonitoringArea.COLUMN_NAME_CODE + "), " +
                    "FOREIGN KEY (" +
                    ObservedSpecies.COLUMN_NAME_SPECIES + ") REFERENCES " +
                    Species.TABLE_NAME + "(" +
                    Species._ID + "), " +
                    "FOREIGN KEY (" +
                    ObservedSpecies.COLUMN_NAME_CENSUS + ") REFERENCES " +
                    BirdCount.TABLE_NAME + "(" +
                    BirdCount._ID + ") )";
    public static final String OBSERVATION_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + ObservedSpecies.TABLE_NAME;
}
