package com.dqsoftwaresolutions.feedMyRead.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteSiteFromTrashTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.RestoreAllSitesFromTrashTable;

import java.util.ArrayList;
import java.util.List;

public class TrashLoader extends AsyncTaskLoader<List<WebSite>> {
    private static final String LOG_TAG = TrashLoader.class.getSimpleName();
    private List<WebSite> mWebSites;
    private ContentResolver mContentProvider;
    private Cursor mCursor;
    private final String[] projection = {BaseColumns._ID,
            TrashContract.TrashColumns.SITE_TITLE,
            TrashContract.TrashColumns.SITE_URL,
            TrashContract.TrashColumns.SITE_IMG_URL,
            TrashContract.TrashColumns.SITE_FAVORITE,
            TrashContract.TrashColumns.SITE_FAVICON,
            TrashContract.TrashColumns.SITE_TAGS,
            TrashContract.TrashColumns.SITE_CONTENT,
            TrashContract.TrashColumns.SITE_GUID,
            TrashContract.TrashColumns.TOKEN};
    public TrashLoader(Context context, ContentResolver contentResolver) {
        super(context);

        mContentProvider = contentResolver;

    }
    @Override
    public List<WebSite> loadInBackground() {

        List<WebSite> entries = new ArrayList<>();

        mCursor = mContentProvider.query(TrashContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    WebSite webSite = getWebSiteData(mCursor);
                    entries.add(webSite);

                } while (mCursor.moveToNext());
            }
        }
        assert mCursor != null;
        mCursor.close();

        return entries;
    }

    @Override
    public void deliverResult(List<WebSite> webSites) {
        super.deliverResult(webSites);
        if (isReset()) {
            if (webSites != null) {
                mCursor.close();
            }
        }
        List<WebSite> oldWebSites = mWebSites;
        if (mWebSites == null || mWebSites.size() == 0) {
            Log.d(LOG_TAG, "+++++++++++ No Data returned form Trash");

        }
        mWebSites = webSites;
        if (isStarted()) {
            super.deliverResult(webSites);
            mCursor.close();
        }
        if (oldWebSites != null && oldWebSites != webSites) {
            mCursor.close();
        }

    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if(mWebSites != null){
            deliverResult(mWebSites);
        }
        if(takeContentChanged() || mWebSites == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mCursor != null){
            mCursor.close();
        }
        mWebSites= null;
    }

    @Override
    public void onCanceled(List<WebSite> webSites) {
        super.onCanceled(webSites);
        if(mCursor != null){
            mCursor.close();
        }
    }

    public void restoreWebSite(String guid) {

        WebSite webSite = new WebSite();
        String selection = TrashContract.TrashColumns.SITE_GUID + " =?";
        String[] selectionArgs = {guid};

        mContentProvider = getContext().getContentResolver();

        assert mContentProvider != null;
        Cursor mCursor = mContentProvider.query(TrashContract.URI_TABLE, projection, selection, selectionArgs, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    webSite=  getWebSiteData(mCursor);
                } while (mCursor.moveToNext());
            }
            ContentValues values = putRestoreValue(webSite);
            ContentResolver cr = getContext().getContentResolver();
            Uri uri = WebSitesContract.URI_TABLE;
            assert cr != null;
            cr.insert(uri, values);
            Uri uriToRestore = TrashContract.Trash.buildTrashUri(String.valueOf(webSite.get_id()));
            ContentResolver contentResolver = getContext().getContentResolver();
            String deleteSelection = TrashContract.TrashColumns.SITE_GUID + " =?";
            String[] deleteSelectionArgs = {guid};
            contentResolver.delete(uriToRestore, deleteSelection, deleteSelectionArgs);
            mCursor.close();
        }
        new RestoreAllSitesFromTrashTable(webSite.getSiteTitle(), webSite.getSiteUrl(), webSite.getSiteImgUrl(), webSite.getSiteFavorite(), webSite.getSiteContent(),guid,getContext());
    }
    private WebSite getWebSiteData(Cursor cursor) {
        int _id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        String siteTitle = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_TITLE));
        String siteUrl = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_URL));
        String siteImgUrl = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_IMG_URL));
        String siteFavorite = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_FAVORITE));
        String siteFavicon = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_FAVICON));
        String siteTags = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_TAGS));
        String siteContent = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_CONTENT));
        String siteGuid = cursor.getString(cursor.getColumnIndex(WebSitesContract.WebSitesColumns.SITE_GUID));
        return new WebSite(_id, siteTitle, siteUrl, siteImgUrl, siteFavorite, siteTags, siteContent, siteFavicon, siteGuid);
    }
    private ContentValues putRestoreValue(WebSite webSite){
        ContentValues values = new ContentValues();
        values.put(WebSitesContract.WebSitesColumns.SITE_TITLE, webSite.getSiteTitle());
        values.put(WebSitesContract.WebSitesColumns.SITE_URL, webSite.getSiteUrl());
        values.put(WebSitesContract.WebSitesColumns.SITE_IMG_URL, webSite.getSiteImgUrl());
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, webSite.getSiteFavorite());
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVICON, webSite.getSiteFavicon());
        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, webSite.getSiteTags());
        values.put(WebSitesContract.WebSitesColumns.SITE_CONTENT, webSite.getSiteContent());
        values.put(WebSitesContract.WebSitesColumns.SITE_GUID, webSite.getSiteGuid());
        values.put(WebSitesContract.WebSitesColumns.TOKEN, "token");

        return values;
    }

    public void restoreAllWebSites() {
        WebSite webSite;

        mContentProvider = getContext().getContentResolver();
        assert mContentProvider != null;
        Cursor mCursor = mContentProvider.query(TrashContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    webSite=  getWebSiteData(mCursor);
                    ContentValues values = putRestoreValue(webSite);
                    new RestoreAllSitesFromTrashTable(webSite.getSiteTitle(), webSite.getSiteUrl(), webSite.getSiteImgUrl(), webSite.getSiteFavorite(), webSite.getSiteContent(),webSite.getSiteGuid(),getContext());
                    ContentResolver cr = getContext().getContentResolver();
                    Uri uri = WebSitesContract.URI_TABLE;


                    cr.insert(uri, values);
                    Uri uriToRestore = TrashContract.Trash.buildTrashUri(String.valueOf(webSite.get_id()));
                    ContentResolver contentResolver = getContext().getContentResolver();
                    assert contentResolver != null;
                    contentResolver.delete(uriToRestore, null, null);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
    }

    public void deleteAllWebSites() {
        WebSite webSite;
        mContentProvider = getContext().getContentResolver();
        assert mContentProvider != null;
        Cursor mCursor = mContentProvider.query(TrashContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    webSite=  getWebSiteData(mCursor);
                    new DeleteSiteFromTrashTable(getContext(),webSite.getSiteGuid());
                    Uri uriToRestore = TrashContract.Trash.buildTrashUri(String.valueOf(webSite.get_id()));
                    ContentResolver contentResolver = getContext().getContentResolver();
                    assert contentResolver != null;
                    contentResolver.delete(uriToRestore, null, null);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
    }
}
