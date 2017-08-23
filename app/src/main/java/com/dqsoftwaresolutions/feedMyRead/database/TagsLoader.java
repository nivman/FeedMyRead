package com.dqsoftwaresolutions.feedMyRead.database;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteTagByGuidFromTagsTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteTagFromTagsTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.UpdateTagsNameTagsTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_NAME;
import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.URI_TABLE;

public class TagsLoader extends AsyncTaskLoader<List<TagName>> {

    private static final String LOG_TAG = TagsLoader.class.getSimpleName();
    private List<TagName> mTagName;
    private ContentResolver mContentResolver;
    private Cursor mCursor;


    private final String[] projection = {BaseColumns._ID,
            TagsContract.TagsColumns.TAGS_NAME,
            TagsContract.TagsColumns.TAGS_SITE_GUID};

    public TagsLoader(Context context, ContentResolver contentResolver) {
        super(context);
        mContentResolver = contentResolver;
    }

    public TagsLoader(Context context) {
        super(context);
    }

    public TagsLoader(Activity context, ContentResolver contentResolver) {
        super(context);
        mContentResolver = contentResolver;
    }

    @Override
    public List<TagName> loadInBackground() {


        List<TagName> entries = new ArrayList<>();

        mCursor = mContentResolver.query(URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    String tagName = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    String tagSiteGuid = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    TagName tagNameObj = new TagName(_id, tagName, tagSiteGuid);

                    entries.add(tagNameObj);

                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }

        return entries;
    }

    @Override
    public void deliverResult(List<TagName> tagName) {

        if (isReset()) {
            if (tagName != null) {
                mCursor.close();
            }
        }
        List<TagName> oldtagName = mTagName;
        if (mTagName == null || mTagName.size() == 0) {
            Log.d(LOG_TAG, "+++++++++++ No Data returned");

        }
        mTagName = tagName;
        if (isStarted()) {
            super.deliverResult(tagName);
        }
        if (oldtagName != null && oldtagName != tagName) {
            mCursor.close();
        }

    }

    @Override
    protected void onStartLoading() {
        if (mTagName != null) {
            deliverResult(mTagName);
        }
        if (takeContentChanged() || mTagName == null) {
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
        mTagName = null;
    }

    @Override
    public void onCanceled(List<TagName> tagName) {
        super.onCanceled(tagName);
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public void removeLastTagEntries(ArrayList<String> tagList, String site_Guid) {
        if (tagList.size() == 0) {
            return;
        }
        String[] selectionArgs = tagList.toArray(new String[0]);
        String selection = TagsContract.TagsColumns.TAGS_NAME + " in (";
        for (String selectionArg : selectionArgs) {
            selection += "?, ";
        }
        selection = selection.substring(0, selection.length() - 2) + ")";
        ArrayList<String> tagToRemove = new ArrayList<>();
        Cursor mCursor = getContext().getContentResolver().query(URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String tagName = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    tagToRemove.add(tagName);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        Set<String> unique = new HashSet<>(tagToRemove);
        ArrayList<String> singlesTagList = new ArrayList<>();
        for (String key : unique) {
            int frequency = Collections.frequency(tagToRemove, key);
            new DeleteTagByGuidFromTagsTable(getContext(), key, site_Guid);
            if (frequency == 1) {
                singlesTagList.add(key);
            }
        }
        String[] singlesTagsList = singlesTagList.toArray(new String[0]);
        for (int i = 0; i < singlesTagsList.length; i++) {
            new DeleteTagFromTagsTable(getContext(), singlesTagsList[i], site_Guid);
            Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(singlesTagsList[i])));
            mContentResolver.delete(uri, TAGS_NAME + " = ?", new String[]{String.valueOf(singlesTagsList[i])});
        }
    }

    public void changeTagName(Context context, String originalTagName, String tagName) {
        ContentValues values = new ContentValues();
        values.put(TagsContract.TagsColumns.TAGS_NAME, tagName);
        String[] selectionArgs = {originalTagName};
        String selection = TagsContract.TagsColumns.TAGS_NAME + " =?";
        Cursor mCursor = getContext().getContentResolver().query(URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String tagGuid = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    values.put(TagsContract.TagsColumns.TAGS_SITE_GUID, tagGuid);
                    mContentResolver.update(Uri.parse(URI_TABLE + "/" + _id), values, selection, selectionArgs);
                    Log.d("TEST", String.valueOf(context));
                    new UpdateTagsNameTagsTable(context, originalTagName, tagName, tagGuid);
                } while (mCursor.moveToNext());
            }

            mCursor.close();
        }

    }

    public String[] getProjection() {
        return projection;
    }
}
