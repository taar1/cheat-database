package com.cheatdatabase.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cheatdatabase.R;
import com.cheatdatabase.callbacks.GenericCallback;
import com.cheatdatabase.data.RoomCheatDatabase;
import com.cheatdatabase.data.dao.FavoriteCheatDao;
import com.cheatdatabase.data.model.Cheat;
import com.cheatdatabase.data.model.Game;
import com.cheatdatabase.data.model.Member;
import com.cheatdatabase.data.model.Screenshot;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ActivityContext;
import needle.Needle;

/**
 * Tools. Copyright (c) 2010, 2011<br>
 *
 * @version 1.0
 */
public class Tools {
    private final String TAG = Tools.class.getSimpleName();

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private final Context context;

    @Inject
    public Tools(@ActivityContext Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(Konstanten.PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public SharedPreferences.Editor getPreferencesEditor() {
        return editor;
    }

    public boolean getBooleanFromSharedPreferences(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public Member getMember() {
        return new Gson().fromJson(sharedPreferences.getString(Konstanten.MEMBER_OBJECT, null), Member.class);
    }

    /**
     * Writes the member (after register or after login) to the sharedPreferences.
     *
     * @param member
     */
    public void putMember(Member member) {
        editor.putString(Konstanten.MEMBER_OBJECT, new Gson().toJson(member));
        editor.apply();
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void removeValue(String key) {
        editor.remove(key);
        editor.apply();
    }

    public boolean isEmailValid(String email) {
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

        // We can only read the media
        // Something else is wrong. It may be one of many other
        // states, but all we need
        // to know is we can neither read nor write
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else mExternalStorageAvailable = Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);

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
        JSONArray jArray;

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

//    /**
//     * Get Systems from xml/systems.xml
//     *
//     * @param activity
//     * @return
//     */
//    public static List<SystemModel> getGameSystemsFromXml(Activity activity) {
//        ArrayList<SystemModel> platformArrayList = new ArrayList<>();
//
//        try {
//            XmlResourceParser xrp = activity.getResources().getXml(R.xml.systems);
//            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
//                if (xrp.getEventType() == XmlPullParser.START_TAG) {
//                    String s = xrp.getName();
//                    if (s.equals("system")) {
//                        String systemId = xrp.getAttributeValue(null, "sysId");
//                        String systemName = xrp.getAttributeValue(null, "name");
//
//                        SystemModel gs = new SystemModel();
//                        gs.setSystemId(Integer.parseInt(systemId));
//                        gs.setSystemName(systemName);
//
//                        platformArrayList.add(gs);
//                    }
//                }
//                xrp.next();
//            }
//        } catch (XmlPullParserException | IOException e) {
//            e.printStackTrace();
//        }
//
//        return platformArrayList;
//    }
//
//    public SystemModel[] getSystems(Activity activity) {
//        List<SystemModel> al = getGameSystemsFromXml(activity);
//        SystemModel[] gsList = new SystemModel[al.size()];
//
//        for (int i = 0; i < al.size(); i++) {
//            gsList[i] = al.get(i);
//        }
//
//        return gsList;
//    }
//
//    public List<SystemModel> getSystemNamesFromXml(Activity activity) {
//        return getGameSystemsFromXml(activity);
//    }
//
//    public static SystemModel getSystemObjectByName(Activity activity, String systemName) {
//        List<SystemModel> al = getGameSystemsFromXml(activity);
//        for (int i = 0; i < al.size(); i++) {
//            SystemModel sys = al.get(i);
//            if (sys.getSystemName().equalsIgnoreCase(systemName)) {
//                return sys;
//            }
//        }
//        return null;
//    }

    /**
     * Convert unconverted Date to user's set Locale date format.
     *
     * @param unformatedDate , format yyyy-MM-dd HH:mm:ss
     * @return
     */
    public String convertDateToLocaleDateFormat(String unformatedDate) {
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

    public void logout() {
        removeValue(Konstanten.MEMBER_OBJECT);
        //Toast.makeText(context, R.string.logout_ok, Toast.LENGTH_LONG).show();
    }

    public Toolbar initToolbarBase(AppCompatActivity a, Toolbar toolbar) {
        toolbar = a.findViewById(R.id.toolbar);
        if (toolbar != null) {
            a.setSupportActionBar(toolbar);
        }
        a.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        a.getSupportActionBar().setHomeButtonEnabled(true);

        return toolbar;
    }

    public Intent setShareText(Cheat visibleCheat) {
        String cheatShareTitle = String.format(context.getString(R.string.share_email_subject), visibleCheat.getGameName());
        String cheatShareBody = visibleCheat.getGameName() + " (" + visibleCheat.getSystemName() + "): " + visibleCheat.getCheatTitle() + "\n";
        cheatShareBody += Konstanten.BASE_URL + "display/switch.php?id=" + visibleCheat.getCheatId() + "\n\n";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, cheatShareTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, cheatShareBody);
        return shareIntent;
    }

    public void showSnackbar(View fromView, String message) {
        showSnackbar(fromView, message, BaseTransientBottomBar.LENGTH_LONG);
    }

    public Snackbar showSnackbar(View fromView, String message, int duration) {
        Snackbar snackbar = Snackbar.make(fromView, message, Snackbar.LENGTH_LONG);
        snackbar.setText(message);
        snackbar.setDuration(duration);

        Needle.onMainThread().execute(() -> {
            if ((message != null) && (fromView != null)) {
                snackbar.show();
            }
        });
        return snackbar;
    }

    public void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    public void saveScreenshotsToSdCard(Cheat cheat, GenericCallback callback) {
        Needle.onBackgroundThread().execute(() -> {
            try {
                for (Screenshot s : cheat.getScreenshotList()) {
                    if (Tools.isSdWriteable()) {
                        s.saveToSd();
                    }
                }

                callback.success();
            } catch (IOException e) {
                callback.fail(e);
            }
        });
    }

    /**
     * Sharing feature for tablets where the sharing button is not inside the action bar.
     *
     * @param cheat
     */
    public void shareCheat(Cheat cheat) {
        String shareText = new StringBuilder(cheat.getGameName())
                .append(" - ")
                .append(cheat.getCheatTitle())
                .append(" (")
                .append(cheat.getSystemName())
                .append(")\n")
                .append("https://cheat-database.com/display/switch.php?id=")
                .append(cheat.getCheatId())
                .toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public void addFavorite(Cheat visibleCheat, int memberId, GenericCallback callback) {
        Needle.onBackgroundThread().execute(() -> {
            FavoriteCheatDao dao = RoomCheatDatabase.getDatabase(context).favoriteDao();
            long insertReturn = dao.insert(visibleCheat.toFavoriteCheatModel(memberId));
            if (insertReturn > 0) {
                saveScreenshotsToSdCard(visibleCheat, callback);
            } else {
                callback.fail(null);
            }
        });
    }
}
