package com.cheatdatabase.helpers;

public class Konstanten {

    public final static String CURRENT_VERSION = "2.4.1";
    public final static int DEFAULT_TOTAL_CHEATS = 161000;

    public static final int INJECT_AD_AFTER_EVERY_POSITION = 20;

    // URL Konstanten
    public final static String BASE_URL = "https://www.cheat-database.com/";
    public final static String BASE_URL_ANDROID = "https://www.cheat-database.com/android/json/";
    public final static String SCREENSHOT_ROOT_WEBDIR = "https://www.cheat-database.com/cheatpics/";
    public final static String WEBDIR_MEMBER_AVATAR = "https://www.cheat-database.com/getblobimage.php?blobid=";
    public final static String CHEATDATABASE_ANDROID_ICON_512 = "https://www.cheat-database.com/images/android_icons/cheat-database_icon_512x512.png";

    public static final int LOGIN_REGISTER_FAIL_RETURN_CODE = 0;
    public static final int LOGIN_REGISTER_OK_RETURN_CODE = 1;
    public static final int LOGIN_SUCCESS_RETURN_CODE = 1;
    public static final int REGISTER_SUCCESS_RETURN_CODE = 2;
    public static final int RECOVER_PASSWORD_SUCCESS_RETURN_CODE = 3;
    public static final int RECOVER_PASSWORD_ATTEMPT = 4;
    public static final int REGISTER_ATTEMPT = 5;

    public final static int HTTP_FILE_NOT_FOUND = 404;

    public final static String DUMMY_DATE = "1999-01-01";
    public final static String PREF_LAST_SYSTEM_SELECTED_VARIABLE = "lastSystemIDSelected";
    public final static String PREF_POSITION_OF_LAST_SELECTED_SYSTEM_IN_SPINER = "lastSelectedSystemSpinnerPosition";
    public final static String PREF_LATEST_INFOSCREEN_VERSION_SEEN_VARIABLE = "latestInfoscreenVersion";
    public final static String PREF_NEWCHEAT1_VARIABLE = "newCheat1";
    public final static String PREF_NEWCHEAT2_VARIABLE = "newCheat2";
    public final static String PREF_NEWCHEAT3_VARIABLE = "newCheat3";
    public final static String PREF_NEWCHEATS_LAST_UPDATE_VARIABLE = "newCheatsLastUpdate"; // YYYY-MM-DD
    public final static String PREF_TOTAL_CHEATS_VARIABLE = "totalCheats";
    public final static String PREF_SHOW_WELCOME_SCREEN = "showWelcomeScreen";
    public final static String PREF_FULLTEXT_SEARCH_SEARCHSTRING = "fulltextSearchString";
    public final static String PREFERENCES_FILE = "MyPrefs";
    public final static String PREFERENCES_PAGE_SELECTED = "PageSelected";
    public final static String PREFERENCES_FAV_PAGE_SELECTED = "FavPageSelected";
    public final static String PREFERENCES_TEMP_GAME_OBJECT_VIEW = "currentViewGameObj";
    public final static String PREFERENCES_TEMP_CHEAT_ARRAY_OBJECT_VIEW = "currentViewCheatArrObj";
    public final static String PREFERENCES_SELECTED_DRAWER_FRAGMENT_ID = "selectedDrawerFragmentId";

    // Wird zum Zwischenspeichern des letzten Ratings verwendet
    public final static String TMP_MEMBER_RATING = "tmpMemberRating";

    // Member Variablen
    public final static String MEMBER_OBJECT = "member_object";
    public final static String MEMBER_ID = "member_id";
    public final static String MEMBER_EMAIL = "member_email";
    public final static String MEMBER_PASSWORD = "member_password";
    public final static String MEMBER_USERNAME = "member_username";
    public final static String MEMBER_BANNED = "member_banned";

    // Position in systems.xml file
    public final static String GOOGLE_ANALYTICS_ID = "UA-6697147-2";
    public final static int TABLE_ROW_MINIMUM_WIDTH = 200;
    public final static int CHEAT_DAY_AGE_SHOW_NEWADDITION_ICON = 10;

    // LANGUAGES
    public static final int ENGLISH = 1;
    public static final int GERMAN = 2;

    public static final int FORMAT_PLAIN = 1;
    public static final int FORMAT_WALKTHROUGH = 2;

    // ADS
    public final static String MOPUB_PHONE_UNIT_ID = "2492994e324a43dd89367f60df00b55b";
    public final static String MOPUB_TABLET_UNIT_ID = "3fa5a07f53664729a9f5cae4b91a0eb8";
    public final static String APPFLOOD_APP_KEY = "4vlWLl4AADI0tBrm";
    public final static String APPFLOOD_SECRET_KEY = "bQ7IdNj72fdeL52e2e134";
    public final static String MOBILECORE_DEVELOPER_HASH = "199AFTT5YE9OVNXRKEAT08E4BWV2T";
    public final static String MDOTM_API_KEY = "ee6de4fe5b8fde6a06f161973f712ded";
    public final static String AMAZON_API_KEY = "cf2064f5f51249ecbd6b86f6c86d382a";
    public final static String AMAZON_APP_ID = "amzn1.devportal.mobileapp.cf2064f5f51249ecbd6b86f6c86d382a";
    public final static String AMAZON_RELEASE_ID = "amzn1.devportal.apprelease.a4e1dfeddc7143f581aaea7999f9f1b7";
    public final static String OGURY_API_KEY = "264890";
    public final static String FACEBOOK_AUDIENCE_NETWORK_NATIVE_BANNER_ID = "148040821872637_2083092671700766";
    public final static String FACEBOOK_AUDIENCE_NETWORK_NATIVE_AD_IN_RECYCLER_VIEW = "148040821872637_2645622672114427";

    //public final static String INMOBI_APP_ID = "4028cba631d63df10132325b781205a2";
    public final static String INMOBI_CHEATDATABASE_APP_ID = "1489144176729570";

    public final static String INMOBI_BANNER_PLACEMENT_ID = "1431977578836721";
    public final static String INMOBI_BANNER_SITE_ID = "1d5e79641a5441fb9887745cdcf005ec";

    public final static String INMOBI_RECYCLERVIEW_LIST_ITEM_PLACEMENT_ID = "1563695474851";
    public final static String INMOBI_RECYCLERVIEW_LIST_ITEM_SITE_ID = "4f8d0ce7ac55411b8852f1da5bd52590";

    public final static String INMOBI_FULLSCREEN_PLACEMENT_ID = "1566307508743";
    public final static String INMOBI_FULLSCREEN_SITE_ID = "e41d5fd5b0c5494fb178afb7f7698fb4";

    // SD CARD SETTINGS
    public final static String APP_PATH_SD_CARD = "/Android/data/com.cheatdatabase/files/";

    public final static String FONT_BOLD = "Lato-Bold.ttf";
    public final static String FONT_LIGHT = "Lato-Light.ttf";
    public final static String FONT_REGULAR = "Lato-Regular.ttf";

    // COLORS
    public final static int COBALT_DARK = 0xFF003f50;
    public final static int CYAN_DARK = 0xFF006464;
    public final static int LIGHT_GREEN = 0xFF336600;
    public final static int SUBMIT_CHEAT_PINK = 0xFFb00072;
    public final static int PURPLE_DARK = 0xFF420058;
    public final static int PURPLE_BRIGHT = 0xFF9900CC;
    public final static int RED_DARK = 0xFFAA0000;

    public final static String ACHIEVEMENTS = "achievements";
    public final static String NO_ACHIEVEMENTS = "noAchievements";

}
