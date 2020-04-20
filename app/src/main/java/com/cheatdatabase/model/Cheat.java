package com.cheatdatabase.model;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cheatdatabase.data.model.FavoriteCheatModel;
import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;
import com.google.gson.annotations.SerializedName;

import java.io.File;
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
public class Cheat extends Game implements Parcelable {

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
    private SystemPlatform system;

    @Inject
    public Cheat() {
    }

    public Cheat(int gameId, String gameName, int cheatId, String cheatTitle, String cheatText, int languageId, int systemId, String systemName, boolean walkthroughFormat) {
        super(gameId, gameName, systemId, systemName);
        this.cheatId = cheatId;
        this.cheatTitle = cheatTitle;
        this.cheatText = cheatText;
        this.languageId = languageId;
        this.walkthroughFormat = walkthroughFormat;
    }

    protected Cheat(Parcel in) {
        // Attention: The order of writing and reading the parcel MUST match.
        screenshots = in.readByte() != 0;
        in.readTypedList(screenshotList, Screenshot.CREATOR);
        cheatTitle = in.readString();
        cheatText = in.readString();
        created = in.readString();
        author = in.readString();
        rating = in.readFloat();
        memberRating = in.readFloat();
        cheatId = in.readInt();
        languageId = in.readInt();
        views = in.readInt();
        votes = in.readInt();
        viewsLifetime = in.readInt();
        viewsToday = in.readInt();
        forumCount = in.readInt();
        walkthroughFormat = in.readByte() != 0;
        game = in.readTypedObject(Game.CREATOR);
        system = in.readTypedObject(SystemPlatform.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Attention: The order of writing and reading the parcel MUST match.
        dest.writeByte((byte) (screenshots ? 1 : 0));
        dest.writeTypedList(screenshotList);
        dest.writeString(cheatTitle);
        dest.writeString(cheatText);
        dest.writeString(created);
        dest.writeString(author);
        dest.writeFloat(rating);
        dest.writeFloat(memberRating);
        dest.writeInt(cheatId);
        dest.writeInt(languageId);
        dest.writeInt(views);
        dest.writeInt(votes);
        dest.writeInt(viewsLifetime);
        dest.writeInt(viewsToday);
        dest.writeInt(forumCount);
        dest.writeByte((byte) (walkthroughFormat ? 1 : 0));
        dest.writeTypedObject(game, Game.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeTypedObject(system, SystemPlatform.PARCELABLE_WRITE_RETURN_VALUE);
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

    public Game getGame() {
        return game;
    }

    public SystemPlatform getSystem() {
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

    /**
     * Schaut, ob auf der SD Karte Screenshots zu dem Cheat existieren.
     *
     * @return boolean
     */
    public boolean hasScreenshotOnSd() {
        if (Tools.isSdReadable()) {
            String fileName = this.getCheatId() + "a.png";
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + Konstanten.APP_PATH_SD_CARD + this.getCheatId());
            File file = new File(dir, fileName);
            return file.isFile();
        } else {
            return false;
        }

    }

    /**
     * @return the screenshots
     */
    public boolean isScreenshots() {
        return screenshots;
    }

    public boolean isWalkthroughFormat() {
        return walkthroughFormat;
    }

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

    @Override
    public int describeContents() {
        return 0;
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

    public void setSystem(SystemPlatform system) {
        this.system = system;
    }

    public FavoriteCheatModel toFavoriteCheatModel(int memberId) {
        return new FavoriteCheatModel(getGameId(), getGameName(), getCheatId(), getCheatTitle(), getCheatText(), getSystemId(), getSystemName(), getLanguageId(), 0, isWalkthroughFormat(), memberId);
    }
}
