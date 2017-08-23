package com.dqsoftwaresolutions.feedMyRead.database;


import android.net.Uri;
import android.provider.BaseColumns;

public class TrashContract {

    public interface TrashColumns{

        String SITE_CONTENT = "site_content";
        String SITE_FAVICON = "site_favicon";
        String SITE_FAVORITE = "site_favorite";
        String SITE_IMG_URL = "site_img_url";
        String SITE_TAGS = "site_tags";
        String SITE_TITLE = "site_title";
        String SITE_URL = "site_url";
        String SITE_GUID= "site_guid";
        String TOKEN = "token";

    }
    public static final String CONTENT_AUTHORITY = "com.dqsoftwaresolutions.feedMyRead.database.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    private static final String PATH_TRASH = "trash";
    public static final Uri URI_TABLE = Uri.parse(BASE_CONTENT_URI.toString()+"/"+PATH_TRASH);
    public static final String[] TOP_LEVEL_PATHS ={
            PATH_TRASH
    };
    public static class Trash implements TrashContract.TrashColumns,BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_TRASH).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."+CONTENT_AUTHORITY+ ".trash";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."+CONTENT_AUTHORITY+ ".trash";

        public static Uri buildTrashUri(String trashId){

            return CONTENT_URI.buildUpon().appendEncodedPath(trashId).build();
        }
        public static String getTrashId(Uri uri){

            return uri.getPathSegments().get(1);
        }
    }
}
