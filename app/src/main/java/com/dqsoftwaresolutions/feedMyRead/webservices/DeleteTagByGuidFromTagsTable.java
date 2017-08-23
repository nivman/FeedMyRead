package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

public class DeleteTagByGuidFromTagsTable {
    private final ContentValues contentValues = new ContentValues();
    private final String mSiteGuid;
    private final String mTagName;
    private final Context mContext;
    private final Utils mUtils;

    public DeleteTagByGuidFromTagsTable(Context context,String tagName, String siteGuid) {
        mSiteGuid = siteGuid;
        mContext = context;
        mTagName=tagName;
        mUtils=new Utils(mContext);
        DeleteTagByGuidFromTagsTable.DeleteTagFromTagsServerTable sDataInServer=  new DeleteTagByGuidFromTagsTable.DeleteTagFromTagsServerTable(mContext);
        sDataInServer.execute((Void) null);
    }
    private class DeleteTagFromTagsServerTable extends WebServiceTask{
        private DeleteTagFromTagsServerTable(Context mContext) {
            super(mContext);
            String token = mUtils.getUserToken();
            contentValues.put(Constants.TAG_NAME, mTagName);
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.DELETE_TAGS_BY_GUID_FROM_TAG_TABLE,
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
