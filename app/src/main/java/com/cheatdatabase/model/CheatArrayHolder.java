package com.cheatdatabase.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class CheatArrayHolder implements Parcelable {

    List<Cheat> cheatList;

    public CheatArrayHolder(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    public List<Cheat> getCheatList() {
        return cheatList;
    }

    public void setCheatList(List<Cheat> cheatList) {
        this.cheatList = cheatList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected CheatArrayHolder(Parcel in) {
        in.readTypedList(cheatList, Cheat.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(cheatList);
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
}
