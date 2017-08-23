package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dqsoftwaresolutions.feedMyRead.data.User;
import com.dqsoftwaresolutions.feedMyRead.database.Database;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.webservices.SaveDataInServer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract.URI_TABLE;

public class ShareData extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<User>> {
    private ContentResolver mContentResolver;
    private String originOfData;
    private final ContentValues values = new ContentValues();
    private final ArrayList<String> siteDetails = new ArrayList<>();
    private String imageSrc = Constants.DEFAULT_IMAGE;
    private String favicon;
    private final String favorite = "";
    private final String tags = "";
    private String html = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_data);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = -20;
        params.height = 500;
        params.width = 550;
        params.y = -10;
        params.dimAmount=1;
        this.getWindow().setAttributes(params);
        mContentResolver = this.getContentResolver();
        new Database(this);
        mContentResolver = ShareData.this.getContentResolver();
        final Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
           if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                //    handleSendImage(intent); // Handle single image being sent
                Log.d("ACTION_SEND_SINGLE", "Handle single image being sent");
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //    handleSendMultipleImages(intent); // Handle multiple images being sent
                Log.d("ACTION_SEND_MULTIPLE", "Handle multiple images being sent");
            }
        } else {
            Log.v("NOT USEFUL INFO", "NOT USEFUL INFO");
            // Handle other intents, such as being started from the home screen
        }

    }

    public ShareData() {

    }

    public ShareData(String url, Context context) {

        getUrlFromRssFeed(url, context);
    }

    private void getUrlFromRssFeed(String mUrl, final Context context) {
        originOfData = "fromRss";
        String url = Constants.FULL_TEXT_FEED_END_POINT + mUrl + Constants.FULL_TEXT_FEED_DETAILS;
        if (mUrl != null) {
            collectData(url, originOfData, context, mUrl);
        }

    }

    private void handleSendText(Intent intent) {
        Object extra = intent.getStringExtra(Intent.EXTRA_TEXT);
        String link = null;
        if (extra != null) {
            String sharedText = extra.toString();
            String[] line1 = sharedText.split("\n");
            for (String aLine1 : line1) {
                Pattern pattern = Pattern.compile("http(.*?)");
                Matcher matcher = pattern.matcher(aLine1);
                if (matcher.find()) {
                    link = aLine1.replaceAll(".+" + "http", "http");
                    break;
                }
            }
        }
        final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String url = Constants.FULL_TEXT_FEED_END_POINT + link + Constants.FULL_TEXT_FEED_DETAILS;
        if (sharedText != null) {
            originOfData = "fromExternalSource";
            collectData(url, originOfData, null, link);
        }

//        finish();
    }

    private void collectData(String url, final String originOfData, final Context context, final String link) {
        RequestQueue queue;
        if (originOfData.equals("fromExternalSource")) {
            queue = Volley.newRequestQueue(this);
        } else {
            queue = Volley.newRequestQueue(context);
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            boolean validJson = isJSONValid(response);
                            if (validJson) {
                                JSONObject json = new JSONObject(response);
                                saveSuccessJsonParse(json, originOfData, context);
                            } else {
                                saveFailedJsonParse(originOfData, context, link);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                toast();

            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);

    }

    private void saveSuccessJsonParse(JSONObject json, String originOfData, Context context) {

        try {
            String title;
            if (json.getJSONObject("rss").getJSONObject("channel").has("item")) {
                html = (String) json.getJSONObject("rss").getJSONObject("channel").getJSONObject("item").get("description");
                title = (String) json.getJSONObject("rss").getJSONObject("channel").get("title");
                Document doc = Jsoup.parseBodyFragment(html);
                Element img = doc.select("img").first();
                if (img != null) {
                    imageSrc = img.attr("src");
                }
            } else {
                title = (String) json.getJSONObject("rss").getJSONObject("channel").get("link");
            }

            String url = (String) json.getJSONObject("rss").getJSONObject("channel").get("link");
            favicon = Constants.FAVICON_LINK + url;
            String guid = (String) json.getJSONObject("rss").getJSONObject("channel").get("guid");
            values.put(WebSitesContract.WebSitesColumns.SITE_TITLE, title);
            values.put(WebSitesContract.WebSitesColumns.SITE_URL, url);
            values.put(WebSitesContract.WebSitesColumns.SITE_IMG_URL, imageSrc);
            values.put(WebSitesContract.WebSitesColumns.SITE_FAVICON, favicon);
            values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, "false");
            values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, "false");
            values.put(WebSitesContract.WebSitesColumns.SITE_CONTENT, html);
            values.put(WebSitesContract.WebSitesColumns.SITE_GUID, guid);
            values.put(WebSitesContract.WebSitesColumns.TOKEN, "token");
            siteDetails.add(title);
            siteDetails.add(url);
            siteDetails.add(imageSrc);
            siteDetails.add(favicon);
            siteDetails.add(favorite);
            siteDetails.add(tags);
            siteDetails.add(html);
            siteDetails.add(guid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setDataInTables(values, siteDetails, originOfData, context);
    }

    private void saveFailedJsonParse(String originOfData, Context context, String link) {
        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        values.put(WebSitesContract.WebSitesColumns.SITE_TITLE, link);
        values.put(WebSitesContract.WebSitesColumns.SITE_URL, link);
        values.put(WebSitesContract.WebSitesColumns.SITE_IMG_URL, "");
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVICON, "");
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, "false");
        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, "false");
        values.put(WebSitesContract.WebSitesColumns.SITE_CONTENT, "NO_CONTENT_TO_SHOW");
        values.put(WebSitesContract.WebSitesColumns.SITE_GUID, randomUUIDString);
        values.put(WebSitesContract.WebSitesColumns.TOKEN, "token");
        favicon = Constants.FAVICON_LINK + link;
        siteDetails.add(link);
        siteDetails.add(link);
        siteDetails.add(imageSrc);
        siteDetails.add(favicon);
        siteDetails.add(favorite);
        siteDetails.add(tags);
        siteDetails.add(html);
        siteDetails.add(randomUUIDString);
        setDataInTables(values, siteDetails, originOfData, context);
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void setDataInTables(ContentValues values, ArrayList<String> siteDetails, String originOfData, final Context context) {
        if (originOfData.equals("fromExternalSource")) {
            mContentResolver.insert(URI_TABLE, values);
            new SaveDataInServer(siteDetails, ShareData.this);
            toast();
            Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("site save from External Source")
                    .build());

        } else {
            mContentResolver = context.getContentResolver();
            if (mContentResolver != null) {
                mContentResolver.insert(URI_TABLE, values);
            }
            new SaveDataInServer(siteDetails, context);
            Toast.makeText(context, "Site Saved", Toast.LENGTH_LONG).show();
            Tracker googleAnalytics = ((FeedMyRead) context.getApplicationContext()).getTracker();
            googleAnalytics.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("site save from Internal Source")
                    .build());
        }
    }
    private void toast(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup) findViewById(R.id.custom_toast_container));
        layout.setBackgroundResource(R.drawable.save_site_toast);
        TextView text = (TextView) layout.findViewById(R.id.text);
        final Toast toast = new Toast(getApplicationContext());
        LinearLayout logoBooks= (LinearLayout) layout.findViewById(R.id.logo_books);
        logoBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShareData.this, MainListActivity.class);
                intent.putExtra("mainListPosition", 0);
                finish();
                toast.cancel();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        text.setText("Site Saved");

        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);

        toast.show();
        finish();
    }
    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {

    }

}
