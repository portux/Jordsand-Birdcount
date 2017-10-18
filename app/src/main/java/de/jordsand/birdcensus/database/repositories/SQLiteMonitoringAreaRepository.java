package de.jordsand.birdcensus.database.repositories;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.jordsand.birdcensus.core.Location;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.core.MonitoringAreaRepository;
import de.jordsand.birdcensus.database.BirdCountContract;

/**
 * Repository to read and write {@link MonitoringArea} instances from and to a SQLite database
 * @author Rico Bergmann
 */
public class SQLiteMonitoringAreaRepository implements MonitoringAreaRepository {
    private SQLiteDatabase db;

    public SQLiteMonitoringAreaRepository(SQLiteDatabase db) {
        this.db = db;
    }

    @Nullable
    @Override
    public MonitoringArea findByName(@NonNull String name) {
        String[] projection = {
                BirdCountContract.MonitoringArea.COLUMN_NAME_NAME,
                BirdCountContract.MonitoringArea.COLUMN_NAME_CODE,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LON,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LAT
        };
        String selection = BirdCountContract.MonitoringArea.COLUMN_NAME_NAME + " = ?";
        String[] selectionArgs = { name };

        Cursor result = db.query(
                BirdCountContract.MonitoringArea.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        MonitoringArea area = rebuildMonitoringArea(result);
        result.close();
        return area;
    }

    @Override
    public String save(MonitoringArea area) {
        db.insert(BirdCountContract.MonitoringArea.TABLE_NAME, null, monitoringAreaToContentValues(area));
        return area.getCode();
    }

    @Override
    public boolean exists(String code) {
        String[] projection = {
                BirdCountContract.MonitoringArea.COLUMN_NAME_CODE
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
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    @Override
    public MonitoringArea findOne(String code) {
        String[] projection = {
                BirdCountContract.MonitoringArea.COLUMN_NAME_NAME,
                BirdCountContract.MonitoringArea.COLUMN_NAME_CODE,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LON,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LAT
        };
        String selection = BirdCountContract.MonitoringArea.COLUMN_NAME_CODE + " = ?";
        String[] selectionArgs = { code };

        Cursor result = db.query(
                BirdCountContract.MonitoringArea.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        MonitoringArea area = rebuildMonitoringArea(result);
        result.close();
        return area;
    }

    @Override
    public Iterable<MonitoringArea> findAll() {
        String[] projection = {
                BirdCountContract.MonitoringArea.COLUMN_NAME_NAME,
                BirdCountContract.MonitoringArea.COLUMN_NAME_CODE,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LON,
                BirdCountContract.MonitoringArea.COLUMN_NAME_LAT
        };

        Cursor result = db.query(
                BirdCountContract.MonitoringArea.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<MonitoringArea> areas = new ArrayList<>(result.getCount());
        while (result.moveToNext()) {
            areas.add(rebuildMonitoringArea(result));
        }
        result.close();
        return areas;
    }

    @Override
    public boolean remove(String s) {
        throw new UnsupportedOperationException("Removal of monitoring areas forbidden");
    }

    /**
     * Creates the matching {@link ContentValues} for a monitoring area
     * @param area the area to convert
     * @return the values
     */
    private ContentValues monitoringAreaToContentValues(MonitoringArea area) {
        ContentValues values = new ContentValues();
        values.put(BirdCountContract.MonitoringArea.COLUMN_NAME_CODE, area.getCode());
        values.put(BirdCountContract.MonitoringArea.COLUMN_NAME_NAME, area.getName());
        values.put(BirdCountContract.MonitoringArea.COLUMN_NAME_LAT, area.getLocation().getLatitude());
        values.put(BirdCountContract.MonitoringArea.COLUMN_NAME_LON, area.getLocation().getLongitude());
        return values;
    }

    /**
     * Creates a {@link MonitoringArea} instance from a database table row
     * @param data the table row
     * @return the monitoring area (may be {@code null} if it may not be re-established)
     */
    private MonitoringArea rebuildMonitoringArea(Cursor data) {
        if (data.getPosition() < 0 && !data.moveToFirst()) {
            return null;
        }

        final int CODE_IDX = data.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_CODE);
        final int NAME_IDX = data.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_NAME);
        final int LAT_IDX = data.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_LAT);
        final int LON_IDX = data.getColumnIndexOrThrow(BirdCountContract.MonitoringArea.COLUMN_NAME_LON);
        String code = data.getString(CODE_IDX);
        String name = data.getString(NAME_IDX);
        Location location = new Location(data.getDouble(LAT_IDX), data.getDouble(LON_IDX));

        return new MonitoringArea(name, code, location);
    }
}
