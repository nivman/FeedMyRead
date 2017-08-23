package com.dqsoftwaresolutions.feedMyRead;


public class Constants {

    //Constants gow web connections
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 100000;
    public static final int STATUS_ERROR = 400;
    public static final int STATUS_UNAUTHORIZED = 401;

    //Application key and secret that come from the server
    //access the API


    //URL to be used to access the API
    private static final String END_POINT = "http://feedmyread.site/api";
    public static final String LOGIN_URL = END_POINT + "/login.php";
    public static final String SIGNUP_URL = END_POINT + "/signup.php";
    public static final String FORGOT_PASSWORD_URL = END_POINT + "/forgotpassword.php";
    public static final String NEW_PASSWORD_URL = END_POINT + "/newpassword.php";
    public static final String SEND_DATA_TO_SERVER = END_POINT + "/SaveDataInServer.php";
    public static final String SET_TAGS_IN_SERVER = END_POINT + "/SetTagsInServer.php";
    public static final String SET_FAVORITE_STATUS_IN_SERVER = END_POINT + "/SetFavoriteStatus.php";
    public static final String SET_TAGS_FLAG_IN_WEBSITE_TABLE = END_POINT + "/SetTagFlagInWebSiteTable.php";
    public static final String SET_NEW_TIME_IN_USER_TABLE = END_POINT + "/SetNewTimeInUserTable.php";
    public static final String GET_TAGS_FROM_SERVER = END_POINT + "/GetTagsFromServer.php";
    public static final String GET_ALL_DATA_FROM_SERVER = END_POINT + "/GetAllDataFromServer.php";
    public static final String GET_ALL_DATA_FROM_TRASH = END_POINT + "/GetTrashFromServer.php";
    public static final String GET_ALL_SITE_GUID_FROM_WEBSITE_TABLE = END_POINT + "/GetAllSiteGuidFromWebSitesTable.php";
    public static final String GET_ALL_SITE_GUID_FROM_TRASH_TABLE = END_POINT + "/GetAllSiteGuidFromTrashTable.php";
    public static final String UPDATE_TAGS_INFO_FROM_TAGS_TABLE = END_POINT + "/UpdateTagsForFromTagsTable.php";
    public static final String UPDATE_TAGS_NAME_TAGS_TABLE = END_POINT + "/UpdateTagsNameTagsTable.php";
    public static final String DELETE_TAG_FROM_TAG_TABLE = END_POINT + "/DeleteTagFromTagsTable.php";
    public static final String DELETE_TAG_PAR_GUID= END_POINT + "/DeleteTagPerGuid.php";
    public static final String DELETE_TAGS_BY_GUID_FROM_TAG_TABLE = END_POINT + "/DeleteTagByGuidFromTagsTable.php";
    public static final String DELETE_SITE_FROM_TRASH_TABLE = END_POINT + "/DeleteSiteFromTrashTable.php";
    public static final String DELETE_ALL_SITES_FROM_TRASH_TABLE = END_POINT + "/DeleteAllTrashFromTrashTable.php";
    public static final String RESTORE_ALL_SITES_FROM_TRASH_TABLE = END_POINT + "/RestoreAllSitesFromTrashTable.php";
    public static final String MOVE_SITE_TO_TRASH_SERVER_TABLE = END_POINT + "/MoveSiteToTrashServerTable.php";
    public static final String COMPARE_UPDATE_TIME_IN_USRES_TABLE = END_POINT + "/CompareUpdateTimeInUsersTable.php";
    public static final String GET_TAGS_UPDATE = END_POINT + "/GetTagsUpdate.php";
    public static final String STATUS = "status";
    public static final String MESSAGE = "msg";
    // Constants used is JSON Parsing or values attached in a URL server connection

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String WEB_SITE_TITLE = "web_site_title";
    public static final String WEB_SITE_URL = "web_site_url";
    public static final String WEB_SITE_IMAGE = "web_site_image";
    public static final String WEB_SITE_FAVICON = "web_site_favicon";
    public static final String WEB_SITE_FAVORITE = "web_site_favorite";
    public static final String WEB_SITE_TAGS = "web_site_tags";
    public static final String WEB_SITE_CONTENT = "web_site_content";
    public static final String WEB_SITE_GUID = "web_site_guid";
    public static final String USER_TOKEN = "user_token";
    public static final String TAG_NAME = "tag_name";
    public static final String OLD_TAG_NAME = "old_tag_name";
    public static final String IS_TAG_EXISTS = "is_tag_exists";
    public static final String CONNECTION_MESSAGE = "No Internet Connection!";
    public static final String CHANGE_TIME = "changeTime";
    public static final String CHANGE_USER_TIME = "changeUserTime";
    // address to get websites content
    public static final String FULL_TEXT_FEED_END_POINT = "http://feedmyread.site/full-text-rss-3.7/makefulltextfeed.php?url=";
    public static final String FULL_TEXT_FEED_DETAILS = "&max=5&links=preserve&exc=1&summary=0&content=1&format=json&submit=Create+Feed";
//    "http://icons.iconarchive.com/icons/icons8/windows-8/256/City-No-Camera-icon.png"

    //set new saved website
    public static final String DEFAULT_IMAGE = "http://feedmyread.site/images/noimage.png";
    public static final String FAVICON_LINK = "https://www.google.com/s2/favicons?domain=";

    //fragment view
    public static final int SEARCH = 3;
    public static final int FAVORITE = 2;
    public static final int TAGS = 1;
    public static final int MAIN_LIST = 0;

