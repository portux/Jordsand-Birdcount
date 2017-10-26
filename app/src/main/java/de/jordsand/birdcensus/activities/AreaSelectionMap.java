package de.jordsand.birdcensus.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteMonitoringAreaRepository;
import de.jordsand.birdcensus.services.SimpleBirdCountService;

/**
 * Shows the user a list of all available monitoring areas
 * <p>
 * The user may choose one and at concrete observations through the {@link AddSighting} activity for
 * the selected area
 * </p>
 * @author Rico Bergmann
 */
public class AreaSelectionMap extends AppCompatActivity {
    private static final int RQ_ADD_SIGHTINGS = 555;
    private static final int RQ_AREA_LIST = 666;
    private static final int AVG_MONITORING_AREAS = 25;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 20;
    private static final int TILE_SIZE = 256;

    private SimpleBirdCountService birdCountService;
    private boolean mBound = false;

    private MapView areaMap;
    private SQLiteMonitoringAreaRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_selection_map);

        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        repo = new SQLiteMonitoringAreaRepository(openHandler.getReadableDatabase());

        areaMap = (MapView) findViewById(R.id.area_map);
        areaMap.setTileSource(new XYTileSource("4uMaps", MIN_ZOOM, MAX_ZOOM, TILE_SIZE, ".png", new String[]{}));
        areaMap.setBuiltInZoomControls(true);
        areaMap.setMultiTouchControls(true);

        IMapController mapController = areaMap.getController();
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(54.6862, 10.0339);
        mapController.setCenter(startPoint);

        this.initMap();
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
        inflater.inflate(R.menu.menu_area_selection_map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder;
        switch (item.getItemId()) {
            case R.id.area_selection_terminate_census:
                builder = new AlertDialog.Builder(this);
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
                return true;
            case R.id.area_selection_abort_census:
                builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.abort_bird_count)
                        .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                birdCountService.abortBirdCount();
                                Intent intent = new Intent();
                                intent.putExtra("aborted", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .setTitle(R.string.abort)
                        .setIcon(R.drawable.ic_warning);
                builder.show();
                return true;
            case R.id.area_list:
                Intent listSelection = new Intent(this, AreaSelectionList.class);
                startActivityForResult(listSelection, RQ_AREA_LIST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == RQ_ADD_SIGHTINGS || requestCode == RQ_AREA_LIST) && resultCode == RESULT_OK) {
            if (!birdCountService.isBirdCountOngoing()) {
                Intent result = new Intent();
                result.putExtra("terminated", true);
                setResult(RESULT_OK, result);
                finish();
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SimpleBirdCountService.BirdCountBinder binder = (SimpleBirdCountService.BirdCountBinder) service;
            birdCountService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    /**
     * Adds the monitoring areas to the maps
     */
    private void initMap() {
        Marker.ENABLE_TEXT_LABELS_WHEN_NO_IMAGE = true;

        for (MonitoringArea area : repo.findAll()) {
            GeoPoint location = new GeoPoint(area.getLocation().getLatitude(), area.getLocation().getLongitude());
            Marker marker = new Marker(areaMap);

            marker.setPosition(location);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(area.getCode());
            marker.setIcon(null);

            marker.setOnMarkerClickListener(new AreaSelectionListener(area));

            areaMap.getOverlays().add(marker);
        }
    }

    /**
     * Listener for the monitoring area's on the map
     */
    private class AreaSelectionListener implements Marker.OnMarkerClickListener {
        private MonitoringArea area;

        AreaSelectionListener(MonitoringArea area) {
            this.area = area;
        }

        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            Intent addSightings = new Intent(AreaSelectionMap.this, AddSighting.class);
            addSightings.putExtra("area", area.getCode());
            startActivityForResult(addSightings, RQ_ADD_SIGHTINGS);
            return true;
        }
    }

}
