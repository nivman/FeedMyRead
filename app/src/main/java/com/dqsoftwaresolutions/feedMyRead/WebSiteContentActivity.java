package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.BaseColumns;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebViewSettingContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebViewSettingLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.R.attr.fontFamily;
import static com.dqsoftwaresolutions.feedMyRead.R.drawable.grey_star;
import static com.dqsoftwaresolutions.feedMyRead.R.drawable.star_dark_green;
import static com.dqsoftwaresolutions.feedMyRead.R.id.read_setting_panel;
import static com.dqsoftwaresolutions.feedMyRead.R.id.speedslider;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.ARTICLE_VIEW;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.DARK;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.DECREASE_FONT_SIZE;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.INCREASE_FONT_SIZE;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.LANGUAGE;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.LIGHT;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.PAUSE;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.RESUME;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.SEEK_BAR_BRIGHTNESS;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.SEEK_BAR_FONT;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.SPEED;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.VOLUME;
import static com.dqsoftwaresolutions.feedMyRead.WebSiteContentActivity.CONTENT_ACTION.WEB_VIEW;

public class WebSiteContentActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TextToSpeech.OnInitListener {

    private static final int LOADER__WEB_VIEW_ID = 1;
    private WebSite webSite;
    private WebView webViewOriginal, webView;
    private TextView siteTitle;
    private Menu menu;
    private ContextWrapper mContextWrapper;
    private ContentResolver mContentResolver;
    private List<TagName> entries;
    private WebSettings webSettings;
    private int position;
    private String fontColor;
    private String backgroundColor;
    private String fontName;
    private String styleTag;
    private String soundFilename;
    private WebViewSettingLoader mWebViewSettingLoader;
    private int curBrightnessValue, brightnessValue, clicksCounter;
    private File soundFile;
    private Speech speech;
    private TextToSpeech tts;
    private String selectedLanguage = "";

    public enum CONTENT_ACTION {
        FAVORITE_SET, FAVORITE_REMOVE, TAG, ARCHIVE, SHARE, ARTICLE_VIEW, WEB_VIEW,DECREASE_FONT_SIZE,INCREASE_FONT_SIZE,LIGHT,DARK,SEEK_BAR_FONT,SEEK_BAR_BRIGHTNESS,
        PAUSE,RESUME,START,VOLUME,SPEED,LANGUAGE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_site_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tts = new TextToSpeech(this, this);
        mContentResolver = getContentResolver();
        mWebViewSettingLoader = new WebViewSettingLoader(this, mContentResolver);
        getSupportLoaderManager().initLoader(LOADER__WEB_VIEW_ID, null, this);
        webSite = getIntent().getParcelableExtra("WEBSITE");
        position = getIntent().getIntExtra("POSITION", 0);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebSiteContentActivity.this, MainListActivity.class);
                intent.putExtra("mainListPosition", position);

