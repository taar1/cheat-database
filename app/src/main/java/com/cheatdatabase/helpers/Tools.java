package com.cheatdatabase.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.mopub.mobileads.MoPubView;

import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tools. Copyright (c) 2010, 2011<br>
 *
 * @version 1.0
 */
@EBean
public class Tools {
    private final String TAG = Tools.class.getSimpleName();

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

		/*
         * Email format: A valid email address will have following format:
		 * [\\w\\.-]+: Begins with word characters, (may include periods and
		 * hypens).
		 * 
		 * @: It must have a '@' symbol after initial characters.
		 * ([\\w\\-]+\\.)+: '@' must follow by more alphanumeric characters (may
		 * include hypens.). This part must also have a "." to separate domain
		 * and subdomain names. [A-Z]{2,4}$ : Must end with two to four
		 * alaphabets. (This will allow domain names with 2, 3 and 4 characters
		 * e.g pa, com, net, wxyz)
		 * 
		 * Examples: Following email addresses will pass validation abc@xyz.net;
		 * ab.c@tx.gov
		 */

        // Initialize reg ex for email.
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        // Make the comparison case-insensitive.
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Errechnet die Differenz in Tagen zwischen 2 Daten.
     * http://www.kodejava.org/examples/90.html
     *
     * @param olderDate 2010-02-02
     * @param newerDate 2010-12-31
     * @return
     */
    public static long getDayDifference(String olderDate, String newerDate) {

        // FIXME Geht nicht ueber 2 Monate
        String[] olderDateSplit = olderDate.split("-");
        int oldYear = Integer.parseInt(olderDateSplit[0]);
        int oldMonth = Integer.parseInt(olderDateSplit[1]);
        int oldDay = Integer.parseInt(olderDateSplit[2]);

        String[] newerDateSplit = newerDate.split("-");
        int newYear = Integer.parseInt(newerDateSplit[0]);
        int newMonth = Integer.parseInt(newerDateSplit[1]);
        int newDay = Integer.parseInt(newerDateSplit[2]);

        Date d1 = new GregorianCalendar(oldYear, oldMonth, oldDay, 0, 0).getTime();
        Date d2 = new GregorianCalendar(newYear, newMonth, newDay, 0, 0).getTime();

        // Get msec from each, and subtract.
        long diff = d2.getTime() - d1.getTime();

        return (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * Gibt das aktuelle Datum aus.
     *
     * @param dateFormat z.B. "yyyy-MM-dd"
     * @return
     */
    public static String now(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    /**
     * Formatiert eine Zahl ins Wunschformat.
     *
     * @param number
     * @param format (z.B. "0.00")
     * @return
     */
    public static String formatNumber(int number, String format) {
        DecimalFormat df = new DecimalFormat(format);
        df.applyPattern("#,###,##0");
        return df.format(number);
    }

    /**
     * Entfernt ein allfällig führendes <br>
     * -Tag
     *
     * @param value
     * @return
     */
    public static String removeLeadingBr(String value) {
        if (value.length() > 4) {
            String retVal = value;
            String leadingbr = value.substring(0, 3);
            if (leadingbr.equals("<br>")) {
                retVal = value.substring(5);
            }
            return retVal;
        } else {
            return value;
        }
    }

    /**
     * Liest einen GameSystem-Namen anhand der übergebenen System-ID aus.
     *
     * @param activity
     * @param sysId
     * @return
     */
    public static String getSystemNameById(Activity activity, int sysId) {
        String finalSystemname = null;
        try {
            Resources res = activity.getResources();
            XmlResourceParser xrp = res.getXml(R.xml.systems);

            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("system")) {
                        String systemId = xrp.getAttributeValue(null, "sysId");
                        String systemName = xrp.getAttributeValue(null, "name");

                        if (systemId.equals(String.valueOf(sysId))) {
                            finalSystemname = systemName;
                            break;
                        }
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                } else if (xrp.getEventType() == XmlPullParser.TEXT) {
                }
                xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return finalSystemname;
    }

    /**
     * Check whether the SD card is readable.
     * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
     *
     * @return boolean
     */
    public static boolean isSdReadable() {
        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other
            // states, but all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }

        return mExternalStorageAvailable;
    }

    /**
     * Prüfen, ob die SD Karte beschreibbar ist.
     * http://developer.android.com/guide
     * /topics/data/data-storage.html#filesExternal
     *
     * @return boolean
     */
    public static boolean isSdWriteable() {
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other
            // states, but all we need
            // to know is we can neither read nor write
            mExternalStorageWriteable = false;
        }

        return mExternalStorageWriteable;
    }

    /**
     * Converts game list string to a Game array
     *
     * @param gameListString
     * @param systemId
     * @return Game[]
     */
    public static Game[] getGameListConvertStringToGameList(Activity activity, String gameListString, int systemId) {

        Game[] games = null;
        JSONArray jArray = null;

        try {

            jArray = new JSONArray(gameListString);
            games = new Game[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int gameId = jsonObject.getInt("id");
                String gameName = jsonObject.getString("name");
                int cheatCount = jsonObject.getInt("cnt");

                Game game = new Game();
                game.setSystemId(systemId);
                game.setSystemName(getSystemNameById(activity, systemId));
                game.setGameId(gameId);
                game.setGameName(gameName.replaceAll("\\\\", ""));
                game.setCheatsCount(cheatCount);

                games[i] = game;
            }

        } catch (JSONException e) {
            Log.e("getGameList", "JSON Parsing Error: " + e);
        }

        return games;
    }

    /**
     * Converts game list string to a Game array
     *
     * @param activity
     * @param gameListString
     * @return
     */
    public static Game[] getGameListConvertStringToGameList(Activity activity, String gameListString) {

        Game[] games = null;
        JSONArray jArray = null;

        try {

            jArray = new JSONArray(gameListString);
            games = new Game[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int gameId = jsonObject.getInt("id");
                String gameName = jsonObject.getString("name");
                int cheatCount = jsonObject.getInt("cnt");
                int system = jsonObject.getInt("s");

                Game game = new Game();
                game.setSystemId(system);
                game.setSystemName(getSystemNameById(activity, system));
                game.setGameId(gameId);
                game.setGameName(gameName.replaceAll("\\\\", ""));
                game.setCheatsCount(cheatCount);

                games[i] = game;
            }

        } catch (JSONException e) {
            Log.e("getGameList", "JSON Parsing Error: " + e);
        }

        return games;
    }

    /**
     * Get Systems from xml/systems.xml
     *
     * @param activity
     * @return
     */
    public ArrayList<SystemPlatform> getGameSystemsFromXml(Activity activity) {
        Log.d(TAG, "XXXXX getGameSystemsFromXml()");

        ArrayList<SystemPlatform> al = new ArrayList<>();

        try {
            XmlResourceParser xrp = activity.getResources().getXml(R.xml.systems);
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("system")) {
                        String systemId = xrp.getAttributeValue(null, "sysId");
                        String systemName = xrp.getAttributeValue(null, "name");

                        SystemPlatform gs = new SystemPlatform();
                        gs.setSystemId(Integer.parseInt(systemId));
                        gs.setSystemName(systemName);

                        al.add(gs);
                    }
                } else if (xrp.getEventType() == XmlPullParser.END_TAG) {
                } else if (xrp.getEventType() == XmlPullParser.TEXT) {
                }
                xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return al;

    }

    public SystemPlatform[] getSystems(Activity activity) {
        ArrayList<SystemPlatform> al = getGameSystemsFromXml(activity);
        SystemPlatform[] gsList = new SystemPlatform[al.size()];

        for (int i = 0; i < al.size(); i++) {
            gsList[i] = al.get(i);
        }

        return gsList;
    }

    public ArrayList<SystemPlatform> getSystemNamesFromXml(Activity activity) {
        return getGameSystemsFromXml(activity);
    }

    public SystemPlatform getSystemObjectByName(Activity activity, String systemName) {
        ArrayList<SystemPlatform> al = getGameSystemsFromXml(activity);
        for (int i = 0; i < al.size(); i++) {
            SystemPlatform sys = al.get(i);
            if (sys.getSystemName().equalsIgnoreCase(systemName)) {
                return sys;
            }
        }
        return null;
    }

//    public static void initGA(Activity activity, Tracker tracker, String screenLabel, String title, String description) {
//        tracker = GoogleAnalytics.getInstance(activity).getTracker(Konstanten.GOOGLE_ANALYTICS_ID);
//        HashMap<String, String> hitParameters = new HashMap<String, String>();
//        hitParameters.put(Fields.HIT_TYPE, "appview");
//        hitParameters.put(Fields.APP_VERSION, Konstanten.CURRENT_VERSION);
//        hitParameters.put(Fields.SCREEN_NAME, screenLabel);
//        hitParameters.put(Fields.DESCRIPTION, description);
//        hitParameters.put(Fields.TITLE, title);
//
//        tracker.send(hitParameters);
//    }

    /**
     * Convert unconverted Date to user's set Locale date format.
     *
     * @param unformatedDate , format yyyy-MM-dd HH:mm:ss
     * @param context
     * @return
     */
    public static String convertDateToLocaleDateFormat(String unformatedDate, Context context) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            cal.setTime(sdf.parse(unformatedDate));
            Date date = cal.getTime();
            java.text.DateFormat dateFormat = DateFormat.getDateFormat(context);
            return dateFormat.format(date);
        } catch (ParseException e) {
            Log.e("Error", e.getLocalizedMessage());
        }
        return unformatedDate;
    }

    public static Typeface getFont(AssetManager assetManager, String fontName) {
        return Typeface.createFromAsset(assetManager, fontName);
    }


    public static MoPubView initMoPubAdView(final Activity activity, MoPubView mAdView) {

//        mAdView = new MoPubView(activity);
        mAdView = (MoPubView) activity.findViewById(R.id.adview);

        try {
            String screen = activity.getString(R.string.screen_type);
            if (screen.equalsIgnoreCase("phone")) {
                mAdView.setAdUnitId(Konstanten.MOPUB_PHONE_UNIT_ID);
            } else {
                mAdView.setAdUnitId(Konstanten.MOPUB_TABLET_UNIT_ID);
            }

            mAdView.setAutorefreshEnabled(true);
            mAdView.setTesting(false);
            mAdView.setKeywords("m_age:15,m_gender:m,m_marital:single");
            mAdView.loadAd();
        } catch (Exception e) {
            Log.e("ADVIEW LOAD", e.getMessage());
            e.printStackTrace();
        }

        return mAdView;
    }

    public void loadAd(MoPubView mAdView, String screenType) {

        try {
            if (screenType.equalsIgnoreCase("phone")) {
                mAdView.setAdUnitId(Konstanten.MOPUB_PHONE_UNIT_ID);
            } else {
                mAdView.setAdUnitId(Konstanten.MOPUB_TABLET_UNIT_ID);
            }

            mAdView.setAutorefreshEnabled(true);
            mAdView.setTesting(false);
            mAdView.setKeywords("m_age:15,m_gender:m,m_marital:single");
            mAdView.loadAd();
        } catch (Exception e) {
            Log.e("ADVIEW LOAD", e.getMessage());
            e.printStackTrace();
        }
    }

    public void logout(Activity activity, Editor editor) {
        editor.remove(Konstanten.MEMBER_OBJECT);
        editor.commit();

        Toast.makeText(activity, R.string.logout_ok, Toast.LENGTH_LONG).show();
    }

    public static Toolbar initToolbarBase(AppCompatActivity a, Toolbar toolbar) {
        toolbar = (Toolbar) a.findViewById(R.id.toolbar);
        if (toolbar != null) {
            a.setSupportActionBar(toolbar);
        }
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        a.getSupportActionBar().setHomeButtonEnabled(true);

        return toolbar;
    }

    public static Intent setShareText(Activity act, Cheat visibleCheat) {
        String cheatShareTitle = String.format(act.getString(R.string.share_email_subject), visibleCheat.getGameName());
        String cheatShareBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
        cheatShareBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, cheatShareTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, cheatShareBody);
        return shareIntent;
    }

    public static void showSnackbar(View fromView, String message) {
        if ((message != null) && (fromView != null)) {
            Snackbar snackbar = Snackbar.make(fromView, message, Snackbar.LENGTH_LONG);
            snackbar.setText(message);
            snackbar.show();
        }
    }
}
