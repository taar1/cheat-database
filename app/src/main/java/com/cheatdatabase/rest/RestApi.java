package com.cheatdatabase.rest;

import com.cheatdatabase.model.Cheat;
import com.cheatdatabase.model.ForumPost;
import com.cheatdatabase.model.Game;
import com.cheatdatabase.model.Member;
import com.cheatdatabase.model.SystemPlatform;
import com.cheatdatabase.model.WelcomeMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RestApi {

    @FormUrlEncoded
    @POST("searchGames.php")
    Call<String> searchGamesAsString(@Field("searchString") String searchString, @Field("systemId") int systemId, @Field("appVersion") String version);

    @FormUrlEncoded
    @POST("searchGames.php")
    Call<String> searchGamesAsString(@Field("searchString") String searchString, @Field("appVersion") String version);

    @FormUrlEncoded
    @POST("searchGames.php")
    Call<List<Cheat>> searchGames(@Field("gameName") String gameName, @Field("systemId") int systemId, @Field("orderBy") String orderBy, @Field("appVersion") String appVersion);


    /**
     * Sendet die Logindaten an einen E-Mail Account
     *
     * @param email
     * @return "ok"; "no_user_found"; other string
     */
    @FormUrlEncoded
    @POST("sendLoginData.php")
    Call<String> sendLoginData(@Field("email") String email);

    @FormUrlEncoded
    @POST("universalGameSearch.php")
    Call<String> universalGameSearch(@Field("q") String searchString, @Field("v") String appVersion);

    /**
     * Gets all cheats from a game as a string.
     *
     * @param gameId              Game ID
     * @param memberId            Member ID
     * @param achievementsEnabled 1=yes, 0=no
     * @return Cheat list as String
     */
    @FormUrlEncoded
    @POST("getCheatListAsString.php")
    Call<String> getCheatListAsString(@Field("gameId") int gameId, @Field("memberId") int memberId, @Field("achievementsEnabled") int achievementsEnabled);

    /**
     * Gets all cheat IDs, titles and content of non-walkthroughs as a String.
     *
     * @param gameId Game ID
     * @return JSON String
     */
    @FormUrlEncoded
    @POST("getCheatTitlesByGameId.php")
    Call<String> getCheatTitleListAsString(@Field("gameId") int gameId, @Field("loadContent") int loadContent);


    /**
     * Creates a new user account (generated password) and returns the member object with error code value.
     * <p>
     * Error Codes:
     * 0: REGISTRATION SUCCESS
     * 1: R.string.err_email_invalid
     * 2: R.string.err_email_used
     * 3: R.string.err_username_used
     * 4: R.string.err_other_problem
     *
     * @param username Desired username
     * @param email    User email address
     * @return Member
     */
    @FormUrlEncoded
    @POST("register.php")
    Call<Member> register(@Field("username") String username, @Field("email") String email);

    /**
     * Authenticates the user and returns a Member object.
     * Member ID = 0 means login was not successful.
     *
     * @param username     Username / email
     * @param password_md5 MD5 Hash of Password
     * @return Member
     */
    @FormUrlEncoded
    @POST("login.php")
    Call<Member> login(@Field("username") String username, @Field("password") String password_md5);


    /**
     * Submit a cheat.
     *
     * @param memberId   Member ID
     * @param gameId     Game ID
     * @param cheatTitle Cheat Title
     * @param cheatText  Cheat Text
     * @return 1 = Member eligible for PocketChange, 0 = Member NOT eligible for PocketChange
     */
    @FormUrlEncoded
    @POST("insertCheat.php")
    Call<Integer> insertCheat(@Field("memberId") int memberId, @Field("gameId") int gameId, @Field("cheatTitle") String cheatTitle, @Field("cheatText") String cheatText);


    /**
     * Insert a forum post.
     * TODO: SWITCH TO PASSWORD MD5!!
     *
     * @param memberId  Member ID
     * @param cheatId   Cheat ID
     * @param password  Password
     * @param forumpost Forum post
     * @return Void
     */
    @FormUrlEncoded
    @POST("insertForum.php")
    Call<Void> insertForum(@Field("mid") int memberId, @Field("cid") int cheatId, @Field("password") String password, @Field("forumpost") String forumpost);


    /**
     * Rates a cheat.
     * If the user has no account yet it will automatically be created.
     * If there is no rating yet it will be added, if it already exists it
     * will be updated with the new value.
     *
     * @param memberId Member ID
     * @param cheatId  Cheat ID
     * @param rating   Rating
     * @return 1 = INSERT, 2 = UPDATE
     */
    @FormUrlEncoded
    @POST("rateCheatWithoutPw.php")
    Call<Void> rateCheat(@Field("mid") int memberId, @Field("cheatId") int cheatId, @Field("rating") String rating);


    /**
     * Gets the rating for a cheat of a member.
     *
     * @param memberId Member ID
     * @param cheatId  Cheat ID
     * @return 1-10
     */
    @FormUrlEncoded
    @POST("getRatingByMemberId.php")
    Call<Void> getCheatRatingByMemberId(@Field("memberId") int memberId, @Field("cheatId") int cheatId);


    /**
     * Gets a list of all images from a cheat.
     *
     * @param cheatId Cheat ID
     * @return Array(String[filename, filesize])
     */
    @FormUrlEncoded
    @POST("getImagesByCheatId.php")
    Call<List<String[]>> getImageListByCheatId(@Field("cheatId") int cheatId);


    /**
     * Gets the last 20 added cheats from the database.
     *
     * @return Cheats
     */
    @FormUrlEncoded
    @GET("getNewAndTotalCheats.php")
    Call<List<Cheat>> getInitialInformation();

    /**
     * Gets the latest total amount of cheats.
     *
     * @return int
     */
    @FormUrlEncoded
    @GET("getInitialData.php")
    Call<Integer> getTotalCheats();

    /**
     * Gets all cheats from a game with average rating and member information.
     *
     * @param memberId            Member ID
     * @param gameId              Game ID
     * @param achievementsEnabled 1=yes, 0=no
     * @return
     */
    @FormUrlEncoded
    @POST("getCheatsAndRatingByGameId.php")
    Call<List<Cheat>> getCheatsAndRatings(@Field("gameId") int gameId, @Field("memberId") int memberId, @Field("achievementsEnabled") int achievementsEnabled);

    /**
     * Gets the meta info of a cheat.
     *
     * @param cheatId Cheat ID
     * @return Cheat
     */
    @FormUrlEncoded
    @POST("getCheatMetaById.php")
    Call<Cheat> getCheatMetaById(@Field("cheatId") int cheatId);

    /**
     * Gets all cheat from a game.
     *
     * @param gameId Game ID
     * @return List<Cheat>
     */
    @FormUrlEncoded
    @POST("getCheatsByGameId.php")
    Call<List<Cheat>> getCheatsByGameId(@Field("gameId") int gameId);

    /**
     * Gets all cheats from a member.
     *
     * @param memberId Member ID.
     * @return List<Cheat>
     */
    @FormUrlEncoded
    @POST("getCheatsByMemberId.php")
    Call<List<Cheat>> getCheatsByMemberId(@Field("memberId") int memberId);

    @FormUrlEncoded
    @GET("getMemberTop20.php")
    Call<List<Member>> getMemberTop20();

    @FormUrlEncoded
    @GET("countGamesAndCheatsBySystem.php")
    Call<List<SystemPlatform>> countGamesAndCheatsBySystem();

    @FormUrlEncoded
    @GET("getWelcomeMessage.php")
    Call<WelcomeMessage> getWelcomeMessage();

    @FormUrlEncoded
    @POST("getForum.php")
    Call<List<ForumPost>> getForum(@Field("cheatId") int cheatId);

    /**
     * Gets all games from a system.
     *
     * @param systemId            System ID
     * @param achievementsEnabled 1=yes, 0=no
     * @return List<Game>
     */
    @FormUrlEncoded
    @POST("getGamesBySystemId.php")
    Call<List<Game>> getGameListBySystemId(@Field("systemId") int systemId, @Field("achievementsEnabled") int achievementsEnabled);

    /**
     * Gets the cheat text (body) of a cheat as a string.
     *
     * @param cheatId Cheat ID
     * @return String
     */
    @FormUrlEncoded
    @POST("getCheatById.php")
    Call<String> getCheatBodyById(@Field("cheatId") int cheatId);

    /**
     * Checks Member-Permissions.
     *
     * @param username Username
     * @return boolean
     */
    @FormUrlEncoded
    @POST("checkMember.php")
    Call<Boolean> hasMemberPermissions(@Field("username") String username);

    /**
     * Reports a cheat as invalid.
     *
     * @param cheatId  Cheat ID
     * @param memberId Member ID
     * @param reason   Reason for reporting
     * @return
     */
    @FormUrlEncoded
    @POST("reportCheat.php")
    Call<Void> reportCheat(@Field("cheatId") int cheatId, @Field("memberId") int memberId, @Field("reason") String reason);

    /**
     * Submits a message to me.
     *
     * @param email   E-Mail
     * @param message Message
     * @return
     */
    @FormUrlEncoded
    @POST("submitMessage.php")
    Call<Void> submitContactForm(@Field("email") String email, @Field("message") String message);


}
