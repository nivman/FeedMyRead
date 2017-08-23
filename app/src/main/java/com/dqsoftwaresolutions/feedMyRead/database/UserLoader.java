package com.dqsoftwaresolutions.feedMyRead.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.data.User;

import java.util.ArrayList;
import java.util.List;

public class UserLoader extends AsyncTaskLoader<List<User>> {
    private static final String LOG_TAG = UserLoader.class.getSimpleName();
    private List<User> mUser;
    private final ContentResolver mContentProvider;
    private Cursor mCursor;

    public UserLoader(Context context, ContentResolver contentResolver) {
        super(context);
        mContentProvider = contentResolver;

    }

    @Override
    public List<User> loadInBackground() {
        String[] projection = {BaseColumns._ID,
                UserContract.UserColumns.USER_NAME,
                UserContract.UserColumns.PASSWORD,
                UserContract.UserColumns.CHANGE_USER_TIME,
                UserContract.UserColumns.TOKEN};
        List<User> entries = new ArrayList<>();

        mCursor = mContentProvider.query(UserContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    String userName = mCursor.getString(mCursor.getColumnIndex(UserContract.UserColumns.USER_NAME));
                    String password = mCursor.getString(mCursor.getColumnIndex(UserContract.UserColumns.PASSWORD));
                    long changeTime = mCursor.getLong(mCursor.getColumnIndex(UserContract.UserColumns.CHANGE_USER_TIME));
                    String token = mCursor.getString(mCursor.getColumnIndex(UserContract.UserColumns.TOKEN));
                    User user = new User(_id, userName, password, token,changeTime);
                    entries.add(user);

                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }

        return entries;
    }

    @Override
    public void deliverResult(List<User> user) {

        if (isReset()) {
            if (user != null) {
                mCursor.close();
            }
        }
        List<User> oldUser = mUser;
        if (mUser == null || mUser.size() == 0) {
            Log.d(LOG_TAG, "+++++++++++ No Data returned");

        }
        mUser = user;
        if (isStarted()) {
            super.deliverResult(user);
        }
        if (oldUser != null && oldUser != user) {
            mCursor.close();
        }

    }

    @Override
    protected void onStartLoading() {
        if(mUser != null){
            deliverResult(mUser);
        }
        if(takeContentChanged() || mUser == null){
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
        mUser= null;
    }

    @Override
    public void onCanceled(List<User> user) {
        super.onCanceled(user);
        if(mCursor != null){
            mCursor.close();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }
}
