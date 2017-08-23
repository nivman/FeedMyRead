package com.dqsoftwaresolutions.feedMyRead.webservices;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.util.Log;

import com.dqsoftwaresolutions.feedMyRead.database.UserContract;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateDataBaseBackGroundService extends IntentService {
    private Timer timer = new Timer();

    private void setTimerIsActive(boolean timerIsActive) {

    }

    public UpdateDataBaseBackGroundService() {
        super("UpdateDataBaseBackGroundService");

        setTimerIsActive(false);
    }

    public UpdateDataBaseBackGroundService(String name) {
        super(name);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        timer.cancel();
        timer.purge();
    }
    public void stopTimer() {
        timer.cancel();
        timer = new Timer();
    }

    public void closeTimer() {
        timer.cancel();
        timer.purge();
    }

    public void setTimer(Context context) {

        if (context == null) {
            return;
        }

        setTimerIsActive(true);
        String token = null;
        long updatesTime = 0;
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {UserContract.UserColumns.TOKEN, UserContract.UserColumns.CHANGE_USER_TIME};
        Cursor cursor = null;
        if (contentResolver != null) {
            cursor = contentResolver.query(UserContract.URI_TABLE, projection, null, null, null);
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    token = cursor.getString(cursor.getColumnIndex(UserContract.UserColumns.TOKEN));
                    updatesTime = cursor.getLong(cursor.getColumnIndex(UserContract.UserColumns.CHANGE_USER_TIME));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }


        timer.schedule(new RemindTask(token, updatesTime, context, UpdateDataBaseBackGroundService.this), 0, 60 * 1000);
    }
}

class RemindTask extends TimerTask {
    private final String mToken;
    private final Context mContext;
    private final long updatesTime;
    private final UpdateDataBaseBackGroundService mUpdateDataBaseBackGroundService;

    public RemindTask(String token, long time, Context context, UpdateDataBaseBackGroundService updateDataBaseBackGroundService) {
        mToken = token;
        mContext = context;
        updatesTime = time;
        mUpdateDataBaseBackGroundService = updateDataBaseBackGroundService;
        Date date = new Date(updatesTime);
        Locale locale =Locale.US;
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS",locale);
        String dateFormatted = formatter.format(date);
        Log.d("dateFormatted", String.valueOf(dateFormatted));
    }

    public void run() {

        doSomeWork();
    }

    private void doSomeWork() {
        boolean hasInternetConnection = hasInternetConnection(mContext);
        if(hasInternetConnection){
            new CompareUpdatesTime(mContext, updatesTime, mToken, mUpdateDataBaseBackGroundService);
        }
     }

    @Override
    public boolean cancel() {
        return super.cancel();
    }

    private boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

}
