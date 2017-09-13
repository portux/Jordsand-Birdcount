package de.jordsand.birdcensus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Rico Bergmann
 */
public class BirdCountOpenHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "jordsand_census.db";

    public BirdCountOpenHandler(Context ctx) {
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
