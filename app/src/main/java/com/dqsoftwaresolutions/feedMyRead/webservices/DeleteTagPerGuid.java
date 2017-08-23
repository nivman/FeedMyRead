package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

import java.util.List;

public class DeleteTagPerGuid {

    private final String mSiteGuid;

    private final Context mContext;
    private final ContentValues contentValues = new ContentValues();
    private final long mUpDateChangeTime;
    //  private ContentResolver mContentResolver;
    private final Utils mUtils;

    public DeleteTagPerGuid(Context context, List<String> tagsName, String siteGuid, long upDateChangeTime) {

        mSiteGuid = siteGuid;
        mContext = context;
        //  mContentResolver = mContext.getContentResolver();
        mUpDateChangeTime = upDateChangeTime;
        mUtils = new Utils(mContext);
        String token = mUtils.getUserToken();
        extractTagNameFromList(tagsName);
        DeleteTagPerGuid.SetTagStatusInWebSitesTable sDataInServer = new DeleteTagPerGuid.SetTagStatusInWebSitesTable(mContext, token);
        sDataInServer.execute((Void) null);
    }

    private void extractTagNameFromList(List<String> tagsName) {
        String token = mUtils.getUserToken();
        StringBuilder tagsNameArray = new StringBuilder(tagsName.size());
        for (String value : tagsName) {
            if (tagsNameArray.length() > 0) {
                tagsNameArray.append(",");
            }
            tagsNameArray.append(value);
        }
        tagsNameArray.insert(0, "[");
        tagsNameArray.append("]");
        String removeFirstAndLastChar = String.valueOf(tagsNameArray);
        removeFirstAndLastChar = removeFirstAndLastChar.substring(1, removeFirstAndLastChar.length() - 1);
        DeleteTagPerGuid.SaveTagInTagsTable mSaveDataInServer = new DeleteTagPerGuid.SaveTagInTagsTable(mContext, removeFirstAndLastChar, token);
        mSaveDataInServer.execute((Void) null);
    }

    private class SaveTagInTagsTable extends WebServiceTask {
        private SaveTagInTagsTable(Context mContext, String tagsName, String token) {
            super(mContext);
            contentValues.put(Constants.TAG_NAME, tagsName);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);
            DeleteTagFromTagsTable deleteTagFromTagsTable = new DeleteTagPerGuid.DeleteTagFromTagsTable(mContext,tagsName, token);
            deleteTagFromTagsTable.execute((Void) null);
        }

        @Override
        public void showProgress() {

        }

        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.DELETE_TAG_PAR_GUID,
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

    private class SetTagStatusInWebSitesTable extends WebServiceTask {
        private SetTagStatusInWebSitesTable(Context mContext, String token) {
            super(mContext);

            contentValues.put(Constants.IS_TAG_EXISTS, "false");
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);
            contentValues.put(Constants.CHANGE_TIME, mUpDateChangeTime);
        }

        @Override
        public void showProgress() {

        }

        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.SET_TAGS_FLAG_IN_WEBSITE_TABLE,
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

    private class DeleteTagFromTagsTable extends WebServiceTask {
        private DeleteTagFromTagsTable(Context mContext, String tagName, String token) {
            super(mContext);
            contentValues.put(Constants.TAG_NAME, tagName);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);

        }

        @Override
        public void showProgress() {

        }

        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.DELETE_TAG_FROM_TAG_TABLE,
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
}
