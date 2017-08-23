package com.dqsoftwaresolutions.feedMyRead.data;

import android.os.Parcel;
import android.os.Parcelable;

import static android.R.attr.id;

public class WebSite implements Parcelable{

    private String mSiteTitle;
    private String mSiteUrl;
    private String mSiteImgUrl;
    private String mSiteFavorite;
    private String mSiteTags;
    private String mSiteContent;
    private String mSiteFavicon;
    private String mSiteGuid;
    private int _id;


    public WebSite(int id,String siteTitle, String siteUrl, String siteImgUrl, String siteFavorite, String siteTags, String siteContent, String siteFavicon,String siteGuid) {
        this.mSiteTitle = siteTitle;
        this.mSiteUrl = siteUrl;
        this.mSiteImgUrl = siteImgUrl;
        this.mSiteFavorite = siteFavorite;
        this.mSiteTags = siteTags;
        this.mSiteContent = siteContent;
        this.mSiteFavicon =siteFavicon;
        this.mSiteGuid =siteGuid;
        this._id =id;
    }

    public WebSite() {

    }
    private WebSite(Parcel in) {
        String[] data = new String[9];
        in.readStringArray(data);
        this.mSiteTitle = data[0];
        this.mSiteUrl = data[1];
        this.mSiteImgUrl = data[2];
        this.mSiteFavorite = data[3];
        this.mSiteTags = data[4];
        this.mSiteContent = data[5];
        this.mSiteFavicon =data[6];
        this.mSiteGuid =data[7];
        this._id = Integer.parseInt(data[8]);
    }

    public String getSiteGuid() {
        return mSiteGuid;
    }

    public void setSiteGuid(String siteGuid) {
        mSiteGuid = siteGuid;
    }

    public String getSiteTitle() {
        return mSiteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.mSiteTitle = siteTitle;
    }

    public String getSiteUrl() {
        return mSiteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.mSiteUrl = siteUrl;
    }

    public String getSiteImgUrl() {
        return mSiteImgUrl;
    }

    public void setSiteImgUrl(String siteImgUrl) {
        this.mSiteImgUrl = siteImgUrl;
    }

    public String getSiteFavorite() {
        return mSiteFavorite;
    }

    public void setSiteFavorite(String siteFavorite) {
        this.mSiteFavorite = siteFavorite;
    }

    public String getSiteTags() {
        return mSiteTags;
    }

    public void setSiteTags(String siteTags) {
        this.mSiteTags = siteTags;
    }

    public String getSiteContent() {
        return mSiteContent;
    }

    public void setSiteContent(String siteContent) {
        this.mSiteContent = siteContent;
    }
    public String getSiteFavicon() {
        return mSiteFavicon;
    }

    public void setSiteFavicon(String siteFavicon) {
        this.mSiteFavicon = siteFavicon;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
        this.mSiteTitle,
        this.mSiteUrl,
        this.mSiteImgUrl,
        this.mSiteFavorite,
        this.mSiteTags,
        this.mSiteContent,
        this.mSiteFavicon,
        this.mSiteGuid,
        String.valueOf(this._id =id)

        });
    }
    public static final Parcelable.Creator<WebSite> CREATOR =new Parcelable.Creator<WebSite>(){


        @Override
        public WebSite createFromParcel(Parcel in) {
            return  new WebSite(in);
        }

        public WebSite[] newArray(int size){return new WebSite[size];}
    };
}
