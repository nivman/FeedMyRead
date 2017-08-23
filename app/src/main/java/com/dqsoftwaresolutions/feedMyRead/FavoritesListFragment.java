package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.test.espresso.core.deps.guava.collect.Lists;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class FavoritesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = FavoritesListFragment.class.getSimpleName();
    private WebSitesCustomAdapter mAdapter;
    private static final int LOADER__FAVORITES_ID=1;
    private static final int LOADER__TAGS_ID=2;
    private ContentResolver mContentResolver;
    private List<WebSite> mWebSites;
    private WebSitesLoader mWebSitesLoader;
    private TagsLoader mTagsLoader;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LOADER__FAVORITES_ID: {
                String selection = WebSitesContract.WebSitesColumns.SITE_FAVORITE + " =?";
                String[] selectionArgs = {"true"};
                String[] projection = mWebSitesLoader.getProjection();
                return new CursorLoader(
                        getActivity(),
                        WebSitesContract.URI_TABLE,
                        projection,
                        selection,
                        selectionArgs,
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

        switch(loader.getId()) {

            case LOADER__FAVORITES_ID: {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            WebSite webSite = mWebSitesLoader.getWebSiteData(cursor);
                            webSiteEntries.add(webSite);
                        } while (cursor.moveToNext());
                    }
                    mWebSites= Lists.reverse(webSiteEntries);
                    mAdapter.setData(mWebSites);
                    if(isResumed()){
                        setListShown(true);
                    }else {
                        setListShownNoAnimation(true);
                    }
                    cursor.close();
                }
                break;
            }
            case LOADER__TAGS_ID: {
                setTagsInWebView();
            }
        }
      }
    private void setTagsInWebView() {
        ArrayList<String> tagsSiteGuidsArr= new ArrayList<>();
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
    private void setTagHash(ArrayList<String> tagsSiteGuidsArr) {
        String[] projection = mTagsLoader.getProjection();
        String[] selectionArgs= tagsSiteGuidsArr.toArray(new String[0]);
        String tagName;
        ArrayList<Hashtable> tagsArr= new ArrayList<>();
        String webSiteSelection = WebSitesContract.WebSitesColumns.SITE_GUID + " in (";
        for (String selectionArg : selectionArgs) {
            webSiteSelection += "?, ";
        }
        Log.d("webSiteSelection",webSiteSelection);
        webSiteSelection = webSiteSelection.substring(0, webSiteSelection.length() - 2) + ")";

        Cursor cursor = mContentResolver.query(TagsContract.URI_TABLE, projection, webSiteSelection, selectionArgs, null);
        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {

                    tagName = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    String tagGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    Hashtable<String, String> hashtable = new Hashtable<>();
                    hashtable.put(tagGuid,tagName);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentResolver = getActivity().getContentResolver();
        mAdapter = new WebSitesCustomAdapter(getActivity(), FavoritesListFragment.this);
        mTagsLoader = new TagsLoader(getContext(),mContentResolver);
        mWebSitesLoader = new WebSitesLoader(getContext(), mContentResolver);
        setEmptyText("No Web Sites");
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER__TAGS_ID,null,FavoritesListFragment.this);
        getLoaderManager().initLoader(LOADER__FAVORITES_ID,null,FavoritesListFragment.this);
        setHasOptionsMenu(true);
        setListShown(false);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tags_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.tags_container);
        listContainer.findViewById(android.R.id.list);
        listContainer.addView(superview);
        return rootView;
    }
    public void setChangesInFavoritesListView() {
        if (FavoritesListFragment.this.isVisible()) {
            int position = mAdapter.getGuidOfDeletedSite();

            mWebSites.remove(position);
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
        }
    }
    public void removeRowFromFavoritesListView( int position) {
        if (FavoritesListFragment.this.isVisible()) {
            mWebSites.remove(position);
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
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Favorites List");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}


