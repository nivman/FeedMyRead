package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.database.UserContract;

import java.util.TimeZone;

public class SetFavoriteStatus {
    private final ContentValues contentValues = new ContentValues();
    private final String mSiteGuid;
    private final String mFavorite;
    private final Context mContext;

    public SetFavoriteStatus(Context context,String favorite, String siteGuid) {
        mSiteGuid = siteGuid;
        mContext = context;
        mFavorite=favorite;
        SetFavoriteStatus.SetFavoriteStatusInServer setFavoriteStatusInServer=  new SetFavoriteStatus.SetFavoriteStatusInServer(mContext);
        setFavoriteStatusInServer.execute((Void) null);
    }
    private class SetFavoriteStatusInServer extends WebServiceTask{
        private SetFavoriteStatusInServer(Context mContext) {
            super(mContext);
            long upDateChangeTime = setTimeChange();
            contentValues.put(Constants.WEB_SITE_FAVORITE, mFavorite);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, getUserToken());
            contentValues.put(Constants.CHANGE_TIME,upDateChangeTime);
            new SetNewTimeInUserTable(mContext,getUserToken(),upDateChangeTime);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
             WebServiceUtils.requestJSONObject(Constants.SET_FAVORITE_STATUS_IN_SERVER,
                    WebServiceUtils.METHOD.POST,
                    contentValues, mContext);

            return false;
        }
        @Override
        public void performSuccessfulOperation() {

        }

        @Override
        public void hideProgress() {

        }
    }
    private long setTimeChange(){
        TimeZone london = TimeZone.getTimeZone("Europe/London");
        long now = System.currentTimeMillis();
        return now + london.getOffset(now);
    }
    private String getUserToken(){
        String token = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] projection = {UserContract.UserColumns.TOKEN};
        assert contentResolver != null;
        Cursor cursor = contentResolver.query(UserContract.URI_TABLE,projection,null,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                do{
                    token = cursor.getString(cursor.getColumnIndex(UserContract.UserColumns.TOKEN));
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
        return token;
    }
}
