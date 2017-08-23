package com.dqsoftwaresolutions.feedMyRead.data;

public class Rss {

    private final String mTitle;
    private final String mUrl;

    private final String mImg;
    private final String mArticleSource;

    public Rss(String title, String img, String url, String articleSource) {
        mTitle = title;

        mImg = img;

        mUrl = url;
        mArticleSource=articleSource;

    }
    public String getArticleSource() {
        return mArticleSource;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getImg() {
        return mImg;
    }

}
