package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.dqsoftwaresolutions.feedMyRead.database.UserContract;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimeZone;

public class Utils {
    private final Context mContext;

    public Utils(Context context) {
        mContext=context;
    }


    public String getUserToken(){
        String token = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        String[] projection = {UserContract.UserColumns.TOKEN};
        assert contentResolver != null;
        Cursor cursor = contentResolver.query(UserContract.URI_TABLE,projection,null,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                do{
                    token = cursor.getString(cursor.getColumnIndex(UserContract.UserColumns.TOKEN));
                }while (cursor.moveToNext());
            }
            cursor.close();
        }
        return token;
    }
    public long setTimeChange(){
        TimeZone london = TimeZone.getTimeZone("Europe/London");
        long now = System.currentTimeMillis();
        long time=  now + london.getOffset(now);

        return time;
    }

    //remove just the basic domain name
    public String getDomainName(String url) throws URISyntaxException {

        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
