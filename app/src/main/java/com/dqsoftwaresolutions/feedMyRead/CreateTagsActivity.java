package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteTagFromTagsTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteTagPerGuid;
import com.dqsoftwaresolutions.feedMyRead.webservices.SetNewTimeInUserTable;
import com.dqsoftwaresolutions.feedMyRead.webservices.UpdateDataInServer;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_NAME;
import static com.dqsoftwaresolutions.feedMyRead.database.TagsContract.TagsColumns.TAGS_SITE_GUID;

public class CreateTagsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<TagName>>, TokenCompleteTextView.TokenListener {

    private TagsCompletionView completionView;
    private List<String> tagsListToShowOnEditView;
    private ContextWrapper mContextWrapper;
    private String siteGuid;
    private ListView createTagsList;
    private CreateTagAdapter mCreateTagAdapter;
    private List<TagName> mTagNamesList = new ArrayList<>();
    private ContentResolver mContentResolver;
    private List<String> tagsListToSave = new ArrayList<>();
    private final List<String> tagsListToClick = new ArrayList<>();
    private Utils mUtils;
    private SwipeToDismissTouchListener<ListViewAdapter> touchListener = null;
    private String tagsRelatedToSite;
    private long upDateChangeTime;
    private int mainListPosition;

    public enum TAG_ACTION {
        SAVE_BUTTON, UNDO, DELETE_SWIPE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tags);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUtils = new Utils(CreateTagsActivity.this);
        upDateChangeTime = mUtils.setTimeChange();
        completionView = (TagsCompletionView) findViewById(R.id.searchView);
        mContentResolver = CreateTagsActivity.this.getContentResolver();
        mContextWrapper = new ContextWrapper(this);
        Bundle extras = getIntent().getExtras();
        getSupportLoaderManager().initLoader(0, null, this);
        siteGuid = extras.getString("siteGuid");
        tagsRelatedToSite = extras.getString("tags");
        mainListPosition = extras.getInt("position");
        putSiteTagInEditView(tagsRelatedToSite);
        mCreateTagAdapter = new CreateTagAdapter(CreateTagsActivity.this, mTagNamesList, tagsRelatedToSite, mContentResolver, CreateTagsActivity.this);
        createTagsList = (ListView) findViewById(R.id.create_tags_list);
        completionView.setTokenListener(this);
        completionView.setSplitChar(',');
        completionView.setThreshold(1);
        Button saveTagsButton = (Button) findViewById(R.id.button_save_tags);
        saveTagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleAnalyticsAction();
                removeTagsFromLocalTable();
                prepareTagsList();
            }
        });
        touchListener = new SwipeToDismissTouchListener<>(
                new ListViewAdapter(createTagsList), new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(ListViewAdapter recyclerView, int position) {

            }
        });
        createTagsList.setOnTouchListener(touchListener);
        createTagsList.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        createTagsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //  googleAnalyticsAction(TAG_ACTION.UNDO);
                if (touchListener.existPendingDismisses()) {

                    touchListener.undoPendingDismiss();
                } else {

                    String tag = tagsListToClick.get(position);
                    addTagNameFromList(tag);
                }
            }
        });
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateTagsActivity.this, MainListActivity.class);
                intent.putExtra("mainListPosition", mainListPosition);
                startActivity(intent);
            }
        });
    }

    //Handle the cases when the comma is in different places in the edit view
    private void prepareTagsList() {
        String getLastChar;
        String getTextFromCompletionView = completionView.getText().toString();
        Pattern getEveryThingAfterLastCommaPattern = Pattern.compile(".*,\\s*(.*)");
        Matcher getEveryThingAfterLastCommaMatcher = getEveryThingAfterLastCommaPattern.matcher(getTextFromCompletionView);
        if (completionView.getText().toString().length() == 0) {
            Toast.makeText(CreateTagsActivity.this, "No added tags", Toast.LENGTH_LONG).show();
            setDelay();
            return;
        } else if (completionView.getText().toString().length() == 1) {
            getLastChar = String.valueOf(getTextFromCompletionView.charAt(getTextFromCompletionView.length() - 1));
        } else {
            getLastChar = String.valueOf(getTextFromCompletionView.charAt(getTextFromCompletionView.length() - 2));
        }
        if (getEveryThingAfterLastCommaMatcher.find() && !getLastChar.equals(",")) {
            if (!getLastChar.equals(" ")) {
                TagName tagName = new TagName();
                tagName.setTagName(getEveryThingAfterLastCommaMatcher.group(1));
                completionView.addObject(tagName, getEveryThingAfterLastCommaMatcher.group(1));
            }
            setDelay();
        } else {
            if (!getLastChar.equals(",")) {
                TagName tagName = new TagName();
                tagName.setTagName(getTextFromCompletionView);
                completionView.addObject(tagName, getTextFromCompletionView);
                setDelay();
            } else {
                setDelay();
            }
        }

    }

    private void setDelay() {
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                removeDuplicateTags();
            }
        };
        handler.postDelayed(r, 50);
    }

    private void removeDuplicateTags() {
        List<String> listOfTags = completionView.getCompletionTagList();
        List<String> trimTagsName = trimTagsName(listOfTags);

        tagsListToSave.clear();
        ContentValues valuesToWebSiteTable = createContentValuesForWebSitesTable("true");
        saveTagsInWebSiteTable(valuesToWebSiteTable, siteGuid);
        Collection<String> listOne = trimTagsName;
        Collection<String> listTwo = tagsListToShowOnEditView;
        Collection<String> different = new HashSet<>();
        Collection<String> similar = new HashSet<>(listOne);
        if (!String.valueOf(tagsListToShowOnEditView).equals("[]")) {
            different.addAll(listOne);
            different.addAll(listTwo);
            similar.retainAll(listTwo);
            different.removeAll(similar);
            tagsListToSave.addAll(different);
        } else {
            tagsListToSave = trimTagsName;
        }
        if (tagsListToSave.size() > 0) {
            prepareTagListToPutInLocalDatabase(trimTagsName);
            saveTagsInServer(trimTagsName);
        }

        listOfTags.clear();
        trimTagsName.clear();
        tagsListToShowOnEditView.clear();
        Intent intent = new Intent(CreateTagsActivity.this, MainListActivity.class);
        intent.putExtra("mainListPosition", mainListPosition);
        startActivity(intent);
    }

    private void prepareTagListToPutInLocalDatabase(List<String> trimTagsName) {
        if (!String.valueOf(trimTagsName).equals("[]")) {
            for (int n = 0; n < trimTagsName.size(); n++) {
                ContentValues createContentValuesForTagsTable = createContentValuesForTagsTable(trimTagsName.get(n));
                saveTagsInTagsTable(createContentValuesForTagsTable);
            }
        }
    }

    private void saveTagsInServer(List<String> trimTagsName) {
        if (String.valueOf(trimTagsName).equals("[]")) {
            new DeleteTagPerGuid(CreateTagsActivity.this, trimTagsName, siteGuid, upDateChangeTime);
        } else {
            new UpdateDataInServer(CreateTagsActivity.this, trimTagsName, siteGuid, upDateChangeTime);

        }
        String token = mUtils.getUserToken();
        new SetNewTimeInUserTable(CreateTagsActivity.this, token, upDateChangeTime);
        Toast.makeText(CreateTagsActivity.this, "tags saved", Toast.LENGTH_LONG).show();
    }

    private List<String> trimTagsName(List<String> listOfTags) {
        List<String> tagList = new ArrayList<>();
        for (int i = 0; i < listOfTags.size(); i++) {
            String tag = listOfTags.get(i).trim();
            tagList.add(tag);
        }
        return tagList;
    }

    private void removeTagsFromLocalTable() {
        Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(siteGuid)));
        mContentResolver.delete(uri, TAGS_SITE_GUID + " = ?", new String[]{String.valueOf(siteGuid)});
    }

    private void saveTagsInTagsTable(ContentValues values) {
        ContentResolver contentResolver = mContextWrapper.getContentResolver();
        Uri uri = Uri.parse(WebSitesContract.BASE_CONTENT_URI + "/tags");
        assert contentResolver != null;
        contentResolver.insert(uri, values);
        mContentResolver = this.getContentResolver();
        new TagsLoader(this, mContentResolver);
    }

    // set true or false in webSites table to point if website have tags
    private void saveTagsInWebSiteTable(ContentValues values, String siteGuid) {

        ContentResolver contentResolver = mContextWrapper.getContentResolver();
        Uri uri = Uri.parse(WebSitesContract.BASE_CONTENT_URI + "/websites");
        String selection = WebSitesContract.WebSitesColumns.SITE_GUID + " = ?";
        String[] selectionArg = {siteGuid};
        assert contentResolver != null;
        contentResolver.update(uri, values, selection, selectionArg);
         new UpdateDataInServer(CreateTagsActivity.this, siteGuid, upDateChangeTime);
    }

    private ContentValues createContentValuesForWebSitesTable(String listOfTAgs) {

        ContentValues values = new ContentValues();
        values.put(WebSitesContract.WebSitesColumns.SITE_TAGS, String.valueOf(listOfTAgs));
        return values;
    }

    private ContentValues createContentValuesForTagsTable(String tag) {

        ContentValues values = new ContentValues();
        values.put(TagsContract.TagsColumns.TAGS_NAME, tag);
        values.put(TagsContract.TagsColumns.TAGS_SITE_GUID, siteGuid);
        return values;
    }

    public void setUpdateTagList() {
        tagsListToClick.clear();
        TagsLoader tagsLoader = new TagsLoader(this, mContentResolver);
        List<TagName> tagsList = tagsLoader.loadInBackground();
        filterDuplicateTagsName(tagsList);
    }

    @Override
    public Loader<List<TagName>> onCreateLoader(int id, Bundle args) {

        mContentResolver = this.getContentResolver();
        return new TagsLoader(this, mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<TagName>> loader, List<TagName> tagNames) {
        filterDuplicateTagsName(tagNames);
        mTagNamesList = tagNames;
    }

    @Override
    public void onLoaderReset(Loader<List<TagName>> loader) {

        mCreateTagAdapter.setTagsData(null);
    }

    private void filterDuplicateTagsName(List<TagName> tagNames) {

        if (tagNames.size() > 0) {
            Collections.sort(tagNames, new Comparator<TagName>() {
                @Override
                public int compare(final TagName tagOne, final TagName tagTwo) {
                    return tagOne.getTagName().compareTo(tagTwo.getTagName());
                }
            });
        }

        final Set<String> filterDuplicateTagsName = new HashSet<>();
        List<TagName> uniqueTagsNameList = new ArrayList<>();
        for (TagName tag : tagNames) {
            if (filterDuplicateTagsName.add(tag.getTagName())) {
                uniqueTagsNameList.add(tag);

            }
        }
        filterTagsRelatedToSite(uniqueTagsNameList);

    }

    //Show in the tags list only the tags that are not related to the website
    private void filterTagsRelatedToSite(List<TagName> uniqueTagsNameList) {
        StringBuilder removeBrackets = removeBrackets(tagsRelatedToSite);
        List<String> tagsRelated = Arrays.asList(removeBrackets.toString().split("\\s*,\\s*"));
        for (int i = 0; i < uniqueTagsNameList.size(); i++) {
            boolean filter = tagsRelated.contains(uniqueTagsNameList.get(i).getTagName());
            if (filter) {
                uniqueTagsNameList.remove(i);

                i--;
            } else {
                tagsListToClick.add(uniqueTagsNameList.get(i).getTagName());
            }
        }
        Collections.sort(tagsListToClick);
        mCreateTagAdapter.setTagsData(uniqueTagsNameList);
        createTagsList.setAdapter(mCreateTagAdapter);
    }

    private StringBuilder removeBrackets(String tagsRelated) {
        StringBuilder removeBrackets = new StringBuilder();
        removeBrackets.append(tagsRelated);
        removeBrackets.deleteCharAt(tagsRelated.length() - 1);
        removeBrackets.deleteCharAt(0);
        return removeBrackets;
    }

    //add tag name to "edit text" line "
    private void addTagNameFromList(String tag) {
        TagName tagName = new TagName();
        tagName.setTagName(tag);
        completionView.addObject(tagName, tag);
    }

    private void putSiteTagInEditView(String tagsRelatedToSite) {
        completionView.clear();
        StringBuilder removeBrackets = removeBrackets(tagsRelatedToSite);
        tagsListToShowOnEditView = new ArrayList<>(Arrays.asList(String.valueOf(removeBrackets).split(",")));
        if (!String.valueOf(tagsListToShowOnEditView).equals("[]")) {
            for (int i = 0; i < tagsListToShowOnEditView.size(); i++) {
                TagName tagName = new TagName();
                tagName.setTagName(tagsListToShowOnEditView.get(i));
                completionView.addObject(tagName, tagsListToShowOnEditView.get(i));
            }
        }
    }

    // set the above row that deleted  back to front view to show the tag name
    public void onItemDismiss() {
        touchListener.undoPendingDismiss();

    }

    public void deleteTagFromDataBase(String tagToDelete) {
        tagCounterPerWebSites(tagToDelete);
        Uri uri = TagsContract.Tags.buildTagsUri((String.valueOf(tagToDelete)));
        mContentResolver.delete(uri, TAGS_NAME + " = ?", new String[]{String.valueOf(tagToDelete)});
        new DeleteTagFromTagsTable(CreateTagsActivity.this, tagToDelete, siteGuid);

    }

    private void tagCounterPerWebSites(String tagToDelete) {
        List<String> siteWithTags = new ArrayList<>();
        TagsLoader tagsLoader = new TagsLoader(CreateTagsActivity.this);
        String[] projection = tagsLoader.getProjection();
        String selection = TagsContract.TagsColumns.TAGS_NAME + " = ?";
        String[] selectionArgs = new String[]{tagToDelete};
        Cursor cursor = getContentResolver().query(TagsContract.URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String siteGuid = cursor.getString(cursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                    siteWithTags.add(siteGuid);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        for (String siteGuid : siteWithTags) {
            checkHowManyTagLeft(siteGuid);
        }
    }

    private void checkHowManyTagLeft(String siteGuid) {
        String[] projection = new String[]{"count(*)"};
        String selection = TagsContract.TagsColumns.TAGS_SITE_GUID + " = ?";
        String[] selectionArgs = new String[]{siteGuid};
        Cursor cursor = getContentResolver().query(TagsContract.URI_TABLE,
                projection,
                selection,
                selectionArgs,
                null);
        assert cursor != null;
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        if (result == 1) {
            ContentValues valuesToWebSiteTable = createContentValuesForWebSitesTable("false");
            saveTagsInWebSiteTable(valuesToWebSiteTable, siteGuid);
        }
        cursor.close();
    }

    public void onBackPressed() {
        Intent intent = new Intent(CreateTagsActivity.this, MainListActivity.class);
        intent.putExtra("mainListPosition", mainListPosition);
        startActivity(intent);
    }

    @Override
    public void onTokenAdded(Object token) {
        System.out.println("Added: " + token);

    }

    @Override
    public void onTokenRemoved(Object token) {

        TagName tagName = new TagName();
        tagName.setTagName(token.toString());
        completionView.removeObject(tagName);
        List<String> completionTagList = new ArrayList<>();
        List<String> completionTags = completionView.getCompletionTagList();
        for (int i = 0; i < completionTags.size(); i++) {
            if (!completionTags.get(i).equals(token.toString())) {

                completionTagList.add(completionTags.get(i));
            } else {
                Log.d("completionTags", completionTags.get(i));
            }
        }

        completionView.removeObject(tagName);
        completionView.setCompletionTagList(completionTagList);
    }

    private void googleAnalyticsAction() {
        Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
        switch (TAG_ACTION.SAVE_BUTTON) {
            case SAVE_BUTTON:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Tag saved button")
                        .build());
                break;
            case UNDO:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Undo swipe tag")
                        .build());
                break;
            case DELETE_SWIPE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Delete swipe tag")
                        .build());
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
        googleAnalytics.setScreenName("Create Tags Screen");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
