package com.dqsoftwaresolutions.feedMyRead.database;

import android.net.Uri;
import android.provider.BaseColumns;


public class TagsContract {
    public interface TagsColumns{
        String TAGS_NAME = "tag_name";
        String TAGS_SITE_GUID = "site_guid";

    }
    public static final String CONTENT_AUTHORITY = "com.dqsoftwaresolutions.feedMyRead.database.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    private static final String PATH_TAGS = "tags";
    public static final Uri URI_TABLE = Uri.parse(BASE_CONTENT_URI.toString()+"/"+PATH_TAGS);
    public static final String[] TOP_LEVEL_PATHS ={
            PATH_TAGS
    };
    public static class Tags implements TagsColumns,BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_TAGS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."+CONTENT_AUTHORITY+ ".tags";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."+CONTENT_AUTHORITY+ ".tags";

        public static Uri buildTagsUri(String tagsId){
            return CONTENT_URI.buildUpon().appendEncodedPath(tagsId).build();
        }
        public static String getTagsId(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }
}
