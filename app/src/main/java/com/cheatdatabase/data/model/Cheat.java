package com.cheatdatabase.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cheatdatabase.helpers.Tools;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Plain Cheat-Object holding various information for display.
 *
 * @author Dominik Erbsland
 * @since 2010
 */
public class Cheat implements Parcelable {

    @SerializedName("cheatId")
    private int cheatId;
    @SerializedName("title")
    private String cheatTitle;
    @SerializedName("cheat")
    private String cheatText;
    @SerializedName("lang")
    private int languageId;
    @SerializedName("style")
    private int style; // 1 = normal cheat, 2 = walkthrough
    @SerializedName("created")
    private String created;
    @SerializedName("author")
    private String author;
    @SerializedName("member")
    private Member submittingMember;
    @SerializedName("rating")
    private float rating;
    @SerializedName("memberRating")
    private float memberRating;
    @SerializedName("screenshots")
    private List<Screenshot> screenshotList = new ArrayList<>();
    @SerializedName("views")
    private int views;
    @SerializedName("votes")
    private int votes;
    @SerializedName("viewsLifetime")
    private int viewsLifetime;
    @SerializedName("viewsToday")
    private int viewsToday;
    @SerializedName("forumCount")
    private int forumCount;
    @SerializedName("isWalkthrough")
    private boolean walkthroughFormat;
    @SerializedName("hasScreenshots")
    private boolean screenshots;

    @SerializedName("game")
    private Game game;
    @SerializedName("system")
    private SystemModel system;

    @Inject
    public Cheat() {
    }

    public Cheat(int cheatId, String cheatTitle, String cheatText, int languageId, boolean walkthroughFormat, Game game, SystemModel systemPlatform) {
        this.cheatId = cheatId;
        this.cheatTitle = cheatTitle;
        this.cheatText = cheatText;
        this.languageId = languageId;
        this.walkthroughFormat = walkthroughFormat;
        this.game = game;
        this.system = systemPlatform;
    }

    public Cheat(Parcel in) {
        cheatId = in.readInt();
        cheatTitle = in.readString();
        cheatText = in.readString();
        languageId = in.readInt();
        walkthroughFormat = in.readByte() != 0;
        screenshots = in.readByte() != 0;
        screenshotList = in.createTypedArrayList(Screenshot.CREATOR);
        game = in.readParcelable(Game.class.getClassLoader());
        system = in.readParcelable(SystemModel.class.getClassLoader());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cheatId);
        dest.writeString(cheatTitle);
        dest.writeString(cheatText);
        dest.writeInt(languageId);
        dest.writeByte((byte) (walkthroughFormat ? 1 : 0));
        dest.writeByte((byte) (screenshots ? 1 : 0));
        dest.writeTypedList(screenshotList);
        dest.writeParcelable(game, flags);
        dest.writeParcelable(system, flags);
    }

    public Game getGame() {
        return game;
    }

    public SystemModel getSystem() {
        return system;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorName() {
        return author;
    }

    public int getCheatId() {
        return cheatId;
    }

    public String getCheatText() {
        cheatText = cheatText.replaceAll("\r\n", "<br>");
        return cheatText.replaceAll("\\\\", "");
    }

    public String getCheatTitle() {
        return cheatTitle.replaceAll("\\\\", "");
    }

    public String getCreatedDate() {
        return created;
    }

    public int getLanguageId() {
        return languageId;
    }

    public Member getSubmittingMember() {
        return submittingMember;
    }

    public float getMemberRating() {
        return memberRating;
    }

    public float getRatingAverage() {
        return rating;
    }

    public List<Screenshot> getScreenshotList() {
        return screenshotList;
    }

    public List<String> getScreenshotUrlList() {
        ArrayList<String> screenshotUrlList = new ArrayList<>();
        for (Screenshot s : screenshotList) {
            screenshotUrlList.add(s.getFullPath());
        }
        return screenshotUrlList;
    }

    public int getViews() {
        return views;
    }

    public int getViewsLifetime() {
        return viewsLifetime;
    }

    public int getViewsToday() {
        return viewsToday;
    }

    public int getViewsTotal() {
        return viewsLifetime;
    }

    public int getVotes() {
        return votes;
    }

    public String getGameName() {
        return game.getGameName();
    }

    public int getGameId() {
        return game.getGameId();
    }

    public String getSystemName() {
        return system.getSystemName();
    }

    public int getSystemId() {
        return system.getSystemId();
    }

    /**
     * Returns the age in days of the cheat
     *
     * @return int
     */
    public int getDayAge() {
        int cheatDayAge = 9999;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date createDate = dateFormat.parse(getCreatedDate());
            String submitted = dateFormat.format(createDate);
            String today = dateFormat.format(calendar.getTime());

            String cheatAge = String.valueOf(Tools.getDayDifference(submitted, today));
            cheatDayAge = Integer.parseInt(cheatAge);
        } catch (Exception e) {
            Log.e(Cheat.class.getName(), "getDayAge() Error: " + e.getMessage());
            return cheatDayAge;
        }

        return cheatDayAge;
    }

    public boolean hasScreenshots() {
        return screenshots;
    }

    public boolean isWalkthroughFormat() {
        return walkthroughFormat;
    }


    public static final Creator<Cheat> CREATOR = new Creator<Cheat>() {
        @Override
        public Cheat createFromParcel(Parcel in) {
            return new Cheat(in);
        }

        @Override
        public Cheat[] newArray(int size) {
            return new Cheat[size];
        }
    };


    public void setAuthor(String author) {
        this.author = author;
    }

    public void setAuthorName(String author) {
        this.author = author;
    }

    public void setCheatId(int cheatId) {
        this.cheatId = cheatId;
    }

    public void setCheatText(String cheatText) {
        this.cheatText = cheatText;
    }

    public void setCheatTitle(String cheatTitle) {
        this.cheatTitle = cheatTitle;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setHasScreenshots(boolean screenshots) {
        this.screenshots = screenshots;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public void setMember(Member member) {
        this.submittingMember = member;
    }

    public void setMemberRating(float memberRating) {
        this.memberRating = memberRating;
    }

    public void setRatingAverage(float rating) {
        this.rating = rating;
    }

    public void setScreenshotList(Screenshot[] sl) {
        if (screenshotList == null) {
            screenshotList = new ArrayList<>();
        }
        Collections.addAll(screenshotList, sl);
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setViewsLifetime(int viewsLifetime) {
        this.viewsLifetime = viewsLifetime;
    }

    public void setViewsToday(int viewsToday) {
        this.viewsToday = viewsToday;
    }

    public void setViewsTotal(int viewsLifetime) {
        this.viewsLifetime = viewsLifetime;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setWalkthroughFormat(boolean walkthroughFormat) {
        this.walkthroughFormat = walkthroughFormat;
    }

    public int getForumCount() {
        return forumCount;
    }

    public void setForumCount(int forumCount) {
        this.forumCount = forumCount;
    }

    public int getStyle() {
        return style; // 1 = normal cheat, 2 = walkthrough
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setSystem(SystemModel system) {
        this.system = system;
    }

    public FavoriteCheatModel toFavoriteCheatModel(int memberId) {
        return new FavoriteCheatModel(getGameId(), getGameName(), getCheatId(), getCheatTitle(), getCheatText(), getSystemId(), getSystemName(), getLanguageId(), isWalkthroughFormat(), memberId);
    }
}
