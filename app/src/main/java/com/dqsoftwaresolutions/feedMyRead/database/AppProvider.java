package com.dqsoftwaresolutions.feedMyRead.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.WebSites.CONTENT_URI;
@SuppressWarnings("ConstantConditions")
public class AppProvider extends ContentProvider {
    private Database mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int USER = 100;
    private static final int USER_ID = 101;

    private static final int WEBSITES = 200;
    private static final int WEBSITES_ID = 201;
    private static final int WEBSITES_GUID= 202;

    private static final int TAGS = 300;
    private static final int TAGS_ID = 301;

    private static final int TRASH = 400;
    private static final int TRASH_ID = 401;

    private static final int WEB_VIEW_SETTING = 500;
    private static final int WEB_VIEW_SETTING_ID = 501;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = UserContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "user", USER);
        matcher.addURI(authority, "user/*", USER_ID);
        authority = WebSitesContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "websites", WEBSITES);
        matcher.addURI(authority, "websites/*", WEBSITES_ID);
        authority = TagsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "tags", TAGS);
        matcher.addURI(authority, "tags/*", TAGS_ID);
        authority = TrashContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "trash", TRASH);
        matcher.addURI(authority, "trash/*", TRASH_ID);
        authority = WebViewSettingContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "web_view_setting", WEB_VIEW_SETTING);
        matcher.addURI(authority, "web_view_setting/*", WEB_VIEW_SETTING_ID);
        return matcher;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        Database.deleteDatabase(getContext());
        mOpenHelper = new Database(getContext());
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new Database(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch(match) {
            case USER:
                return UserContract.User.CONTENT_TYPE;
            case USER_ID:
                return UserContract.User.CONTENT_ITEM_TYPE;
            case WEBSITES:
                return WebSitesContract.WebSites.CONTENT_TYPE;
            case WEBSITES_ID:
                return WebSitesContract.WebSites.CONTENT_ITEM_TYPE;
            case TAGS:
                return TagsContract.Tags.CONTENT_TYPE;
            case TAGS_ID:
                return TagsContract.Tags.CONTENT_ITEM_TYPE;
            case TRASH:
                return TrashContract.Trash.CONTENT_TYPE;
            case TRASH_ID:
                return TrashContract.Trash.CONTENT_ITEM_TYPE;
            case WEB_VIEW_SETTING:
                return WebViewSettingContract.WebViewSetting.CONTENT_TYPE;
            case WEB_VIEW_SETTING_ID:
                return WebViewSettingContract.WebViewSetting.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(match) {
            case USER:
                queryBuilder.setTables(Database.Tables.USER);
                break;
            case USER_ID:
                queryBuilder.setTables( Database.Tables.USER);
                String userId = UserContract.User.getUserId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + userId);
                break;
            case WEBSITES:
                queryBuilder.setTables( Database.Tables.WEBSITES);
                break;
            case WEBSITES_ID:
                queryBuilder.setTables(Database.Tables.WEBSITES);
                String webSitesId = WebSitesContract.WebSites.getWebSitesId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + webSitesId);
                break;
            case TAGS:
                queryBuilder.setTables(Database.Tables.TAGS);
                break;
            case TAGS_ID:
                queryBuilder.setTables(Database.Tables.TAGS);
                String tagsId = TagsContract.Tags.getTagsId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + tagsId);
                break;
            case TRASH:
                queryBuilder.setTables(Database.Tables.TRASH);
                break;
            case TRASH_ID:
                queryBuilder.setTables(Database.Tables.TRASH);
                String trashId = TrashContract.Trash.getTrashId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + trashId);
                break;
            case WEB_VIEW_SETTING:
                queryBuilder.setTables(Database.Tables.WEB_VIEW_SETTING);
                  break;
            case WEB_VIEW_SETTING_ID:
                queryBuilder.setTables(Database.Tables.WEB_VIEW_SETTING);
                String webViewSettingId = WebViewSettingContract.WebViewSetting.getWebViewSettingsId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + webViewSettingId);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        cursor.close();

        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case USER:
                long userRecordId = db.insertOrThrow(Database.Tables.USER, null, values);
                return UserContract.User.buildUserUri(String.valueOf(userRecordId));
            case WEBSITES:
                long websiteRecordId = db.insertOrThrow(Database.Tables.WEBSITES, null, values);
                final Uri newObjectUri = ContentUris.withAppendedId(CONTENT_URI , websiteRecordId );
                getContext().getContentResolver().notifyChange(newObjectUri , null);
                return WebSitesContract.WebSites.buildWebSitesUri(String.valueOf(websiteRecordId));
            case TAGS:
                long tagsRecordId = db.insertOrThrow(Database.Tables.TAGS, null, values);
                final Uri newTagsObjectUri = ContentUris.withAppendedId(TagsContract.Tags.CONTENT_URI , tagsRecordId );
                getContext().getContentResolver().notifyChange(newTagsObjectUri, null);
                return TagsContract.Tags.buildTagsUri(String.valueOf(tagsRecordId));
            case TRASH:
                long trashRecordId = db.insertOrThrow(Database.Tables.TRASH, null, values);
                final Uri newTrashObjectUri = ContentUris.withAppendedId(CONTENT_URI , trashRecordId );
                getContext().getContentResolver().notifyChange(newTrashObjectUri , null);
                 return TrashContract.Trash.buildTrashUri(String.valueOf(trashRecordId));
            case WEB_VIEW_SETTING:
                long webViewSettingId = db.insertOrThrow(Database.Tables.WEB_VIEW_SETTING, null, values);
                return WebViewSettingContract.WebViewSetting.buildWebViewSettingsUri(String.valueOf(webViewSettingId));
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String selectionCriteria;
        switch (match) {
            case USER:
                return db.update(Database.Tables.USER, values, selection, selectionArgs);
            case USER_ID:
                String userId = UserContract.User.getUserId(uri);
                selectionCriteria = BaseColumns._ID + "=" + userId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(Database.Tables.USER, values, selectionCriteria, selectionArgs);
            case WEBSITES:
                getContext().getContentResolver().notifyChange(uri, null);
                return db.update(Database.Tables.WEBSITES, values, selection, selectionArgs);
            case WEBSITES_ID:
                String webSitesId = WebSitesContract.WebSites.getWebSitesId(uri);
                selectionCriteria = BaseColumns._ID + "=" + webSitesId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");

                return db.update(Database.Tables.WEBSITES, values, selectionCriteria, selectionArgs);
            case WEBSITES_GUID:
                String webSitesGuid = WebSitesContract.WebSites.getWebSitesId(uri);
                selectionCriteria = WebSitesContract.WebSitesColumns.SITE_GUID + "=" + webSitesGuid
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");

                return db.update(Database.Tables.WEBSITES, values, selectionCriteria, selectionArgs);
            case TAGS:
                long tagsRecordId = db.insertOrThrow(Database.Tables.TAGS, null, values);
                final Uri newTagsObjectUri = ContentUris.withAppendedId(TagsContract.Tags.CONTENT_URI , tagsRecordId );
                getContext().getContentResolver().notifyChange(newTagsObjectUri, null);

                return db.update(Database.Tables.TAGS, values, selection, selectionArgs);
            case TAGS_ID:

                String tagsId = TagsContract.Tags.getTagsId(uri);

                selectionCriteria = BaseColumns._ID + "=" + tagsId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(Database.Tables.TAGS, values, selectionCriteria, selectionArgs);
            case TRASH:
                return db.update(Database.Tables.TRASH, values, selection, selectionArgs);
            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                selectionCriteria = BaseColumns._ID + "=" + trashId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(Database.Tables.TRASH, values, selectionCriteria, selectionArgs);
            case WEB_VIEW_SETTING:

                return db.update(Database.Tables.WEB_VIEW_SETTING, values, selection, selectionArgs);
            case WEB_VIEW_SETTING_ID:

                String webViewSettingId = WebViewSettingContract.WebViewSetting.getWebViewSettingsId(uri);
                selectionCriteria = BaseColumns._ID + "=" + webViewSettingId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.update(Database.Tables.WEB_VIEW_SETTING, values, selectionCriteria, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        if (uri.equals(UserContract.BASE_CONTENT_URI)) {
            deleteDatabase();
            return 0;
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case USER_ID:
                String userId = UserContract.User.getUserId(uri);
                String notesSelectionCriteria = BaseColumns._ID + "=" + userId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(Database.Tables.USER, notesSelectionCriteria, selectionArgs);

            case WEBSITES_ID:
                String websiteId = WebSitesContract.WebSites.getWebSitesId(uri);
                String websiteSelectionCriteria = BaseColumns._ID + "=" + websiteId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(Database.Tables.WEBSITES, websiteSelectionCriteria, selectionArgs);
            case TAGS_ID:
                String tagsId = TagsContract.Tags.getTagsId(uri);
                String tagsSelectionCriteria = BaseColumns._ID + "=" + tagsId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(Database.Tables.TAGS,  selection, selectionArgs);
            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                String trashSelectionCriteria = BaseColumns._ID + "=" + trashId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(Database.Tables.TRASH, selection, selectionArgs);
            case WEB_VIEW_SETTING_ID:
                String webSiteSettingId = WebViewSettingContract.WebViewSetting.getWebViewSettingsId(uri);
                String webSiteSettingSelectionCriteria = BaseColumns._ID + "=" + webSiteSettingId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return db.delete(Database.Tables.WEB_VIEW_SETTING, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

}















