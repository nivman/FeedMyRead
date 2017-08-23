package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

public class DeleteSiteFromTrashTable {
    private final ContentValues contentValues = new ContentValues();
    private final String mSiteGuid;
    private final Context mContext;
    private final Utils mUtils;

    public DeleteSiteFromTrashTable(Context context,String siteGuid) {
        mSiteGuid = siteGuid;
        mContext = context;
        mUtils=new Utils(mContext);
        DeleteSiteFromTrashTable.DeleteSiteFromServerTrashTable sDataInServer=  new DeleteSiteFromTrashTable.DeleteSiteFromServerTrashTable(mContext);
        sDataInServer.execute((Void) null);
    }
    private class DeleteSiteFromServerTrashTable extends WebServiceTask{
        private DeleteSiteFromServerTrashTable(Context mContext) {
            super(mContext);
            String token = mUtils.getUserToken();
            long upDateChangeTime = mUtils.setTimeChange();
            contentValues.put(Constants.WEB_SITE_GUID, mSiteGuid);
            contentValues.put(Constants.USER_TOKEN, token);
            new SetNewTimeInUserTable(mContext,token,upDateChangeTime);

        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
           WebServiceUtils.requestJSONObject(Constants.DELETE_SITE_FROM_TRASH_TABLE,
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
