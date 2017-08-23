package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.AsyncTask;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.database.UserContract;

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

public class SetNewTimeInUserTable{

 //   private ContentResolver mContentResolver;

    private final String mToken;
    private final ContextWrapper mContextWrapper;
    private final long mChangeUpdateTime;
    private final ContentValues contentValues = new ContentValues();
    public SetNewTimeInUserTable(Context context, String token, long changeUpdateTime) {

        mToken=token;
        mChangeUpdateTime=changeUpdateTime;
      //  mContentResolver = mContext.getContentResolver();
        mContextWrapper = new ContextWrapper(context);
        setChangeTime();
    }
    private void setChangeTime(){

        contentValues.put(UserContract.UserColumns.TOKEN,mToken);

        contentValues.put(UserContract.UserColumns.CHANGE_USER_TIME,mChangeUpdateTime);
        new SetNewTimeInUsersTable().execute();
        setNewChangeTimeInLocalStorge(contentValues,mToken);
    }
    private class SetNewTimeInUsersTable extends AsyncTask<URL, Void, Long> {
        HttpURLConnection urlConnection = null;
        public SetNewTimeInUsersTable() {

        }

        protected Long doInBackground(URL... urls) {

            try {
                URL urlToRequest = new URL(Constants.SET_NEW_TIME_IN_USER_TABLE);
                urlConnection = (HttpURLConnection) urlToRequest.openConnection();
                urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
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
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                convertInputStreamToString(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Long result) {

        }
    }
    private static void convertInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;
        try {
            while ((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.d("stringBuilder", String.valueOf(stringBuilder));
    }
    private void setNewChangeTimeInLocalStorge(ContentValues values, String token) {
        ContentResolver contentResolver = mContextWrapper.getContentResolver();
        Uri uri = Uri.parse(UserContract.BASE_CONTENT_URI + "/user");
        String selection = UserContract.UserColumns.TOKEN + " =?";
        String[] selectionArg = {token};

        assert contentResolver != null;
        contentResolver.update(uri, values, selection, selectionArg);
    }
}
