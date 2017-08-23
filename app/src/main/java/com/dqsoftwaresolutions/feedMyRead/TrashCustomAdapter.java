package com.dqsoftwaresolutions.feedMyRead;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.dqsoftwaresolutions.feedMyRead.data.WebSite;
import com.dqsoftwaresolutions.feedMyRead.database.TrashContract;
import com.dqsoftwaresolutions.feedMyRead.database.TrashLoader;
import com.dqsoftwaresolutions.feedMyRead.webservices.DeleteSiteFromTrashTable;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.List;

import static com.dqsoftwaresolutions.feedMyRead.database.TrashContract.TrashColumns.SITE_GUID;

public class TrashCustomAdapter extends ArrayAdapter<WebSite> {

    private final LayoutInflater mLayoutInflater;
    private final Utils mUtils;
    private final ContentResolver mContentResolver;
    private final TrashListFragment mTrashListFragment;

    public enum ARCHIVE_ACTION {
        RESTORE, DELETE
    }

    public TrashCustomAdapter(Context context, TrashListFragment trashListFragment) {
        super(context, android.R.layout.simple_list_item_2);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentResolver = getContext().getContentResolver();
        this.mTrashListFragment = trashListFragment;
        mUtils = new Utils(context);
    }


    @NonNull
    @Override
    public View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        final ViewHolder holder;
        final View view;
        if (convertView == null) {

            view = mLayoutInflater.inflate(R.layout.trash_row, parent, false);
            holder = new ViewHolder();
            SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(R.id.swipeLayout);
            holder.siteTitle = (TextView) view.findViewById(R.id.site_title);
            holder.siteUrl = (TextView) view.findViewById(R.id.site_url);
            holder.siteImage = (ImageView) view.findViewById(R.id.site_image);
            holder.trash = (ImageView) view.findViewById(R.id.trash_icon);
            holder.restoreButton = (Button) view.findViewById(R.id.trash_restore);
            final WebSite webSite = getItem(position);

            int _id = 0;
            if (webSite != null) {
                _id = webSite.get_id();
            }

            view.setTag(_id);


            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, view.findViewById(R.id.bottom_wrapper));
            swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
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

                }
            });
            swipeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView url = (TextView) view.findViewById(R.id.site_url);
                }
            });
            view.setTag(holder);

        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        final WebSite webSite = getItem(position);
        final String site_Guid;
        if (webSite != null) {
            site_Guid = webSite.getSiteGuid();

            final String siteTitle = webSite.getSiteTitle();
            final String siteUrl = webSite.getSiteUrl();
            final String siteImgUrl = webSite.getSiteImgUrl();
            ((TextView) view.findViewById(R.id.site_title)).setText(siteTitle);
            try {
                String cleanUrl = mUtils.getDomainName(siteUrl);
                ((TextView) view.findViewById(R.id.site_url)).setText(cleanUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
//            ((TextView) view.findViewById(R.id.site_url)).setText(siteUrl);
            if (!siteImgUrl.equals("")) {
                Picasso.with(getContext()).load(siteImgUrl).resize(50, 50).centerCrop().into((holder.siteImage));
            } else {
                Picasso.with(getContext()).load(Constants.DEFAULT_IMAGE).resize(50, 50).centerCrop().into((holder.siteImage));
            }
            final ImageView trashIcon = (ImageView) view.findViewById(R.id.trash_icon);
            trashIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googleAnalyticsAction(ARCHIVE_ACTION.DELETE);
                    mTrashListFragment.removeRowFromTrashList(position);
                    deleteWebSite(site_Guid);
                }
            });
            final Button restoreButton = (Button) view.findViewById(R.id.trash_restore);
            restoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googleAnalyticsAction(ARCHIVE_ACTION.RESTORE);
                    restoreWebSite(site_Guid);
                    mTrashListFragment.removeRowFromTrashList(position);
                }
            });
        }
        return view;
    }

    private void deleteWebSite(String site_guid) {
        Uri uri = TrashContract.Trash.buildTrashUri((String.valueOf(site_guid)));
        mContentResolver.delete(uri, SITE_GUID + " = ?", new String[]{String.valueOf(site_guid)});
        new DeleteSiteFromTrashTable(getContext(), site_guid);
    }

    private void restoreWebSite(String guid) {
        TrashLoader mTrashLoader = new TrashLoader(getContext(),  mContentResolver);
        mTrashLoader.restoreWebSite(guid);
        this.notifyDataSetChanged();
    }

    public void setData(List<WebSite> webSites) {
        clear();
        if (webSites != null) {
            for (WebSite webSite : webSites) {
                add(webSite);
            }
        }
        notifyDataSetChanged();
    }

    private void googleAnalyticsAction(ARCHIVE_ACTION action) {
        Tracker googleAnalytics = ((FeedMyRead) mTrashListFragment.getActivity().getApplication()).getTracker();
        switch (action) {
            case DELETE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Delete one site from archive")
                        .build());
                break;
            case RESTORE:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Restore one site from archive")
                        .build());
                break;

        }
    }

    private static class ViewHolder {
        public TextView siteTitle;
        public TextView siteUrl;
        public ImageView siteImage;
        public ImageView trash;
        public Button restoreButton;
        public int _id;
    }
}