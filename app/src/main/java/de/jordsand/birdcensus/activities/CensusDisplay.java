package de.jordsand.birdcensus.activities;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;

import de.jordsand.birdcensus.R;
import de.jordsand.birdcensus.fragments.CensusDisplayDetailsFragment;
import de.jordsand.birdcensus.fragments.CensusDisplayOverviewFragment;

/**
 * Showing a past bird count.
 * <p>
 * There are two tabs available: one which display the metadata of the bird count (time, observer
 * and weather) and one which displays a list of all observed species
 * </p>
 * @author Rico Bergmann
 */
public class CensusDisplay extends AppCompatActivity {
    private static final int TABS_COUNT = 2;
    private static final int TAB_OVERVIEW_POSITION = 0;
    private static final int TAB_DETAILS_POSITION = 1;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        return super.onOptionsItemSelected(item);
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
