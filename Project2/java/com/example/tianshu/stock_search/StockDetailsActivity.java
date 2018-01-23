package com.example.tianshu.stock_search;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StockDetailsActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private String stock_symbol;

    private static final String TAG = "StockDetailsActivity";

    private TabCurrentFragment fragment1;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
//        mViewPager.setAdapter(mSectionsPagerAdapter);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String stock_message = intent.getStringExtra(MainActivity.STOCK_MESSAGE);
        stock_symbol = processMessage(stock_message);

        // Capture the layout's toolbar and set the string as its title
        getSupportActionBar().setTitle(stock_symbol);
//        TextView textView = findViewById(R.id.textView);
//        textView.setText(stock_symbol);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        fragment1 = new TabCurrentFragment();

        adapter.addFragment(fragment1, "CURRENT");
        adapter.addFragment(new TabHistoryFragment(), "HISTORICAL");
        adapter.addFragment(new TabNewsFragment(), "NEWS");
        viewPager.setAdapter(adapter);
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
//            return 3;
            return fragmentList.size();
        }
    }

    /**
     * extract stock symbol from the message
     */
    private String processMessage(String message) {
        int idx = message.indexOf("-");
        return idx == -1 ? message.trim().toUpperCase() : message.substring(0, idx - 1);
    }

    public String getSymbol() {
        return stock_symbol;
    }

    /**
     * called when star_button is clicked
     */
    public void changeStar(View v) {
        Log.d(TAG, "changeStar: clicked!");
        fragment1.changeStar(v);
    }

    /**
     * called when fb_button is clicked
     */
    public void shareToFB(View v) {
        Log.d(TAG, "shareToFB: clicked!");
        fragment1.shareToFB(v);
    }

    /**
     * called when change_button is clicked
     */
    public void changeChart(View v) {
        fragment1.changeChart();
    }
}
