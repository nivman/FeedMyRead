package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.dqsoftwaresolutions.feedMyRead.data.MainListPosition;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesContract;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.SetFavoriteStatus;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.dqsoftwaresolutions.feedMyRead.WebSitesCustomAdapter.ACTION.ARCHIVE;
import static com.dqsoftwaresolutions.feedMyRead.WebSitesCustomAdapter.ACTION.FAVORITE_SET;
import static com.dqsoftwaresolutions.feedMyRead.WebSitesCustomAdapter.ACTION.SHARE;
import static com.dqsoftwaresolutions.feedMyRead.WebSitesCustomAdapter.ACTION.TAG;

class WebSitesCustomAdapter extends ArrayAdapter<WebSite> {

    private boolean isSurfaceViewClose = false;
    private boolean deleteFromLocalDevice = false;
    private final LayoutInflater mLayoutInflater;
    private final ContextWrapper mContextWrapper;
    private final ContentResolver mContentResolver;
    private int positionToDelete;
    private int mRowInScreen;
    private String favorite = "false";
    private final Utils mUtils;
    private final List<TagName> mTagNames;
    private final List<Hashtable> mTagHashMapNames = new ArrayList<>();
    private final List<WebSite> mWebSiteList;
    private SortWebSitesListFragment mSortWebSitesListFragment;
    private FavoritesListFragment mFavoritesListFragment;
    private SearchListFragment mSearchListFragment;
    private WebSitesListFragment mWebSitesListFragment;
    private final int mFragmentView;

    public enum ACTION {
        FAVORITE_SET, FAVORITE_REMOVE, TAG, ARCHIVE, SHARE,GO_TO_SITE
    }

    public WebSitesCustomAdapter(Context context, SortWebSitesListFragment sortWebSitesListFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContextWrapper = new ContextWrapper(context);
        mContentResolver = getContext().getContentResolver();
        mFragmentView = Constants.TAGS;
        mSortWebSitesListFragment = sortWebSitesListFragment;
        mTagNames = new ArrayList<>();
        mWebSiteList = new ArrayList<>();
        mUtils = new Utils(context);
    }

    public WebSitesCustomAdapter(Context context, FavoritesListFragment favoritesListFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContextWrapper = new ContextWrapper(context);
        mContentResolver = getContext().getContentResolver();
        mFragmentView = Constants.FAVORITE;
        mFavoritesListFragment = favoritesListFragment;
        mTagNames = new ArrayList<>();
        mWebSiteList = new ArrayList<>();
        mUtils = new Utils(context);
    }

    public WebSitesCustomAdapter(Context context, WebSitesListFragment webSitesListFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContextWrapper = new ContextWrapper(context);
        mContentResolver = getContext().getContentResolver();
        mFragmentView = Constants.MAIN_LIST;
        mWebSitesListFragment = webSitesListFragment;
        mTagNames = new ArrayList<>();
        mWebSiteList = new ArrayList<>();
        mUtils = new Utils(context);

    }

