package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

public class DeleteAllTrashFromTrashTable {
    private final ContentValues contentValues = new ContentValues();

    private final Context mContext;

    private final Utils mUtils;
    public DeleteAllTrashFromTrashTable(Context context) {

        mContext = context;
        mUtils=new Utils(mContext);
        DeleteAllTrashFromTrashTable.DeleteSiteFromServerTrashTable sDataInServer=  new DeleteAllTrashFromTrashTable.DeleteSiteFromServerTrashTable(mContext);
        sDataInServer.execute((Void) null);
    }
    private class DeleteSiteFromServerTrashTable extends WebServiceTask{
        private DeleteSiteFromServerTrashTable(Context mContext) {
            super(mContext);
            String token = mUtils.getUserToken();
            contentValues.put(Constants.USER_TOKEN, token);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
            WebServiceUtils.requestJSONObject(Constants.DELETE_ALL_SITES_FROM_TRASH_TABLE,
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
