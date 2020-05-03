package com.cheatdatabase.data.model;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.cheatdatabase.helpers.Konstanten;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.inject.Inject;

public class Member implements Parcelable {

    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("website")
    private String website;
    @SerializedName("city")
    private String city;
    @SerializedName("profileText")
    private String profileText;
    @SerializedName("memberId")
    private int mid;
    @SerializedName("errorCode")
    private int errorCode;
    @SerializedName("cheatsSubmitted")
    private int cheatSubmissionCount;
    @SerializedName("isBanned")
    private boolean banned;

    private Bitmap avatar;
    private String password;

    @Inject
    public Member() {
    }

    public Member(String username, String password, String email, int mid, boolean banned) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.mid = mid;
        this.banned = banned;
    }

    protected Member(Parcel in) {
        username = in.readString();
        password = in.readString();
        email = in.readString();
        website = in.readString();
        city = in.readString();
        profileText = in.readString();
        mid = in.readInt();
        errorCode = in.readInt();
        cheatSubmissionCount = in.readInt();
        banned = in.readByte() != 0;
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

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

    public String getProfileText() {
        return profileText;
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

    public void setProfileText(String profileText) {
        this.profileText = profileText;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(website);
        dest.writeString(city);
        dest.writeString(profileText);
        dest.writeInt(mid);
        dest.writeInt(errorCode);
        dest.writeInt(cheatSubmissionCount);
        dest.writeByte((byte) (banned ? 1 : 0));
        dest.writeParcelable(avatar, flags);
    }
}
