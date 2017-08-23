package com.dqsoftwaresolutions.feedMyRead;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class RssWebSiteContent extends AppCompatActivity{
    private String mUrl;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_web_site_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RssWebSiteContent.this, MainListActivity.class);
                intent.putExtra("GO_TO_RSS_FRAGMENT","TRUE");
                finish();
            }
        });

        WebView rssWebView = (WebView) findViewById(R.id.article_web_view);
        rssWebView.setWebViewClient(new WebViewClient());
        Intent intent = getIntent();
        mTitle = intent.getStringExtra("title");
        mUrl= intent.getStringExtra("url");
        rssWebView.loadUrl(mUrl);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rss_menu, menu);
        MenuItem saveRssArticle= menu.findItem(R.id.save_rss_site);
         saveRssArticle.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new ShareData(mUrl,RssWebSiteContent.this);
                Tracker googleAnalytics= ((FeedMyRead) getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Save Site From Rss Site content")
                        .build());
                return false;
            }
        });
        MenuItem shareRssArticle= menu.findItem(R.id.share_rss_site);
        shareRssArticle.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Tracker googleAnalytics= ((FeedMyRead) getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Share Site From Rss Site content")
                        .build());
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mTitle);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
                sendIntent.setType("text/plain");
              //  startActivity(sendIntent);
                startActivity(Intent.createChooser(sendIntent,getResources().getText(R.string.share_this)));
                return false;
            }
        });

        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
        googleAnalytics.setScreenName("Rss website content");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
