package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

public class UpdateTagsNameTagsTable {
    private final ContentValues contentValues = new ContentValues();
    private final String mSiteGuid;
    private final String mOldTag;
    private final String mNewTag;
    private final Context mContext;
    private final Utils mUtils;
    public UpdateTagsNameTagsTable(Context context,String oldTag,String newTag, String siteGuid) {
        mSiteGuid = siteGuid;
        mContext = context;
        mOldTag=oldTag;
        mNewTag=newTag;
        mUtils=new Utils(mContext);
        UpdateTagsNameTagsTable.SetNewTagNameInServer setFavoriteStatusInServer=  new UpdateTagsNameTagsTable.SetNewTagNameInServer(mContext);
        setFavoriteStatusInServer.execute((Void) null);

    }
    private class SetNewTagNameInServer extends WebServiceTask{
        private SetNewTagNameInServer(Context mContext) {
            super(mContext);
            long upDateChangeTime = mUtils.setTimeChange();
           String token= mUtils.getUserToken();
            contentValues.put(Constants.TAG_NAME, mNewTag);
            contentValues.put(Constants.OLD_TAG_NAME,mOldTag);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);
            contentValues.put(Constants.CHANGE_TIME,upDateChangeTime);
            new SetNewTimeInUserTable(mContext,token,upDateChangeTime);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
             WebServiceUtils.requestJSONObject(Constants.UPDATE_TAGS_NAME_TAGS_TABLE,
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
