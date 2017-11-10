package org.codethechange.culturemesh;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.GONE;


public class FindNetworkActivity extends AppCompatActivity {

    //TODO: Replace dummy data with real data
    private static ArrayList<String> dummy = new ArrayList<String>();

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

    /*
     * The {@link SearchManager} that will associate the SearchView with our search config.
     */
    private static SearchManager mSearchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_network);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSearchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find_network, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FindLocationFragment extends Fragment implements SearchView.OnQueryTextListener {


        private ListView searchList;
        private ArrayAdapter<String> adapter;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public FindLocationFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FindLocationFragment newInstance(int sectionNumber) {
            FindLocationFragment fragment = new FindLocationFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Get the intent, verify the action and get the query
            View rootView = inflater.inflate(R.layout.fragment_find_location, container,
                    false);
            SearchView searchView = rootView.findViewById(R.id.from_location_search_view);
            searchView.setOnQueryTextListener(this);
            searchView.setSearchableInfo(mSearchManager.
                    getSearchableInfo(getActivity().getComponentName()));
            searchList = rootView.findViewById(R.id.search_suggestions_list_view);
            //TODO: Remove dummy data
            dummy.add("New York, New York, United States");
            dummy.add("Singapore");
            dummy.add("London, United Kingdom");
            dummy.add("Stanford, California, United States");
            adapter = new LocationSearchAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, dummy);
            searchList.setTextFilterEnabled(true);
            searchList.setAdapter(adapter);
            return rootView;

        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Toast.makeText(getActivity(), "Changing text", Toast.LENGTH_LONG).show();
            adapter.getFilter().filter(newText);
            if (searchList.getVisibility() == GONE) {
                searchList.setVisibility(View.VISIBLE);
            }
            searchList.setAdapter(adapter);
            return true;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return FindLocationFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
