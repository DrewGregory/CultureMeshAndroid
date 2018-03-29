package org.codethechange.culturemesh;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.codethechange.culturemesh.models.User;

public class ViewProfileActivity extends AppCompatActivity {
    public static final String SELECTED_USER = "seluser";
    ViewPager mViewPager;
    TabLayout mTabLayout;
    TextView userName, bio, fullName;
    ImageView profilePic;
    long selUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        selUser = getIntent().getLongExtra(SELECTED_USER, -1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mViewPager = findViewById(R.id.contributions_pager);
        PagerAdapter mPagerAdapter = new ContributionsPager(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout = findViewById(R.id.contributions_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
        fullName = findViewById(R.id.full_name);
        userName = findViewById(R.id.user_name);
        bio = findViewById(R.id.bio);
        profilePic = findViewById(R.id.user_profile);
        new LoadUserData().execute(selUser);
    }

    /**
     * This PagerAdapter returns the correct fragment based on which list the user wishes to see.
     * This could be seeing the list of networks the user is subscribed to, the list of posts
     * the user has written, or the list of events the user has attended.
     */
    class ContributionsPager extends FragmentStatePagerAdapter {

        ContributionsPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ListNetworksFragment.newInstance(selUser);
                case 1:
                    return ListUserPostsFragment.newInstance(selUser);
            }
            return ListUserEventsFragment.newInstance(selUser);
        }

        @Override
        public int getCount() {
            //We have three sections: Posts, Events, and Networks.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.networks);
                case 1:
                    return getResources().getString(R.string.posts);
            }
            return getResources().getString(R.string.events);
        }
    }

    /**
     * This allows the user to hit the back button on the toolbar to go to the previous activity.
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    class LoadUserData extends AsyncTask<Long, Void, NetworkResponse<User>> {

        @Override
        protected NetworkResponse<User> doInBackground(Long... longs) {
            API.loadAppDatabase(getApplicationContext());
            NetworkResponse res = API.Get.user(longs[0]);
            API.closeDatabase();
            return res;
        }

        @Override
        protected void onPostExecute(NetworkResponse<User> res) {
            if (res.fail()) {
                res.showErrorDialog(ViewProfileActivity.this);
            } else {
                User user = res.getPayload();
                bio.setText(user.aboutMe);
                fullName.setText(user.firstName + " " + user.lastName);
                userName.setText(user.username);
                Picasso.with(getApplicationContext()).load(user.imgURL).into(profilePic);
            }
        }
    }
}
