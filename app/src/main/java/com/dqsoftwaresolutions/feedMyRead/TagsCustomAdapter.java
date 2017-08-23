package com.dqsoftwaresolutions.feedMyRead;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TagsContract;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.database.WebSitesLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.SetNewTimeInUserTable;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Flipv;

class TagsCustomAdapter extends ArrayAdapter<TagName> {

  //  private static FragmentManager sFragmentManager;
    private final LayoutInflater inflater;
    private final ContentResolver mContentResolver;
    private final TagsListFragment mTagsListFragment;
    private final Context mContext;
    private final Utils mUtils;
    private List<TagName> tagsList = new ArrayList<>();

    public TagsCustomAdapter(Context context, TagsListFragment tagsListFragment, List<TagName> tagsNameList) {
        super(context, android.R.layout.simple_list_item_2, tagsNameList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       // sFragmentManager = fragmentManager;
        mContentResolver = getContext().getContentResolver();
        mTagsListFragment = tagsListFragment;
        mContext = context;
        mUtils = new Utils(mContext);
        tagsList = tagsNameList;
    }

    @Override
    public int getPosition(TagName item) {
        return super.getPosition(item);
    }

    @Override
    public int getCount() {

        return tagsList.size();
    }

    @Override
    public TagName getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        TagViewHolder tagViewHolder;
        if (row == null) {
            tagViewHolder = new TagViewHolder();
            row = inflater.inflate(R.layout.tag_row, parent, false);
            tagViewHolder.holderTagName = (TextView) row.findViewById(R.id.tag_name);
            tagViewHolder.txtDelete = (TextView) row.findViewById(R.id.txt_delete);
            tagViewHolder.txtDelete.setTag(position);
            tagViewHolder.tagNameLinearLayout = (FrameLayout) row.findViewById(R.id.lyt_container);
            tagViewHolder.editTag = (ImageView) row.findViewById(R.id.tag_edit);
            tagViewHolder.tagTrash = (ImageView) row.findViewById(R.id.tag_trash);
            row.setTag(tagViewHolder);

        } else {
            tagViewHolder = (TagViewHolder) row.getTag();

        }
        final TagName tagName = getItem(position);
        final String name;
        if (tagName != null) {
            name = tagName.getTagName();
           // ((TextView) row.findViewById(R.id.tag_name)).setText(name);
            tagViewHolder.holderTagName.setText(name);
        }
        tagViewHolder.tagName = tagsList.get(position);
        final TagViewHolder finalTagViewHolder = tagViewHolder;
        tagViewHolder.holderTagName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagName = finalTagViewHolder.holderTagName.getText().toString();

                TagsCustomAdapter.GetTagListByTagName getTagListParSiteGuid = new TagsCustomAdapter.GetTagListByTagName(getContext(), mContentResolver, tagName);
                getTagListParSiteGuid.loadInBackground();
            }
        });
        tagViewHolder.editTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagToEdit = finalTagViewHolder.holderTagName.getText().toString();
                editTag(tagToEdit, finalTagViewHolder.holderTagName);

            }
        });
        tagViewHolder.tagTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagToDelete = finalTagViewHolder.holderTagName.getText().toString();
                deleteTagFromTrashIcon(position, tagToDelete);

            }
        });
        return row;
    }

    private void deleteTagFromTrashIcon(final int position, final String tagToDelete) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(mContext);
        dialogBuilder
                .withTitle("Delete " + tagToDelete)
                .withEffect(Flipv)
                .withDialogColor("#005954")
                .withMessage("Are you sure you want to delete this tag?")
                .withButton1Text("OK")
                .withButton2Text("Cancel")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tagsList.remove(position);
                        mTagsListFragment.deleteTagFromDataBase(tagToDelete);
                        long mUpdatesTime = mUtils.setTimeChange();
                        String token = mUtils.getUserToken();
                        notifyDataSetChanged();
                        new SetNewTimeInUserTable(mContext, token, mUpdatesTime);
                        dialogBuilder.hide();
                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.hide();
                    }
                })
                .show();
    }

    private void editTag(String tagToEdit, final TextView holderTagName) {
        final NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(mContext);
        RelativeLayout layout = (RelativeLayout) dialogBuilder.findViewById(R.id.main);
        layout.setPadding(20, 20, 20, 20);
        final String finalTagToEdit = tagToEdit;
        dialogBuilder
                .withTitle("Edit tag")
                .withEffect(Flipv)
                .withDialogColor("#005954")
                .setCustomView(R.layout.edit_tag_dialog, mContext)
                .withMessage("Enter new name for this tag.")
                .withButton1Text("OK")
                .withButton2Text("Cancel")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText editTag = (EditText) dialogBuilder.findViewById(R.id.edit_tag_custom);
                        String tagName = editTag.getText().toString();
                       // ((TextView) v.findViewById(R.id.tag_name)).setText(tagName);
                        holderTagName.setText(tagName);
                        dialogBuilder.hide();
                        changeTagName(finalTagToEdit, tagName);
                        //   mCreateTagsActivity.setUpdateTagList();

                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.hide();
                    }
                })
                .show();

        EditText editTag = (EditText) dialogBuilder.findViewById(R.id.edit_tag_custom);
        editTag.setTextColor(Color.WHITE);
        editTag.setPadding(60, 0, 0, 0);
        editTag.setText(tagToEdit);

    }

    private void changeTagName(String originalTagName, String tagName) {
        TagsLoader tagsLoader = new TagsLoader((Activity) mContext, mContentResolver);
        tagsLoader.changeTagName(mContext, originalTagName, tagName);
        mTagsListFragment.setUpdateTagList();
        this.notifyDataSetChanged();
    }

    public void setTagsData(List<TagName> tagsName) {
        clear();
        if (tagsName != null) {
            for (TagName tagName : tagsName) {
                add(tagName);
            }
        }
        // tagsList=tagsName;
        notifyDataSetChanged();
    }

    public class TagViewHolder {

        TagName tagName;
        TextView txtDelete;
        FrameLayout tagNameLinearLayout;
        TextView holderTagName;
        ImageView editTag;
        ImageView tagTrash;
    }

    private class GetTagListByTagName extends AsyncTaskLoader<List<TagName>> {
        private final String LOG_TAG = TagsCustomAdapter.GetTagListByTagName.class.getSimpleName();
        private List<TagName> mTags;
        private ContentResolver mContentResolver;
        private Cursor mCursor;
        private final String tagName;
        private final WebSitesLoader mWebSitesLoader;

        public GetTagListByTagName(Context context, ContentResolver contentResolver, String filterText) {
            super(context);
            mContentResolver = contentResolver;
            tagName = filterText;
            mWebSitesLoader = new WebSitesLoader(context, mContentResolver);
        }

        @Override
        public List<TagName> loadInBackground() {
            String selection = TagsContract.TagsColumns.TAGS_NAME + " =?";
            String[] selectionArgs = {tagName};
            List<TagName> entries = new ArrayList<>();
            List<String> siteGuid = new ArrayList<>();
            mContentResolver = getContext().getContentResolver();
            String[] projection = {BaseColumns._ID, TagsContract.TagsColumns.TAGS_NAME, TagsContract.TagsColumns.TAGS_SITE_GUID};
            assert mContentResolver != null;
            mCursor = mContentResolver.query(TagsContract.URI_TABLE, projection, selection, selectionArgs, null);
            if (mCursor != null) {

                if (mCursor.moveToFirst()) {
                    do {
                        String tagSiteGuid = mCursor.getString(mCursor.getColumnIndex(TagsContract.TagsColumns.TAGS_SITE_GUID));
                        TagName tagNameObj = new TagName(tagSiteGuid);
                        entries.add(tagNameObj);
                        siteGuid.add(tagSiteGuid);
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
            ArrayList<WebSite> listOfWebSite = (ArrayList<WebSite>) mWebSitesLoader.getAllWebSiteForTagNameWithAssociationGuid(siteGuid);
            mTagsListFragment.showTagSortResult(listOfWebSite);
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
}
