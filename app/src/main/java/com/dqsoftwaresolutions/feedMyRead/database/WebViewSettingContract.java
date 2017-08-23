package com.dqsoftwaresolutions.feedMyRead.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class WebViewSettingContract {

    public interface WebViewSettingColumns{
        String _ID = "_id";
        String FONT_SIZE = "font_size";
        String FONT_COLOR = "font_color";
        String BACKGROUND_COLOR = "background_color";
        String FONT_FAMILY = "font_family";
        String VIEW_BRIGHTNESS = "view_brightness";
       }
    public static final String CONTENT_AUTHORITY = "com.dqsoftwaresolutions.feedMyRead.database.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    private static final String PATH_WEB_VIEW_SETTING = "web_view_setting";
    public static final Uri URI_TABLE = Uri.parse(BASE_CONTENT_URI.toString()+"/"+PATH_WEB_VIEW_SETTING);
    public static final String[] TOP_LEVEL_PATHS ={
            PATH_WEB_VIEW_SETTING
    };
    public static class WebViewSetting implements WebViewSettingColumns,BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_WEB_VIEW_SETTING).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."+CONTENT_AUTHORITY+ ".web_view_setting";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."+CONTENT_AUTHORITY+ ".web_view_setting";

        public static Uri buildWebViewSettingsUri(String webViewSettingId){

            return CONTENT_URI.buildUpon().appendEncodedPath(webViewSettingId).build();
        }
        public static String getWebViewSettingsId(Uri uri){

            return uri.getPathSegments().get(1);
        }
    }
}
