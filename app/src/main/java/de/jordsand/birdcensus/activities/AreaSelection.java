package de.jordsand.birdcensus.activities;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.MonitoringArea;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteMonitoringAreaRepository;

public class AreaSelection extends ListActivity implements OnItemClickListener {
    private MonitoringAreaAdapter adapter;
    private SQLiteMonitoringAreaRepository repo;
    private BirdCount birdCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_selection);

        BirdCountOpenHandler openHandler = new BirdCountOpenHandler(getApplicationContext());
        repo = new SQLiteMonitoringAreaRepository(openHandler.getReadableDatabase());

        adapter = new MonitoringAreaAdapter(this, repo.findAll());
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        birdCount = getIntent().getParcelableExtra("bird_count");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent addObservation = new Intent();
        addObservation.putExtra("bird_count", birdCount);
    }

    private static class MonitoringAreaAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<MonitoringArea> monitoringAreas;

        public MonitoringAreaAdapter(Context ctx, Iterable<MonitoringArea> areas) {
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
                holder.name = (TextView) convertView.findViewById(R.id.areas_list_name);
                holder.code = (TextView) convertView.findViewById(R.id.areas_list_code);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MonitoringArea area = monitoringAreas.get(position);
            holder.name.setText(area.getName());
            holder.code.setText(area.getCode());
            return convertView;
        }

        static class ViewHolder {
            TextView name, code;
        }
    }
}
