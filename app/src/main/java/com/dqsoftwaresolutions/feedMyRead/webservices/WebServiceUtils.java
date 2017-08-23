package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.FeedMyRead;

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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WebServiceUtils {
    private static final String TAG = WebServiceUtils.class.getName();


    public enum METHOD {
        POST, GET, DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method, ContentValues bodyValues, Context context) {

        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

                 JSONObject jsonObject = new JSONObject();
            for (String key : bodyValues.keySet()) {
                jsonObject.put(key, bodyValues.getAsString(key));
            }
                String str = jsonObject.toString();

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());

                osw.write(str);
                osw.flush();
                osw.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "Unauthorized Access!");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "JSONObject URL Response Error "+statusCode);
                if(statusCode==500){
                    FeedMyRead mFeedMyRead=new FeedMyRead();
                   mFeedMyRead.myToast(context);

                }
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            return new JSONObject(convertInputStreamToString(in));

        } catch (IOException | JSONException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return null;
    }
    public static JSONArray requestJSONArray(String serviceUrl, METHOD method, ContentValues bodyValues, Context context) {

        HttpURLConnection urlConnection = null;
        try {
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            for (String key : bodyValues.keySet()) {
                jsonObject.put(key, bodyValues.getAsString(key));
            }
            String str = jsonObject.toString();

            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());

            osw.write(str);
            osw.flush();
            osw.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "Unauthorized Access!");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "JSONArray URL Response Error "+statusCode);
                if(statusCode==500){

                    FeedMyRead mFeedMyRead=new FeedMyRead();
                    mFeedMyRead.myToast(context);
                }
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            return new JSONArray(convertInputStreamToString(in));

        } catch (MalformedURLException e) {
            Log.d(TAG,"MalformedURLException "+ e.getMessage());
        } catch (SocketTimeoutException e) {
            Log.d(TAG,"SocketTimeoutException "+ e.getMessage());
        } catch (IOException e) {
            Log.d(TAG,"IOException "+ e.getMessage());
        } catch (JSONException e) {

            Log.d(TAG,"JSONException "+ e.getMessage());
        } finally {
            if (urlConnection != null) {

                urlConnection.disconnect();
            }
        }

        return null;
    }
    private static String convertInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;
        try {

            while ((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);

            }

        } catch (IOException e) {
            Log.d(TAG, "IOException in convertInputStreamToString");
            e.printStackTrace();
        }

              return stringBuilder.toString();
    }

    public static boolean hasInternetConnection(Context context) {


//        ConnectivityManager connectivityManager = ((ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE));
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
