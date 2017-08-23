package com.dqsoftwaresolutions.feedMyRead;


import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.dqsoftwaresolutions.feedMyRead.data.User;
import com.dqsoftwaresolutions.feedMyRead.database.UserLoader;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<User>> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForUpdates();
        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        ContentResolver contentResolver = this.getContentResolver();
        return new UserLoader(this, contentResolver);
    }
    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
        if (data.size() == 0) {
            Intent intent = new Intent(MainActivity.this, LoginRegisterActivity.class);
            startActivity(intent);
        } else {

            Intent intent = new Intent(MainActivity.this, MainListActivity.class);
            intent.putExtra("ENTER_FROM_LOGIN", 0);
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {

    }

    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }
    private void checkForCrashes() {
        CrashManager.register(this);
    }
    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }
    private void unregisterManagers() {
        UpdateManager.unregister();
    }
}
