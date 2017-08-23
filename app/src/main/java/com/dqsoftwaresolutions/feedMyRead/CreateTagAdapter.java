package com.dqsoftwaresolutions.feedMyRead;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.dqsoftwaresolutions.feedMyRead.database.TagsLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.SetNewTimeInUserTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Flipv;
import static com.dqsoftwaresolutions.feedMyRead.CreateTagAdapter.TAG_ACTION.DELETE_CLICK;
import static com.dqsoftwaresolutions.feedMyRead.CreateTagAdapter.TAG_ACTION.DELETE_SWIPE;
import static com.dqsoftwaresolutions.feedMyRead.CreateTagAdapter.TAG_ACTION.DIALOG_EDIT_TAG;
import static com.dqsoftwaresolutions.feedMyRead.CreateTagAdapter.TAG_ACTION.EDIT_TAG;


public class CreateTagAdapter extends ArrayAdapter<TagName> {

    private final Activity mContext;
    private List<TagName> data = new ArrayList<>();
    private final int layoutResourceId;
    private long mUpdatesTime;
    private final Utils mUtils;
    private final ContentResolver mContentResolver;
    private final CreateTagsActivity mCreateTagsActivity;
    public enum TAG_ACTION {
        SAVE_BUTTON, DELETE_SWIPE, DELETE_CLICK, DIALOG_EDIT_TAG,EDIT_TAG,CANCEL_EDIT_TAG
    }
    public CreateTagAdapter(Activity context, List<TagName> objs, String tagsRelatedToSite, ContentResolver contentResolver, CreateTagsActivity createTagsActivity) {

        super(context, R.layout.tag_row, objs);
        data = objs;
        mContext = context;
        layoutResourceId = R.layout.tag_row;
        mUtils=new Utils(mContext);
        mContentResolver=contentResolver;
        mCreateTagsActivity=createTagsActivity;
         putSiteTagInEditView(tagsRelatedToSite);
    }

    @Override
    public int getPosition(TagName item) {
        return super.getPosition(item);
    }

