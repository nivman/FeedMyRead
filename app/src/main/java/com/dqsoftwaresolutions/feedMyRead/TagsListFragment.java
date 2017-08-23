package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteTagFromTagsTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.UpdateDataInServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_NAME;

public class TagsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<TagName>>{

    private TagsCustomAdapter mAdapter;
    private ContentResolver mContentResolver;
    private final List<TagName> tagsNameList = new ArrayList<>();
    private TagsLoader mTagsLoader;
    private Utils mUtils;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentResolver = getActivity().getContentResolver();
        mAdapter = new TagsCustomAdapter(getActivity(), TagsListFragment.this,tagsNameList);
        setEmptyText("No Tags");
        setListAdapter(mAdapter);
        setListShown(false);
        int LOADER__TAGS_ID = 1;
        getLoaderManager().initLoader(LOADER__TAGS_ID,null,this);
        setHasOptionsMenu(true);
        mTagsLoader = new TagsLoader(getContext(), mContentResolver);
        mUtils=new Utils(getContext());
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
    @Override
    public Loader<List<TagName>> onCreateLoader(int id, Bundle args) {
        return new TagsLoader(getActivity(), mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<TagName>> loader, List<TagName> tagNameList) {
        if (tagNameList.size() > 0) {
            Collections.sort(tagNameList, new Comparator<TagName>() {
                @Override
                public int compare(final TagName tagOne, final TagName tagTwo) {
                    return tagOne.getTagName().compareTo(tagTwo.getTagName());
                }
            });
        }
        final Set<String> filterDuplicateTagsName = new HashSet<>();
        List<TagName> uniqueTagsNameList = new ArrayList<>();
        for (TagName tag : tagNameList) {
            if (filterDuplicateTagsName.add(tag.getTagName())) {
                uniqueTagsNameList.add(tag);
            }
        }
        mAdapter.setTagsData(uniqueTagsNameList);
        if(isResumed()){
            setListShown(true);
        }else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<TagName>> loader) {
        mAdapter.setTagsData(null);
    }
    void showTagSortResult(final ArrayList<WebSite> listOfWebSite){
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable r=new Runnable() {
            public void run() {
                try {
                    Intent intent = new Intent(getActivity().getBaseContext(),MainListActivity.class);
                    intent.putParcelableArrayListExtra("webSiteList",listOfWebSite);
                    getActivity().startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        handler.postDelayed(r, 10);
    }
    public void deleteTagFromDataBase(String tagToDelete) {
        tagCounterPerWebSites(tagToDelete);
        Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(tagToDelete)));
        mContentResolver.delete(uri, TAGS_NAME + " = ?", new String[]{String.valueOf(tagToDelete)});
         new DeleteTagFromTagsTable(getContext(), tagToDelete);
    }
    private void tagCounterPerWebSites(String tagToDelete) {
        List<String> siteWithTags = new ArrayList<>();
        TagsLoader tagsLoader = new TagsLoader(getContext());
        String[] projection = tagsLoader.getProjection();
        String selection = TagsContract.TagsColumns.TAGS_NAME + " = ?";
        String[] selectionArgs = new String[]{tagToDelete};
        Cursor cursor = getContext().getContentResolver().query(TagsContract.URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String siteGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    siteWithTags.add(siteGuid);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        for (String siteGuid : siteWithTags) {
            checkHowManyTagLeft(siteGuid);
        }
    }
    private void checkHowManyTagLeft(String siteGuid) {
        String[] projection = new String[]{"count(*)"};
        String selection = TagsContract.TagsColumns.TAGS_SITE_GUID + " = ?";
        String[] selectionArgs = new String[]{siteGuid};
        Cursor cursor = getContext().getContentResolver().query(TagsContract.URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        assert cursor != null;
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        if (result == 1) {
            ContentValues valuesToWebSiteTable = createContentValuesForWebSitesTable();
            saveTagsInWebSiteTable(valuesToWebSiteTable, siteGuid);
        }
        cursor.close();
    }
    private ContentValues createContentValuesForWebSitesTable() {

        ContentValues values = new ContentValues();
        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, String.valueOf("false"));
        return values;
    }
    private void saveTagsInWebSiteTable(ContentValues values, String siteGuid) {

        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = Uri.parse(WebSitesContract.BASE_CONTENT_URI + "/websites");
        String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " = ?";
        String[] selectionArg = {siteGuid};
        assert contentResolver != null;
        contentResolver.update(uri, values, selection, selectionArg);
        long upDateChangeTime=mUtils.setTimeChange();
        new UpdateDataInServer(getContext(), siteGuid, upDateChangeTime);
    }
    public void setUpdateTagList(){

        mTagsLoader = new TagsLoader(getActivity(),mContentResolver);
        List<TagName> tagsList=  mTagsLoader.loadInBackground();
        filterDuplicateTagsName(tagsList);
    }
    private void filterDuplicateTagsName(List<TagName> tagNames) {

        if (tagNames.size() > 0) {
            Collections.sort(tagNames, new Comparator<TagName>() {
                @Override
                public int compare(final TagName tagOne, final TagName tagTwo) {
                    return tagOne.getTagName().compareTo(tagTwo.getTagName());
                }
            });
        }

        final Set<String> filterDuplicateTagsName = new HashSet<>();
        List<TagName> uniqueTagsNameList = new ArrayList<>();
        for (TagName tag : tagNames) {
            if (filterDuplicateTagsName.add(tag.getTagName())) {
                uniqueTagsNameList.add(tag);

            }
        }
        mAdapter.setTagsData(uniqueTagsNameList);

    }

    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Tags List");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}


