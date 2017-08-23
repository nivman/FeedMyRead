package com.dqsoftwaresolutions.feedMyRead.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.data.WebViewSetting;

public class WebViewSettingLoader extends AsyncTaskLoader<WebViewSetting>{
    private static final String LOG_TAG = WebViewSettingLoader.class.getSimpleName();
    private WebViewSetting mWebViewSetting;
    private final ContentResolver mContentProvider;
    private Cursor mCursor;
    private String[] projection = {BaseColumns._ID,
            WebViewSettingContract.WebViewSettingColumns.FONT_SIZE,
            WebViewSettingContract.WebViewSettingColumns.FONT_COLOR ,
            WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR,
            WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY,
            WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS};

         public WebViewSettingLoader(Context context, ContentResolver contentResolver) {
        super(context);
        mContentProvider = contentResolver;
             setProjection(projection);

    }

    public String[] getProjection() {
        return projection;
    }

    private void setProjection(String[] projection) {
        this.projection = projection;
    }

    @Override
    public WebViewSetting loadInBackground() {

        mCursor = mContentProvider.query(WebViewSettingContract.URI_TABLE, projection, null, null, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    int fontSize = mCursor.getInt(mCursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_SIZE));
                    int screenBrightness = mCursor.getInt(mCursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS));
                    String fontColor = mCursor.getString(mCursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_COLOR));
                    String backgroundColor = mCursor.getString(mCursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR));
                    String fontFamily = mCursor.getString(mCursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY));
                    mWebViewSetting =new WebViewSetting(_id,fontSize,fontColor,backgroundColor,fontFamily,screenBrightness);
                    Log.d(LOG_TAG, String.valueOf(fontSize));
                } while (mCursor.moveToNext());
            }
        }
        assert mCursor != null;
        mCursor.close();

        return mWebViewSetting;

    }

    @Override
    public void deliverResult(WebViewSetting webViewSetting) {
        super.deliverResult(webViewSetting);
        if (isReset()) {
            if (webViewSetting != null) {
                mCursor.close();
            }
        }
        WebViewSetting oldWebViewSetting = mWebViewSetting;
        if (mWebViewSetting == null) {
            Log.d(LOG_TAG, "+++++++++++ No Data returned form WebViewSettingLoader");

        }
        mWebViewSetting = webViewSetting;
        if (isStarted()) {
            super.deliverResult(webViewSetting);
            mCursor.close();
        }
        if (oldWebViewSetting != null && oldWebViewSetting != webViewSetting) {
            mCursor.close();
        }

    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if(mWebViewSetting != null){
            deliverResult(mWebViewSetting);
        }
        if(takeContentChanged() || mWebViewSetting == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mCursor != null){
            mCursor.close();
        }
        mWebViewSetting= null;
    }

    @Override
    public void onCanceled(WebViewSetting webViewSetting) {
        super.onCanceled(webViewSetting);
        if(mCursor != null){
            mCursor.close();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }
}
