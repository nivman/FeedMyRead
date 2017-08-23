package com.dqsoftwaresolutions.feedMyRead.data;


public class WebViewSetting {
    private int mId;
    private int mFontSize;
    private int mScreenBrightness;
    private String mFontFamily;
    private String mFontColor;
    private String mBackgroundColor;

    public WebViewSetting(int id, int fontSize, String fontColor, String backgroundColor, String fontFamily, int screenBrightness) {
        this.mId=id;
        this.mFontSize = fontSize;
        this.mFontColor = fontColor;
        this.mBackgroundColor=backgroundColor;
        this.mFontFamily = fontFamily;
        this.mScreenBrightness = screenBrightness;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getFontColor() {
        return mFontColor;
    }

    public void setFontColor(String fontColor) {
        mFontColor = fontColor;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        mBackgroundColor = backgroundColor;
    }


    public WebViewSetting() {

    }

    public int getFontSize() {
        return mFontSize;
    }

    public void setFontSize(int fontSize) {
        mFontSize = fontSize;
    }

    public int getScreenBrightness() {
        return mScreenBrightness;
    }

    public void setScreenBrightness(int screenBrightness) {
        mScreenBrightness = screenBrightness;
    }

    public String getFontFamily() {
        return mFontFamily;
    }

    public void setFontFamily(String fontFamily) {
        mFontFamily = fontFamily;
    }

}
