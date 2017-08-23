package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_NAME;
import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_SITE_GUID;
import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.URI_TABLE;

public class UpdateTagsForFromTagsTable {
    private final ContentValues contentValues = new ContentValues();


    private final String mToken;
    private final Context mContext;
    private final long mUpdatesTime;
    private final ContentResolver mContentResolver;
    private final String[] projection = {BaseColumns._ID,
            TagsContract.TagsColumns.TAGS_NAME,
            TAGS_SITE_GUID};
    public UpdateTagsForFromTagsTable(Context context, long updatesTime, String token) {
        mContext=context;
        new WebSitesLoader(mContext);
        mToken=token;
        mUpdatesTime=updatesTime;
        mContentResolver = mContext.getContentResolver();
        new TagsLoader(mContext);
        UpdateTagsForFromTagsTable.GetAllSitesGuidFromWebSitesTable getAllSitesGuidFromWebSitesTable=  new UpdateTagsForFromTagsTable.GetAllSitesGuidFromWebSitesTable(mContext);
        getAllSitesGuidFromWebSitesTable.execute((Void) null);

    }

    private class GetAllSitesGuidFromWebSitesTable extends WebServiceTask{
        private GetAllSitesGuidFromWebSitesTable(Context mContext) {
            super(mContext);
            contentValues.put(Constants.USER_TOKEN, mToken);
            contentValues.put(Constants.CHANGE_USER_TIME, mUpdatesTime);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
            JSONArray obj = WebServiceUtils.requestJSONArray(Constants.UPDATE_TAGS_INFO_FROM_TAGS_TABLE,
                    WebServiceUtils.METHOD.POST,
                    contentValues,mContext);

            if(obj!=null ){
                UpdateTagsForFromTagsTable.CompareSiteGuidFromWebSitesTable  updateWebSitesDataFromServer=  new UpdateTagsForFromTagsTable.CompareSiteGuidFromWebSitesTable(obj);
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

    public class CompareSiteGuidFromWebSitesTable extends AsyncTask<JSONArray, Integer, List<HashMap>> {
        HashMap<String,String> tagsInfoFromServer;
        HashMap<String,String> tagsInfoFromClient;
        private final JSONArray mTagsDetailsArray;
        private final ArrayList<HashMap> tagSiteGuidFromServerArr =new ArrayList<>();
        private final ArrayList<HashMap> tagsSiteGuidFromClientArr =new ArrayList<>();
        public CompareSiteGuidFromWebSitesTable(JSONArray obj) {
            mTagsDetailsArray=obj;

        }
        @Override
        protected List<HashMap> doInBackground(JSONArray... params) {
            if (params == null) {
                return null;
            }
            for(int i=0;i<mTagsDetailsArray.length();++i){
                try {
                    JSONObject json= new JSONObject(mTagsDetailsArray.get(i).toString());
                    String tagSiteGuidFromServer = (String) json.get("tagSiteGuid");
                    String tagNameFromServer = (String) json.get("tagName");
                    tagsInfoFromServer = new HashMap<>();
                    tagsInfoFromServer.put(tagSiteGuidFromServer,tagNameFromServer);
                    tagSiteGuidFromServerArr.add(tagsInfoFromServer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            assert mContentResolver != null;
            Cursor cursor = mContentResolver.query(URI_TABLE, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        tagsInfoFromClient = new HashMap<>();
                        String tagSiteGuidFromClient = cursor.getString(cursor.getColumnIndex(TAGS_SITE_GUID));
                        String tagNameFromClient = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                        tagsInfoFromClient.put(tagSiteGuidFromClient,tagNameFromClient);
                        tagsSiteGuidFromClientArr.add(tagsInfoFromClient);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            //compare between the tags list from the server and the tags list from the client and remove all the tags that exists in the client list
            for(int i=0;i<tagSiteGuidFromServerArr.size();++i){
                String tagGuidFromServer = (String) tagSiteGuidFromServerArr.get(i).keySet().iterator().next();
                for(int j=0;j<tagsSiteGuidFromClientArr.size();++j){
                    String tagGuidFromClient = (String) tagsSiteGuidFromClientArr.get(j).keySet().iterator().next();
                    if(tagGuidFromServer.equals(tagGuidFromClient)){
                        String tagNameFromServer = (String) tagSiteGuidFromServerArr.get(i).values().iterator().next();
                        String tagNameFromClient = (String) tagsSiteGuidFromClientArr.get(j).values().iterator().next();
                        if(tagNameFromServer.equals(tagNameFromClient)){
                            tagsSiteGuidFromClientArr.remove(tagsSiteGuidFromClientArr.get(j));
                        }
                    }
                }
            }
            //remove from client database all the tags in the tagsSiteGuidFromClientArr
            for(int n=0;n<tagsSiteGuidFromClientArr.size();++n){
                String tagGuid = (String) tagsSiteGuidFromClientArr.get(n).keySet().iterator().next();
                String tagName = (String) tagsSiteGuidFromClientArr.get(n).values().iterator().next();
                Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(tagGuid)));
                mContentResolver.delete(uri, TAGS_SITE_GUID + " = ? AND "+TAGS_NAME + " =?" , new String[]{tagGuid,tagName});
                assert cursor != null;
                cursor.close();
            }

            return  tagsSiteGuidFromClientArr;

        }
        @Override
        protected void onPostExecute(List<HashMap> result) {
            super.onPostExecute(result);
        }
    }
}
