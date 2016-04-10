package com.cheatdatabase.helpers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.cheatdatabase.R;
import com.cheatdatabase.businessobjects.Cheat;
import com.cheatdatabase.businessobjects.ForumPost;
import com.cheatdatabase.businessobjects.Game;
import com.cheatdatabase.businessobjects.Member;
import com.cheatdatabase.businessobjects.Screenshot;
import com.cheatdatabase.businessobjects.SystemPlatform;
import com.cheatdatabase.businessobjects.WelcomeMessage;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * REST Calls class.
 */
public class Webservice {

    private static final String TAG = Webservice.class.getSimpleName();

    /**
     * Holt eine Liste von Games von einem System anhand des übergebenen
     * Suchstrings.
     *
     * @param searchString
     * @param systemId
     * @return
     */
    public static String searchGamesAsString(String searchString, int systemId) {
        String ret = null;
        try {
            String urlParameters = "q=" + URLEncoder.encode(searchString.trim(), "UTF-8") + "&s=" + URLEncoder.encode(String.valueOf(systemId), "UTF-8") + "&v=" + URLEncoder.encode(Konstanten.CURRENT_VERSION, "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "searchGames.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Search for Games and return a JSON String.
     *
     * @param searchString
     * @return
     */
    public static String searchGamesAsString(String searchString) {
        String ret = null;
        try {
            String urlParameters = "q=" + URLEncoder.encode(searchString.trim(), "UTF-8") + "&v=" + URLEncoder.encode(Konstanten.CURRENT_VERSION, "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "searchGames.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Sendet die Logindaten an einen E-Mail Account
     *
     * @param email
     * @return
     */
    public static int sendLoginData(String email) {
        int responseCode = R.string.err_email_invalid;

        if (Tools.isEmailValid(email)) {
            String urlParameters = null;
            try {
                urlParameters = "email=" + URLEncoder.encode(email, "UTF-8");
                String response = excutePost(Konstanten.BASE_URL_ANDROID + "sendLoginData.php", urlParameters);

                if (response.equalsIgnoreCase("ok")) {
                    responseCode = R.string.login_sent_ok;
                } else if (response.equalsIgnoreCase("no_user_found")) {
                    responseCode = R.string.err_email_user_not_found;
                } else {
                    // invalid_email
                    responseCode = R.string.err_email_invalid;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            responseCode = R.string.err_email_invalid;
        }

        return responseCode;
    }

    /**
     * Universal Search
     *
     * @param searchString
     * @return
     */
    public static String universalGameSearch(String searchString) {
        String ret = null;
        try {
            String urlParameters = "q=" + URLEncoder.encode(searchString.trim(), "UTF-8") + "&v=" + URLEncoder.encode(Konstanten.CURRENT_VERSION, "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "universalGameSearch.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Holt alle Nicht-Walkthrough Cheats anhand der übergebenen Game-ID und
     * liefert sie als String zurück.
     *
     * @param gameId
     * @param memberId
     * @return
     */
    public static String getCheatListAsString(int gameId, int memberId) {
        String ret = null;
        try {
            String urlParameters = "memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8") + "&gameId=" + URLEncoder.encode(String.valueOf(gameId), "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatsAndRatingByGameId.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Holt alle Cheat-IDs, -Titel und -Inhalte von Nicht-Walkthroughs anhand
     * der übergebenen Game-ID und liefert sie als String zurück.
     *
     * @param gameId
     * @return
     */
    public static String getCheatTitleListAsString(int gameId) {
        String ret = null;
        try {
            String urlParameters = "gameId=" + URLEncoder.encode(String.valueOf(gameId), "UTF-8") + "&loadContent=" + URLEncoder.encode("1", "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatTitlesByGameId.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Richtet einen neuen Benutzer ein oder Liefert eine Fehlermeldung zurück.
     * Member-Daten bei Erfolg. Ansonsten String mit Error Kurzmeldung.
     *
     * @param username
     * @param email
     * @return err_invalidemail=E-Mail ungültig, err_email=E-Mail existiert
     * schon, err_user=Username existiert schon, err_length=Eine Eingabe
     * ist zu kurz, INTEGER=OK (Member-ID)
     */
    public static Member register(String username, String email) {

        Member member = new Member();

        try {
            if (Tools.isEmailValid(email)) {
                String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8") + "&email=" + URLEncoder.encode(email, "UTF-8");
                String responseString = excutePost(Konstanten.BASE_URL_ANDROID + "register.php", urlParameters);

                if (responseString.length() > 1) {
                    JSONObject jsonObject = null;

                    jsonObject = new JSONObject(responseString);

                    int mid = jsonObject.getInt("id");
                    // String username_returned =
                    // jsonObject.getString("username");
                    // String email_returned = jsonObject.getString("email");
                    String password = jsonObject.getString("password");

                    member.setMid(mid);
                    member.setUsername(username);
                    member.setPassword(password);
                    member.setEmail(email);
                    member.setErrorCode(0);

                } else {
                    if (responseString.equalsIgnoreCase("")) {
                        member.setErrorCode(4);
                    } else {
                        member.setErrorCode(Integer.parseInt(responseString));
                    }
                }
            } else {
                // Email invalid
                member.setErrorCode(1);
            }
        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getMessage());
            e.printStackTrace();
            member.setErrorCode(4);
        }

        return member;

    }

    /**
     * Loginprozess: MEMBER_ID = Erfolgreich, 0 = nicht erfolgreich
     *
     * @param username / email
     * @param password
     * @return
     */
    public static Member login(String username, String password) {

        String responseString = null;
        Member member = null;
        String password_md5 = password.trim();

        try {
            // MD5 Hash vom Passwort generieren
            password_md5 = AeSimpleMD5.MD5(password);
            String urlParameters = "username=" + URLEncoder.encode(username, "UTF-8") +
                    "&password=" + URLEncoder.encode(password_md5, "UTF-8");
            responseString = excutePost(Konstanten.BASE_URL_ANDROID + "login_md5.php", urlParameters);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // 0 = Fehler beim Login
        if (responseString != null) {
            if (!responseString.equalsIgnoreCase("0")) {
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(responseString);

                    int mid = jsonObject.getInt("id");
                    String username2 = jsonObject.getString("username");
                    String email = jsonObject.getString("email");
                    // String banned = jsonObject.getString("banned");
                    int banned_b = jsonObject.getInt("banned_b");

                    member = new Member();
                    member.setMid(mid);
                    member.setUsername(username2);
                    member.setPassword(password);
                    member.setEmail(email);

                    if (banned_b == 1) {
                        member.setBanned(true);
                    } else {
                        member.setBanned(false);
                    }

                } catch (JSONException e) {
                    Log.e("login", "JSON Parsing Error: " + e);
                }
            }
        } else {
            Log.d("responseString is NULL", "0");
        }

        return member;
    }

    /**
     * Sendet einen Cheat ein.
     *
     * @param memberId
     * @param gameId
     * @param cheatTitle
     * @param cheatText
     * @return 1 = Member eligible for PocketChange, 0 = Member NOT eligible for
     * PocketChange
     */
    public static int insertCheat(int memberId, int gameId, String cheatTitle, String cheatText) {
        int ret = 0;
        try {
            String urlParameters = "memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8") +
                    "&gameId=" + URLEncoder.encode(String.valueOf(gameId), "UTF-8") +
                    "&cheatTitle=" + URLEncoder.encode(cheatTitle, "UTF-8") +
                    "&cheatText=" + URLEncoder.encode(cheatText, "UTF-8");
            ret = Integer.parseInt(excutePost(Konstanten.BASE_URL_ANDROID + "submitCheat.php", urlParameters));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Macht einen Forumeintrag.
     *
     * @param cheatId
     * @param memberId
     * @param password
     * @param forumPost
     */
    public static void insertForum(int cheatId, int memberId, String password, String forumPost) {
        String urlParameters = null;
        try {
            urlParameters = "mid=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8") + "&cid=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8") + "&forumpost=" + URLEncoder.encode(forumPost, "UTF-8");
            excutePost(Konstanten.BASE_URL_ANDROID + "insertForum.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bewertet einen Cheat. Wenn noch kein Benutzerkonto eingerichtet wurde,
     * wird dieses vorher automatisch erstellt. Besteht noch kein Rating, wird
     * eins eingetragen, ansonsten wird das Rating von diesem User mit dem neuen
     * überschrieben.
     *
     * @param memberId
     * @param cheatId
     * @param rating
     * @return 1 = INSERT, 2 = UPDATE
     */
    public static int rateCheat(int memberId, int cheatId, int rating) {
        int ret = 0;
        try {
            String urlParameters = "mid=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8") + "&cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8") + "&rating=" + URLEncoder.encode(String.valueOf(rating), "UTF-8");
            ret = Integer.parseInt(excutePost(Konstanten.BASE_URL_ANDROID + "rateCheatWithoutPw.php", urlParameters));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return ret;
    }

    /**
     * Holt das Rating des Benutzers von diesem Cheat.
     *
     * @param memberId
     * @param cheatId
     * @return int (1-10)
     */
    public static float getCheatRatingByMemberId(int memberId, int cheatId) {
        float ret = 0;
        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8") + "&memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8");
            // Return Value 1-10
            ret = Float.parseFloat(excutePost(Konstanten.BASE_URL_ANDROID + "getRatingByMemberId.php", urlParameters));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return ret;
    }

    /**
     * Holt die URLs zu den Bildern mit Dateigrüsse von einem Cheat.
     *
     * @param cheatId
     * @return Array(String[Filename, Filegrüsse])
     */
    public static ArrayList<String[]> getImageListByCheatId(int cheatId) {
        JSONArray jsonArray = null;
        ArrayList<String[]> al = null;

        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8");
            String responseString = excutePost(Konstanten.BASE_URL_ANDROID + "getImagesByCheatId.php", urlParameters);

            jsonArray = new JSONArray(responseString);
            al = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String url = jsonObject.getString("url");
                String size = jsonObject.getString("size");

                String[] screenshot = {url, size};

                al.add(screenshot);
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getLocalizedMessage());
        }

        return al;
    }


    /**
     * Holt drei der 20 neusten Cheats zufüllig aus der Datenbank und liefert
     * sie als Cheat-Array zurück (inkl. Anzahl Cheats in der DB)
     *
     * @return
     */
    public static Cheat[] getInitialInformation() {
        int ANZAHL = 3;
        String newCheats = executeGet(Konstanten.BASE_URL_ANDROID + "getNewAndTotalCheats.php");

        Cheat[] cheats = new Cheat[ANZAHL];

        JSONArray jArray;
        try {
            jArray = new JSONArray(newCheats);

            for (int i = 0; i < ANZAHL; i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int gameIdValue = jsonObject.getInt("gid");
                int cheatIdValue = jsonObject.getInt("cid");
                String gameNameValue = jsonObject.getString("name");
                String titleValue = jsonObject.getString("title");
                String systemValue = jsonObject.getString("system");
                // int totalCheats = jsonObject.getInt("t");

                Cheat tmpCheat = new Cheat();
                tmpCheat.setGameId(gameIdValue);
                tmpCheat.setCheatId(cheatIdValue);
                tmpCheat.setGameName(gameNameValue);
                tmpCheat.setCheatTitle(titleValue);
                tmpCheat.setSystemName(systemValue);

                cheats[i] = tmpCheat;
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    public static String serverStatus() {
        return executeGet("http://www.e-nature.ch/android/serverstatus");
    }

    /**
     * Holt alle initialen Informationen und speichert sie ins Flat-File: -
     * Totale Anzahl Cheats - 3 neuste Cheats - Anzahl Games pro System
     */
    public static void getInitialData(SharedPreferences settings) {
        String newCheats = executeGet(Konstanten.BASE_URL_ANDROID + "getInitialData.php");

        Gson gson = new Gson();
        Cheat[] cheats;
        JSONObject jo;
        JSONArray jaNewCheats, jaGamesPerSystem;

        SharedPreferences.Editor editor = settings.edit();

        try {
            jo = new JSONObject(newCheats);
            jaNewCheats = jo.getJSONArray("cheats");
            jaGamesPerSystem = jo.getJSONArray("gps");

            int totalCheats = jo.getInt("totalCheats");

            cheats = new Cheat[jaNewCheats.length()];
            for (int i = 0; i < jaNewCheats.length(); i++) {
                JSONObject cheatsObject = jaNewCheats.getJSONObject(i);
                int gameIdValue = cheatsObject.getInt("gid");
                int cheatIdValue = cheatsObject.getInt("cid");
                String gameNameValue = cheatsObject.getString("name");
                String titleValue = cheatsObject.getString("title");
                String systemValue = cheatsObject.getString("system");

                Cheat tmpCheat = new Cheat();
                tmpCheat.setGameId(gameIdValue);
                tmpCheat.setCheatId(cheatIdValue);
                tmpCheat.setGameName(gameNameValue);
                tmpCheat.setCheatTitle(titleValue);
                tmpCheat.setSystemName(systemValue);

                cheats[i] = tmpCheat;
            }

			/*
             * Neue Cheats ins Flat-File speichern
			 */
            String cheat1 = gson.toJson(cheats[0]);
            String cheat2 = gson.toJson(cheats[1]);
            String cheat3 = gson.toJson(cheats[2]);
            editor.putString(Konstanten.PREF_NEWCHEAT1_VARIABLE, cheat1);
            editor.putString(Konstanten.PREF_NEWCHEAT2_VARIABLE, cheat2);
            editor.putString(Konstanten.PREF_NEWCHEAT3_VARIABLE, cheat3);
            editor.putString(Konstanten.PREF_NEWCHEATS_LAST_UPDATE_VARIABLE, Tools.now("yyyy-MM-dd"));

			/*
             * Total Cheats ins Flat-File speichern
			 */
            editor.putInt(Konstanten.PREF_TOTAL_CHEATS_VARIABLE, totalCheats);

			/*
             * Anzahl Games pro System auslesen und ins Flat-File speichern
			 */
            for (int j = 0; j < jaGamesPerSystem.length(); j++) {
                JSONObject gpsObject = jaGamesPerSystem.getJSONObject(j);
                int sysId = gpsObject.getInt("s");
                int count = gpsObject.getInt("c");

                editor.putInt(String.valueOf(sysId), count);
            }

            editor.commit();
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }
    }

    /**
     * Holt eine Liste von Games von einem System anhand des übergebenen
     * Suchstrings und liefert sie als Game-Objekte in einem Game-Array zurück.
     *
     * @param searchString
     * @param systemId
     * @return Game[]
     */
    public static Game[] searchGames(Activity activity, String searchString, int systemId) {
        String gameListString = searchGamesAsString(searchString, systemId);
        return Tools.getGameListConvertStringToGameList(activity, gameListString, systemId);
    }

    public static Game[] searchGames(Activity activity, String searchString) {
        String gameListString = searchGamesAsString(searchString);
        return Tools.getGameListConvertStringToGameList(activity, gameListString);
    }

    /**
     * Holt eine Liste von Cheat-Titeln von einem Game anhand der übergebenen
     * Game-ID und liefert sie als String-Arrays in einer ArrayList zurück.
     *
     * @param gameId
     * @return Cheat[]
     * @deprecated Ab Version 1.4.2 getCheatList(int gameId, int memberId)
     * verwenden
     */
    @Deprecated
    public static Cheat[] getCheatTitleList(int gameId) {
        String systemString = getCheatTitleListAsString(gameId);

        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {
            jArray = new JSONArray(systemString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int cheatId = jsonObject.getInt("id");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");
                int language = jsonObject.getInt("lang");
                int cheatFormat = jsonObject.getInt("format");
                String rating = jsonObject.getString("rating");

                Cheat cheat = new Cheat();

				/*
                 * Screenshot-Informationen auslesen
				 */
                JSONArray screenshots = jsonObject.getJSONArray("screenshots");
                Screenshot[] screens = new Screenshot[screenshots.length()];
                for (int j = 0; j < screenshots.length(); j++) {
                    JSONArray screenshot = screenshots.getJSONArray(j);
                    String filename = screenshot.getString(0);
                    String filesize_kb = screenshot.getString(1);

                    screens[j] = new Screenshot(filesize_kb, filename, cheatId);

                    cheat.setHasScreenshots(true);
                }
                cheat.setScreens(screens);

                cheat.setCheatId(cheatId);
                cheat.setCheatTitle(cheatTitle.replaceAll("\\\\", ""));
                cheat.setCheatText(cheatText.replaceAll("\\\\", ""));
                cheat.setLanguageId(language);
                cheat.setRatingAverage(Float.parseFloat(rating));
                cheat.setGameId(gameId);
                if (cheatFormat == 2) {
                    cheat.setWalkthroughFormat(true);
                } else {
                    cheat.setWalkthroughFormat(false);
                }

                cheats[i] = cheat;
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    /**
     * Holt alle Cheats inkl. Member- und Average-Rating anhand einer Game-ID
     *
     * @param game
     * @param memberId
     * @return
     */
    public static Cheat[] getCheatList(Game game, int memberId) {
        String systemString = getCheatListAsString(game.getGameId(), memberId);

        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {
            jArray = new JSONArray(systemString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int cheatId = jsonObject.getInt("id");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");
                int language = jsonObject.getInt("lang");
                int cheatFormat = jsonObject.getInt("format");
                String rating = jsonObject.getString("rating");
                String memberRating = jsonObject.getString("member_rating");
                String created = jsonObject.getString("created");

                Cheat cheat = new Cheat();

				/*
                 * Screenshot-Informationen auslesen
				 */
                JSONArray screenshots = jsonObject.getJSONArray("screenshots");
                Screenshot[] screens = new Screenshot[screenshots.length()];
                for (int j = 0; j < screenshots.length(); j++) {
                    JSONArray screenshot = screenshots.getJSONArray(j);
                    String filename = screenshot.getString(0);
                    String filesize_kb = screenshot.getString(1);

                    screens[j] = new Screenshot(filesize_kb, filename, cheatId);

                    cheat.setHasScreenshots(true);
                }
                cheat.setScreens(screens);

                cheat.setCheatId(cheatId);
                cheat.setCheatTitle(cheatTitle.replaceAll("\\\\", ""));
                cheat.setCheatText(cheatText.replaceAll("\\\\", ""));
                cheat.setLanguageId(language);
                cheat.setRatingAverage(Float.parseFloat(rating));
                cheat.setMemberRating(Float.parseFloat(memberRating));
                cheat.setGameId(game.getGameId());
                cheat.setGameName(game.getGameName());
                if (cheatFormat == 2) {
                    cheat.setWalkthroughFormat(true);
                } else {
                    cheat.setWalkthroughFormat(false);
                }
                cheat.setSystemId(game.getSystemId());
                cheat.setSystemName(game.getSystemName());
                cheat.setCreated(created);

                cheats[i] = cheat;
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    /**
     * Holt die Meta-Informationen zu einem Cheat.
     *
     * @param cheatId
     * @return Cheat
     */
    public static Cheat getCheatMetaById(int cheatId) {

        Cheat cheat = new Cheat();

        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8");
            String metaArray = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatMetaById.php", urlParameters);

            JSONObject jsonObject = new JSONObject(metaArray);

            int counterTotal = jsonObject.optInt("counterTotal", 1);
            int counterToday = jsonObject.optInt("counterToday", 1);
            String author = jsonObject.optString("author", "");
            String created = jsonObject.getString("created");
            int memberId = jsonObject.getInt("memberId");
            String rating = jsonObject.optString("rating", "0");
            String memberUsername = jsonObject.optString("username", "");
            int votes = jsonObject.optInt("votes", 0);
            String website = jsonObject.optString("website", "");
            float rating_float = Float.parseFloat(rating);
            int memberCheatSubmissionCount = jsonObject.optInt("memberSubmissions", 0);

            Member member = new Member();
            member.setMid(memberId);
            member.setWebsite(website);
            member.setUsername(memberUsername);
            member.setCheatSubmissionCount(memberCheatSubmissionCount);

            cheat.setCheatId(cheatId);
            cheat.setViewsTotal(counterTotal);
            cheat.setViewsToday(counterToday);
            cheat.setAuthorName(author);
            cheat.setCreated(created);
            cheat.setRatingAverage(rating_float);
            cheat.setVotes(votes);

            cheat.setMember(member);

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getLocalizedMessage());
        }

        return cheat;
    }

    /**
     * Holt alle Cheats von einem Game.
     *
     * @param gameId
     * @param gameName
     * @return
     */
    public static Cheat[] getCheatsByGameId(int gameId, String gameName) {
        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {
            String urlParameters = "gameId=" + URLEncoder.encode(String.valueOf(gameId), "UTF-8");
            String cheatsString = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatsByGameId.php", urlParameters);

            jArray = new JSONArray(cheatsString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int cheatId = jsonObject.getInt("id");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");
                int language = jsonObject.getInt("lang");
                int style = jsonObject.getInt("style");
                String created = jsonObject.getString("created");
                String rating = jsonObject.getString("rating");

                Cheat cheat = new Cheat();

                JSONArray screenshotsArray = jsonObject.getJSONArray("screenshots");
                Screenshot[] screens = new Screenshot[screenshotsArray.length()];
                for (int x = 0; x < screenshotsArray.length(); x++) {
                    screens[x] = new Screenshot(screenshotsArray.getJSONObject(x).getString("size"), screenshotsArray.getJSONObject(x).getString("url"), cheatId);
                    cheat.setHasScreenshots(true);
                }

                cheat.setCheatId(cheatId);
                cheat.setCheatTitle(cheatTitle.replaceAll("\\\\", ""));
                cheat.setCheatText(cheatText.replaceAll("\\\\", ""));
                cheat.setLanguageId(language);

                if (style == 1) {
                    cheat.setWalkthroughFormat(false);
                } else {
                    cheat.setWalkthroughFormat(true);
                }

                cheat.setCreated(created);
                cheat.setRatingAverage(Float.parseFloat(rating));
                cheat.setGameId(gameId);
                cheat.setGameName(gameName);

                cheats[i] = cheat;
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    /**
     * Holt alle Cheats verschachtelt von einem Mitglied.
     *
     * @param memberId
     * @return SystemPlatform[]> Game[]> Cheat[]
     */
    public static SystemPlatform[] getCheatsByMemberIdNested(int memberId) {
        SystemPlatform[] systems = null;
        Game[] games = null;
        Cheat[] cheats = null;
        ArrayList<Game> temporaryGameList = new ArrayList<>();

        JSONArray jArray = null;

        Map<Integer, String> systemsHashMap = new HashMap<>();
        Map<Integer, String> gamesHashMap = new HashMap<>();

        try {
            String urlParameters = "memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8");
            String cheatsString = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatsByMemberId.php", urlParameters);

            jArray = new JSONArray(cheatsString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int cheatId = jsonObject.getInt("cheat_id");
                int gameId = jsonObject.getInt("game_id");
                String gameName = jsonObject.getString("gamename");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");
                int language = jsonObject.getInt("lang");
                int style = jsonObject.getInt("style");
                String created = jsonObject.getString("created");
                String rating = jsonObject.optString("rating", "0");
                String system = jsonObject.getString("system");
                int systemId = jsonObject.getInt("system_id");

                systemsHashMap.put(systemId, system);
                gamesHashMap.put(gameId, gameName);

                // Games in ArrayList speichern, damit man spüter die
                // System-ID und System-Name wieder rauskriegt
                Game tmpGameX = new Game(gameId, gameName, systemId, system);
                temporaryGameList.add(tmpGameX);

                Cheat cheat = new Cheat();

                JSONArray screenshotsArray = jsonObject.getJSONArray("screenshots");
                Screenshot[] screens = new Screenshot[screenshotsArray.length()];
                for (int x = 0; x < screenshotsArray.length(); x++) {
                    screens[x] = new Screenshot(screenshotsArray.getJSONObject(x).getString("size"), screenshotsArray.getJSONObject(x).getString("url"), cheatId);
                    cheat.setHasScreenshots(true);
                }

                cheat.setCheatId(cheatId);
                cheat.setCheatTitle(cheatTitle.replaceAll("\\\\", ""));
                cheat.setCheatText(cheatText.replaceAll("\\\\", ""));
                cheat.setLanguageId(language);

                if (style == 1) {
                    cheat.setWalkthroughFormat(false);
                } else {
                    cheat.setWalkthroughFormat(true);
                }

                cheat.setCreated(created);
                cheat.setRatingAverage(Float.parseFloat(rating));
                cheat.setGameId(gameId);
                cheat.setGameName(gameName);

                cheats[i] = cheat;
            }

            Iterator<Map.Entry<Integer, String>> it1 = systemsHashMap.entrySet().iterator();
            ArrayList<SystemPlatform> temporarySystemList = new ArrayList<>();
            while (it1.hasNext()) {
                Map.Entry<Integer, String> pairs1 = it1.next();
                int tmpSystemId = Integer.valueOf(pairs1.getKey().toString());
                String tmpSystemName = pairs1.getValue();

                SystemPlatform tempSys = new SystemPlatform(tmpSystemId, tmpSystemName);
                temporarySystemList.add(tempSys);
            }
            systems = new SystemPlatform[temporarySystemList.size()];
            for (int m = 0; m < temporarySystemList.size(); m++) {
                systems[m] = temporarySystemList.get(m);
            }

            Iterator<Map.Entry<Integer, String>> it2 = gamesHashMap.entrySet().iterator();
            ArrayList<Game> temporaryInnerGameList = new ArrayList<>();
            while (it2.hasNext()) {
                Map.Entry<Integer, String> pairs2 = it2.next();
                int tmpGameId = Integer.valueOf(pairs2.getKey().toString());
                String tmpGameName = pairs2.getValue();
                Game tmpGame = new Game();
                tmpGame.setGameId(tmpGameId);
                tmpGame.setGameName(tmpGameName);

				/*
                 * Dem Game-Objekt die System-ID und System-Namen ergünzen
				 */
                for (int k = 0; k < temporaryGameList.size(); k++) {
                    Game gg = temporaryGameList.get(k);
                    if (gg.getGameId() == tmpGameId) {
                        tmpGame.setSystemId(gg.getSystemId());
                        tmpGame.setSystemName(gg.getSystemName());
                    }
                }

                ArrayList<Cheat> matchingCheats = new ArrayList<>();
                for (Cheat cheat : cheats) {
                    if (cheat.getGameId() == tmpGame.getGameId()) {
                        matchingCheats.add(cheat);
                    }
                }

                /*
                 * Dem Game-Object die korrekte Anzahl Cheat-Objekten zuweisen
				 */
                tmpGame.createCheatCollection(matchingCheats.size());

				/*
                 * Die passenden Cheats dem Game-Objekt hinzufügen
				 */
                for (int i = 0; i < matchingCheats.size(); i++) {
                    tmpGame.addCheat(matchingCheats.get(i));
                }

                temporaryInnerGameList.add(tmpGame);
            }

			/*
             * Die Games zum globalen Game-Array-Objekt hinzufügen
			 */
            games = new Game[temporaryInnerGameList.size()];
            for (int n = 0; n < temporaryInnerGameList.size(); n++) {
                games[n] = temporaryInnerGameList.get(n);
            }

			/*
             * Die passenden Games dem System[]-Objekt hinzufügen.
			 */
            for (int j2 = 0; j2 < systems.length; j2++) {
                SystemPlatform ss = systems[j2];

                ArrayList<Game> matchingGames = new ArrayList<>();

                for (Game game : games) {
                    if (ss.getSystemId() == game.getSystemId()) {
                        matchingGames.add(game);
                    }
                }
                systems[j2].createGameCollection(matchingGames.size());

				/*
                 * Die passenden Game-Objekte dem System-Objekt hinzufügen
				 */
                for (Game game : matchingGames) {
                    systems[j2].addGame(game);
                }
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getLocalizedMessage());
        }

        return systems;
    }

    /**
     * Holt alle eingeschickten Cheats von einem Member.
     *
     * @param memberId
     * @return Cheat[]
     */
    public static Cheat[] getCheatsByMemberId(int memberId) {
        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {
            String urlParameters = "memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8");
            String cheatsString = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatsByMemberId.php", urlParameters);

            jArray = new JSONArray(cheatsString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int cheatId = jsonObject.getInt("cheat_id");
                int gameId = jsonObject.getInt("game_id");
                String gameName = jsonObject.getString("gamename");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");
                int language = jsonObject.getInt("lang");
                int style = jsonObject.getInt("style");
                String created = jsonObject.getString("created");
                String rating = jsonObject.optString("rating", "0");
                String systemName = jsonObject.getString("system");
                int systemId = jsonObject.getInt("system_id");

                Cheat cheat = new Cheat();

				/*
                 * Screenshot-Informationen auslesen
				 */
                JSONArray screenshots = jsonObject.getJSONArray("screenshots");
                Screenshot[] screens = new Screenshot[screenshots.length()];
                for (int j = 0; j < screenshots.length(); j++) {
                    JSONArray screenshot = screenshots.getJSONArray(j);
                    String filename = screenshot.getString(0);
                    String filesize_kb = screenshot.getString(1);

                    screens[j] = new Screenshot(filesize_kb, filename, cheatId);

                    cheat.setHasScreenshots(true);
                }
                cheat.setScreens(screens);

                cheat.setCheatId(cheatId);
                cheat.setCheatTitle(cheatTitle);
                cheat.setCheatText(cheatText);
                cheat.setLanguageId(language);

                if (style == 1) {
                    cheat.setWalkthroughFormat(false);
                } else {
                    cheat.setWalkthroughFormat(true);
                }

                cheat.setCreated(created);
                cheat.setRatingAverage(Float.parseFloat(rating));
                cheat.setGameId(gameId);
                cheat.setGameName(gameName);
                cheat.setSystemId(systemId);
                cheat.setSystemName(systemName);

                cheats[i] = cheat;
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "JSONException | UnsupportedEncodingException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    /**
     * Holt die Top 20 Members
     *
     * @return
     */
    public static Member[] getMemberTop20() {
        String topMembers = executeGet(Konstanten.BASE_URL_ANDROID + "getMemberTop20.php");

        Member[] members = null;

        JSONArray jArray;
        try {
            jArray = new JSONArray(topMembers);
            members = new Member[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int mid = jsonObject.getInt("mid");
                String memberName = jsonObject.getString("username");
                String city = jsonObject.getString("city");
                String website = jsonObject.getString("url");
                String greeting = jsonObject.getString("greeting");
                int cheats_submitted = jsonObject.getInt("cheats_submitted");

                Member member = new Member();
                member.setMid(mid);
                member.setUsername(memberName);
                member.setCity(city);
                member.setWebsite(website);
                member.setGreeting(greeting);
                member.setCheatSubmissionCount(cheats_submitted);

                members[i] = member;
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return members;
    }

    /**
     * Gets the amount of games and total cheats of each system.
     *
     * @return SystemPlatform[]
     */
    public static ArrayList<SystemPlatform> countGamesAndCheatsBySystem() {
        String gamesAndCheatCounter = executeGet(Konstanten.BASE_URL_ANDROID + "countGamesAndCheatsBySystem.php");

        ArrayList<SystemPlatform> systems = new ArrayList<>();

        JSONArray jArray;
        try {
            jArray = new JSONArray(gamesAndCheatCounter);

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                SystemPlatform system = new SystemPlatform();
                system.setSystemId(jsonObject.getInt("systemId"));
                system.setSystemName(jsonObject.getString("systemName"));
                system.setGameCount(jsonObject.getInt("gamesCounter"));
                system.setCheatCount(jsonObject.getInt("cheatsCounter"));
                system.setLastModTimeStamp(System.currentTimeMillis());

                systems.add(system);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return systems;
    }

    public static WelcomeMessage getWelcomeMessage() {
        WelcomeMessage welcomeMessage = null;

        String url = executeGet(Konstanten.BASE_URL_ANDROID + "getWelcomeMessage.php");

        try {
            JSONObject jsonObject = new JSONObject(url);
            Log.i(TAG, "WelcomeMessage: " + jsonObject.toString());

            welcomeMessage = new WelcomeMessage();
            welcomeMessage.setId(jsonObject.getInt("id"));
            welcomeMessage.setTitle(jsonObject.getString("title"));
            welcomeMessage.setTitle_de(jsonObject.getString("title_de"));
            welcomeMessage.setWelcomeMessage(jsonObject.getString("welcomemessage"));
            welcomeMessage.setWelcomeMessage_de(jsonObject.getString("welcomemessage_de"));
            welcomeMessage.setCreated(jsonObject.getString("created"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return welcomeMessage;
    }

    /**
     * Holt das Forum eines Cheats.
     *
     * @param cheatId
     * @return
     */
    public static ForumPost[] getForum(int cheatId) {
        ForumPost[] thread = null;
        JSONArray jArray = null;

        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8");
            String forumString = excutePost(Konstanten.BASE_URL_ANDROID + "getForum.php", urlParameters);

            jArray = new JSONArray(forumString);
            thread = new ForumPost[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int fpid = jsonObject.getInt("fpid");
                String name = jsonObject.getString("name");
                String username = jsonObject.getString("username");
                int mid = jsonObject.getInt("mid");
                String email = jsonObject.getString("email");
                String text = jsonObject.getString("text");
                String created = jsonObject.getString("created");
                String updated = jsonObject.getString("updated");
                String ip = jsonObject.getString("ip");

                ForumPost forum = new ForumPost();
                forum.setPostId(fpid);
                forum.setName(name);
                forum.setUsername(username);
                forum.setMemberId(mid);
                forum.setEmail(email);
                forum.setText(text);
                forum.setCreated(created);
                forum.setUpdated(updated);
                forum.setIp(ip);

                thread[i] = forum;
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "getGameListBySystemId | UnsupportedEncodingException: " + e);
        }

        return thread;
    }

    /**
     * Holt saemtliche Games von einem System und liefert sie als String-Arrays
     * in einer ArrayList zurück.
     *
     * @param systemId
     * @return Game[]
     */
    public static Game[] getGameListBySystemId(int systemId, String systemName) {
        Game[] games = null;
        JSONArray jArray = null;

        try {
            String urlParameters = "systemId=" + URLEncoder.encode(String.valueOf(systemId), "UTF-8");
            String responseString = excutePost(Konstanten.BASE_URL_ANDROID + "getGamesBySystemId.php", urlParameters);

            jArray = new JSONArray(responseString);
            games = new Game[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);

                int gameId = jsonObject.getInt("id");
                String gameName = jsonObject.getString("name");
                int cheatCount = jsonObject.getInt("cnt");

                Game game = new Game();
                game.setGameId(gameId);
                game.setGameName(gameName.replaceAll("\\\\", ""));
                game.setCheatsCount(cheatCount);
                game.setSystemId(systemId);
                game.setSystemName(systemName);

                games[i] = game;
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Log.e(TAG, "getGameListBySystemId | UnsupportedEncodingException: " + e);
        }

        return games;
    }

    /**
     * Holt einen Cheat anhand der übergebenen Cheat-ID
     *
     * @param cheatId
     * @return
     */
    public static String getCheatById(int cheatId) {
        String urlParameters = null;
        String returnString = null;
        try {
            urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8");
            returnString = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatById.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    /**
     * Checks Member-Permissions.
     *
     * @param member
     * @return true/false
     */
    public static boolean hasMemberPermissions(Member member) {
        try {
            String urlParameters = "username=" + URLEncoder.encode(member.getUsername(), "UTF-8");
            JSONObject retValObj = new JSONObject(excutePost(Konstanten.BASE_URL_ANDROID + "checkMemberNew.php", urlParameters));

            return retValObj.getInt("allow") != 0;
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            e.getStackTrace();
            return false;
        }
    }

    /**
     * Gets a cheat by cheat ID and highlights
     *
     * @param cheatId
     * @param highlights
     * @return
     */
    public static String getCheatByIdHighlighted(int cheatId, String highlights) {
        String returnString = null;
        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8") + "&highlights=" + URLEncoder.encode(highlights.trim(), "UTF-8");
            returnString = excutePost(Konstanten.BASE_URL_ANDROID + "getCheatTextByCheatIdHighlighted.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    /**
     * Einen Cheat melden.
     *
     * @param cheatId
     * @param memberId
     * @param reason
     */
    public static void reportCheat(int cheatId, int memberId, String reason) {
        try {
            String urlParameters = "cheatId=" + URLEncoder.encode(String.valueOf(cheatId), "UTF-8") + "&memberId=" + URLEncoder.encode(String.valueOf(memberId), "UTF-8") + "&reason=" + URLEncoder.encode(reason, "UTF-8");
            excutePost(Konstanten.BASE_URL_ANDROID + "reportCheat.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send message through contact form.
     *
     * @param email
     * @param message
     */
    public static void submitContactForm(String email, String message) {
        try {
            String urlParameters = "email=" + URLEncoder.encode(email.trim(), "UTF-8") + "&message=" + URLEncoder.encode(message.trim(), "UTF-8");
            excutePost(Konstanten.BASE_URL_ANDROID + "submitMessage.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Volltext-Suche in einem Array von Games oder in allen Games.
     *
     * @param query
     * @param cheatIds          (z.b. 34,52345,234,123)
     * @param gameIds           (z.b. 555,400,2500)
     * @param systemIds         (z.b. 2,16,4)
     * @param searchInTitlesToo (0 = Titel nicht durchsuchen, 1 = Titel durchsuchen)
     * @return Cheat[]
     * @link http://www.exampledepot.com/egs/java.net/Post.html
     */
    public static Cheat[] fulltextSearch(String query, String cheatIds, String gameIds, String systemIds, int searchInTitlesToo) {
        return fulltextSearchConvertJSONStringToCheatArrayOffline(fulltextSearchToJSONString(query, cheatIds, gameIds, systemIds, searchInTitlesToo));
    }

    public static String fulltextSearchToJSONString(String query, String cheatIds, String gameIds, String systemIds, int searchInTitlesToo) {
        String ret = null;
        try {
            String urlParameters = "executeGet=" + URLEncoder.encode(query, "UTF-8") +
                    "&cheatIds=" + URLEncoder.encode(cheatIds, "UTF-8") +
                    "&gameIds=" + URLEncoder.encode(gameIds, "UTF-8") +
                    "&systemIds=" + URLEncoder.encode(systemIds, "UTF-8") +
                    "&searchInTitlesToo=" + URLEncoder.encode(String.valueOf(searchInTitlesToo), "UTF-8");
            ret = excutePost(Konstanten.BASE_URL_ANDROID + "searchFulltext.php", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Wandelt den Rückgabestring bei der Volltextsuche in ein Cheat-Array um.
     *
     * @param jsonString
     * @return
     */
    public static Cheat[] fulltextSearchConvertJSONStringToCheatArrayOffline(String jsonString) {
        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {
            jArray = new JSONArray(jsonString);
            cheats = new Cheat[jArray.length()];

            for (int i = 0; i < jArray.length(); i++) {

                JSONObject jsonObject = jArray.getJSONObject(i);
                int cheatId = jsonObject.getInt("id");
                int gameId = jsonObject.getInt("gid");
                int systemId = jsonObject.getInt("sid");
                String gameName = jsonObject.getString("name");
                String cheatTitle = jsonObject.getString("title");
                String cheatText = jsonObject.getString("cheat");

                Cheat cheat = new Cheat();
                cheat.setCheatId(cheatId);
                cheat.setCheatText(cheatText.replaceAll("\\\\", ""));
                cheat.setCheatTitle(cheatTitle.replaceAll("\\\\", ""));
                cheat.setGameId(gameId);
                cheat.setGameName(gameName.replaceAll("\\\\", ""));
                cheat.setLanguageId(0); // FIXME
                cheat.setRatingAverage(0); // FIXME
                cheat.setHasScreenshots(false); // FIXME
                cheat.setSystemId(systemId);
                cheat.setSystemName(""); // FIXME
                cheat.setWalkthroughFormat(false); // FIXME

                cheats[i] = cheat;
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getLocalizedMessage());
        }

        return cheats;
    }

    /**
     * Aufrufen einer parameterlosen URL mit GZIP-Kompression
     */
    private static String executeGet(String targetURL) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "The response is: " + responseCode);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

//        HttpUriRequest request = null;
//        HttpClient httpClient = null;
//        HttpResponse response = null;
//
//        try {
//            request = new HttpGet(parameterlessUrl);
//            request.addHeader("Accept-Encoding", "gzip");
//
//            httpClient = new DefaultHttpClient();
//            response = httpClient.execute(request);
//
//            // GZIP Daten entpacken
//            ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
//
//            InputStream instream = response.getEntity().getContent();
//            Header contentEncoding = response.getFirstHeader("Content-Encoding");
//            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
//                instream = new GZIPInputStream(instream);
//            }
//            int bytesRead;
//            byte[] tempBuffer = new byte[8192 * 2];
//
//            while ((bytesRead = instream.read(tempBuffer)) != -1) {
//                streamBuilder.write(tempBuffer, 0, bytesRead);
//            }
//
//            responseString = streamBuilder.toString();
//
//        } catch (Exception e) {
//            // URL Incorrect
//            responseString = e.getMessage();
//            Log.e("getDataFromServer ERROR", e.getMessage());
//        }
//
//        return responseString;
    }

    /**
     * Do a REST Call - HTTP POST.
     *
     * @param targetURL
     * @param urlParameters FORMAT: urlParameters = "fName=" + URLEncoder.encode("???", "UTF-8") + "&lName=" + URLEncoder.encode("???", "UTF-8")
     * @return
     */
    public static String excutePost(String targetURL, String urlParameters) {

        // PARAMETER FORMAT
        // String urlParameters = "fName=" + URLEncoder.encode("???", "UTF-8") + "&lName=" + URLEncoder.encode("???", "UTF-8");

        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            // Remove last \r
            String responseString = response.toString();
            responseString = responseString.substring(0, responseString.length() - 1);

            return responseString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}
