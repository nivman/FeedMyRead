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
import com.dqsoftwaresolutions.feedMyRead.webservices.MoveSiteToTrashServerTable;

import java.util.ArrayList;
import java.util.List;

public class WebSitesLoader extends AsyncTaskLoader<List<WebSite>> {
    private static final String LOG_TAG = WebSitesLoader.class.getSimpleName();
    private List<WebSite> mWebSites;
    private ContentResolver mContentResolver;
    private Cursor mCursor;

    public WebSitesLoader(Context context) {
        super(context);
    }

    public String[] getProjection() {
        return projection;
    }

    private final  String[] projection = {BaseColumns._ID,
            WebSitesContract.WebSitesColumns.SITE_TITLE,
            WebSitesContract.WebSitesColumns.SITE_URL,
            WebSitesContract.WebSitesColumns.SITE_IMG_URL,
            WebSitesContract.WebSitesColumns.SITE_FAVORITE,
            WebSitesContract.WebSitesColumns.SITE_FAVICON,
            WebSitesContract.WebSitesColumns.SITE_TAGS,
            WebSitesContract.WebSitesColumns.SITE_CONTENT,
            WebSitesContract.WebSitesColumns.SITE_GUID,
            WebSitesContract.WebSitesColumns.TOKEN};

    public WebSitesLoader(Context context, ContentResolver contentResolver) {
        super(context);

        mContentResolver = contentResolver;

    }

    @Override
    public List<WebSite> loadInBackground() {

        List<WebSite> entries = new ArrayList<>();

        mCursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    WebSite webSite = getWebSiteData(mCursor);
                    entries.add(webSite);

                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }


        return entries;
    }

    @Override
    public void deliverResult(List<WebSite> webSites) {

        if (isReset()) {
            if (webSites != null) {
                mCursor.close();
            }
        }
        List<WebSite> oldWebSites = mWebSites;
        if (mWebSites == null || mWebSites.size() == 0) {
            Log.d(LOG_TAG, "+++++++++++ No Data returned form WebSites");

        }
        mWebSites = webSites;
        if (isStarted()) {
            super.deliverResult(webSites);
        }
        if (oldWebSites != null && oldWebSites != webSites) {
            mCursor.close();
        }

    }

    @Override
    protected void onStartLoading() {
        if (mWebSites != null) {
            deliverResult(mWebSites);
        }
        if (takeContentChanged() || mWebSites == null) {
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
        if (mCursor != null) {
            mCursor.close();
        }
        mWebSites = null;
    }

    @Override
    public void onCanceled(List<WebSite> webSites) {
        super.onCanceled(webSites);
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }
    public void moveToTrash(String guid) {
        WebSite webSite = new WebSite();
        String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " =?";
        String[] selectionArgs = {guid};
        mContentResolver = getContext().getContentResolver();
        assert mContentResolver != null;
        Cursor mCursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, selection, selectionArgs, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                     webSite=  getWebSiteData(mCursor);
                } while (mCursor.moveToNext());
            }
            ContentValues values = new ContentValues();
            values.put(TrashContract.TrashColumns.SITE_TITLE, webSite.getSiteTitle());
            values.put(TrashContract.TrashColumns.SITE_URL, webSite.getSiteUrl());
            values.put(TrashContract.TrashColumns.SITE_IMG_URL, webSite.getSiteImgUrl());
            values.put(TrashContract.TrashColumns.SITE_FAVORITE, webSite.getSiteFavorite());
            values.put(TrashContract.TrashColumns.SITE_FAVICON, webSite.getSiteFavicon());
            values.put(TrashContract.TrashColumns.SITE_TAGS, webSite.getSiteTags());
            values.put(TrashContract.TrashColumns.SITE_CONTENT, webSite.getSiteContent());
            values.put(TrashContract.TrashColumns.SITE_GUID, webSite.getSiteGuid());
            values.put(TrashContract.TrashColumns.TOKEN, "token");
            ContentResolver cr = getContext().getContentResolver();
            Uri uri = TrashContract.URI_TABLE;

            assert cr != null;
            cr.insert(uri, values);
            Uri uriToTrash = WebSitesContract.WebSites.buildWebSitesUri(String.valueOf(webSite.get_id()));
            ContentResolver contentResolver = getContext().getContentResolver();

            assert contentResolver != null;
            contentResolver.delete(uriToTrash, null, null);
            new MoveSiteToTrashServerTable(webSite.getSiteTitle(), webSite.getSiteUrl(), webSite.getSiteImgUrl(), webSite.getSiteFavorite(), webSite.getSiteContent(),webSite.getSiteGuid(),getContext());
            mCursor.close();
        }
    }
    public List<WebSite> getAllWebSiteForTagNameWithAssociationGuid(List<String> siteGuid){
        String[] strarray = siteGuid.toArray(new String[0]);
        String webSiteSelection = WebSitesContract.WebSitesColumns.SITE_GUID + " in (";
        for (String aStrarray : strarray) {
            webSiteSelection += "?, ";
        }
        webSiteSelection = webSiteSelection.substring(0, webSiteSelection.length() - 2) + ")";

        List<WebSite> entries = new ArrayList<>();
        mCursor = mContentResolver.query(WebSitesContract.URI_TABLE, projection, webSiteSelection, strarray, null);
        if (mCursor != null) {

            if (mCursor.moveToFirst()) {
                do {
                    WebSite webSite =getWebSiteData(mCursor);
                    entries.add(webSite);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        return entries;
    }

    public WebSite getWebSiteData(Cursor cursor) {
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
}
