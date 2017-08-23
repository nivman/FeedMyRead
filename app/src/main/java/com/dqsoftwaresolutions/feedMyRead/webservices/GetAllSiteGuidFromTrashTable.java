package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.database.TrashContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.dqsoftwaresolutions.feedMyRead.database.TrashContract.TrashColumns.SITE_GUID;
import static com.dqsoftwaresolutions.feedMyRead.database.TrashContract.URI_TABLE;

public class GetAllSiteGuidFromTrashTable {
    private final ContentValues contentValues = new ContentValues();


    private final String mToken;
    private final Context mContext;

    private final ContentResolver mContentResolver;
    private final WebSitesLoader mWebSitesLoader;
  //  private TagsLoader mTagsLoader;
    public GetAllSiteGuidFromTrashTable(Context context, String token) {
        mContext=context;
        mWebSitesLoader= new WebSitesLoader(mContext);
        mToken=token;
        mContentResolver = mContext.getContentResolver();
     //   mTagsLoader = new TagsLoader(mContext);

        GetAllSiteGuidFromTrashTable.GetAllSitesGuidFromWebSitesTable getAllSitesGuidFromWebSitesTable=  new GetAllSiteGuidFromTrashTable.GetAllSitesGuidFromWebSitesTable(mContext);
        getAllSitesGuidFromWebSitesTable.execute((Void) null);

    }

    private class GetAllSitesGuidFromWebSitesTable extends WebServiceTask{
        private GetAllSitesGuidFromWebSitesTable(Context mContext) {
            super(mContext);
            contentValues.put(Constants.USER_TOKEN, mToken);

        }

        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
            JSONArray obj = WebServiceUtils.requestJSONArray(Constants.GET_ALL_SITE_GUID_FROM_TRASH_TABLE,
                    WebServiceUtils.METHOD.POST,
                    contentValues,mContext);

            if(obj!=null){
                GetAllSiteGuidFromTrashTable.CompareSiteGuidFromWebSitesTable  updateWebSitesDataFromServer=  new GetAllSiteGuidFromTrashTable.CompareSiteGuidFromWebSitesTable(obj);
                updateWebSitesDataFromServer.execute(obj);
            }

            return false;
        }
        @Override
        public void performSuccessfulOperation() {

        }

        @Override
        public void hideProgress() {

        }
    }

    public class CompareSiteGuidFromWebSitesTable extends AsyncTask<JSONArray, Integer, List<String>> {

        private final JSONArray mTagsDetailsArray;
        private final ArrayList<String> tagSiteGuidFromServerArr =new ArrayList<>();
        private final ArrayList<String> siteGuidFromClientArr =new ArrayList<>();
        public CompareSiteGuidFromWebSitesTable(JSONArray obj) {
            mTagsDetailsArray=obj;

        }
        @Override
        protected List<String> doInBackground(JSONArray... params) {
            if (params == null) {
                return null;
            }
            for(int i=0;i<mTagsDetailsArray.length();++i){
                try {
                    JSONObject json= new JSONObject(mTagsDetailsArray.get(i).toString());
                    String tagSiteGuidFromServer = (String) json.get("siteGuid");
                    tagSiteGuidFromServerArr.add(tagSiteGuidFromServer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String[] projection = mWebSitesLoader.getProjection();
            assert mContentResolver != null;
            Cursor cursor = mContentResolver.query(URI_TABLE, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String siteGuidFromClient = cursor.getString(cursor.getColumnIndex(TrashContract.TrashColumns.SITE_GUID));
                        siteGuidFromClientArr.add(siteGuidFromClient);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            Collection<String> listFromServerArr = tagSiteGuidFromServerArr;
            Collection<String> listFromClientArr =siteGuidFromClientArr;
            List<String> sourceList = new ArrayList<>(listFromServerArr);
            List<String> destinationList = new ArrayList<>(listFromClientArr);
            sourceList.removeAll( listFromClientArr );
            destinationList.removeAll( listFromServerArr );
            return  destinationList;

        }
        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            for(String guid:result){
                Uri uri = TrashContract.Trash.buildTrashUri((String.valueOf(guid)));
                mContentResolver.delete(uri, SITE_GUID + " = ?", new String[]{String.valueOf(guid)});

            }
        }
    }
}
