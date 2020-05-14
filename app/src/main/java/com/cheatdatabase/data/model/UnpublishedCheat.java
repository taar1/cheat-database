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

    @ColumnInfo(name = "cheatId")
    @SerializedName("cheatId")
    public int cheatId;

    @ColumnInfo(name = "game")
    @SerializedName("game")
    private Game game;

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
    private Date created;

    @ColumnInfo(name = "system")
    @SerializedName("system")
    private SystemModel system;

    @ColumnInfo(name = "checkedDate")
    @SerializedName("checked_date")
    @TypeConverters(RoomDateConverter.class)
    private Date checkedDate;

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
        dest.writeParcelable(game, flags);
        dest.writeString(title);
        dest.writeString(cheat);
        dest.writeInt(lang);
        dest.writeInt(style);
        dest.writeSerializable(created);
        dest.writeParcelable(system, flags);
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
}
