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

import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SortWebSitesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private List<WebSite> mWebSites;

    private WebSitesCustomAdapter mAdapter;
    private ContentResolver mContentResolver;
    private static final int LOADER__TAGS_ID=1;
    private TagsLoader mTagsLoader;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mWebSites = bundle.getParcelableArrayList("siteList");
        }
        mContentResolver = getActivity().getContentResolver();
        mTagsLoader = new TagsLoader(getContext(),mContentResolver);
        getLoaderManager().initLoader(LOADER__TAGS_ID,null,this);
        mAdapter = new WebSitesCustomAdapter(getActivity(), SortWebSitesListFragment.this);
        setEmptyText("No Web Sites");
        setListAdapter(mAdapter);
        setListShown(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_list_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.listcontainer);
        listContainer.addView(superview);

        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                TagsContract.URI_TABLE,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setTagsInWebView();
        mAdapter.setData(mWebSites);
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
        String tagName="";
        ArrayList<Hashtable> tagsArr= new ArrayList<>();
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

    public void setChangesInSortSiteListView() {
        if (SortWebSitesListFragment.this.isVisible()) {
            int position = mAdapter.getGuidOfDeletedSite();

            mWebSites.remove(position);
            mAdapter.notifyDataSetChanged();
            mAdapter.setData(mWebSites);
            if (isResumed()) {
                setListShown(true);
            } else {
                try {
                    setListShownNoAnimation(true);
                } catch (IllegalStateException e) {
                    Log.d("LOG_TAG", e.toString());
                }

            }
        }
    }
    public void displayFavoritesStarListView( int position) {

        if (SortWebSitesListFragment.this.isVisible()) {
            mAdapter.notifyDataSetChanged();
            if(mWebSites.get(position).getSiteFavorite().equals("false"))
            {
                mWebSites.get(position).setSiteFavorite("true");
            }else{
                mWebSites.get(position).setSiteFavorite("false");
            }


            mAdapter.setData(mWebSites);
            if (isResumed()) {
                setListShown(true);
            } else {
                try {
                    setListShownNoAnimation(true);
                } catch (IllegalStateException e) {
                    Log.d("LOG_TAG", e.toString());
                }
            }
        }

    }
    @Override
    public void onResume() {
        super.onResume();

        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Sort by Tag name");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
