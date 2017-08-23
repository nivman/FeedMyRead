package com.dqsoftwaresolutions.feedMyRead;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dqsoftwaresolutions.feedMyRead.data.Rss;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.List;


public class RssFeedAdapter extends ArrayAdapter<Rss> {
    private final LayoutInflater mLayoutInflater;
    private final RssTabFragment mRssTabFragment;
    public enum RSS_ACTION {
        SAVE, SHARE, GO_TO_SITE
    }
    public RssFeedAdapter(Context context, RssTabFragment rssTabFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRssTabFragment=rssTabFragment;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final RssViewHolder rssViewHolder;
        final View view;

        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.rss_row, parent, false);
            rssViewHolder = new RssViewHolder();
            rssViewHolder.articleRssTitle = (TextView) view.findViewById(R.id.article_rss_title);
            rssViewHolder.articleRssImage = (ImageView) view.findViewById(R.id.article_rss_image);
            rssViewHolder.articleRssUrl = (TextView) view.findViewById(R.id.article_rss_url);
            rssViewHolder.articleSource = (TextView) view.findViewById(R.id.article_rss_source);
            rssViewHolder.articleRssRow = (LinearLayout) view.findViewById(R.id.article_rss_row);
            rssViewHolder.saveRssArticle = (Button) view.findViewById(R.id.save_rss_article);
            rssViewHolder.shareRssArticle = (Button) view.findViewById(R.id.share_rss_article);
        }
        else {
            view = convertView;
            rssViewHolder = (RssViewHolder) view.getTag();
        }
        view.setTag(rssViewHolder);
        final Rss webSite = getItem(position);
        assert webSite != null;
        final String title = webSite.getTitle();
        final String image = webSite.getImg();
        final String url = webSite.getUrl();
        final String articleSource = webSite.getArticleSource();
        rssViewHolder.articleRssTitle.setText(title);
        rssViewHolder.articleSource.setText(articleSource);
        Picasso.with(getContext()).load(image).resize(150, 100).centerCrop().into((rssViewHolder.articleRssImage));
        rssViewHolder.articleRssUrl.setText(url);
        rssViewHolder.articleRssRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(RSS_ACTION.GO_TO_SITE);
                Intent intent = new Intent(v.getContext(), RssWebSiteContent.class);
                intent.putExtra("url", url);
                intent.putExtra("title", title);
                v.getContext().startActivity(intent);
            }
        });
        rssViewHolder.saveRssArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(RSS_ACTION.SAVE);
               new ShareData(url,getContext());

            }
        });
        rssViewHolder.shareRssArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(RSS_ACTION.SHARE);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, title);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
              //  v.getContext().startActivity(sendIntent);
                v.getContext().startActivity(Intent.createChooser(sendIntent, getContext().getResources().getText(R.string.share_this)));
            }
        });
        return view;
    }
    // pull to refresh
    public void setClearData(List<Rss> webSites) {
       clear();
        if (webSites != null) {
            for (Rss webSite : webSites) {
                add(webSite);
            }
        }
        notifyDataSetChanged();
    }
    //add item in the bottom
    public void addData(List<Rss> webSites) {
        setNotifyOnChange(false);

        if (webSites != null) {
            for (Rss webSite : webSites) {

                add(webSite);
            }
        }
        notifyDataSetChanged();
    }
    private void googleAnalyticsAction(RSS_ACTION action) {
        Tracker googleAnalytics = ((FeedMyRead) mRssTabFragment.getActivity().getApplication()).getTracker();

        switch (action) {

            case GO_TO_SITE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Go In RSS Article")
                        .build());
                break;
            case SAVE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Save RSS Article")
                        .build());
                break;
            case SHARE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Share RSS Article")
                        .build());
                break;
        }
    }
    private static class RssViewHolder {
        private TextView articleSource;
        private TextView articleRssTitle;
        private TextView articleRssUrl;
        private ImageView articleRssImage;
        private LinearLayout articleRssRow;
        private Button saveRssArticle;
        private Button shareRssArticle;
        public int _id;

    }
}
