package de.jordsand.birdcensus.database.repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.jordsand.birdcensus.core.Species;
import de.jordsand.birdcensus.core.SpeciesRepository;
import de.jordsand.birdcensus.database.BirdCountContract;

/**
 * Repository to read and write {@link Species} instances from and to a SQLite database
 * @author Rico Bergmann
 */
public class SQLiteSpeciesRepository implements SpeciesRepository {
    private SQLiteDatabase db;

    public SQLiteSpeciesRepository(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public void save(Species species) {
        db.insert(BirdCountContract.Species.TABLE_NAME, null, speciesToContentValues(species));
    }

    @Override
    public boolean exists(Long speciesId) {
        String[] projection = {
          BirdCountContract.Species._ID
        };
        String selection = BirdCountContract.Species._ID + " = ?";
        String[] selectionArgs = { speciesId.toString() };

        Cursor result = db.query(
                BirdCountContract.Species.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean exists = result.moveToFirst();
        result.close();
        return exists;
    }

    @Override
    public Species findOne(Long speciesId) {
        String[] projection = {
                BirdCountContract.Species.COLUMN_NAME_NAME,
                BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME
        };
        String selection = BirdCountContract.Species._ID + " = ?";
        String[] selectionArgs = { speciesId.toString() };

        Cursor result = db.query(
                BirdCountContract.Species.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        Species species = rebuildSpecies(result);
        result.close();
        return species;
    }

    @Override
    public Iterable<Species> findAll() {
        String[] projection = {
                BirdCountContract.Species.COLUMN_NAME_NAME,
                BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME
        };
        Cursor result = db.query(
                BirdCountContract.Species.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<Species> species = new ArrayList<>(result.getCount());
        while (result.moveToNext()) {
            species.add(rebuildSpecies(result));
        }
        result.close();
        return species;
    }

    @Override
    public boolean remove(Long aLong) {
        throw new UnsupportedOperationException("Deleting species is forbidden");
    }

    /**
     * Creates the matching {@link ContentValues} for a species
     * @param species the species to convert
     * @return the values
     */
    private ContentValues speciesToContentValues(Species species) {
        ContentValues values = new ContentValues();
        values.put(BirdCountContract.Species.COLUMN_NAME_NAME, species.getName());
        if (species.hasScientificName()) {
            values.put(BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME, species.getScientificName());
        }
        return values;
    }

    /**
     * Creates a {@link Species} instance from a database table row
     * @param data the table row
     * @return the species (may be {@code null} if it may not be re-established)
     */
    private Species rebuildSpecies(Cursor data) {
        if (data.getPosition() < 0 && !data.moveToFirst()) {
            return null;
        }

        final int NAME_IDX = data.getColumnIndexOrThrow(BirdCountContract.Species.COLUMN_NAME_NAME);
        final int SCIENTIFIC_NAME_IDX = data.getColumnIndexOrThrow(BirdCountContract.Species.COLUMN_NAME_SCIENTIFIC_NAME);
        String name = data.getString(NAME_IDX);
        String scientificName = data.getString(SCIENTIFIC_NAME_IDX);
        return new Species(name, scientificName);
    }
}
