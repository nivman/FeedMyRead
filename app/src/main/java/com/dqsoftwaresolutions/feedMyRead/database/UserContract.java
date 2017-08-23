package com.dqsoftwaresolutions.feedMyRead.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class UserContract {
    public interface UserColumns{

        String USER_NAME = "user_name";
        String PASSWORD = "password";
        String TOKEN = "token";
        String CHANGE_USER_TIME ="changeUserTime";
    }
    public static final String CONTENT_AUTHORITY = "com.dqsoftwaresolutions.feedMyRead.database.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);

    private static final String PATH_USER = "user";
    public static final Uri URI_TABLE = Uri.parse(BASE_CONTENT_URI.toString()+"/"+PATH_USER);
    public static final String[] TOP_LEVEL_PATHS ={
            PATH_USER
    };
    public static class User implements UserColumns,BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_USER).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."+CONTENT_AUTHORITY+ ".user";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."+CONTENT_AUTHORITY+ ".user";

        public static Uri buildUserUri(String userId){
            return CONTENT_URI.buildUpon().appendEncodedPath(userId).build();
        }
        public static String getUserId(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }
}
