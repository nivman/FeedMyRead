package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class StatisticsTest {


    @Test
    public void countSavedArticlesExpression() {

        Context appContext = InstrumentationRegistry.getTargetContext();
        ContentResolver  mContentResolver = appContext.getContentResolver();
        Statistics statistics = new Statistics();
        int sum= statistics.countSavedArticles(mContentResolver);
        assertEquals(58, sum);
    }
    @Test
    public void countArchiveArticles() {

        Context appContext = InstrumentationRegistry.getTargetContext();
        ContentResolver  mContentResolver = appContext.getContentResolver();
        Statistics statistics = new Statistics();
        int sum= statistics.countArchiveArticles(mContentResolver);
        assertEquals(31, sum);
    }
    @Test
    public void countFavoritesArticles() {

        Context appContext = InstrumentationRegistry.getTargetContext();
        ContentResolver  mContentResolver = appContext.getContentResolver();
        Statistics statistics = new Statistics();
        int sum= statistics.countFavoritesArticles(mContentResolver);
        assertEquals(6, sum);
    }
    @Test
    public void countTags() {

        Context appContext = InstrumentationRegistry.getTargetContext();
        ContentResolver  mContentResolver = appContext.getContentResolver();
        Statistics statistics = new Statistics();
        int sum= statistics.countTags(mContentResolver);
        assertEquals(18, sum);
    }
}
