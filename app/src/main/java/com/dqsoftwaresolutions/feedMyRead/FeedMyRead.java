package com.dqsoftwaresolutions.feedMyRead;


import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dqsoftwaresolutions.feedMyRead.data.User;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class FeedMyRead extends Application {
    private FeedMyRead instance;
    private User user;
    private RequestQueue mRequestQueue;
    private Context mContext;

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        user = new User();
        mContext = FeedMyRead.this;
        setContext(mContext);



    }

    synchronized private Tracker getSynchronizedTracker() {
        if (mTracker == null) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);

        }
        return mTracker;
    }

    public Tracker getTracker() {
        return getSynchronizedTracker();
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public  synchronized FeedMyRead getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public Context getContext() {
        return mContext;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(getBaseContext());
    }
    public void myToast(final Context context) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context, "Server is not respond site save locally", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
