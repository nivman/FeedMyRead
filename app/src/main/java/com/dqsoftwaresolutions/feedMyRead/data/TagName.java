package com.dqsoftwaresolutions.feedMyRead.data;

import java.io.Serializable;

public class TagName implements Serializable {
    private String mTagName;
    private int _id;
    private String mTagSiteGuid;


    public TagName(int id, String tagName, String tagSiteGuid) {

        this._id = id;
        this.mTagName = tagName;
        this.mTagSiteGuid = tagSiteGuid;
    }

    public TagName(String tagName) {
        this.mTagName = tagName;
    }

    public TagName() {

    }


    public String getName() {
        return mTagName;
    }

    @Override
    public String toString() {
        return mTagName;
    }

    public String getTagName() {
        return mTagName;
    }

    public void setTagName(String tagName) {
        mTagName = tagName;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTagSiteGuid() {
        return mTagSiteGuid;
    }

    public void setTagSiteGuid(String tagSiteGuid) {
        mTagSiteGuid = tagSiteGuid;
    }
}
