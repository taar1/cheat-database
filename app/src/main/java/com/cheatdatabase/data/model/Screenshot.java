package com.cheatdatabase.data.model;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cheatdatabase.helpers.Konstanten;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

/**
 * Screenshot within a Cheat
 *
 * @author Dominik Erbsland
 */
public class Screenshot implements Parcelable {
    private static final String TAG = "Screenshot";

    @SerializedName("kbyteSize")
    private String kbyteSize;
    @SerializedName("filename")
    private String filename;
    @SerializedName("fullPath")
    private String fullPath;
    @SerializedName("cheatId")
    private int cheatId;

    @Inject
    public Screenshot() {

    }

    public Screenshot(String kbyteSize, String filename, String fullPath, int cheatId) {
        super();
        this.kbyteSize = kbyteSize;
        this.filename = filename;
        this.fullPath = fullPath;
        this.cheatId = cheatId;
    }

    protected Screenshot(Parcel in) {
        // Attention: The order of writing and reading the parcel MUST match.
        kbyteSize = in.readString();
        filename = in.readString();
        fullPath = in.readString();
        cheatId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Attention: The order of writing and reading the parcel MUST match.
        dest.writeString(kbyteSize);
        dest.writeString(filename);
        dest.writeString(fullPath);
        dest.writeInt(cheatId);
    }

    public static final Creator<Screenshot> CREATOR = new Creator<Screenshot>() {
        @Override
        public Screenshot createFromParcel(Parcel in) {
            return new Screenshot(in);
        }

        @Override
        public Screenshot[] newArray(int size) {
            return new Screenshot[size];
        }
    };

    public String getKbyteSize() {
        return kbyteSize;
    }

    public String getFilename() {
        return filename;
    }

    public int getCheatId() {
        return cheatId;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getFullPathOnSdCard() {
        String fullPathOnSdCard = Environment.getExternalStorageDirectory().getAbsolutePath() + Konstanten.APP_PATH_SD_CARD + getCheatId() + getFilename();
        Log.d(TAG, "XXXXX getFullPathOnSdCard: " + fullPathOnSdCard);
        return fullPathOnSdCard;
    }

    /**
     * Ladet den Screenshot vom Server und speichert ihn auf die SD Karte.
     *
     * @return boolean
     */
    public void saveToSd() throws IOException {
        String fileName = getCheatId() + getFilename();
        String fileURL = Konstanten.SCREENSHOT_ROOT_WEBDIR + fileName;

        URL u = new URL(fileURL);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setDoOutput(true);
        c.connect();

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + Konstanten.APP_PATH_SD_CARD + getCheatId());
        dir.mkdirs();
        File file = new File(dir, fileName);

        FileOutputStream f = new FileOutputStream(file);

        InputStream in = c.getInputStream();

        byte[] buffer = new byte[1024];
        int len1 = 0;
        while ((len1 = in.read(buffer)) > 0) {
            f.write(buffer, 0, len1);
        }
        f.close();
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
