package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.test.espresso.core.deps.guava.collect.Lists;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.CompareUpdatesTime;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.URI_TABLE;

public class WebSitesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = WebSitesListFragment.class.getSimpleName();
    private WebSitesCustomAdapter mAdapter;
    private  final int LOADER_ID = 1;
    private  final int LOADER__TAGS_ID = 2;
    private ContentResolver mContentResolver;
    private List<WebSite> mWebSites;
    private final MyObserver myObserver = new MyObserver(new Handler());
    private final Handler handler = new Handler();
    private WebSitesLoader mWebSitesLoader;
    private TagsLoader mTagsLoader;
    private ListView list;
    private int screenWidth = 0;
    private int mainListPosition=0;
     private int rowInScreen;
    private SwipeRefreshLayout mSwipeRefreshRssListLayout;
    private Utils mUtils;
      @Override
    public void onPause() {
        super.onPause();

        getActivity().getContentResolver().unregisterContentObserver(myObserver);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getContentResolver().unregisterContentObserver(myObserver);
        handler.removeCallbacks(fitsOnScreen);
    }
    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Main List");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
        myObserver.onChange(true,URI_TABLE);
        getActivity().getContentResolver().registerContentObserver(URI_TABLE, true, myObserver);
    }

    private void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        mainListPosition=0;
        if (bundle != null) {
            boolean backFromFeed =bundle.getBoolean("BECK_FROM_FEED");
            //set the last position of the list
            if(backFromFeed){
                mainListPosition = bundle.getInt("POSITION");
            }else{
                mainListPosition = bundle.getInt("mainListPosition");
            }
            screenWidth = bundle.getInt("SCREEN_WIDTH");
             setScreenWidth(screenWidth);
           }
        mUtils= new Utils(getContext());
        mWebSitesLoader = new WebSitesLoader(getContext(), mContentResolver);
        mTagsLoader = new TagsLoader(getContext(), mContentResolver);
        mContentResolver = getActivity().getContentResolver();
        mAdapter = new WebSitesCustomAdapter(getActivity(), WebSitesListFragment.this);
        setEmptyText("No Web Sites");
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        getLoaderManager().initLoader(LOADER__TAGS_ID, null, this);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_list_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.listcontainer);
        list = (ListView) listContainer.findViewById(android.R.id.list);
        listContainer.addView(superview);
        mSwipeRefreshRssListLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_main_list);
        mSwipeRefreshRssListLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        String token = mUtils.getUserToken();
                        new CompareUpdatesTime(getContext(),-1,token);
                        mSwipeRefreshRssListLayout.setRefreshing(true);
                        mSwipeRefreshRssListLayout.setRefreshing(false);
                      //  listPosition = "top";
                    }
                });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID: {
                return new CursorLoader(
                        getActivity(),
                        URI_TABLE,
                        null,
                        null,
                        null,
                        null);
            }
            case LOADER__TAGS_ID: {
                return new CursorLoader(
                        getActivity(),
                        TagsContract.URI_TABLE,
                        null,
                        null,
                        null,
                        null);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<WebSite> webSiteEntries = new ArrayList<>();
        switch (loader.getId()) {
            case LOADER_ID: {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            WebSite webSite = mWebSitesLoader.getWebSiteData(cursor);
                            webSiteEntries.add(webSite);
                        } while (cursor.moveToNext());
                    }
                    mWebSites = Lists.reverse(webSiteEntries);
                    mAdapter.setData(mWebSites);
                    if (isResumed()) {
                        setListShown(true);
                    } else {
                        setListShownNoAnimation(true);
                    }
                    cursor.close();
                }
                handler.removeCallbacks(fitsOnScreen);
                handler.post(fitsOnScreen);

                break;
            }
            case LOADER__TAGS_ID: {
                String[] projection = mTagsLoader.getProjection();
                cursor = mContentResolver.query(TagsContract.URI_TABLE, projection, null, null, null);
                ArrayList<String> tagsSiteGuidsArr = new ArrayList<>();
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String tagSiteGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                            tagsSiteGuidsArr.add(tagSiteGuid);
                        } while (cursor.moveToNext());
                        setTagHash(tagsSiteGuidsArr);
                    }
                    cursor.close();
                }
                break;
            }
        }
        list.setSelectionFromTop(mainListPosition, 0);
    }

    private void setTagHash(ArrayList<String> tagsSiteGuidsArr) {
        String[] projection = mTagsLoader.getProjection();
        String[] selectionArgs = tagsSiteGuidsArr.toArray(new String[0]);
        String tagName;
        ArrayList<Hashtable> tagsArr = new ArrayList<>();
        String webSiteSelection = WebSitesContract.WebSitesColumns.SITE_GUID + " in (";
        for (String selectionArg : selectionArgs) {
            webSiteSelection += "?, ";
        }
        webSiteSelection = webSiteSelection.substring(0, webSiteSelection.length() - 2) + ")";
        Cursor cursor = mContentResolver.query(TagsContract.URI_TABLE, projection, webSiteSelection, selectionArgs, null);
        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {

                    tagName = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    String tagGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    Hashtable<String, String> hashtable = new Hashtable<>();
                    hashtable.put(tagGuid, tagName);
                    tagsArr.add(hashtable);

                } while (cursor.moveToNext());
                mAdapter.setTagsHashTable(tagsArr);
                //   margeTagGuid(tagsArr);
                cursor.close();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setData(null);
    }

    class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {

            if (mWebSites == null) {
                return;
            }
            int index = list.getFirstVisiblePosition();
            View v = list.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - list.getPaddingTop());
            if (uri.equals(WebSitesContract.URI_TABLE)) {
                mWebSites.clear();
                String[] projection = mWebSitesLoader.getProjection();
                Cursor cursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            WebSite webSite = mWebSitesLoader.getWebSiteData(cursor);
                            mWebSites.add(webSite);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                mWebSites = Lists.reverse(mWebSites);
                mAdapter.setData(mWebSites);
                setTagsInWebView();
                list.setSelectionFromTop(index, top);
                return;
            }
            String newEntryId = setArrayOfNewEntryId(uri);
            int webSiteListSize = mWebSites.size();
            int position;
            int countRowInDataBase = mAdapter.count();
            if (webSiteListSize < countRowInDataBase) {
                WebSite webSite;
                String[] projection = mWebSitesLoader.getProjection();
                String selection = WebSitesContract.WebSitesColumns.SITE_ID + " =?";
                String[] selectionArgs = {newEntryId};
                Cursor cursor = mContentResolver.query(URI_TABLE, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            webSite = mWebSitesLoader.getWebSiteData(cursor);
                            mWebSites.add(0, webSite);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            } else {
                position = mAdapter.getGuidOfDeletedSite();
                if (mWebSites.size() == 0 || mWebSites.size() == position) {
                    return;
                }
                boolean deleteFromLocalDevice = mAdapter.isDeleteFromLocalDevice();
                if (deleteFromLocalDevice) {
                    mWebSites.remove(position);
                } else {
                    mWebSites.clear();
                    String[] projection = mWebSitesLoader.getProjection();
                    Cursor cursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, null, null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                WebSite webSite = mWebSitesLoader.getWebSiteData(cursor);
                                mWebSites.add(webSite);
                            } while (cursor.moveToNext());
                        }
                        mWebSites = Lists.reverse(mWebSites);
                        cursor.close();
                        mAdapter.setDeleteFromLocalDevice(false);
                    }
                }
            }
            mAdapter.setData(mWebSites);
            if (isResumed()) {
                setListShown(true);
            } else {
                try {
                    setListShownNoAnimation(true);
                } catch (IllegalStateException e) {
                    Log.d(LOG_TAG, e.toString());
                }
            }
            list.setSelectionFromTop(index, top);
        }

    }

    private void setTagsInWebView() {
        ArrayList<String> tagsSiteGuidsArr = new ArrayList<>();
        String[] projection = mTagsLoader.getProjection();
        Cursor cursor = mContentResolver.query(TagsContract.URI_TABLE, projection, null, null, null);
        List<TagName> tagsEntries = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    String tagName = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    String tagSiteGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    TagName tagNameObj = new TagName(_id, tagName, tagSiteGuid);
                    tagsEntries.add(tagNameObj);
                    tagsSiteGuidsArr.add(tagSiteGuid);
                } while (cursor.moveToNext());
                tagsEntries = Lists.reverse(tagsEntries);
                mAdapter.setTags(tagsEntries);
                setTagHash(tagsSiteGuidsArr);
            }
            cursor.close();
        }
    }

    private String setArrayOfNewEntryId(Uri uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private final Runnable fitsOnScreen = new Runnable() {
        @Override
        public void run() {

            int first = list.getFirstVisiblePosition();
            int last = list.getLastVisiblePosition();
            rowInScreen = last - first;
            mAdapter.rowInScreen(rowInScreen);
            setRowInScreen(rowInScreen);
            checkIfListIsOnTop();

        }
    };

    private void checkIfListIsOnTop() {
        list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    // check if we reached the top or bottom of the list
                    View v = list.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        list.smoothScrollToPosition(0);
                        mAdapter.setListViewPositionFromRss(-1);
                        // Log.d("reached", String.valueOf("reached the top"));

                    }
                } else if (totalItemCount - visibleItemCount == firstVisibleItem) {
                    View v = list.getChildAt(totalItemCount - 1);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        // reached the bottom:
                        mAdapter.setListViewPositionFromRss(mWebSites.size());
                        //   Log.d("reached", String.valueOf("reached the bottom"));

                    }
                }
            }
        });
       }

    private void setRowInScreen(int rowInScreen) {
        this.rowInScreen = rowInScreen;
    }

}
