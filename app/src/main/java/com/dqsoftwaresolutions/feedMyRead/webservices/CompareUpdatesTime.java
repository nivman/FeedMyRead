package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.FeedMyRead;
import com.dqsoftwaresolutions.feedMyRead.Utils;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.UserContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_SITE_GUID;
import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.URI_TABLE;

public class CompareUpdatesTime {
    private final ContentValues contentValues = new ContentValues();
    private final String mToken;
    private final Context mContext;
    private final long mUpdatesTime;
    private final ContentResolver mContentResolver;
    private final Utils mUtils;
    private UpdateDataBaseBackGroundService mBackGroundService;
    private final long timeInLocalDatabase;
    private final CompareUpdatesTime.GetTagsUpdate getTagsUpdate;
    private final String[] projection = {BaseColumns._ID,
            WebSitesContract.WebSitesColumns.SITE_TITLE,
            WebSitesContract.WebSitesColumns.SITE_URL,
            WebSitesContract.WebSitesColumns.SITE_IMG_URL,
            WebSitesContract.WebSitesColumns.SITE_FAVORITE,
            WebSitesContract.WebSitesColumns.SITE_FAVICON,
            WebSitesContract.WebSitesColumns.SITE_TAGS,
            WebSitesContract.WebSitesColumns.SITE_CONTENT,
            WebSitesContract.WebSitesColumns.SITE_GUID,
            WebSitesContract.WebSitesColumns.TOKEN};

    public CompareUpdatesTime(Context context, long updatesTime, String token, UpdateDataBaseBackGroundService backGroundService) {
        mContext = context;
        mUpdatesTime = updatesTime;
        mToken = token;
        mContentResolver = mContext.getContentResolver();
        mUtils = new Utils(mContext);
        mBackGroundService = backGroundService;
        getTagsUpdate = new CompareUpdatesTime.GetTagsUpdate(mContext);
        getTagsUpdate.execute((Void) null);
        CompareUpdatesTime.CompareUpdatesTimeInServer compareUpdatesTimeInServer = new CompareUpdatesTime.CompareUpdatesTimeInServer(mContext);
        compareUpdatesTimeInServer.execute((Void) null);
        timeInLocalDatabase =getClientTime();
    }
    public CompareUpdatesTime(Context context, long updatesTime, String token) {
        mContext = context;
        mUpdatesTime = updatesTime;
        mToken = token;
        mContentResolver = mContext.getContentResolver();
        mUtils = new Utils(mContext);
        getTagsUpdate = new CompareUpdatesTime.GetTagsUpdate(mContext);
        getTagsUpdate.execute((Void) null);
        CompareUpdatesTime.CompareUpdatesTimeInServer compareUpdatesTimeInServer = new CompareUpdatesTime.CompareUpdatesTimeInServer(mContext);
        compareUpdatesTimeInServer.execute((Void) null);
         timeInLocalDatabase =getClientTime();
    }
    private class CompareUpdatesTimeInServer extends WebServiceTask {
        private CompareUpdatesTimeInServer(Context mContext) {
            super(mContext);
            contentValues.put(Constants.USER_TOKEN, mToken);
            contentValues.put(Constants.CHANGE_USER_TIME, mUpdatesTime);
        }

        @Override
        public void showProgress() {
        }

