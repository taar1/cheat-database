package com.cheatdatabase.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteCheatModel {

    @PrimaryKey
    @ColumnInfo(name = "cheat_id")
    public int cheatId;
    @ColumnInfo(name = "game_id")
    public int gameId;
    @ColumnInfo(name = "game_name")
    public String gameName;
    @ColumnInfo(name = "cheat_title")
    public String cheatTitle;
    @ColumnInfo(name = "cheat_text")
    public String cheatText;
    @ColumnInfo(name = "system_id")
    public int systemId;
    @ColumnInfo(name = "system_name")
    public String systemName;
    @ColumnInfo(name = "language_id")
    public int languageId;
    @ColumnInfo(name = "walkthrough_format")
    public Boolean isWalkthrough;
    // Local member who saved the favorite (for syncing online)
    @ColumnInfo(name = "member_id")
    public int memberId;

    public FavoriteCheatModel(int gameId, String gameName, int cheatId, String cheatTitle, String cheatText, int systemId, String systemName, int languageId, Boolean isWalkthrough, int memberId) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.cheatId = cheatId;
        this.cheatTitle = cheatTitle;
        this.cheatText = cheatText;
        this.systemId = systemId;
        this.systemName = systemName;
        this.languageId = languageId;
        this.isWalkthrough = isWalkthrough;
        this.memberId = memberId;
    }

    public int getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public int getCheatId() {
        return cheatId;
    }

    public String getCheatTitle() {
        return cheatTitle;
    }

    public String getCheatText() {
        return cheatText;
    }

    public int getSystemId() {
        return systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public int getLanguageId() {
        return languageId;
    }

    public Boolean isWalkthrough() {
        return isWalkthrough;
    }

    public int getMemberId() {
        return memberId;
    }

    public Cheat toCheat() {
        // TODO add screenshots...
        return new Cheat(getCheatId(), getCheatTitle(), getCheatText(), getLanguageId(), isWalkthrough(), toGame(), toSystem());
    }

    public Game toGame() {
        return new Game(getGameId(), getGameName(), getSystemId(), getSystemName());
    }

    public SystemModel toSystem() {
        return new SystemModel(getSystemId(), getSystemName());
    }
}

