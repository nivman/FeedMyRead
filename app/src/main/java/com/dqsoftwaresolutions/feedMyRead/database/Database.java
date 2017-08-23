package com.dqsoftwaresolutions.feedMyRead.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class Database extends SQLiteOpenHelper {


    interface Tables {
        String USER = "user";
        String WEBSITES ="websites";
        String TAGS ="tags";
        String TRASH ="trash";
        String WEB_VIEW_SETTING ="webview_setting";
    }

    public static final String TAG = Database.class.getSimpleName();
    private static final String DATABASE_NAME = "keepmystuff.db";
    private static final int DATABASE_VERSION = 1;


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.USER + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UserContract.UserColumns.USER_NAME + " TEXT NOT NULL,"
                + UserContract.UserColumns.PASSWORD + " TEXT NOT NULL,"
                + UserContract.UserColumns.CHANGE_USER_TIME + " BIGINT NOT NULL,"
                + UserContract.UserColumns.TOKEN + " TEXT NOT NULL)");


        db.execSQL("CREATE TABLE " + Tables.WEBSITES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WebSitesContract.WebSitesColumns.SITE_GUID + " TEXT NOT NULL ,"
                + WebSitesContract.WebSitesColumns.SITE_TITLE + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_URL + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_IMG_URL + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_FAVORITE + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_TAGS + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_FAVICON + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_CONTENT + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.TOKEN + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + Tables.TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagsContract.TagsColumns.TAGS_NAME + " TEXT,"
                + TagsContract.TagsColumns.TAGS_SITE_GUID + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + Tables.TRASH + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WebSitesContract.WebSitesColumns.SITE_GUID + " TEXT NOT NULL ,"
                + WebSitesContract.WebSitesColumns.SITE_TITLE + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_URL + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_IMG_URL + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_FAVORITE + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_TAGS + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_FAVICON + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.SITE_CONTENT + " TEXT NOT NULL,"
                + WebSitesContract.WebSitesColumns.TOKEN + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + Tables.WEB_VIEW_SETTING + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + WebViewSettingContract.WebViewSettingColumns.FONT_SIZE + " INT ,"
                + WebViewSettingContract.WebViewSettingColumns.FONT_COLOR + " TEXT,"
                + WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR + " TEXT,"
                + WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY + " TEXT,"
                + WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS + " INT)");
        db.execSQL("INSERT INTO " + Tables.WEB_VIEW_SETTING + "("
                + BaseColumns._ID +","
                + WebViewSettingContract.WebViewSettingColumns.FONT_SIZE+","
                + WebViewSettingContract.WebViewSettingColumns.FONT_COLOR+","
                + WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR+","
                + WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY+","
                + WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS+") VALUES (1,15,'black', 'white','p{font-family:serif !important;}',200)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if (version == 1) {
            version = 3;
        }
        if (version != DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS "+ Tables.USER);
            db.execSQL("DROP TABLE IF EXISTS "+ Tables.WEBSITES);
            onCreate(db);
        }
    }
    public static void deleteDatabase(Context context){
        context.deleteDatabase(DATABASE_NAME);
    }
}
