package com.dqsoftwaresolutions.feedMyRead;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dqsoftwaresolutions.feedMyRead.data.TagName;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class TagsCompletionView extends TokenCompleteTextView<TagName> {
    private List<String> CompletionTagList=new ArrayList<>();
    public TagsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(TagName tagName) {
        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) l.inflate(R.layout.tags_token, (ViewGroup) getParent(), false);
        view.setText(tagName.getName());

        CompletionTagList.add(tagName.getName());

        return view;
    }

    @Override
    protected TagName defaultObject(String completionText) {
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new TagName(completionText);
        } else {
            return new TagName(completionText.substring(0, index));
        }
    }

    public List<String> getCompletionTagList() {
        return CompletionTagList;
    }

    public void setCompletionTagList(List<String> completionTagList) {
        CompletionTagList = completionTagList;
    }

}

