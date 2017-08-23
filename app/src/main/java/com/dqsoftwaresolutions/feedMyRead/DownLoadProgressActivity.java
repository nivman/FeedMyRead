package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;

import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TrashContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.webservices.WebServiceTask;
import com.dqsoftwaresolutions.feedMyRead.webservices.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.URI_TABLE;

public class DownLoadProgressActivity extends AppCompatActivity {
    private ContentResolver mContentResolver;
    private final ContentValues contentValues = new ContentValues();
    private  String mToken;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContentResolver = DownLoadProgressActivity.this.getContentResolver();
        Intent intent = getIntent();
        String token=  intent.getStringExtra("TOKEN");
        mToken=token;
        mContext=this;
        DownLoadProgressActivity.GetTrashFromServer getTrashFromServer=  new DownLoadProgressActivity.GetTrashFromServer(getApplicationContext(),token);
        getTrashFromServer.execute((Void) null);
        DownLoadProgressActivity.GetTagsFromServer getTagsFromServer=  new DownLoadProgressActivity.GetTagsFromServer(getApplicationContext(),token);
        getTagsFromServer.execute((Void) null);
        DownLoadProgressActivity.GetWebSitesDataFromServer  getWebSitesFromServer=  new DownLoadProgressActivity.GetWebSitesDataFromServer();
        getWebSitesFromServer.execute(mToken);


    }
    private class GetTagsFromServer extends WebServiceTask {
        private GetTagsFromServer(Context mContext,String token) {
            super(mContext);

            contentValues.put(Constants.USER_TOKEN, token);

        }
        @Override
        public void showProgress() {

        }
        @Override
        public boolean performRequest() {
            JSONArray obj = WebServiceUtils.requestJSONArray(Constants.GET_TAGS_FROM_SERVER,
                    WebServiceUtils.METHOD.GET,
                    contentValues,mContext);
            if(obj!=null){
                insertTagsResultToLocalDatabase(obj);
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
    private class GetTrashFromServer extends WebServiceTask {
        private GetTrashFromServer(Context mContext,String token) {
            super(mContext);
            contentValues.put(Constants.USER_TOKEN, token);
        }
        @Override
        public void showProgress() {
        }
        @Override
        public boolean performRequest() {
            JSONArray obj = WebServiceUtils.requestJSONArray(Constants.GET_ALL_DATA_FROM_TRASH,
                    WebServiceUtils.METHOD.GET,
                    contentValues,mContext);
            if(obj!=null){
                insertTrashResultToLocalDatabase(obj);
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
    private void insertTagsResultToLocalDatabase(JSONArray obj){
        for(int i=0;i<obj.length();i++){
            JSONObject json;
            try {
                json = new JSONObject(obj.get(i).toString());
                ContentValues values = new ContentValues();
                values.put(TagsContract.TagsColumns.TAGS_NAME, (String) json.get("tagName"));
                values.put(TagsContract.TagsColumns.TAGS_SITE_GUID,(String) json.get("tagSiteGuid"));
                mContentResolver.insert(TagsContract.URI_TABLE, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void insertTrashResultToLocalDatabase(JSONArray jsonArray){
        for(int i=0;i<jsonArray.length();i++){
            JSONObject json;
            try {
                json= new JSONObject(jsonArray.get(i).toString());
                ContentValues values = new ContentValues();
                values.put(TrashContract.TrashColumns.SITE_TITLE, (String) json.get("siteTitle"));
                values.put(TrashContract.TrashColumns.SITE_URL, (String) json.get("siteUrl"));
                values.put(TrashContract.TrashColumns.SITE_IMG_URL, (String) json.get("siteImgUrl"));
                values.put(TrashContract.TrashColumns.SITE_FAVICON, (String) json.get("siteFavIcon"));
                values.put(TrashContract.TrashColumns.SITE_FAVORITE, (String) json.get("siteFavorite"));
                values.put(TrashContract.TrashColumns.SITE_TAGS, (String) json.get("siteTags"));
                values.put(TrashContract.TrashColumns.SITE_CONTENT,(String) json.get("siteContent"));
                values.put(TrashContract.TrashColumns.SITE_GUID,(String) json.get("siteGuid"));
                values.put(TrashContract.TrashColumns.TOKEN, (String) json.get("userToken"));
                mContentResolver.insert(TrashContract.URI_TABLE, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public class GetWebSitesDataFromServer extends AsyncTask<String, Integer, String> {
        private final String LOG_TAG = DownLoadProgressActivity.GetWebSitesDataFromServer.class.getSimpleName();
        ProgressBar progressBar;

        long jsonLength=0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(R.layout.activity_download_progress);
            progressBar = (ProgressBar) findViewById(R.id.progressBar1);


        }
        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);

            progressBar.setProgress(values[0]);
            progressBar.setIndeterminate(false);
            progressBar.setMax((int) jsonLength);
            progressBar.setProgress(values[0]);
        }
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            if (params == null) {
                return null;
            }
            try {
                URL url = new URL(Constants.GET_ALL_DATA_FROM_SERVER);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                contentValues.put(Constants.USER_TOKEN, mToken);
                JSONObject jsonObject = new JSONObject();
                for (String key : contentValues.keySet()) {
                    try {
                        jsonObject.put(key, contentValues.getAsString(key));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                String str = jsonObject.toString();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
                osw.write(str);
                osw.flush();
                osw.close();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String responseText;
                try {
                    while ((responseText = bufferedReader.readLine()) != null) {
                        stringBuilder.append(responseText);
                    }
                } catch (IOException e) {
                    Log.d("IOException", "IOException in convertInputStreamToString");
                    e.printStackTrace();
                }
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                try {
                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    jsonLength=jsonArray.length();
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json= new JSONObject(jsonArray.get(i).toString());
                        ContentValues values = new ContentValues();
                        values.put(WebSitesContract.WebSitesColumns.SITE_TITLE, (String) json.get("siteTitle"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_URL, (String) json.get("siteUrl"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_IMG_URL, (String) json.get("siteImgUrl"));
                        values.put( WebSitesContract.WebSitesColumns.SITE_FAVICON, (String) json.get("siteFavIcon"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, (String) json.get("siteFavorite"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, (String) json.get("siteTags"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_CONTENT,(String) json.get("siteContent"));
                        values.put(WebSitesContract.WebSitesColumns.SITE_GUID,(String) json.get("siteGuid"));
                        values.put(WebSitesContract.WebSitesColumns.TOKEN, (String) json.get("userToken"));

                        mContentResolver.insert(URI_TABLE, values);
                        publishProgress(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return  stringBuilder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error closing stream");
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Intent intent = new Intent(DownLoadProgressActivity.this, MainListActivity.class);
            intent.putExtra("mainListPosition", 0);
            finish();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
