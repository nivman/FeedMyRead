package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TrashContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;

import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.URI_TABLE;

public class Statistics extends Fragment {
    private TextView articlesNumber;
    private TextView articlesArchiveNumber;
    private TextView  articlesFavoritesNumber;
    private TextView tagsNumber;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.statistics_fregment, container, false);
     //   View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);

        articlesNumber =(TextView) rootView.findViewById(R.id.articles_number);
        articlesArchiveNumber = (TextView) rootView.findViewById(R.id.articles_archive_number);
        articlesFavoritesNumber = (TextView) rootView.findViewById(R.id.articles_favorites_number);
        tagsNumber = (TextView) rootView.findViewById(R.id.tags_number);
        ContentResolver  mContentResolver = getActivity().getContentResolver();
        countSavedArticles(mContentResolver);
        countArchiveArticles(mContentResolver);
        countFavoritesArticles(mContentResolver);
        countTags(mContentResolver);
        return rootView;
    }


    public int countSavedArticles(ContentResolver contentResolver){
        int result;
       Cursor cursor = contentResolver.query(URI_TABLE, new String[]{"count(*)"},null, null, null);
        {
            assert cursor != null;
            if (cursor.getCount() == 0) {
                cursor.close();
                return 4;
            } else {
                cursor.moveToFirst();
                result = cursor.getInt(0);
                String num = String.valueOf(result);
                articlesNumber.setText(num);
                cursor.close();
                //    return result;
            }
        }
       return result;
    }
    public int countArchiveArticles(ContentResolver contentResolver){
        int result;
        Cursor cursor = contentResolver.query(TrashContract.URI_TABLE, new String[]{"count(*)"},null, null, null);
        {
            assert cursor != null;
            if (cursor.getCount() == 0) {
                cursor.close();
                return 4;
            } else {
                cursor.moveToFirst();
                result = cursor.getInt(0);
                String num = String.valueOf(result);
                articlesArchiveNumber.setText(num);
                cursor.close();
                //    return result;
            }
        }
        return result;
    }

    public int countFavoritesArticles(ContentResolver contentResolver){

        String selection = WebSitesContract.WebSitesColumns.SITE_FAVORITE + " =?";
        String[] selectionArgs = {"true"};
        int result;
        Cursor cursor = contentResolver.query(URI_TABLE, new String[]{"count(*)"},selection, selectionArgs, null);
        {
            assert cursor != null;
            if (cursor.getCount() == 0) {
                cursor.close();
                return 4;
            } else {
                cursor.moveToFirst();
                result = cursor.getInt(0);
                String num = String.valueOf(result);
                articlesFavoritesNumber.setText(num);
                cursor.close();
                //    return result;
            }
        }
        return result;
    }
    public int countTags(ContentResolver contentResolver){


        int result;
        Cursor cursor = contentResolver.query(TagsContract.URI_TABLE, new String[]{"count(*)"},null, null, null);
        {
            assert cursor != null;
            if (cursor.getCount() == 0) {
                cursor.close();
                return 4;
            } else {
                cursor.moveToFirst();
                result = cursor.getInt(0);
                String num = String.valueOf(result);
                tagsNumber.setText(num);
                cursor.close();
                //    return result;
            }
        }
        return result;
    }
    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Statistics");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
