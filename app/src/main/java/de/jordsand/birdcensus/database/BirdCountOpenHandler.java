package de.jordsand.birdcensus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class to easily get access to the SQLite database.
 * To control access to the handler, it uses the Singleton-Pattern.
 * @author Rico Bergmann
 */
public class BirdCountOpenHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "jordsand_census.db";

    private static BirdCountOpenHandler instance = null;

    /**
     * Provides access to the handler.
     * @param ctx if no handler was created yet, the context will be used to instantiate
     * @return the handler
     */
    public static BirdCountOpenHandler instance(Context ctx) {
        if (instance == null) {
            instance = new BirdCountOpenHandler(ctx);
        }
        return instance;
    }

    /**
     * We are a singleton, thus the constructor is private
     * @param ctx context to use for opening the database
     */
    private BirdCountOpenHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BirdCountContract.BIRD_COUNT_TABLE_CREATE);
        db.execSQL(BirdCountContract.MONITORING_AREA_TABLE_CREATE);
        db.execSQL(BirdCountContract.SPECIES_TABLE_CREATE);
        db.execSQL(BirdCountContract.OBSERVATION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BirdCountContract.BIRD_COUNT_TABLE_DELETE);
        db.execSQL(BirdCountContract.MONITORING_AREA_TABLE_DELETE);
        db.execSQL(BirdCountContract.SPECIES_TABLE_DELETE);
        db.execSQL(BirdCountContract.OBSERVATION_TABLE_DELETE);

        onCreate(db);
    }
}
