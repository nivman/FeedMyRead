package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

public class RestoreAllSitesFromTrashTable {
    private final String mTitle;
    private final String mUrl;
    private final String mImageSrc;
    private final String mFavicon;
    private final String mContent;
    private final String mGuid;
    private final ContentValues contentValues = new ContentValues();
    private final Context mContext;
    private final Utils mUtils;

    public RestoreAllSitesFromTrashTable(String title, String url, String imageSrc, String favicon, String content, String guid, Context context) {

        this.mTitle = title;
        this.mUrl = url;
        this.mImageSrc = imageSrc;
        this.mFavicon = favicon;

        this.mContent = content;
        this.mGuid = guid;
        mContext = context ;
        mUtils=new Utils(mContext);
       // mContentResolver = mContext.getContentResolver();
        RestoreAllSitesFromTrashTable.RestoreSiteFromTrashToMainTable restoreSiteFromTrashToMainTable=  new RestoreAllSitesFromTrashTable.RestoreSiteFromTrashToMainTable(mContext);
        restoreSiteFromTrashToMainTable.execute((Void) null);

    }

    private class RestoreSiteFromTrashToMainTable extends WebServiceTask{
        private RestoreSiteFromTrashToMainTable(Context context) {
            super(mContext);
            String token =mUtils.getUserToken();
            long upDateChangeTime = mUtils.setTimeChange();
            contentValues.put(Constants.WEB_SITE_TITLE, mTitle);
            contentValues.put(Constants.WEB_SITE_URL, mUrl);
            contentValues.put(Constants.WEB_SITE_IMAGE, mImageSrc);
            contentValues.put(Constants.WEB_SITE_FAVICON, mFavicon);
            contentValues.put(Constants.WEB_SITE_FAVORITE, "fav");
            contentValues.put(Constants.WEB_SITE_TAGS, "false");
            contentValues.put(Constants.WEB_SITE_CONTENT, mContent);
            contentValues.put(Constants.WEB_SITE_GUID, mGuid);
            contentValues.put(Constants.USER_TOKEN, token);
            contentValues.put(Constants.CHANGE_TIME, upDateChangeTime);
            new SetNewTimeInUserTable(context,token, upDateChangeTime);
        }
        @Override
        public void showProgress() {

        }
        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.RESTORE_ALL_SITES_FROM_TRASH_TABLE,
                    WebServiceUtils.METHOD.POST,
                    contentValues,mContext);
            return false;
        }
        @Override
        public void performSuccessfulOperation() {

        }

        @Override
        public void hideProgress() {

        }
    }

}
