package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.test.espresso.core.deps.guava.collect.Lists;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class SearchListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = SearchListFragment.class.getSimpleName();
    private WebSitesCustomAdapter mAdapter;
    private static final int LOADER__QUERY_ID = 1;
    private static final int LOADER__TAGS_ID = 2;
    private ContentResolver mContentResolver;
    private List<WebSite> mWebSites;
    private WebSitesLoader mWebSitesLoader;
    private TagsLoader mTagsLoader;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER__QUERY_ID: {
                String selection = WebSitesContract.WebSitesColumns.SITE_TITLE + " LIKE ?";
                String[] selectionArgs = {"%" + args.getString("search") + "%"};
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

    public void setChangesInSearchListView() {
        if (SearchListFragment.this.isVisible()) {
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
                    Log.d(LOG_TAG, e.toString());
                }
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        List<WebSite> webSiteEntries = new ArrayList<>();
        List<TagName> tagsEntries = new ArrayList<>();
        switch (loader.getId()) {
            case LOADER__QUERY_ID: {
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
                break;
            }
            case LOADER__TAGS_ID: {
                setTagsInWebView();
            }
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = this.getArguments();
        mContentResolver = getActivity().getContentResolver();
        mAdapter = new WebSitesCustomAdapter(getActivity(), SearchListFragment.this);
        mTagsLoader = new TagsLoader(getContext(), mContentResolver);
        mWebSitesLoader = new WebSitesLoader(getContext(), mContentResolver);
        setEmptyText("No WebSites");
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER__TAGS_ID, null, SearchListFragment.this);
        getLoaderManager().initLoader(LOADER__QUERY_ID, bundle, SearchListFragment.this);
        setHasOptionsMenu(true);
        setListShown(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.search_container);
        listContainer.addView(superview);

        return rootView;
    }

    private void getSearchResult(String query) {
        String selection = WebSitesContract.WebSitesColumns.SITE_TITLE + " LIKE ?";
        String[] selectionArgs = {"%" + query + "%"};
        String[] projection = mWebSitesLoader.getProjection();
        List<WebSite> entries = new ArrayList<>();
        Cursor cursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, selection, selectionArgs, null);
        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {
                    WebSite webSite = mWebSitesLoader.getWebSiteData(cursor);
                    entries.add(webSite);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        mAdapter = new WebSitesCustomAdapter(getActivity(), SearchListFragment.this);
        mTagsLoader = new TagsLoader(getContext(), mContentResolver);
        setEmptyText("No WebSites");
        mAdapter.setData(entries);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER__TAGS_ID, null, SearchListFragment.this);
        setHasOptionsMenu(true);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }

    }

    public void displayFavoritesStarListView(int position) {

        if (SearchListFragment.this.isVisible()) {
            mAdapter.notifyDataSetChanged();
            if (mWebSites.get(position).getSiteFavorite().equals("false")) {
                mWebSites.get(position).setSiteFavorite("true");
            } else {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.search_view);
        final SearchView mSearchView = (SearchView) searchItem.getActionView();
        searchItem.expandActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSearchView, InputMethodManager.SHOW_IMPLICIT);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                getSearchResult(newText);
                return true;
            }

        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // finish();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.delete_all:
                //  deleteAll();
                return true;
            case R.id.restore_all:
                // restoreAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Search List");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }

}