                startActivity(intent);
            }
        });
        mContextWrapper = new ContextWrapper(this);

        siteTitle = (TextView) findViewById(R.id.site_title);
        webView = (WebView) findViewById(R.id.web_view);
        webViewOriginal = (WebView) findViewById(R.id.web_view_original);
        webViewOriginal.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        webSettings.setDefaultFontSize(15);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if (webSite.getSiteContent().equals("NO_CONTENT_TO_SHOW") || webSite.getSiteContent().length() < 1000) {
            webView.setVisibility(View.INVISIBLE);
            siteTitle.setVisibility(View.INVISIBLE);
            webViewOriginal.setVisibility(View.VISIBLE);
            webViewOriginal.loadUrl(webSite.getSiteUrl());

        }
        siteTitle.setText(webSite.getSiteTitle());
        Button decreaseFontSize = (Button) findViewById(R.id.decrease_font_size);
        decreaseFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(DECREASE_FONT_SIZE);
                clicksCounter -= 1;
                clickCounter(clicksCounter);
            }
        });
        Button increaseFontSize = (Button) findViewById(R.id.increase_font_size);
        increaseFontSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(INCREASE_FONT_SIZE);
                clicksCounter += 1;
                clickCounter(clicksCounter);
            }
        });
        Button lightButton = (Button) findViewById(R.id.light);
        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(LIGHT);
                fontColor = "black";
                backgroundColor = "white";
                SetBackgroundMode();
            }
        });
        Button darkButton = (Button) findViewById(R.id.dark);
        darkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(DARK);
                fontColor = "white";
                backgroundColor = "#000";
                SetBackgroundMode();
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seek_bar_font);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                googleAnalyticsAction(SEEK_BAR_FONT);
                progressValue = progress;
                switch (progressValue) {
                    case 0:
                        fontName = "serif";
                        break;
                    case 1:
                        fontName = "Verdana";
                        break;
                    case 2:
                        fontName = "cursive";
                        break;
                    case 3:
                        fontName = "monospace";
                        break;
                }
                setStyleTag();
                setFontFamily();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout settingPanel = (LinearLayout) findViewById(R.id.setting_panel);
                LinearLayout readSettingPanel = (LinearLayout) findViewById(R.id.read_setting_panel);
                if (event.getAction() == 1 && settingPanel.getVisibility() == View.VISIBLE) {
                    ContentValues values = createContentFontSizeValues();
                    setFontSize(values);
                    settingPanel.setVisibility(View.INVISIBLE);
                }
                if (readSettingPanel.getVisibility() == View.VISIBLE) {
                    readSettingPanel.setVisibility(View.INVISIBLE);
                    onPauseSpeech(getCurrentFocus());
                }
                return false;
            }
        });
        SeekBar speedSlider = (SeekBar) findViewById(speedslider);
        setSpeedSpeech(speedSlider);
        Thread thread = new Thread() {
            public void run() {
                String site_guid = webSite.getSiteGuid();
                entries = getTagList(site_guid);
            }
        };
        thread.start();
        selectLang();
    }

    private void selectLang() {

        Spinner dropdown = (Spinner) findViewById(R.id.langSelector);
        String[] items = new String[]{"ENGLISH", "FRANCE", "GERMAN", "ITALY", "CHINESE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                selectedLanguage = (String) item;
                //   setLangToSpeech(selectedLanguage);
                googleAnalyticsAction(LANGUAGE);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dropdown.setAdapter(adapter);
    }

    private void setLangToSpeech(String selectedLanguage) {

        switch (selectedLanguage) {
            case Constants.ENGLISH:
                tts.setLanguage(Locale.ENGLISH);
                break;
            case Constants.FRANCE:
                tts.setLanguage(Locale.FRANCE);
                break;
            case Constants.ITALY:
                tts.setLanguage(Locale.ITALY);
                break;
            case Constants.CHINESE:
                tts.setLanguage(Locale.CHINESE);
                break;
            case Constants.GERMAN:
                tts.setLanguage(Locale.GERMAN);
                break;
        }

    }

    public void onStartSpeech(View view) {
        setLangToSpeech(selectedLanguage);

        soundFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/soundFile";
        soundFile = new File(soundFilename);
        FileInputStream fileInputStream = null;
        File textContent = new File(soundFilename + "/soundFileText.txt");
        try {
            fileInputStream = new FileInputStream(textContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        speech = new Speech(tts, fileInputStream);
        Thread speechThread = new Thread(speech);
        speechThread.start(); // start thread to speak
    }

    // Pause button clicked
    public void onPauseSpeech(View view) {
        if (speech != null)
            googleAnalyticsAction(PAUSE);
        assert speech != null;
        speech.speechPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) this.getApplication()).getTracker();
        googleAnalytics.setScreenName("Article content screen");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Resume button clicked
    public void onResumeSpeech(View view) {
        if (speech != null)
            googleAnalyticsAction(RESUME);
        assert speech != null;
        speech.speechResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                curBrightnessValue);
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    protected void onStop() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1);
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS,
                        getBrightnessValue());
            }
        };
        handler.postDelayed(r, 270);


    }

    private void setStyleTag() {
        styleTag = "<style> body{font-family:"
                + fontName + " !important;"
                + "color:" + fontColor + ";background-color:" +
                backgroundColor +
                ";font-size:" + String.valueOf(clicksCounter) + "px;}</style>";
    }

    private String getStyleTag() {
        return styleTag;
    }

    private void setScreenBrightness(SeekBar seekBarBrightness) {
        seekBarBrightness.setMax(255);
        seekBarBrightness.setProgress(getBrightnessValue());
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                brightnessValue = progress;
                setBrightnessValue(brightnessValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS,
                        brightnessValue);
            }
        });
    }

    private int getBrightnessValue() {
        return brightnessValue;
    }

    private void setBrightnessValue(int brightnessValue) {
        this.brightnessValue = brightnessValue;
    }

    private void setFontFamily() {

        String data = Constants.HEAD_START_LINE
                + getStyleTag()
                + Constants.HEAD_BODY_HTML
                + webSite.getSiteContent()
                + Constants.BODY_HTML_CLOSER;
        webView.loadDataWithBaseURL(null, data, null, "text/html;charset=UTF-8", null);
    }

    private void SetBackgroundMode() {
        setStyleTag();
        String data = Constants.HEAD_START_LINE + getStyleTag() + Constants.HEAD_BODY_HTML + webSite.getSiteContent() + Constants.BODY_HTML_CLOSER;
        webView.loadDataWithBaseURL(null, data, null, "text/html;charset=UTF-8", null);
    }

    private void clickCounter(int clicksCounter) {
        setStyleTag();
        String data = Constants.HEAD_START_LINE + getStyleTag()
                + Constants.HEAD_BODY_HTML
                + webSite.getSiteContent() + Constants.BODY_HTML_CLOSER;
        webView.loadDataWithBaseURL(null, data, null, "text/html;charset=UTF-8", null);
        webSettings.setDefaultFontSize(clicksCounter);
    }

    private ContentValues createContentFontSizeValues() {

        ContentValues values = new ContentValues();
        values.put(WebViewSettingContract.WebViewSettingColumns.FONT_SIZE, clicksCounter);
        values.put(WebViewSettingContract.WebViewSettingColumns.FONT_COLOR, fontColor);
        values.put(WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR, backgroundColor);
        values.put(WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY, fontName);
        values.put(WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS, getBrightnessValue());
//        values.put(WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS, clicksCounter);
        return values;
    }

    private void setFontSize(ContentValues values) {
        ContentResolver contentResolver = mContextWrapper.getContentResolver();
        Uri uri = Uri.parse(WebViewSettingContract.BASE_CONTENT_URI + "/web_view_setting");
        String selection = WebViewSettingContract.WebViewSettingColumns._ID + " =?";
        String[] selectionArg = {"1"};
        assert contentResolver != null;
        contentResolver.update(uri, values, selection, selectionArg);
    }

    private void setFavorits(ContentValues values, String site_Guid) {
        ContentResolver contentResolver = mContextWrapper.getContentResolver();
        Uri uri = Uri.parse(WebSitesContract.BASE_CONTENT_URI + "/websites");
        String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " =?";
        String[] selectionArg = {site_Guid};
        assert contentResolver != null;
        contentResolver.update(uri, values, selection, selectionArg);
    }

    private ContentValues createContentValues(String favorite) {
        ContentValues values = new ContentValues();
        values.put(WebSitesContract.WebSitesColumns.SITE_FAVORITE, favorite);
        return values;
    }

    private void setFavoriteStar(boolean favoriteStatus, MenuItem favoriteFullStar, MenuItem favoriteEmptyStar) {
        if (favoriteStatus) {
            favoriteEmptyStar.setVisible(false);
            favoriteFullStar.setVisible(true);
        } else {
            favoriteFullStar.setVisible(false);
            favoriteEmptyStar.setVisible(true);
        }
    }

    private List<TagName> getTagList(String site_guid) {
        String selection = TagsContract.TagsColumns.TAGS_SITE_GUID + " =?";
        String[] selectionArgs = {site_guid};
        List<TagName> entries = new ArrayList<>();
        mContentResolver = getContentResolver();
        Cursor mCursor;
        String[] projection = {BaseColumns._ID, TagsContract.TagsColumns.TAGS_NAME, TagsContract.TagsColumns.TAGS_SITE_GUID};
        assert mContentResolver != null;
        mCursor = mContentResolver.query(TagsContract.URI_TABLE, projection, selection, selectionArgs, null);
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    String tagName = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_NAME));
                    String tagSiteGuid = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    TagName tagNameObj = new TagName(_id, tagName, tagSiteGuid);
                    entries.add(tagNameObj);
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        return entries;
    }

    private void moveToTrash(String guid) {
        WebSitesLoader mSitesLoader = new WebSitesLoader(this, mContentResolver);
        mSitesLoader.moveToTrash(guid);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = mWebViewSettingLoader.getProjection();
        return new CursorLoader(
                this,
                WebViewSettingContract.URI_TABLE,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        String[] projection = mWebViewSettingLoader.getProjection();
        cursor = mContentResolver.query(WebViewSettingContract.URI_TABLE, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int fontSize = cursor.getInt(cursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_SIZE));
                    int screenBrightness = cursor.getInt(cursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.VIEW_BRIGHTNESS));
                    String fontColor = cursor.getString(cursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_COLOR));
                    String backgroundColor = cursor.getString(cursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.BACKGROUND_COLOR));
                    String fontFamily = cursor.getString(cursor.getColumnIndex(WebViewSettingContract.WebViewSettingColumns.FONT_FAMILY));
                    this.clicksCounter = fontSize;
                    this.fontName = fontFamily;
                    this.fontColor = fontColor;
                    this.backgroundColor = backgroundColor;
                    setBrightnessValue(screenBrightness);
                    showWebSite();

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showWebSite() {

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                getBrightnessValue());
        getBrightnessValue();
        String data = Constants.HEAD_START_LINE + "<style>body{font-size:"
                + String.valueOf(clicksCounter) + "px;font-family:" + fontFamily + ";color:" + fontColor + ";background-color:" + backgroundColor + ";}</style>"
                + Constants.HEAD_BODY_HTML + webSite.getSiteContent()
                + Constants.BODY_HTML_CLOSER;
        webView.loadDataWithBaseURL(null, data, null, "text/html;charset=UTF-8", null);
    }

    @Override
    public void onInit(int status) {
        //TODO add Language and toast when its not support
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);


            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Initialization SUCCESS.
                Log.d("SUCCESS", "SUCCESS initialize TextToSpeech.");
            }
        } else {
            // Initialization failed.
            Log.e("failed", "Could not initialize TextToSpeech.");
            Toast.makeText(getApplicationContext(), "Language is not support", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        //Close the Text to Speech Library
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void createTextFile() {
        String headlineText = webSite.getSiteTitle();
        String text = html2text(headlineText + "." + webSite.getSiteContent());
        soundFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/soundFile";
        soundFile = new File(soundFilename);
        soundFile.mkdirs();
        File textContent = new File(soundFilename + "/soundFileText.txt");
        String[] data = String.valueOf(text).split(System.getProperty("line.separator"));
        saveFileContent(textContent, data);
    }

    private static void saveFileContent(File file, String[] data) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            try {
                for (int i = 0; i < data.length; i++) {
                    if (fos != null) {
                        fos.write(data[i].getBytes());
                    }
                    if (i < data.length - 1) {
                        if (fos != null) {
                            fos.write("\n".getBytes());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String html2text(String html) {
        if (html == null) {
            return null;
        }
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve line breaks and spacing
        document.select("br").append("");
        document.select("p").prepend("");
        String removeEmptyLines = document.html().replaceAll("\\n", "");
        String lineBreakAfterPoint = removeEmptyLines.replaceAll("(?<=[.])", "\n");
        String lineBreakAfterComma = lineBreakAfterPoint.replaceAll("(?<=[,])", "\n");

        return Jsoup.clean(lineBreakAfterComma, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    private void setVolume(SeekBar seekVolume) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int amGetCurrentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekVolume.setProgress(amGetCurrentVol);
        seekVolume.setMax(amStreamMusicMaxVol);
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                googleAnalyticsAction(VOLUME);
            }
        });
    }

    private void setSpeedSpeech(final SeekBar seekSpeech) {
        seekSpeech.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double speed = (seekSpeech.getProgress() + 1);
                speed = speed / 10;
                tts.setSpeechRate((float) speed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                googleAnalyticsAction(SPEED);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_web_site_content, menu);
        final String site_Guid = webSite.getSiteGuid();
        final MenuItem favoriteEmptyStar = menu.findItem(R.id.favorite_empty_star);
        final MenuItem favoriteFullStar = menu.findItem(R.id.favorite_full_star);
        if (webSite.getSiteFavorite().equals("true")) {
            setFavoriteStar(true, favoriteFullStar, favoriteEmptyStar);
        } else {
            setFavoriteStar(false, favoriteFullStar, favoriteEmptyStar);
        }
        favoriteEmptyStar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                googleAnalyticsAction(CONTENT_ACTION.FAVORITE_REMOVE);
                setFavoriteStar(true, favoriteFullStar, favoriteEmptyStar);
                favoriteEmptyStar.setIcon(star_dark_green);
                String favorite = "true";
                ContentValues values = createContentValues(favorite);
                setFavorits(values, site_Guid);
                return false;
            }
        });
        favoriteFullStar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                googleAnalyticsAction(CONTENT_ACTION.FAVORITE_SET);
                setFavoriteStar(false, favoriteFullStar, favoriteEmptyStar);
                favoriteFullStar.setIcon(grey_star);
                String favorite = "false";
                ContentValues values = createContentValues(favorite);
                setFavorits(values, site_Guid);
                return false;
            }
        });
        final MenuItem tagIcon = menu.findItem(R.id.tag_icon);
        tagIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                googleAnalyticsAction(CONTENT_ACTION.TAG);
                Intent intent = new Intent(WebSiteContentActivity.this, CreateTagsActivity.class);
                intent.putExtra("tags", String.valueOf(entries));
                intent.putExtra("siteGuid", site_Guid);
                startActivity(intent);
                return false;
            }
        });
        final MenuItem trashIcon = menu.findItem(R.id.trash_icon);
        trashIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                googleAnalyticsAction(CONTENT_ACTION.ARCHIVE);
                moveToTrash(site_Guid);
                Intent intent = new Intent(WebSiteContentActivity.this, MainListActivity.class);
                intent.putExtra("mainListPosition", position);
                startActivity(intent);
                return false;
            }
        });
        final MenuItem shareIcon = menu.findItem(R.id.share_icon);
        shareIcon.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                googleAnalyticsAction(CONTENT_ACTION.SHARE);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, webSite.getSiteTitle());
                sendIntent.putExtra(Intent.EXTRA_TEXT, webSite.getSiteUrl());
                sendIntent.setType("text/plain");
                //  startActivity(sendIntent);
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_this)));
                return false;
            }
        });
        final MenuItem textToSpeech = menu.findItem(R.id.text_to_speech);
        textToSpeech.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                createTextFile();
                LinearLayout readSettingPanel = (LinearLayout) findViewById(read_setting_panel);
                readSettingPanel.setVisibility(View.VISIBLE);
                SeekBar seekVolume = (SeekBar) findViewById(R.id.volume_slider);
                setVolume(seekVolume);
                return false;
            }
        });

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        MenuItem MenuItemWebViewTitle = menu.findItem(R.id.show_web_view);
        switch (item.getItemId()) {
            case R.id.show_web_view:

                if (webView.getVisibility() == View.VISIBLE) {
                    googleAnalyticsAction(ARTICLE_VIEW);
                    MenuItemWebViewTitle.setTitle("Show Article View");
                    webView.setVisibility(View.INVISIBLE);
                    siteTitle.setVisibility(View.INVISIBLE);
                    webViewOriginal.loadUrl(webSite.getSiteUrl());
                    webViewOriginal.setVisibility(View.VISIBLE);
                } else {
                    googleAnalyticsAction(WEB_VIEW);
                    MenuItemWebViewTitle.setTitle("Show Web View");
                    webView.setVisibility(View.VISIBLE);
                    siteTitle.setVisibility(View.VISIBLE);
                    webViewOriginal.setVisibility(View.GONE);
                }
                return true;
            case R.id.layout_control:
                LinearLayout settingPanel = (LinearLayout) findViewById(R.id.setting_panel);
                settingPanel.setVisibility(View.VISIBLE);
                SeekBar seekBarBrightness = (SeekBar) findViewById(R.id.seek_bar_brightness);
                googleAnalyticsAction(SEEK_BAR_BRIGHTNESS);
                setScreenBrightness(seekBarBrightness);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void googleAnalyticsAction(CONTENT_ACTION action) {
        Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
        switch (action) {
            case FAVORITE_SET:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Set Favorite Click From site Content screen")
                        .build());
                break;
            case FAVORITE_REMOVE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Remove Favorite Click From site Content screen")
                        .build());
                break;
            case TAG:
                googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Tag icon From site Content screen")
                        .build());
                break;
            case ARCHIVE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Archive icon From site Content screen")
                        .build());
                break;
            case SHARE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Share icon From site Content screen")
                        .build());
                break;
            case ARTICLE_VIEW:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Article View  menu")
                        .build());
                break;
            case DECREASE_FONT_SIZE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on decrease font size")
                        .build());
                break;
            case INCREASE_FONT_SIZE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on increase font size")
                        .build());
                break;
            case LIGHT:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on light button")
                        .build());
                break;
            case DARK:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on dark button")
                        .build());
                break;
            case SEEK_BAR_FONT:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on change font seek bar")
                        .build());
                break;
            case SEEK_BAR_BRIGHTNESS:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on seek bar brightness")
                        .build());
                break;
            case PAUSE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on pause")
                        .build());
                break;
            case RESUME:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on resume")
                        .build());
                break;
            case START:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on start")
                        .build());
                break;
            case VOLUME:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on volume")
                        .build());
                break;
            case SPEED:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on speed")
                        .build());
                break;
            case LANGUAGE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on language")
                        .build());
                break;
            case WEB_VIEW:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Web View menu")
                        .build());
                break;

        }
    }
}
