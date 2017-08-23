package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dqsoftwaresolutions.feedMyRead.Constants;
import com.dqsoftwaresolutions.feedMyRead.R;

import org.json.JSONObject;

public abstract class WebServiceTask extends AsyncTask<Void, Void, Boolean> {


    public abstract void showProgress();

    public abstract boolean performRequest();

    public abstract void performSuccessfulOperation();

    public abstract void hideProgress();

    private String mMessage;
    private final Context mContext;

    public WebServiceTask(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        showProgress();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (!WebServiceUtils.hasInternetConnection(mContext)) {
            mMessage = Constants.CONNECTION_MESSAGE;
            return false;
        }


        return performRequest();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        hideProgress();

        if (success) {

            performSuccessfulOperation();
        }
        if (mMessage != null && !mMessage.isEmpty()) {
            Log.d("mMessage", mMessage);

            Toast.makeText(mContext, mMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        hideProgress();
    }

    public boolean hasError(JSONObject obj) {

        if (obj != null) {
            int status = obj.optInt(Constants.STATUS);
            mMessage = obj.optString(Constants.MESSAGE);
//            return status == Constants.STATUS_ERROR || status == Constants.STATUS_UNAUTHORIZED;
            return status == Constants.STATUS_ERROR || status == Constants.STATUS_UNAUTHORIZED;
        }
        mMessage = mContext.getString(R.string.error_url_not_found);
        return true;
    }

}











