package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

import java.util.List;

public class UpdateDataInServer {

    private final String mSiteGuid;
    private final Context mContext;
    private final ContentValues contentValues = new ContentValues();
    private final long mUpDateChangeTime;
    private final Utils mUtils;

    public UpdateDataInServer(Context context, List<String> tagsName, String siteGuid, long upDateChangeTime) {

        mSiteGuid = siteGuid;
        mContext = context;
        mUpDateChangeTime = upDateChangeTime;
        mUtils = new Utils(mContext);
        String token = mUtils.getUserToken();
        extractTagNameFromList(tagsName);
        UpdateDataInServer.SetTagStatusInWebSitesTable sDataInServer = new UpdateDataInServer.SetTagStatusInWebSitesTable(mContext, token);
        sDataInServer.execute((Void) null);
    }

    public UpdateDataInServer(Context context, String siteGuid, long upDateChangeTime) {

        mSiteGuid = siteGuid;
        mContext = context;
        mUpDateChangeTime = upDateChangeTime;
        mUtils = new Utils(mContext);
        String token = mUtils.getUserToken();
        UpdateDataInServer.SetTagStatusInWebSitesTable mSaveDataInServer = new UpdateDataInServer.SetTagStatusInWebSitesTable(mContext, token);
        mSaveDataInServer.execute((Void) null);
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
        UpdateDataInServer.SaveTagInTagsTable mSaveDataInServer = new UpdateDataInServer.SaveTagInTagsTable(mContext, removeFirstAndLastChar, token);
        mSaveDataInServer.execute((Void) null);
    }

    private class SaveTagInTagsTable extends WebServiceTask {
        private SaveTagInTagsTable(Context mContext, String tagsName, String token) {
            super(mContext);
            contentValues.put(Constants.TAG_NAME, tagsName);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);

        }

        @Override
        public void showProgress() {

        }

        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.SET_TAGS_IN_SERVER,
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

            contentValues.put(Constants.IS_TAG_EXISTS, "true");
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