    //html and css for webView
    public static final String HEAD_START_LINE = "<html><head><style>img{display: inline; height: auto; max-width: 100%;}</style> ";
    public static final String HEAD_BODY_HTML = "<meta name='viewport' content='width=device-width, initial-scale=1'></head><body>";
    public static final String BODY_HTML_CLOSER = "</body></html>";

    //rss url
    public static final String RSS_URL_ARTICLES ="https://newsapi.org/v1/articles?source=";
//    public static final String RSS_URL_SOURCE ="https://newsapi.org/v1/sources?language=en&";
//    public static final String RSS_URL_ARTICLES_SORT="&sortBy=";
//    public static final String RSS_URL_ARTICLES_TOP="top&";
//    public static final String RSS_URL_ARTICLES_LATEST="latest&";
//    public static final String RSS_URL_ARTICLES_POPULAR="popular&";
    public static final String RSS_URL_API_KEY ="apiKey=edbd3aa791304131b35695ee636daa00";

//

    public static final String GERMAN = "GERMAN";
    public static final String FRANCE = "FRANCE";
    public static final String ENGLISH = "ENGLISH";
    public static final String ITALY = "ITALY";
    public static final String CHINESE = "CHINESE";

//    public static final String ABC_NEWS_AU = "abc-news-au";
//    public static final String ARS_TECHNICA = "ars-technica";
//    public static final String ASSOCIATED_PRESS = "associated-press";
//    public static final String BBC_NEWS = "bbc-news";
//    public static final String BBC_SPORT = "bbc-sport";
//    public static final String BLOOMBERG = "bloomberg";
//    public static final String BUSINESS_INSIDER = "business-insider";
//    public static final String BUZZFEED = "buzzfeed";
//    public static final String CNBC = "cnbc";
//    public static final String CNN = "cnn";
//    public static final String DAILY_MAIL = "daily-mail";
//    public static final String ENGADGET = "engadget";
//    public static final String ENTERTAINMENT_WEEKLY = "entertainment-weekly";
//    public static final String ESPN = "espn";
//    public static final String FINANCIAL_TIMES = "financial-times";
//    public static final String FOCUS = "focus";
//    public static final String FORTUNE = "fortune";
//    public static final String FOUR_FOUR_TWO = "four-four-two";
//    public static final String GOOGLE_NEWS = "google-news";
//    public static final String HACKER_NEWS = "hacker-news";
//    public static final String IGN = "ign";
//    public static final String INDEPENDENT = "independent";
//    public static final String MASHABLE = "mashable";
//    public static final String METRO = "metro";
//    public static final String MIRROR = "mirror";
//    public static final String MTV_NEWS = "mtv-news";
//    public static final String NATIONAL_GEOGRAPHIC = "national-geographic";
//    public static final String NEW_SCIENTIST = "new-scientist";
//    public static final String NEWSWEEK = "newsweek";
//    public static final String NEW_YORK_MAGAZINE = "new-york-magazine";
//    public static final String POLYGON = "polygon";
//    public static final String RECODE = "recode";
//    public static final String REDDIT_R_ALL = "reddit-r-all";
//    public static final String REUTERS = "reuters";
//    public static final String SKY_NEWS = "sky-news";
//    public static final String SKY_SPORTS_NEWS = "sky-sports-news";
//    public static final String TECHCRUNCH = "techcrunch";
//    public static final String THE_ECONOMIST = "the-economist";
//    public static final String THE_GUARDIAN_UK = "the-guardian-uk";
//    public static final String THE_GUARDIAN_AU = "the-guardian-au";
//    public static final String THE_HINDU = "the-hindu";
//    public static final String THE_HUFFINGTON_POST = "the-huffington-post";
//    public static final String THE_LAD_BIBLE = "the-lad-bible";
//    public static final String THE_NEW_YORK_TIMES = "the-new-york-times";
//    public static final String THE_NEXT_WEB = "the-next-web";
//    public static final String THE_SPORT_BIBLE = "the-sport-bible";
//    public static final String THE_TELEGRAPH = "the-telegraph";
//    public static final String THE_TIMES_OF_INDIA = "the-times-of-india";
//    public static final String THE_VERGE = "the-verge";
//    public static final String THE_WALL_STREET_JOURNAL = "the-wall-street-journal";
//    public static final String THE_WASHINGTON_POST = "the-washington-post";
//    public static final String TIME = "time";
//    public static final String USA_TODAY = "usa-today";

    public static final String[] RSS_ARTICLES_SOURCE_LIST ={"abc-news-au", "ars-technica", "associated-press","bbc-news","bbc-sport","bloomberg","business-insider","business-insider",
            "buzzfeed","cnbc","cnn", "daily-mail","engadget","entertainment-weekly","espn","financial-times", "focus","fortune","four-four-two","google-news", "hacker-news","ign",
            "independent","mashable","metro","mirror","mtv-news","new-scientist","newsweek","new-york-magazine","polygon","recode", "reddit-r-all","reuters","sky-news","sky-sports-news",
            "techcrunch","the-economist","the-guardian-uk","the-guardian-au","the-hindu","the-huffington-post","the-lad-bible","the-new-york-times","the-next-web","the-sport-bible",
            "the-telegraph","the-times-of-india","the-verge","the-wall-street-journal","the-washington-post", "time","usa-today"

    };
//    public static final String [] RSS_ARTICLES_LIST_CATEGORY ={"business", "entertainment", "gaming", "general","music", "science-and-nature", "sport", "technology"};
}