    @Override
    public int getCount() {

        return data.size();
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        final TagViewHolder tagViewHolder;
        if (row == null) {
            tagViewHolder = new TagViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(layoutResourceId, parent, false);
            tagViewHolder.holderTagName = (TextView) row.findViewById(R.id.tag_name);
            tagViewHolder.txtDelete = (TextView) row.findViewById(R.id.txt_delete);
            tagViewHolder.txtDelete.setTag(position);
            tagViewHolder.tagNameLinearLayout = (FrameLayout) row.findViewById(R.id.lyt_container);
            tagViewHolder.editTag =(ImageView) row.findViewById(R.id.tag_edit);
            tagViewHolder.tagTrash=(ImageView) row.findViewById(R.id.tag_trash);
            row.setTag(tagViewHolder);
        } else {
            tagViewHolder = (TagViewHolder) row.getTag();
        }
        tagViewHolder.tagName = data.get(position);
        tagViewHolder.holderTagName.setText(tagViewHolder.tagName.getTagName());

        tagViewHolder.txtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagToDelete = tagViewHolder.holderTagName.getText().toString();
                data.remove(position);
                ((CreateTagsActivity) mContext).onItemDismiss();
                ((CreateTagsActivity) mContext).deleteTagFromDataBase(tagToDelete);
                googleAnalyticsAction(DELETE_SWIPE);
                mUpdatesTime=mUtils.setTimeChange();
                String token = mUtils.getUserToken();
                new SetNewTimeInUserTable(mContext,token,mUpdatesTime);
            }

        });

        tagViewHolder.editTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googleAnalyticsAction(DIALOG_EDIT_TAG);
               String tagToEdit = tagViewHolder.holderTagName.getText().toString();
               editTag(tagToEdit,tagViewHolder.holderTagName);

            }
        });
        tagViewHolder.tagTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(DELETE_CLICK);
                String tagToDelete = tagViewHolder.holderTagName.getText().toString();
                deleteTagFromTrashIcon(position, tagToDelete);

            }
        });
        return row;
    }
    private void deleteTagFromTrashIcon(final int position, final String tagToDelete){
        final NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(mContext);
        dialogBuilder
                .withTitle("Delete "+tagToDelete)
                .withEffect(Flipv)
                .withDialogColor("#005954")
                .withMessage("Are you sure you want to delete this tag?")
                .withButton1Text("OK")
                .withButton2Text("Cancel")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        data.remove(position);
                        ((CreateTagsActivity) mContext).deleteTagFromDataBase(tagToDelete);
                        mUpdatesTime=mUtils.setTimeChange();
                        String token = mUtils.getUserToken();
                        notifyDataSetChanged();
                        new SetNewTimeInUserTable(mContext,token,mUpdatesTime);
                        dialogBuilder.hide();
                        mCreateTagsActivity.setUpdateTagList();
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
    private void editTag(String tagToEdit, final TextView holderTagName){
       final NiftyDialogBuilder dialogBuilder=NiftyDialogBuilder.getInstance(mContext);
        RelativeLayout layout=(RelativeLayout) dialogBuilder.findViewById(R.id.main);
        layout.setPadding(20,20,20,20);
        final String finalTagToEdit=tagToEdit;
        dialogBuilder
                .withTitle("Edit tag")
                .withEffect(Flipv)
                .withDialogColor("#005954")
               .setCustomView(R.layout.edit_tag_dialog,mContext)
                .withMessage("Enter new name for this tag.")
                .withButton1Text("OK")
                .withButton2Text("Cancel")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        googleAnalyticsAction(EDIT_TAG);
                        EditText editTag = (EditText) dialogBuilder.findViewById(R.id.edit_tag_custom);
                        String tagName=editTag.getText().toString();
                        holderTagName.setText(editTag.getText().toString());
                        dialogBuilder.hide();
                        changeTagName(finalTagToEdit,tagName);
                        mCreateTagsActivity.setUpdateTagList();
                        notifyDataSetChanged();
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
        editTag.setPadding(60,0,0,0);
        editTag.setText(tagToEdit);

    }
    private void changeTagName(String originalTagName, String tagName){
        TagsLoader tagsLoader = new TagsLoader(mContext, mContentResolver);
        tagsLoader.changeTagName(mContext,originalTagName,tagName);
      this.notifyDataSetChanged();
    }
    public void setTagsData(List<TagName> tagsName) {
        clear();
        if (tagsName != null) {
            for (TagName tagName : tagsName) {
                add(tagName);
            }
        }

    }

    private void putSiteTagInEditView(String tagsRelatedToSite) {
        StringBuilder removeBrackets = new StringBuilder();
        removeBrackets.append(tagsRelatedToSite);
        removeBrackets.deleteCharAt(tagsRelatedToSite.length() - 1);
        removeBrackets.deleteCharAt(0);
        List<String> tagsListToShowOnEditView = new ArrayList<>(Arrays.asList(String.valueOf(removeBrackets).split(",")));
        if (!String.valueOf(tagsListToShowOnEditView).equals("[]")) {
            for (int i = 0; i < tagsListToShowOnEditView.size(); i++) {
                TagName tagName = new TagName();
                tagName.setTagName(tagsListToShowOnEditView.get(i));
            }
        }
    }
    private void googleAnalyticsAction(TAG_ACTION action){
        Tracker googleAnalytics= ((FeedMyRead) mCreateTagsActivity.getApplication()).getTracker();
        switch (action) {

            case DIALOG_EDIT_TAG:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Open edit tag dialog")
                        .build());
                break;
            case EDIT_TAG:
                 googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Edit tag name")
                        .build());
                break;
            case CANCEL_EDIT_TAG:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Cancel Edit tag name")
                        .build());
                break;
            case DELETE_SWIPE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Delete swipe tag")
                        .build());
                break;
            case DELETE_CLICK:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Delete tag from trash icon")
                        .build());
                break;
        }
    }
    public class TagViewHolder {
        TagName tagName;
        TextView txtDelete;
        FrameLayout tagNameLinearLayout;
        TextView holderTagName;
        ImageView editTag;
        ImageView tagTrash;
    }
}
