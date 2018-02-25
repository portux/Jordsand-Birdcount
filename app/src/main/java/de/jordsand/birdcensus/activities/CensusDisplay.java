package de.jordsand.birdcensus.activities;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.core.BirdCount;
import de.jordsand.birdcensus.core.BirdCountRepository;
import de.jordsand.birdcensus.database.BirdCountOpenHandler;
import de.jordsand.birdcensus.database.repositories.SQLiteBirdCountRepository;
import de.jordsand.birdcensus.fragments.CensusDisplayDetailsFragment;
import de.jordsand.birdcensus.fragments.CensusDisplayOverviewFragment;
import de.jordsand.birdcensus.services.exporter.csv.CsvBirdCountExporter;
import de.jordsand.birdcensus.services.exporter.csv.ExportException;
import de.jordsand.birdcensus.util.FileSystem;

/**
 * Showing a past bird count.
 * <p>
 * There are two tabs available: one which display the metadata of the bird count (time, observer
 * and weather) and one which displays a list of all observed species
 * </p>
 * @author Rico Bergmann
 */
public class CensusDisplay extends AppCompatActivity {
    private static final String TAG = CensusDisplay.class.getSimpleName();
    private static final int TABS_COUNT = 2;
    private static final int TAB_OVERVIEW_POSITION = 0;
    private static final int TAB_DETAILS_POSITION = 1;

    private BirdCountRepository birdCountRepo;

    private Date censusStartDate;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_census_display);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        BirdCountOpenHandler openHandler = BirdCountOpenHandler.instance(this);
        birdCountRepo = new SQLiteBirdCountRepository(openHandler.getReadableDatabase());

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.display_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.census_display_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        censusStartDate = new Date(getIntent().getLongExtra("census_start_date", -1));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_census_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BirdCount birdCount;
        boolean successful;
        switch (item.getItemId()) {
            case R.id.export_census_complete:
                birdCount = birdCountRepo.findByStartDate(censusStartDate);
                successful = exportCensusComplete(birdCount);
                alertExportFinished(successful);
                break;
            case R.id.export_census_summary:
                birdCount = birdCountRepo.findByStartDate(censusStartDate);
                successful = exportCensusSummary(birdCount);
                alertExportFinished(successful);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a CSV-file containing all the summary of the displayed bird count
     * @param birdCount the bird count to export
     * @return whether the export succeeded
     */
    private boolean exportCensusSummary(BirdCount birdCount) {
        File documentDir = FileSystem.getDocumentDirectoryRoot();
        String filename = generateExportFilename(birdCount, getString(R.string.census_export_summary_suffix), documentDir);
        File exportFile = new File(documentDir, filename);

        try {
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(exportFile);
            CsvBirdCountExporter exporter = new CsvBirdCountExporter(new OutputStreamWriter(fos), this);
            exporter.exportBirdCountSummary(birdCount);
        } catch (IOException | ExportException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

    /**
     * Creates a CSV-file containing all the sightings for the displayed bird count
     * @param birdCount the bird count to export
     * @return whether the export succeeded
     */
    private boolean exportCensusComplete(BirdCount birdCount) {
        File documentDir = FileSystem.getDocumentDirectoryRoot();
        String filename = generateExportFilename(birdCount, getString(R.string.census_export_complete_suffix), documentDir);
        File exportFile = new File(documentDir, filename);

        try {
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(exportFile);
            CsvBirdCountExporter exporter = new CsvBirdCountExporter(new OutputStreamWriter(fos), this);
            exporter.exportCompleteBirdCount(birdCount);
        } catch (IOException | ExportException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

    /**
     * Creates the name for the CSV file to contain the bird count.
     * It is guaranteed to not conflict with any existing files.
     * @param birdCount the bird count to export+
     * @param suffix the file name suffix (e.g. "<em>summary</em>" or "<em>complete</em>")
     * @param path where the file will be created. This parameter is especially important to
     *             generate a unique file name
     * @return the file name
     */
    @NonNull
    private String generateExportFilename(BirdCount birdCount, String suffix, @NonNull File path) {
        String fileType = ".csv";
        String prefix = getString(R.string.census_export_prefix);
        String date = String.format(getString(R.string.census_export_date_pattern), birdCount.getStartTime(), birdCount.getStartTime(), birdCount.getStartTime());
        String separator = getString(R.string.census_export_fname_separator);

        String filename = prefix + separator + date + separator + suffix;

        File file = new File(path, filename + fileType);
        int exportNumber = file.exists() ? 1 : -1;

        while (file.exists()) {
            exportNumber++;
            String fname = filename + separator + exportNumber + fileType;
            file = new File(path, fname);
        }

        if (exportNumber > -1) {
            filename += separator + exportNumber;
        }

        return filename + fileType;
    }

    /**
     * Displays an {@link AlertDialog} to notify the user about the termination of the export
     * @param successful whether the export finished without any errors
     */
    private void alertExportFinished(boolean successful) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        if (successful) {
            alertDialog.setMessage(R.string.census_export_success);
            alertDialog.setIcon(R.drawable.ic_info);
        } else {
            alertDialog.setMessage(R.string.census_export_error);
            alertDialog.setIcon(R.drawable.ic_warning);
        }
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_OVERVIEW_POSITION:
                    return CensusDisplayOverviewFragment.newInstance(censusStartDate);
                case TAB_DETAILS_POSITION:
                    return CensusDisplayDetailsFragment.newInstance(censusStartDate);
                default:
                    throw new IllegalArgumentException("Unknown position: " + position);
            }
        }

        @Override
        public int getCount() {
            return TABS_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_OVERVIEW_POSITION:
                    return getString(R.string.census_overview);
                case TAB_DETAILS_POSITION:
                    return getString(R.string.census_details);
                default:
                    throw new IllegalArgumentException("Unknown position: " + position);
            }
        }
    }
}
