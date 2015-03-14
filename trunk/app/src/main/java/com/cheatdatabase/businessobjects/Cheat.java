package com.cheatdatabase.businessobjects;

import android.os.Environment;
import android.util.Log;

import com.cheatdatabase.helpers.Konstanten;
import com.cheatdatabase.helpers.Tools;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Plain Cheat-Object holding various information for display.
 *
 * @author Dominik Erbsland
 * @since 2010
 */
public class Cheat extends Game implements Serializable {

    private String cheatTitle, cheatText, created, author;
    private Member submittingMember;
    private float rating, memberRating;
    private Screenshot[] screens;
    private int cheatId, languageId, views, votes, viewsLifetime, viewsToday;
    private boolean walkthroughFormat, screenshots;

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

    public Game getGame() {
        return new Game(getGameId(), getGameName(), getSystemId(), getSystemName());
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
        return cheatText.replaceAll("\r\n", "<br>");
    }

    public String getCheatTitle() {
        return cheatTitle;
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

    public Screenshot[] getScreens() {
        return screens;
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
     * @return
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

    public void setScreens(Screenshot[] screens) {
        this.screens = screens;
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

}
