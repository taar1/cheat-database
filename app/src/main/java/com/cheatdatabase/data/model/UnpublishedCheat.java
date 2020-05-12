package com.cheatdatabase.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "UnpublishedCheats")
public class UnpublishedCheat implements Parcelable {

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
    // This auto generate ID is needed because in theory the cheatId can be the same for several entries
    // because the query on the server is done on two different tables (SELECT UNION)
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "cheatId")
    @SerializedName("cheatId")
    public int cheatId;
    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "cheatText")
    public String cheat;

    @ColumnInfo(name = "languageId")
    @SerializedName("lang")
    public int lang;

    @ColumnInfo(name = "style")
    public int style;
    @ColumnInfo(name = "reject_reason")
    public String rejectReason;
    @ColumnInfo(name = "table_info")
    public String tableInfo;
    @SerializedName("game")
    private Game game;
    private Date created;
    private SystemModel system;
    @SerializedName("checked_date")
    private Date checkedDate;

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
        dest.writeTypedObject(game);
        dest.writeString(title);
        dest.writeString(cheat);
        dest.writeInt(lang);
        dest.writeInt(style);
        dest.writeSerializable(created);
        dest.writeSerializable(system);
        dest.writeSerializable(checkedDate);
        dest.writeString(rejectReason);
        dest.writeString(tableInfo);
    }
}
