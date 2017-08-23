package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.test.espresso.core.deps.guava.collect.Lists;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TrashLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteAllTrashFromTrashTable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;


public class TrashListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<WebSite>> {

    private TrashCustomAdapter mAdapter;
    private static final int LOADER_ID_TRASH = 2;
    private ContentResolver mContentResolver;
    private List<WebSite> mWebSites;
    public enum ARCHIVE_FRAGMENT_ACTION {
        RESTORE_ALL, DELETE_ALL
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentResolver = getActivity().getContentResolver();
        getLoaderManager().initLoader(LOADER_ID_TRASH, null, TrashListFragment.this);
        mAdapter = new TrashCustomAdapter(getActivity(), TrashListFragment.this);
        setEmptyText("No Web Sites");
        setListAdapter(mAdapter);
        setListShown(false);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.trash_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.trash_container);
        listContainer.findViewById(android.R.id.list);
        listContainer.addView(superview);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public Loader<List<WebSite>> onCreateLoader(int id, Bundle args) {

        return new TrashLoader(getContext(),  mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<WebSite>> loader, List<WebSite> trashSite) {
        mWebSites = Lists.reverse(trashSite);
        mAdapter.setData(mWebSites);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<WebSite>> loader) {
        mAdapter.setData(null);
    }

    public void removeRowFromTrashList(int position) {
        mWebSites.remove(position);
        mAdapter.notifyDataSetChanged();
        mAdapter.setData(mWebSites);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.trash_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.delete_all:
                googleAnalyticsAction(ARCHIVE_FRAGMENT_ACTION.DELETE_ALL);
                deleteAll();
                return true;
            case R.id.restore_all:
                googleAnalyticsAction(ARCHIVE_FRAGMENT_ACTION.RESTORE_ALL);
                restoreAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Archive List");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void deleteAll() {
        TrashLoader mTrashLoader = new TrashLoader(getContext(),  mContentResolver);
        mTrashLoader.deleteAllWebSites();
        new DeleteAllTrashFromTrashTable(getContext());
        setListAdapter(null);
    }

    private void restoreAll() {
        TrashLoader mTrashLoader = new TrashLoader(getContext(), mContentResolver);
        mTrashLoader.restoreAllWebSites();
        setListAdapter(null);
    }
    private void googleAnalyticsAction(ARCHIVE_FRAGMENT_ACTION action) {
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        switch (action) {
            case DELETE_ALL:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Delete all sites")
                        .build());
                break;
            case RESTORE_ALL:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Restore all sites")
                        .build());
                break;

        }
    }
}