    public WebSitesCustomAdapter(Context context, SearchListFragment searchListFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContextWrapper = new ContextWrapper(context);
        mContentResolver = getContext().getContentResolver();
        mFragmentView = Constants.SEARCH;
        mSearchListFragment = searchListFragment;
        mTagNames = new ArrayList<>();
        mWebSiteList = new ArrayList<>();
        mUtils = new Utils(context);
    }


    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder holder;
        final View view;
        if (convertView == null) {
            view = mLayoutInflater.inflate(R.layout.list_row, parent, false);
            holder = new ViewHolder();
            holder.siteTitle = (TextView) view.findViewById(R.id.site_title);
            holder.siteUrl = (TextView) view.findViewById(R.id.site_url);
            holder.favoriteStar = (ImageView) view.findViewById(R.id.favorite_star);
            holder.emptyFavoriteStar = (ImageView) view.findViewById(R.id.favorite_empty_star);
            holder.siteImage = (ImageView) view.findViewById(R.id.site_image);
            holder.trash = (ImageView) view.findViewById(R.id.trash_icon);
            holder.share = (ImageView) view.findViewById(R.id.share_icon);
            holder.tagOverFlowCounter = (TextView) view.findViewById(R.id.tag_over_flowCounter);

            holder.tagsLinearLayoutContainer = (LinearLayout) view.findViewById(R.id.tags_linear_layout_container);
            holder.mSwipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
            final WebSite webSite = getItem(position);
            assert webSite != null;
            int _id = webSite.get_id();
            holder.siteFavorite = webSite.getSiteFavorite();
            view.setTag(_id);
            view.setTag(holder.siteFavorite);
            holder.mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            holder.mSwipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_wrapper));
            holder.mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onClose(SwipeLayout layout) {
                    //when the SurfaceView totally cover the BottomView.
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //you are swiping.
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {
                }

                @Override
                public void onOpen(SwipeLayout layout) {
                    //when the BottomView totally show.
                }

                @Override
                public void onStartClose(SwipeLayout layout) {
                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                    isSurfaceViewClose = String.valueOf(layout.getOpenStatus()).equals("Close");
                }
            });


        } else {
            setListViewPositionFromRss(position);
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        view.setTag(holder);
        final WebSite webSite = getItem(position);
        final int _id;
        if (webSite != null) {
            _id = webSite.get_id();

            final String site_Guid = webSite.getSiteGuid();
            final String siteTitle = webSite.getSiteTitle();
            final String siteUrl = webSite.getSiteUrl();
            final String siteImgUrl = webSite.getSiteImgUrl();
            final String siteFavorite = webSite.getSiteFavorite();
            final String siteTags = webSite.getSiteTags();
            holder.tagsLinearLayoutContainer.removeAllViews();
            holder.tagOverFlowCounter.setText("");
            parseTags(holder, siteTags, site_Guid);

            holder.mSwipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if the row is on SurfaceView mode
                    if (isSurfaceViewClose) {
                        googleAnalyticsAction(ACTION.GO_TO_SITE, mFragmentView);
                        Intent intent = new Intent(v.getContext(), WebSiteContentActivity.class);
                        intent.putExtra("WEBSITE", webSite);
                        intent.putExtra("POSITION", position);
                        v.getContext().startActivity(intent);
                    }
                }
            });

            holder.emptyFavoriteStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (siteFavorite.equals("true")) {
                        favorite = "false";
                        (holder.emptyFavoriteStar).setVisibility(View.VISIBLE);
                        holder.emptyFavoriteStar.setImageResource(R.drawable.grey_star);
                        googleAnalyticsAction(ACTION.FAVORITE_REMOVE, mFragmentView);
                    } else {
                        favorite = "true";
                        (holder.emptyFavoriteStar).setVisibility(View.VISIBLE);
                        holder.emptyFavoriteStar.setImageResource(R.drawable.favorite);
                        googleAnalyticsAction(FAVORITE_SET, mFragmentView);
                    }
                    ContentValues values = createContentValues(favorite);
                    v.getTag();
                    new SetFavoriteStatus(getContext(), favorite, site_Guid);
                    setFavorites(values, site_Guid);
                    setFavoritsInListView(site_Guid);
                    if (mFragmentView == Constants.FAVORITE) {
                        mFavoritesListFragment.removeRowFromFavoritesListView(position);
                    } else if (mFragmentView == Constants.TAGS) {
                        mSortWebSitesListFragment.displayFavoritesStarListView(position);
                    }
                    else if (mFragmentView == Constants.SEARCH) {
                        mSearchListFragment.displayFavoritesStarListView(position);
                    }
                }
            });

            final ImageView tagIcon = (ImageView) view.findViewById(R.id.tag_icon);
            tagIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googleAnalyticsAction(TAG, mFragmentView);
                    Intent intent = new Intent(v.getContext(), CreateTagsActivity.class);
                    GetTagListParSiteGuid getTagListParSiteGuid = new GetTagListParSiteGuid(getContext(), mContentResolver, site_Guid);
                    List<TagName> tags = getTagListParSiteGuid.loadInBackground();
                    intent.putExtra("tags", String.valueOf(tags));
                    intent.putExtra("siteGuid", site_Guid);
                    intent.putExtra("position", position);
                    intent.putExtra("id", _id);
                    v.getContext().startActivity(intent);
                }
            });
            holder.trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googleAnalyticsAction(ARCHIVE, mFragmentView);
                    setDeleteFromLocalDevice(true);
                    setGuidOfDeletedSite(position);
                    moveToTrash(site_Guid);
                    removeLastTagFromDataBase(v, site_Guid);
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googleAnalyticsAction(SHARE, mFragmentView);
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, siteTitle);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, siteUrl);
                    sendIntent.setType("text/plain");
                    v.getContext().startActivity(Intent.createChooser(sendIntent, getContext().getResources().getText(R.string.share_this)));

                }
            });
            setSiteTitle(holder, siteTitle);
            setSiteUrl(holder, siteUrl);
            setImageSite(holder, siteImgUrl);
            setFavorite(holder, siteFavorite);
        }
        return view;
    }

    private void setSiteTitle(ViewHolder holder, String siteTitle) {
        //regex remove all blank lines from title
        siteTitle = siteTitle.replaceAll("(?m)^[ \t]*\r?\n", "");
        holder.siteTitle.setText(siteTitle);
    }

    private void setSiteUrl(ViewHolder holder, String siteUrl) {
        try {
            String url = mUtils.getDomainName(siteUrl);
            holder.siteUrl.setText(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void setImageSite(ViewHolder holder, String siteImage) {
        if (!siteImage.equals("")) {

            Picasso.with(getContext()).load(siteImage).resize(70, 70).centerCrop().into((holder.siteImage));
        } else {
            Picasso.with(getContext()).load(Constants.DEFAULT_IMAGE).resize(70, 70).centerCrop().into((holder.siteImage));
        }
    }

    private void setFavorite(ViewHolder holder, String siteFavorite) {
        if (siteFavorite.equals("true")) {
            (holder.favoriteStar).setVisibility(View.VISIBLE);
            holder.emptyFavoriteStar.setImageResource(R.drawable.favorite);
            favorite = "true";
        } else {
            (holder.favoriteStar).setVisibility(View.GONE);
            holder.emptyFavoriteStar.setImageResource(R.drawable.grey_star);
            favorite = "false";
        }

    }

    private void parseTags(ViewHolder holder, String siteTags, String siteGuid) {
        int n = 0;
        if (siteTags.equals("true")) {

            for (int j = 0; j < mTagHashMapNames.size(); ++j) {
                if (mTagHashMapNames.get(j).keySet().iterator().next().equals(siteGuid)) {

                    holder.rowTextView = new TextView(getContext());
                    holder.rowTextView.setText(mTagHashMapNames.get(j).elements().nextElement().toString());
                    holder.tagHolder = new LinearLayout(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    holder.tagHolder.setBackgroundResource(R.drawable.tags_gradient);
                    holder.rowTextView.setTextColor(Color.WHITE);
                    holder.tagHolder.setPadding(10, 0, 10, 0);
                    layoutParams.setMargins(10, 10, 10, 10);
                    holder.tagHolder.setLayoutParams(layoutParams);
                    holder.tagsLinearLayoutContainer.addView(holder.tagHolder);
                    holder.tagHolder.setOrientation(LinearLayout.HORIZONTAL);
                    holder.tagHolder.addView(holder.rowTextView);
                    holder.tagHolder.setId(j);
                    holder.tagsLinearLayoutContainer.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int width = holder.tagsLinearLayoutContainer.getMeasuredWidth();
                    if (width > 400) {
                        n++;
                        holder.tagsLinearLayoutContainer.removeView(holder.tagHolder);
                        String plusSign = "+" + n;
                        holder.tagOverFlowCounter.setText(plusSign);
                    }
                }
            }
        }
    }

    private void setFavoritsInListView(String site_guid) {

        for (WebSite webSite : mWebSiteList) {
            if (webSite.getSiteGuid().equals(site_guid)) {
                if (webSite.getSiteFavorite().equals("true")) {
                    webSite.setSiteFavorite("false");
                } else {
                    webSite.setSiteFavorite("true");
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    private void removeLastTagFromDataBase(View view, String site_Guid) {
        ArrayList<String> tagsAssociationToWebSite = new ArrayList<>();
        SwipeLayout sl = (SwipeLayout) view.getParent().getParent();
        LinearLayout ll = (LinearLayout) sl.getChildAt(1);
        LinearLayout contextLinearLayout = (LinearLayout) ll.findViewById(R.id.tags_linear_layout_container);
        int count = contextLinearLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = contextLinearLayout.getChildAt(i);
            LinearLayout tagHolder = (LinearLayout) v.findViewById(v.getId());
            TextView tv = (TextView) tagHolder.getChildAt(0);
            String tagName = tv.getText().toString();
            tagsAssociationToWebSite.add(tagName);

        }
        TagsLoader tagsLoader = new TagsLoader(getContext(), mContentResolver);
        tagsLoader.removeLastTagEntries(tagsAssociationToWebSite, site_Guid);
    }

    private void moveToTrash(String guid) {
        WebSitesLoader mSitesLoader = new WebSitesLoader(getContext(), mContentResolver);
        mSitesLoader.moveToTrash(guid);

        if (mFragmentView == Constants.TAGS) {
            mSortWebSitesListFragment.setChangesInSortSiteListView();
        } else if (mFragmentView == Constants.FAVORITE) {
            mFavoritesListFragment.setChangesInFavoritesListView();
        } else if (mFragmentView == Constants.SEARCH) {
            mSearchListFragment.setChangesInSearchListView();
        }
    }

    private void setGuidOfDeletedSite(int position) {
        this.positionToDelete = position;

    }

    public int getGuidOfDeletedSite() {
        return positionToDelete;
    }

    public boolean isDeleteFromLocalDevice() {
        return deleteFromLocalDevice;
    }

    public void setDeleteFromLocalDevice(boolean deleteFromLocalDevice) {
        this.deleteFromLocalDevice = deleteFromLocalDevice;
    }

    private void setFavorites(ContentValues values, String site_Guid) {
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

    public void setData(List<WebSite> webSites) {
        clear();

        if (webSites != null) {
            for (WebSite webSite : webSites) {
                add(webSite);
                //    notifyDataSetChanged();
            }

        }
    }

    public void setTags(List<TagName> tags) {
        mTagNames.clear();
        if (tags != null) {
            for (TagName tag : tags) {
                mTagNames.add(tag);
            }
        }
    }

    public void setTagsHashTable(ArrayList<Hashtable> tagsArr) {
        mTagHashMapNames.clear();
        if (tagsArr != null) {
            for (Hashtable tag : tagsArr) {
                mTagHashMapNames.add(tag);
            }
        }
    }

    public int count() {

        Cursor cursor = getContext().getContentResolver().query(WebSitesContract.URI_TABLE, new String[]{"count(*)"},
                null, null, null);
        assert cursor != null;
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);

            cursor.close();
            return result;
        }
    }

    public void rowInScreen(int rowInScreen) {

        mRowInScreen = rowInScreen;
    }

    //responsible to set the list in the position it was when user go back from rss fragment
    public void setListViewPositionFromRss(int position) {

        int currentPosition;
        if (position == -1) {
            currentPosition = 0;

        } else if (position > mRowInScreen && mRowInScreen != 0 && position != mRowInScreen + 1) {
            currentPosition = position - mRowInScreen;
        }
        else {
            currentPosition = position;
        }

        MainListPosition mainListPosition = new MainListPosition(currentPosition);
        mainListPosition.setPosition(currentPosition);
        MainListPosition.getInstance();

    }

    private class GetTagListParSiteGuid extends AsyncTaskLoader<List<TagName>> {
        private final String LOG_TAG = GetTagListParSiteGuid.class.getSimpleName();
        private List<TagName> mTags;
        private ContentResolver mContentResolver;
        private Cursor mCursor;
        private final String siteGuid;

        public GetTagListParSiteGuid(Context context, ContentResolver contentResolver, String filterText) {
            super(context);
            mContentResolver = contentResolver;
            siteGuid = filterText;

        }

        @Override
        public List<TagName> loadInBackground() {

            String selection = TagsContract.TagsColumns.TAGS_SITE_GUID + " =?";
            String[] selectionArgs = {siteGuid};
            List<TagName> entries = new ArrayList<>();
            mContentResolver = getContext().getContentResolver();
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

        @Override
        public void deliverResult(List<TagName> tags) {
            if (isReset()) {
                if (tags != null) {
                    mCursor.close();
                }
            }

            List<TagName> oldTagsList = mTags;
            if (mTags == null || mTags.size() == 0) {
                Log.d(LOG_TAG, "+++++++++ mTags No Data returned");
            }
            mTags = tags;
            if (isStarted()) {
                super.deliverResult(tags);
            }
            if (oldTagsList != null && oldTagsList != tags) {
                mCursor.close();
            }
        }

        @Override
        protected void onStartLoading() {
            if (mTags != null) {
                deliverResult(mTags);
            }

            if (takeContentChanged() | mTags == null) {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        protected void onReset() {
            onStopLoading();
            if (mCursor != null) {
                mCursor.close();
            }

            mTags = null;
        }

        @Override
        public void onCanceled(List<TagName> tags) {
            super.onCanceled(tags);
            if (mCursor != null) {
                mCursor.close();
            }
        }

        @Override
        public void forceLoad() {
            super.forceLoad();
        }
    }

    private void googleAnalyticsAction(ACTION action, int fragmentView) {
        Tracker googleAnalytics;
        ListFragment fragment = new ListFragment();

        switch (fragmentView) {
            case 0:
                fragment = mWebSitesListFragment;
                break;
            case 1:
                fragment = mSortWebSitesListFragment;
                break;
            case 2:
                fragment = mFavoritesListFragment;
                break;
            case 3:
                fragment = mSearchListFragment;
                break;
        }
        switch (action) {
            case FAVORITE_SET:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Set Favorite Click From List")
                        .build());
                break;
            case FAVORITE_REMOVE:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Remove Favorite Click From List")
                        .build());
                break;
            case TAG:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Tag icon From List")
                        .build());
                break;
            case ARCHIVE:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Archive icon From List")
                        .build());
                break;
            case SHARE:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Click on Share icon From List")
                        .build());
                break;
            case GO_TO_SITE:
                googleAnalytics = ((FeedMyRead) fragment.getActivity().getApplication()).getTracker();
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Go In Article")
                        .build());
                break;
        }

    }

    private static class ViewHolder {

        public TextView siteTitle;
        public TextView siteUrl;
        public ImageView favoriteStar;
        public ImageView emptyFavoriteStar;
        public ImageView siteImage;
        public ImageView trash;
        public ImageView share;
        public String siteFavorite;
        public LinearLayout tagsLinearLayoutContainer;
        public LinearLayout tagHolder;
        public TextView rowTextView;
        public TextView tagOverFlowCounter;
        public SwipeLayout mSwipeLayout;
        public int _id;

    }

}