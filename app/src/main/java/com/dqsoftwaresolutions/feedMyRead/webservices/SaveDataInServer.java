package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.Utils;

import java.util.ArrayList;

public class SaveDataInServer {
    private final ArrayList<String> mSiteDetails;
    private final ContentValues contentValues = new ContentValues();
    private final Utils mUtils;
    private final Context mContext;
    public SaveDataInServer(ArrayList<String> siteDetails,Context context) {
        this.mSiteDetails=siteDetails;

         mContext = context;

        mUtils=new Utils(mContext);
        SaveDataInServer.SetWebServiceTask mSaveDataInServer=  new SaveDataInServer.SetWebServiceTask(mContext);
        mSaveDataInServer.execute((Void) null);

    }

    public class SetWebServiceTask extends WebServiceTask{
        public SetWebServiceTask(Context mContext) {
            super(mContext);
            String token =mUtils.getUserToken();
            long upDateChangeTime = mUtils.setTimeChange();
            contentValues.put(Constants.WEB_SITE_TITLE, mSiteDetails.get(0));
            contentValues.put(Constants.WEB_SITE_URL, mSiteDetails.get(1));
            contentValues.put(Constants.WEB_SITE_IMAGE, mSiteDetails.get(2));
            contentValues.put(Constants.WEB_SITE_FAVICON, mSiteDetails.get(3));
            contentValues.put(Constants.WEB_SITE_FAVORITE, "fav");
            contentValues.put(Constants.WEB_SITE_TAGS, "false");
            contentValues.put(Constants.WEB_SITE_CONTENT, mSiteDetails.get(6));
            contentValues.put(Constants.WEB_SITE_GUID, mSiteDetails.get(7));
            contentValues.put(Constants.USER_TOKEN, token);
            contentValues.put(Constants.CHANGE_TIME,upDateChangeTime);
            new SetNewTimeInUserTable(mContext,token,upDateChangeTime);
        }
        @Override
        public void showProgress() {

        }
        @Override
        public boolean performRequest() {
             WebServiceUtils.requestJSONObject(Constants.SEND_DATA_TO_SERVER,
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