        @Override
        public boolean performRequest() {
            JSONArray getSitesLatestChange = WebServiceUtils.requestJSONArray(Constants.COMPARE_UPDATE_TIME_IN_USRES_TABLE,
                    WebServiceUtils.METHOD.POST,
                    contentValues,mContext);

            JSONArray sitesList = new JSONArray();
            boolean isLoginActivityVisible = FeedMyRead.isActivityVisible();
            String checkRespondStatus = "";
            if (isLoginActivityVisible) {
                mBackGroundService.closeTimer();
            }
            try {
                if (getSitesLatestChange != null) {

                    if(getSitesLatestChange.getJSONObject(0).has("changeUserTime")){
                        Log.d("getJSONObject_length", String.valueOf(getSitesLatestChange.get(0)));
                    }else{
                        checkRespondStatus = String.valueOf(getSitesLatestChange.getJSONObject(0).getString("id"));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (getSitesLatestChange != null && !checkRespondStatus.equals("noUpdates")) {

                int len = getSitesLatestChange.length();
                for (int i = 0; i < len; i++) {
                    if (i != len - 1) {
                        try {
                            sitesList.put(getSitesLatestChange.get(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    String changeTimeFromServer;
                    if(!getSitesLatestChange.getJSONObject(0).has("changeUserTime")) {
                        changeTimeFromServer = getSitesLatestChange.getJSONArray(getSitesLatestChange.length() - 1).getJSONObject(0).getString("changeUserTime");
                    }
                    else{
                        changeTimeFromServer =  getSitesLatestChange.getJSONObject(0).getString("changeUserTime");

                    }
                    long getClientTime = getClientTime();
                    long newTime = Long.parseLong(changeTimeFromServer);
                    if (getClientTime == newTime) {
                        Log.d("getClientTime", String.valueOf(getClientTime));
                    } else {
                         String token = mUtils.getUserToken();
                        performServerRequests(token);
                        updateWebSitesDataFromServer(sitesList);
                        closeAndSetBackGroundTimer(token,newTime);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
        private void performServerRequests(String token){
            new UpdateTagsForFromTagsTable(mContext, mUpdatesTime, token);
            new GetAllSiteGuidFromWebSitesTable(mContext, token);
            new GetAllSiteGuidFromTrashTable(mContext, token);
        }
        private void updateWebSitesDataFromServer(JSONArray sitesList){
            CompareUpdatesTime.UpdateWebSitesDataFromServer updateWebSitesDataFromServer = new CompareUpdatesTime.UpdateWebSitesDataFromServer(sitesList);
            updateWebSitesDataFromServer.execute(sitesList);
        }
        private void closeAndSetBackGroundTimer(String token, long newTime){
           //if mUpdatesTime ==true its mean that the update is from the automatic update and not from refresh gesture
            if(mUpdatesTime!=-1){

                mBackGroundService.stopTimer();
                mBackGroundService.setTimer(mContext);
            }
            new SetNewTimeInUserTable(mContext, token, newTime);
        }
        @Override
        public void performSuccessfulOperation() {

        }

        @Override
        public void hideProgress() {

        }
    }

    public class UpdateWebSitesDataFromServer extends AsyncTask<JSONArray, Integer, JSONArray> {
        private final JSONArray mTagsDetailsArray;




        public UpdateWebSitesDataFromServer(JSONArray obj) {
            mTagsDetailsArray = obj;
        }

        @Override
        protected JSONArray doInBackground(JSONArray... params) {
            if (params == null) {
                return null;
            }
            try {

                for (int i = 0; i < mTagsDetailsArray.length(); i++) {
                    String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " =?";
                    JSONObject json = new JSONObject(mTagsDetailsArray.get(i).toString());
                    String[] selectionArg = {(String) json.get("siteGuid")};
                    ContentValues values = setResultInLocalDatabase(json);
                    Cursor c = mContentResolver.query(URI_TABLE, projection, selection, selectionArg, null);

                    if ((c != null ? c.getCount() : 0) == 0) {
                        mContentResolver.insert(URI_TABLE, values);
                    }

                    if (c != null) {
                        c.close();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mTagsDetailsArray;

        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            try {

                for (int i = 0; i < result.length(); i++) {
                    String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " =?";
                    JSONObject json = new JSONObject(result.get(i).toString());
                    ContentValues values = setResultInLocalDatabase(json);
                    String[] selectionArg = {(String) json.get("siteGuid")};
                    assert mContentResolver != null;
                    Cursor c = mContentResolver.query(URI_TABLE, projection, selection, selectionArg, null);
                    long timeInServerDatabase = Long.parseLong(String.valueOf(json.get("changeTime")));
                    if(timeInServerDatabase>timeInLocalDatabase){
                      mContentResolver.update(URI_TABLE, values, selection, selectionArg);
                    }
                    if (c != null) {
                        c.close();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class GetTagsUpdate extends WebServiceTask {
        private GetTagsUpdate(Context mContext) {
            super(mContext);
            contentValues.put(Constants.USER_TOKEN, mToken);
            contentValues.put(Constants.CHANGE_USER_TIME, mUpdatesTime);
        }

        @Override
        public void showProgress() {
        }

        @Override
        public boolean performRequest() {
            JSONArray obj = WebServiceUtils.requestJSONArray(Constants.GET_TAGS_UPDATE,
                    WebServiceUtils.METHOD.POST,
                    contentValues,mContext);
            Locale locale =Locale.US;
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS",locale);

            String dateFormatted = formatter.format(date);
            Log.d("chack dates", String.valueOf(dateFormatted));
            String checkRespondStatus = "";

            try {

                if (obj != null && !String.valueOf(obj).equals("[]")) {

                    checkRespondStatus = String.valueOf(obj.getJSONObject(0).getString("tagSiteGuid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (obj != null && !checkRespondStatus.equals("noUpdates")) {

                CompareUpdatesTime.UpdateTagsTableFromServer updateTagsTableFromServer = new CompareUpdatesTime.UpdateTagsTableFromServer(obj);
                updateTagsTableFromServer.execute(obj);

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

    public class UpdateTagsTableFromServer extends AsyncTask<JSONArray, Integer, JSONArray> {



        private final JSONArray mTagsDetailsArray;
        private final ArrayList<String> tagsSiteGuid = new ArrayList<>();


        public UpdateTagsTableFromServer(JSONArray obj) {
            mTagsDetailsArray = obj;
        }

        @Override
        protected JSONArray doInBackground(JSONArray... params) {

            JSONArray jsonArray = new JSONArray();
            if (params == null) {
                return null;
            }
            if(mTagsDetailsArray==null){
                return null;
            }
            try {

                for (int j = 0; j < mTagsDetailsArray.length(); j++) {

                    JSONObject json = new JSONObject(mTagsDetailsArray.get(j).toString());
                    String tagSiteGuid = (String) json.get("tagSiteGuid");
                    tagsSiteGuid.add(tagSiteGuid);
                }
                Set<String> removeDuplicate = new HashSet<>();
                removeDuplicate.addAll(tagsSiteGuid);
                tagsSiteGuid.clear();
                tagsSiteGuid.addAll(removeDuplicate);
                for (int n = 0; n < tagsSiteGuid.size(); n++) {
                    Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(tagsSiteGuid.get(n))));
                    assert mContentResolver != null;
                    mContentResolver.delete(uri, TAGS_SITE_GUID + " = ?", new String[]{String.valueOf(tagsSiteGuid.get(n))});
                }
                for (int i = 0; i < mTagsDetailsArray.length(); i++) {
                    JSONObject json = new JSONObject(mTagsDetailsArray.get(i).toString());
                    ContentValues values = new ContentValues();
                    values.put(TagsContract.TagsColumns.TAGS_NAME, (String) json.get("tagName"));
                    values.put(TAGS_SITE_GUID, (String) json.get("tagSiteGuid"));
                    assert mContentResolver != null;
                    mContentResolver.insert(TagsContract.URI_TABLE, values);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonArray;
//            } finally {
//
//            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
        }
    }

    private long getClientTime() {
        String[] projection = {UserContract.UserColumns.TOKEN, UserContract.UserColumns.CHANGE_USER_TIME};
        Cursor cursor = mContentResolver.query(UserContract.URI_TABLE, projection, null, null, null);
        long updatesTime = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                     updatesTime = cursor.getLong(cursor.getColumnIndex(UserContract.UserColumns.CHANGE_USER_TIME));
                } while (cursor.moveToNext());
            }
            cursor.close();
            return updatesTime;
        }
        return 0;
    }

    private ContentValues setResultInLocalDatabase(JSONObject json) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(WebSitesContract.WebSitesColumns.SITE_TITLE, (String) json.get("siteTitle"));
        values.put(WebSitesContract.WebSitesColumns.SITE_URL, (String) json.get("siteUrl"));
        values.put(WebSitesContract.WebSitesColumns.SITE_IMG_URL, (String) json.get("siteImgUrl"));
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVICON, (String) json.get("siteFavIcon"));
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, (String) json.get("siteFavorite"));
        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, (String) json.get("siteTags"));
        values.put(WebSitesContract.WebSitesColumns.SITE_CONTENT, (String) json.get("siteContent"));
        values.put(WebSitesContract.WebSitesColumns.SITE_GUID, (String) json.get("siteGuid"));
        values.put(WebSitesContract.WebSitesColumns.TOKEN, (String) json.get("userToken"));
        return values;
    }
}
