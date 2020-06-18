package com.cheatdatabase.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.cheatdatabase.data.RoomDateConverter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "UnpublishedCheats")
public class UnpublishedCheat implements Parcelable {

    // This auto generate ID is needed because in theory the cheatId can be the same for several entries
    // because the query on the server is done on two different tables (SELECT UNION)
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Unique ID in table cheat_submissions or rejected_cheats in the server database.
     */
    @ColumnInfo(name = "cheatId")
    @SerializedName("cheatId")
    public int cheatId;

    @ColumnInfo(name = "game")
    @SerializedName("game")
    public Game game;

    @ColumnInfo(name = "title")
    @SerializedName("title")
    public String title;

    @ColumnInfo(name = "cheat")
    @SerializedName("cheat")
    public String cheat;

    @ColumnInfo(name = "lang")
    @SerializedName("lang")
    public int lang;

    @ColumnInfo(name = "style")
    @SerializedName("style")
    public int style;

    @ColumnInfo(name = "created")
    @SerializedName("created")
    @TypeConverters(RoomDateConverter.class)
    public Date created;

    @ColumnInfo(name = "system")
    @SerializedName("system")
    public SystemModel system;

    @ColumnInfo(name = "checkedDate")
    @SerializedName("checked_date")
    @TypeConverters(RoomDateConverter.class)
    public Date checkedDate;

    @ColumnInfo(name = "rejectReason")
    @SerializedName("reject_reason")
    public String rejectReason;

    @ColumnInfo(name = "tableInfo")
    @SerializedName("table_info")
    public String tableInfo;

    protected UnpublishedCheat(Parcel in) {
        id = in.readInt();
        cheatId = in.readInt();
        game = in.readTypedObject(Game.CREATOR);
        title = in.readString();
        cheat = in.readString();
        lang = in.readInt();
        style = in.readInt();
        created = (java.util.Date) in.readSerializable();
        system = in.readTypedObject(SystemModel.CREATOR);
        checkedDate = (java.util.Date) in.readSerializable();
        rejectReason = in.readString();
        tableInfo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(cheatId);
        dest.writeTypedObject(game, flags);
        dest.writeString(title);
        dest.writeString(cheat);
        dest.writeInt(lang);
        dest.writeInt(style);
        dest.writeSerializable(created);
        dest.writeTypedObject(system, flags);
        dest.writeSerializable(checkedDate);
        dest.writeString(rejectReason);
        dest.writeString(tableInfo);
    }

    public static final Creator<UnpublishedCheat> CREATOR = new Creator<UnpublishedCheat>() {
        @Override
        public UnpublishedCheat createFromParcel(Parcel in) {
            return new UnpublishedCheat(in);
        }

        @Override
        public UnpublishedCheat[] newArray(int size) {
            return new UnpublishedCheat[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCheatId() {
        return cheatId;
    }

    public void setCheatId(int cheatId) {
        this.cheatId = cheatId;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCheat() {
        return cheat;
    }

    public void setCheat(String cheat) {
        this.cheat = cheat;
    }

    public int getLang() {
        return lang;
    }

    public void setLang(int lang) {
        this.lang = lang;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public SystemModel getSystem() {
        return system;
    }

    public void setSystem(SystemModel system) {
        this.system = system;
    }

    public Date getCheckedDate() {
        return checkedDate;
    }

    public void setCheckedDate(Date checkedDate) {
        this.checkedDate = checkedDate;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(String tableInfo) {
        this.tableInfo = tableInfo;
    }

    public static Creator<UnpublishedCheat> getCREATOR() {
        return CREATOR;
    }

    public Game toGame() {
        return new Game(game.getGameId(), game.getGameName(), system.getSystemId(), system.getSystemName());
    }

}
