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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

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
public class AreaSelection extends AppCompatActivity {
    private static final int RQ_ADD_SIGHTINGS = 555;

    private SimpleBirdCountService birdCountService;
    private boolean mBound = false;

    private GridView grid;
    private AreaAdapter adapter;
    private SQLiteMonitoringAreaRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_selection);

        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        repo = new SQLiteMonitoringAreaRepository(openHandler.getReadableDatabase());

        grid = (GridView) findViewById(R.id.area_selection);
        adapter = new AreaAdapter(this, repo.findAll());
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AreaGridOnClickListener());
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
        inflater.inflate(R.menu.menu_area_selection, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_ADD_SIGHTINGS && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("terminated", false)) {
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
     * Adapter for the area view
     */
    private class AreaAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        private List<MonitoringArea> monitoringAreas;

        AreaAdapter(Context ctx, Iterable<MonitoringArea> areas) {
            inflater = LayoutInflater.from(ctx);
            monitoringAreas = new LinkedList<>();
            for (MonitoringArea a : areas) {
                monitoringAreas.add(a);
            }
        }

        @Override
        public int getCount() {
            return monitoringAreas.size();
        }

        @Override
        public Object getItem(int i) {
            return monitoringAreas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.monitoring_areas_list, parent, false);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.areas_list_name);
                holder.code = convertView.findViewById(R.id.areas_list_code);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MonitoringArea area = (MonitoringArea) getItem(position);
            holder.name.setText(area.getName());
            holder.code.setText(area.getCode());
            return convertView;
        }

        class ViewHolder {
            TextView name, code;
        }
    }

    /**
     * Handler for click events on a specific monitoring area
     */
    private class AreaGridOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MonitoringArea selectedArea = (MonitoringArea) adapter.getItem(position);
            Intent addSightings = new Intent(AreaSelection.this, AddSighting.class);
            addSightings.putExtra("area", selectedArea.getCode());
            startActivityForResult(addSightings, RQ_ADD_SIGHTINGS);
        }
    }
}
