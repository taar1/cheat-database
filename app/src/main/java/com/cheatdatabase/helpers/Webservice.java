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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * From:
 * http://www.anddev.org/getting_data_from_the_web_urlconnection_via_http-t351
 * .html
 *
 * @author erbsland
 */
public class Webservice {

    /**
     * Holt eine Liste von Games von einem System anhand des übergebenen
     * Suchstrings.
     *
     * @param searchString
     * @param systemId
     * @return
     */
    public static String searchGamesAsString(String searchString, int systemId) {

        String[] query = {"q", searchString};
        String[] system = {"s", String.valueOf(systemId)};
        String[] version = {"v", Konstanten.CURRENT_VERSION};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(query);
        al.add(system);
        al.add(version);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "searchGames.php", al);
    }

    /*
     * Searches for games
     */
    public static String searchGamesAsString(String searchString) {

        String[] query = {"q", searchString};
        String[] version = {"v", Konstanten.CURRENT_VERSION};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(query);
        al.add(version);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "searchGames.php", al);
    }

    /**
     * Sendet die Logindaten an einen E-Mail Account
     *
     * @param email
     * @return
     */
    public static int sendLoginData(String email) {

        int responseCode = R.string.err_email_invalid;

        if (Tools.isEmailValid(email) == true) {
            String[] arr_email = {"email", email};

            ArrayList<String[]> al = new ArrayList<String[]>();
            al.add(arr_email);

            String response = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "sendLoginData.php", al);

            if (response.equalsIgnoreCase("ok")) {
                responseCode = R.string.login_sent_ok;
            } else if (response.equalsIgnoreCase("no_user_found")) {
                responseCode = R.string.err_email_user_not_found;
            } else {
                // invalid_email
                responseCode = R.string.err_email_invalid;
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

        String[] query = {"q", searchString};
        String[] version = {"v", Konstanten.CURRENT_VERSION};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(query);
        al.add(version);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "universalGameSearch.php", al);
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

        String[] param2 = {"memberId", "0"};
        if (memberId > 0) {
            param2[1] = String.valueOf(memberId);
        }
        String[] param1 = {"gameId", String.valueOf(gameId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        if (param2 != null) {
            al.add(param2);
        }

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatsAndRatingByGameId.php", al);
    }

    /**
     * Holt alle Cheat-IDs, -Titel und -Inhalte von Nicht-Walkthroughs anhand
     * der übergebenen Game-ID und liefert sie als String zurück.
     *
     * @param gameId
     * @return
     */
    public static String getCheatTitleListAsString(int gameId) {

        String[] param = {"gameId", String.valueOf(gameId)};
        String[] loadContent = {"loadContent", "1"};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);
        al.add(loadContent);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatTitlesByGameId.php", al);
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
            if (Tools.isEmailValid(email) == true) {
                String[] param1 = {"username", username};
                String[] param2 = {"email", email};

                ArrayList<String[]> al = new ArrayList<String[]>();
                al.add(param1);
                al.add(param2);

                String responseString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "register.php", al);

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
        } catch (JSONException e) {
            Log.e("register", "JSON Parsing Error: " + e.getMessage());
            member.setErrorCode(4);
        } catch (Exception ee) {
            Log.e("register", "General exception: " + ee.getMessage());
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

        String password_md5 = password.trim();
        try {
            // MD5 Hash vom Passwort generieren
            password_md5 = AeSimpleMD5.MD5(password);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String[] param1 = {"username", username};
        String[] param2 = {"password", password_md5};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param2);

        String responseString = null;
        try {
            responseString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "login_md5.php", al);
        } catch (Exception e) {
            Log.e("getDataAsStringFromServer failed", e.getLocalizedMessage());
        }
        Member member = null;

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
        String[] param1 = {"memberId", String.valueOf(memberId)};
        String[] param2 = {"gameId", String.valueOf(gameId)};
        String[] param3 = {"cheatTitle", cheatTitle};
        String[] param4 = {"cheatText", cheatText};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param2);
        al.add(param3);
        al.add(param4);

        return Integer.parseInt(getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "submitCheat.php", al));
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
        SendHTTPPostData send = new SendHTTPPostData();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("cid", String.valueOf(cheatId));
        vars.put("mid", String.valueOf(memberId));
        vars.put("pw", password);
        vars.put("forumpost", forumPost);

        send.parametrosHttp(Konstanten.BASE_URL_ANDROID + "insertForum.php", vars);
        send.go();
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

        String[] param1 = {"mid", String.valueOf(memberId)};
        String[] param3 = {"cheatId", String.valueOf(cheatId)};
        String[] param4 = {"rating", String.valueOf(rating)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param3);
        al.add(param4);

        return Integer.parseInt(getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "rateCheatWithoutPw.php", al));
    }

    /**
     * Holt das Rating des Benutzers von diesem Cheat.
     *
     * @param memberId
     * @param cheatId
     * @return int (1-10)
     */
    public static float getCheatRatingByMemberId(int memberId, int cheatId) {

        String[] param1 = {"memberId", String.valueOf(memberId)};
        String[] param2 = {"cheatId", String.valueOf(cheatId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param2);

        // Value 1-10
        return Float.parseFloat(getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getRatingByMemberId.php", al));
    }

    /**
     * Holt die URLs zu den Bildern mit Dateigrüsse von einem Cheat.
     *
     * @param cheatId
     * @return Array(String[Filename, Filegrüsse])
     */
    public static ArrayList<String[]> getImageListByCheatId(int cheatId) {

        String[] param = {"cheatId", String.valueOf(cheatId)};
        ArrayList<String[]> alx = new ArrayList<String[]>();
        alx.add(param);

        String responseString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getImagesByCheatId.php", alx);

        JSONArray jsonArray = null;
        ArrayList<String[]> al = null;

        try {
            jsonArray = new JSONArray(responseString);
            al = new ArrayList<String[]>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String url = jsonObject.getString("url");
                String size = jsonObject.getString("size");

                String[] screenshot = {url, size};

                al.add(screenshot);
            }

        } catch (JSONException e) {
            Log.e("getImageListByCheatId", "JSON Parsing Error: " + e);
        }

        return al;
    }

    /**
     * Holt drei der 20 neusten Cheats zufüllig aus der Datenbank und liefert
     * sie als Cheat-Array zurück.
     *
     * @return
     */
    // public static Cheat[] getNewCheats() {
    // int ANZAHL = 3;
    // String newCheats =
    // query(Konstanten.BASE_URL_ANDROID + "getNewCheats.php");
    //
    // Cheat[] cheats = new Cheat[ANZAHL];
    //
    // JSONArray jArray;
    // try {
    //
    // jArray = new JSONArray(newCheats);
    //
    // for (int i = 0; i < ANZAHL; i++) {
    //
    // JSONObject jsonObject = jArray.getJSONObject(i);
    //
    // int gameIdValue = jsonObject.getInt("gid");
    // int cheatIdValue = jsonObject.getInt("cid");
    // String gameNameValue = jsonObject.getString("name");
    // String titleValue = jsonObject.getString("title");
    // String systemValue = jsonObject.getString("system");
    //
    // Cheat tmpCheat = new Cheat();
    // tmpCheat.setGameId(gameIdValue);
    // tmpCheat.setCheatId(cheatIdValue);
    // tmpCheat.setGameName(gameNameValue);
    // tmpCheat.setCheatTitle(titleValue);
    // tmpCheat.setSystemName(systemValue);
    //
    // cheats[i] = tmpCheat;
    // }
    //
    // } catch (JSONException e) {
    // Log.e("getNewCheats", "JSON Parsing Error: " + e);
    // }
    //
    // return cheats;
    // }

    /**
     * Holt drei der 20 neusten Cheats zufüllig aus der Datenbank und liefert
     * sie als Cheat-Array zurück (inkl. Anzahl Cheats in der DB)
     *
     * @return
     */
    public static Cheat[] getInitialInformation() {
        int ANZAHL = 3;
        String newCheats = query(Konstanten.BASE_URL_ANDROID + "getNewAndTotalCheats.php");

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
            Log.e("getNewCheats", "JSON Parsing Error: " + e);
        }

        return cheats;
    }

    public static String serverStatus() {
        return query("http://www.e-nature.ch/android/serverstatus");
    }

    /**
     * Holt alle initialen Informationen und speichert sie ins Flat-File: -
     * Totale Anzahl Cheats - 3 neuste Cheats - Anzahl Games pro System
     */
    public static void getInitialData(SharedPreferences settings) {
        String newCheats = query(Konstanten.BASE_URL_ANDROID + "getInitialData.php");

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
            Log.e("getInitialData", "JSON Parsing Error: " + e);
        }
    }

    /**
     * Aufrufen einer parameterlosen URL mit GZIP-Kompression
     */
    private static String query(String parameterlessUrl) {
        String responseString;

        HttpUriRequest request = null;
        HttpClient httpClient = null;
        HttpResponse response = null;

        try {
            request = new HttpGet(parameterlessUrl);
            request.addHeader("Accept-Encoding", "gzip");

            httpClient = new DefaultHttpClient();
            response = httpClient.execute(request);

            // GZIP Daten entpacken
            ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();

            InputStream instream = response.getEntity().getContent();
            Header contentEncoding = response.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                instream = new GZIPInputStream(instream);
            }
            int bytesRead;
            byte[] tempBuffer = new byte[8192 * 2];

            while ((bytesRead = instream.read(tempBuffer)) != -1) {
                streamBuilder.write(tempBuffer, 0, bytesRead);
            }

            responseString = streamBuilder.toString();

        } catch (Exception e) {
            // URL Incorrect
            responseString = e.getMessage();
            Log.e("getDataFromServer ERROR", e.getMessage());
        }

        return responseString;

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
            Log.e("getCheatTitleList", "JSON Parsing Error: " + e);
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
            Log.e("getCheatTitleList", "JSON Parsing Error: " + e);
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

        String[] param = {"cheatId", String.valueOf(cheatId)};
        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);

        String metaArray = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatMetaById.php", al);

        try {
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

        } catch (JSONException e) {
            Log.e("getCheatMetaById", "JSON Parsing Error: " + e);
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

        String[] param = {"gameId", String.valueOf(gameId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);

        String cheatsString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatsByGameId.php", al);

        Cheat[] cheats = null;
        JSONArray jArray = null;

        try {

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

        } catch (JSONException e) {
            Log.e("getCheatTitleList", "JSON Parsing Error: " + e);
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
        String[] param = {"memberId", String.valueOf(memberId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);
        String cheatsString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatsByMemberId.php", al);

        SystemPlatform[] systems = null;
        Game[] games = null;
        Cheat[] cheats = null;
        ArrayList<Game> temporaryGameList = new ArrayList<Game>();

        JSONArray jArray = null;

        Map<Integer, String> systemsHashMap = new HashMap<Integer, String>();
        Map<Integer, String> gamesHashMap = new HashMap<Integer, String>();

        try {
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

                systemsHashMap.put(Integer.valueOf(systemId), system);
                gamesHashMap.put(Integer.valueOf(gameId), gameName);

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
            ArrayList<SystemPlatform> temporarySystemList = new ArrayList<SystemPlatform>();
            while (it1.hasNext()) {
                Map.Entry<Integer, String> pairs1 = it1.next();
                int tmpSystemId = Integer.valueOf(pairs1.getKey().toString());
                String tmpSystemName = pairs1.getValue().toString();

                SystemPlatform tempSys = new SystemPlatform(tmpSystemId, tmpSystemName);
                temporarySystemList.add(tempSys);
            }
            systems = new SystemPlatform[temporarySystemList.size()];
            for (int m = 0; m < temporarySystemList.size(); m++) {
                systems[m] = temporarySystemList.get(m);
            }

            Iterator<Map.Entry<Integer, String>> it2 = gamesHashMap.entrySet().iterator();
            ArrayList<Game> temporaryInnerGameList = new ArrayList<Game>();
            while (it2.hasNext()) {
                Map.Entry<Integer, String> pairs2 = it2.next();
                int tmpGameId = Integer.valueOf(pairs2.getKey().toString());
                String tmpGameName = pairs2.getValue().toString();
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

                ArrayList<Cheat> matchingCheats = new ArrayList<Cheat>();
                for (int k = 0; k < cheats.length; k++) {
                    Cheat cc = cheats[k];
                    if (cc.getGameId() == tmpGame.getGameId()) {
                        matchingCheats.add(cc);
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

                ArrayList<Game> matchingGames = new ArrayList<Game>();
                for (int k2 = 0; k2 < games.length; k2++) {
                    Game ggg = games[k2];

                    if (ss.getSystemId() == ggg.getSystemId()) {
                        matchingGames.add(ggg);
                    }
                }
                systems[j2].createGameCollection(matchingGames.size());

				/*
				 * Die passenden Game-Objekte dem System-Objekt hinzufügen
				 */
                for (int i = 0; i < matchingGames.size(); i++) {
                    systems[j2].addGame(matchingGames.get(i));
                }
            }
        } catch (JSONException e) {
            Log.e("getCheatsByMemberIdNested", "JSON Parsing Error: " + e);
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
        String[] param = {"memberId", String.valueOf(memberId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);
        String cheatsString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatsByMemberId.php", al);

        Cheat[] cheats = null;

        JSONArray jArray = null;

        try {
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

        } catch (JSONException e) {
            Log.e("getCheatsByMemberId", "JSON Parsing Error: " + e);
        }

        return cheats;
    }

    /**
     * Holt die Top 20 Members
     *
     * @return
     */
    public static Member[] getMemberTop20() {
        String topMembers = query(Konstanten.BASE_URL_ANDROID + "getMemberTop20.php");

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
            Log.e("getMemberTop20", "JSON Parsing Error: " + e);
        }

        return members;
    }

    /**
     * Gets the amount of games and total cheats of each system.
     *
     * @return SystemPlatform[]
     */
    public static ArrayList<SystemPlatform> countGamesAndCheatsBySystem() {
        String gamesAndCheatCounter = query(Konstanten.BASE_URL_ANDROID + "countGamesAndCheatsBySystem.php");

        ArrayList<SystemPlatform> systems = new ArrayList<SystemPlatform>();

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
            Log.e("countGamesAndCheatsBySystem", "JSON Parsing Error: " + e);
        }

        return systems;
    }

    public static WelcomeMessage getWelcomeMessage() {
        String url = query(Konstanten.BASE_URL_ANDROID + "getWelcomeMessage.php");
        WelcomeMessage welcomeMessage = new WelcomeMessage();

        try {
            JSONObject jsonObject = new JSONObject(url);

            welcomeMessage.setId(jsonObject.getInt("id"));
            welcomeMessage.setTitle(jsonObject.getString("title"));
            welcomeMessage.setTitle_de(jsonObject.getString("title_de"));
            welcomeMessage.setWelcomeMessage(jsonObject.getString("welcomemessage"));
            welcomeMessage.setWelcomeMessage_de(jsonObject.getString("welcomemessage_de"));
            welcomeMessage.setCreated(jsonObject.getString("created"));

        } catch (JSONException e) {
            Log.e("getWelcomeMessage", "JSON Parsing Error: " + e);
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

        String[] param = {"cheatId", String.valueOf(cheatId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param);

        String forumString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getForum.php", al);

        ForumPost[] thread = null;
        JSONArray jArray = null;

        try {

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

        } catch (JSONException e) {
            Log.e("getForum", "JSON Parsing Error: " + e);
        }

        return thread;
    }

    /**
     * Holt sümtliche Games von einem System und liefert sie als String-Arrays
     * in einer ArrayList zurück.
     *
     * @param systemId
     * @return Game[]
     */
    public static Game[] getGameListBySystemId(int systemId, String systemName) {

        String[] param1 = {"systemId", String.valueOf(systemId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);

        String responseString = getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getGamesBySystemId.php", al);



        Game[] games = null;
        JSONArray jArray = null;

        try {
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

        } catch (JSONException e) {
            Log.e("getGameListBySystemId", "JSON Parsing Error: " + e);
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
        String[] param1 = {"cheatId", String.valueOf(cheatId)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatById.php", al);
    }

    /**
     * Checks Member-Permissions.
     *
     * @param member
     * @return true/false
     */
    public static boolean hasMemberPermissions(Member member) {
        String[] param1 = {"username", member.getUsername()};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);

        try {
            JSONObject retValObj = new JSONObject(getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "checkMemberNew.php", al));

            if (retValObj.getInt("allow") == 0) {
                return false;
            } else {
                // allow = 1
                return true;
            }
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
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
        String[] param1 = {"cheatId", String.valueOf(cheatId)};
        String[] param2 = {"highlights", highlights.trim()};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param2);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "getCheatTextByCheatIdHighlighted.php", al);
    }

    /**
     * Holt Daten vom Webserver GZIP komprimiert und entpackt diese bei der
     * Ausgabe.
     *
     * @param postURL  URL der aufzurufenden Website
     * @param alParams ArrayList mit String[]-Arrays (0 = Parameter-Name, 1 =
     *                 Parameter-Wert)
     * @return
     */
    private static String getDataAsStringFromServer(String postURL, ArrayList<String[]> alParams) {

        String responseString = null;
        HttpClient client = null;
        HttpPost post = null;
        HttpResponse responsePOST = null;

        try {
            client = new DefaultHttpClient();

            post = new HttpPost(postURL);
            post.addHeader("Accept-Encoding", "gzip");

            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            for (int i = 0; i < alParams.size(); i++) {
                String[] param = alParams.get(i);
                params.add(new BasicNameValuePair(param[0], param[1]));
            }
            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params);
            post.setEntity(ent);
            responsePOST = client.execute(post);
            StatusLine status = responsePOST.getStatusLine();
            int statusCode = status.getStatusCode(); // 404 = not found

            Log.i("getDataAsStringFromServer", "statusCode: " + statusCode);
            if (statusCode == Konstanten.HTTP_FILE_NOT_FOUND) {
                return String.valueOf(statusCode);
            }

            // GZIP Daten entpacken
            ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();

            InputStream instream = responsePOST.getEntity().getContent();
            Header contentEncoding = responsePOST.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                instream = new GZIPInputStream(instream);
            }
            int bytesRead;
            byte[] tempBuffer = new byte[8192 * 2];

            while ((bytesRead = instream.read(tempBuffer)) != -1) {
                streamBuilder.write(tempBuffer, 0, bytesRead);
            }

            responseString = streamBuilder.toString();

        } catch (Exception e) {
            Log.e("getDataFromServer ERROR", "" + e.getMessage());
            e.printStackTrace();
        }
        return responseString;
    }

    /**
     * Einen Cheat melden.
     *
     * @param cheatId
     * @param memberId
     * @param reason
     */
    public static void reportCheat(int cheatId, int memberId, String reason) {
        SendHTTPPostData send = new SendHTTPPostData();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("cheatId", String.valueOf(cheatId));
        vars.put("memberId", String.valueOf(memberId));
        vars.put("reason", reason);

        send.parametrosHttp(Konstanten.BASE_URL_ANDROID + "reportCheat.php", vars);
        send.go();
    }

    /**
     * Send message through contact form.
     *
     * @param email
     * @param message
     */
    public static void submitContactForm(String email, String message) {
        SendHTTPPostData send = new SendHTTPPostData();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("email", email.trim());
        vars.put("message", message.trim());

        send.parametrosHttp(Konstanten.BASE_URL_ANDROID + "submitMessage.php", vars);
        send.go();
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

        String[] param1 = {"query", query};
        String[] param2 = {"cheatIds", cheatIds};
        String[] param3 = {"gameIds", gameIds};
        String[] param4 = {"systemIds", systemIds};
        String[] param5 = {"searchInTitlesToo", String.valueOf(searchInTitlesToo)};

        ArrayList<String[]> al = new ArrayList<String[]>();
        al.add(param1);
        al.add(param2);
        al.add(param3);
        al.add(param4);
        al.add(param5);

        return getDataAsStringFromServer(Konstanten.BASE_URL_ANDROID + "searchFulltext.php", al);
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
            Log.e("fulltextSearch", "JSON Parsing Error: " + e);
        }

        return cheats;
    }

}
