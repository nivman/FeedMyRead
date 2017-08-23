package com.dqsoftwaresolutions.feedMyRead;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dqsoftwaresolutions.feedMyRead.data.Rss;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RssTabFragment extends ListFragment {
    private final String LOG_TAG = RssTabFragment.class.getSimpleName();
    private RssFeedAdapter mRssFeedAdapter;
    private SwipeRefreshLayout mSwipeRefreshRssListLayout;
    private final List<Rss> mRssList = new ArrayList<>();
    private String listPosition = "top";
    private ProgressBar mProgressBar;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<String> urls = setRssSourceInList();

        DownloadRssData getRssData = new DownloadRssData(urls);
        getRssData.execute();
        mRssFeedAdapter = new RssFeedAdapter(getContext(), RssTabFragment.this);
        setListAdapter(mRssFeedAdapter);
        setListShown(true);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.rss_tab_fragment, container, false);
        View superview = super.onCreateView(inflater, (ViewGroup) rootView, savedInstanceState);
        FrameLayout listContainer = (FrameLayout) rootView.findViewById(R.id.rss_list_container);
        mProgressBar =(ProgressBar)  rootView.findViewById(R.id.rss_progress_bar);

        ListView rssListView = (ListView) rootView.findViewById(android.R.id.list);
        final TextView newsApiLink =(TextView) rootView.findViewById(R.id.news_api_link);
        newsApiLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsApiLink.setMovementMethod(LinkMovementMethod.getInstance());
                newsApiLink.setText( Html.fromHtml( "https://newsapi.org/") );
            }
        });
        rssListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int lastVisibleItem;
            private int totalItemCount;
            private boolean isEndOfList;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.totalItemCount = totalItemCount;
                this.lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
                // prevent checking on short lists
                if (totalItemCount > visibleItemCount)
                    checkEndOfList();
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // state doesn't matter
            }

            private synchronized void checkEndOfList() {
                // trigger after 2nd to last item
                if (lastVisibleItem >= (totalItemCount - 2)) {
                    if (!isEndOfList) {

                        List<String> urls = setRssSourceInList();
                        DownloadRssData getRssData = new DownloadRssData(urls);
                        getRssData.execute();
                        listPosition = "bottom";
                    }
                    isEndOfList = true;
                } else {
                    isEndOfList = false;
                }
            }
        });
        mSwipeRefreshRssListLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout_listView);
        mSwipeRefreshRssListLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        List<String> urls = setRssSourceInList();
                        DownloadRssData getRssData = new DownloadRssData(urls);
                        getRssData.execute();
                        mSwipeRefreshRssListLayout.setRefreshing(true);
                        listPosition = "top";
                    }
                });

        listContainer.addView(superview);
        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }
    private void goToNewsAPI(){
        Log.d("TEST","TEST");
    }
    private List<String> setRssSourceInList() {
        List<String> urls = new ArrayList<>();
        int[] randomNumbers = setRandomNumbers();
        for (int i = 0; i < 10; ++i) {
            StringBuilder baseUrl = new StringBuilder(Constants.RSS_URL_ARTICLES);
            int random = randomNumbers[i];
            baseUrl.append(Constants.RSS_ARTICLES_SOURCE_LIST[random]).append("&");
            baseUrl.append(Constants.RSS_URL_API_KEY);
            urls.add(baseUrl.toString());

        }

        return urls;
    }

    private int[] setRandomNumbers() {
        int[] randomNumbers = new int[10];
        for (int i = 0; i < randomNumbers.length; i++) {
            randomNumbers[i] = randomFill();

        }


        return randomNumbers;
    }

    private static int randomFill() {
        Random rand = new Random();
        return rand.nextInt(43 - 1) + 1;
    }

    private void setRssData(List<Rss> data) {

        Collections.shuffle(data);

        if (isResumed()) {
            if (listPosition.equals("top")) {
                mRssFeedAdapter.setClearData(data);
            } else {
                mRssFeedAdapter.addData(data);
            }
            setListShown(true);
            mProgressBar.setVisibility(View.GONE);
        } else {
        //    setListShownNoAnimation(true);
        }
    }

    public class DownloadRssData extends AsyncTask<String, Void, List<String>> {
        private final List<String> mNewApi;

        final List<String> sourceList = new ArrayList<>();

        public DownloadRssData(List<String> newsApiOrg) {
            mNewApi = newsApiOrg;
        }

        @Override
        protected List<String> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder buffer;

            if (params == null) {
                return null;
            }
            try {
                for (int i = 0; i < mNewApi.size(); ++i) {
                    URL url = new URL(mNewApi.get(i));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    InputStream inputStream = urlConnection.getInputStream();
                    if (inputStream == null) {
                        return null;
                    }
                    buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                        sourceList.add(buffer.toString());
                    }

                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error closing stream");
                    }
                }
            }
            return sourceList;
        }

        @Override
        protected void onPostExecute(List<String> sourceList) {


            if (sourceList != null) {
                try {

                    String image;
                    for (int j = 0; j < sourceList.size(); j++) {
                        JSONObject rssList = new JSONObject(sourceList.get(j));
                        JSONArray articles = rssList.getJSONArray("articles");
                        String articleSource = (String) rssList.get("source");
                        for (int i = 0; i < articles.length(); ++i) {
                            JSONObject article = new JSONObject(articles.get(i).toString());
                            String title = (String) article.get("title");

                            if (!article.get("urlToImage").toString().equals("null")) {
                                image = (String) article.get("urlToImage");
                            } else {
                                image = Constants.DEFAULT_IMAGE;
                            }
                            String url = (String) article.get("url");
                            Rss rss = new Rss(title, image, url, articleSource);
                            mRssList.add(rss);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setRssData(mRssList);
                mSwipeRefreshRssListLayout.setRefreshing(false);
            }
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.rss_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
                    default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        List<String> urls = setRssSourceInList();
        DownloadRssData getRssData = new DownloadRssData(urls);
        getRssData.execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) getActivity().getApplication()).getTracker();
        googleAnalytics.setScreenName("Rss feed list");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
//"http://www.rssmix.com/u/8218075/rss.json"