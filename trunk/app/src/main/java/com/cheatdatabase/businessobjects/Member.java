package com.cheatdatabase.businessobjects;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.cheatdatabase.helpers.Konstanten;
import com.google.gson.Gson;

import java.io.Serializable;

public class Member implements Serializable {

    private String username, password, email, website, city, greeting;
    private int mid, errorCode, cheatSubmissionCount;
    private boolean banned;
    private Bitmap avatar;

    public Member() {
    }

    public Member(String username, String password, String email, int mid, boolean banned) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.mid = mid;
        this.banned = banned;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public int getCheatSubmissionCount() {
        return cheatSubmissionCount;
    }

    public String getCity() {
        return city;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Error Codes: 1: R.string.err_email_invalid 2: R.string.err_email_used 3:
     * R.string.err_username_used 4: R.string.err_other_problem
     *
     * @return
     */
    public int getErrorCode() {
        return errorCode;
    }

    public String getGreeting() {
        return greeting;
    }

    /**
     * @return the mid
     */
    public int getMid() {
        return mid;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public String getWebsite() {
        return website;
    }

    /**
     * @return the banned
     */
    public boolean isBanned() {
        return banned;
    }

    /**
     * Deletes member data from local preferences file
     *
     * @param settings
     */
    public void logout(SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(Konstanten.MEMBER_OBJECT);
        editor.commit();
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    /**
     * @param banned the banned to set
     */
    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public void setCheatSubmissionCount(int cheatSubmissionCount) {
        this.cheatSubmissionCount = cheatSubmissionCount;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    /**
     * @param mid the mid to set
     */
    public void setMid(int mid) {
        this.mid = mid;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Schreibt die Member-Daten ins Flatfile
     *
     * @param member
     * @param settings
     */
    public void writeMemberData(Member member, SharedPreferences settings) {
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(Konstanten.MEMBER_OBJECT, new Gson().toJson(member));
        editor.commit();
    }
}
