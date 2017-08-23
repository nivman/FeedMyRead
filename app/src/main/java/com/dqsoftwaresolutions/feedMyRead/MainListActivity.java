package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.dqsoftwaresolutions.feedMyRead.data.MainListPosition;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TrashContract;
import com.dqsoftwaresolutions.feedMyRead.database.UserContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.webservices.UpdateDataBaseBackGroundService;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import java.util.ArrayList;

import static com.dqsoftwaresolutions.feedMyRead.R.id.tab_layout;

public class MainListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ContentResolver mContentResolver;
    private MainListPosition mMainListPosition;
    private final Bundle bundle = new Bundle();
    private final UpdateDataBaseBackGroundService dataBasePulService = new UpdateDataBaseBackGroundService();
    private int position=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main_list);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        TabLayout tabLayout = (TabLayout) findViewById(tab_layout);
        WebSitesListFragment webSitesListFragment = new WebSitesListFragment();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
                    googleAnalytics.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Click on 'My List' Tab")
                            .build());
                    WebSitesListFragment webSitesListFragment = new WebSitesListFragment();
                    position=0;
                    mMainListPosition = MainListPosition.getInstance();
                    position = mMainListPosition.getInstantiationCounter();
                    int screenWidth = getScreenMetrics();
                    bundle.putInt("SCREEN_WIDTH", screenWidth);
                    bundle.putInt("POSITION", position);
                    bundle.putBoolean("BECK_FROM_FEED", true);
                    Log.d("POSITION1", String.valueOf(position));
                    webSitesListFragment.setArguments(bundle);
                    fragmentManager.beginTransaction().replace(R.id.flContent, webSitesListFragment).commit();

                } else if (tab.getPosition() == 1) {

                    Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
                    googleAnalytics.send(new HitBuilders.EventBuilder()
                            .setCategory("Action")
                            .setAction("Click on 'Feed' Tab")
                            .build());
                    RssTabFragment rssTabFragment = new RssTabFragment();
                    fragmentManager.beginTransaction().replace(R.id.flContent, rssTabFragment).commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mContentResolver = MainListActivity.this.getContentResolver();
        Intent intent = getIntent();
        String goToRssList = intent.getStringExtra("GO_TO_RSS_FRAGMENT");
        ArrayList<WebSite> webSiteList = intent.getParcelableArrayListExtra("webSiteList");
        if (goToRssList != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) {
                tab.select();
            }
        } else {
            if (fragmentManager.findFragmentById(R.id.flContent) == null) {
                if (webSiteList == null) {
                    int position = getIntent().getIntExtra("POSITION", 0);
                    Bundle extras = getIntent().getExtras();
                    int mainListPosition = extras.getInt("mainListPosition");
                    int screenWidth = getScreenMetrics();
                    bundle.putInt("SCREEN_WIDTH", screenWidth);
                    bundle.putInt("POSITION", position);
                    bundle.putInt("mainListPosition", mainListPosition);
                    webSitesListFragment.setArguments(bundle);

                    fragmentManager.beginTransaction().replace(R.id.flContent, webSitesListFragment).commit();

                } else {

                    SortWebSitesListFragment sortWebSitesListFragment = new SortWebSitesListFragment();
                    bundle.putParcelableArrayList("siteList", webSiteList);
                    sortWebSitesListFragment.setArguments(bundle);
                    fragmentManager.beginTransaction().replace(R.id.flContent, sortWebSitesListFragment).commit();
                }
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LinearLayout logOut = (LinearLayout) findViewById(R.id.nav_logout);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on 'Log Out' from Navigation drawer")
                        .build());
                logOut();
            }
        });
        LinearLayout linearLayout = (LinearLayout) tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(191,217,227));
        drawable.setSize(2, 10);
        linearLayout.setDividerPadding(1);
        linearLayout.setDividerDrawable(drawable);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dataBasePulService.stopTimer();
        dataBasePulService.setTimer(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataBasePulService.closeTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBasePulService.closeTimer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.search_view);
        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getSupportFragmentManager();
                SearchListFragment searchListFragment = new SearchListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("search", "");
                searchListFragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContent, searchListFragment).commit();
                getSupportFragmentManager().executePendingTransactions();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // finish();
                return false;
            }
        });
        final MenuItem aboutView = menu.findItem(R.id.about_view);
        aboutView.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            //    new Intent(MainListActivity.this,About.class);
                startActivity(new Intent(MainListActivity.this,About.class));
                return false;
            }
        });
        return true;
    }

    private void logOut() {
        Uri uri = UserContract.BASE_CONTENT_URI;
        Uri webSites = WebSitesContract.BASE_CONTENT_URI;
        Uri trashSites = TrashContract.BASE_CONTENT_URI;
        Uri tags = TagsContract.BASE_CONTENT_URI;
        mContentResolver.delete(uri, null, null);
        mContentResolver.delete(webSites, null, null);
        mContentResolver.delete(trashSites, null, null);
        mContentResolver.delete(tags, null, null);
        Intent intent = new Intent(MainListActivity.this, LoginRegisterActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        final FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        Class fragmentClass = null;
        final int id = item.getItemId();
        TabLayout tabLayout = (TabLayout) findViewById(tab_layout);
        if (id == R.id.main_list) {
            fragmentClass = WebSitesListFragment.class;
            tabLayout.setVisibility(View.VISIBLE);
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Click on 'My List' from Navigation drawer")
                    .build());
        } else if (id == R.id.tags_list) {
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Click on 'Tags' from Navigation drawer")
                    .build());
            fragmentClass = TagsListFragment.class;
            tabLayout.setVisibility(View.GONE);
        } else if (id == R.id.favorites_list) {
            tabLayout.setVisibility(View.GONE);
            fragmentClass = FavoritesListFragment.class;
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Click on 'Favorites' from Navigation drawer")
                    .build());
        } else if (id == R.id.statistics) {
            tabLayout.setVisibility(View.GONE);
            fragmentClass = Statistics.class;
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Click on 'Statistics' from Navigation drawer")
                    .build());

        } else if (id == R.id.archive_list) {
            tabLayout.setVisibility(View.GONE);
            fragmentClass = TrashListFragment.class;
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("Click on 'Archive' from Navigation drawer")
                    .build());
        }
        try {

            assert fragmentClass != null;
            fragment = (Fragment) fragmentClass.newInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        final Fragment finalFragment = fragment;
        Handler handler = new Handler();

        Runnable r = new Runnable() {
            public void run() {
                TabLayout tabLayout = (TabLayout) findViewById(tab_layout);
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                assert tab != null;
                tab.select();
                fragmentManager.beginTransaction().replace(R.id.flContent, finalFragment).commit();
            }
        };

        handler.postDelayed(r, 270);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return true;
    }

    private int getScreenMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
      //  int height = displaymetrics.heightPixels;
       // int width = displaymetrics.widthPixels;

        return displaymetrics.widthPixels;
        //  new ScreenMetrics(width);
    }


}
