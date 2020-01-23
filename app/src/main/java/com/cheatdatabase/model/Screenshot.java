package com.cheatdatabase.model;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cheatdatabase.helpers.Konstanten;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

/**
 * Screenshot-Objekt eines Cheats.
 *
 * @author erbsland
 */
public class Screenshot implements Parcelable {
    private String kbyteSize;
    private String filename;
    private int cheatId;

    @Inject
    public Screenshot() {

    }

    @Inject
    public Screenshot(String kbyteSize, String filename, int cheatId) {
        super();
        this.kbyteSize = kbyteSize;
        this.filename = filename;
        this.cheatId = cheatId;
    }

    @Inject
    protected Screenshot(Parcel in) {
        // Attention: The order of writing and reading the parcel MUST match.
        kbyteSize = in.readString();
        filename = in.readString();
        cheatId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Attention: The order of writing and reading the parcel MUST match.
        dest.writeString(kbyteSize);
        dest.writeString(filename);
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

    public void setKbyteSize(String kbyteSize) {
        this.kbyteSize = kbyteSize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getCheatId() {
        return cheatId;
    }

    public void setCheatId(int cheatId) {
        this.cheatId = cheatId;
    }

    /**
     * Ladet den Screenshot vom Server und speichert ihn auf die SD Karte.
     *
     * @return
     */
    public boolean saveToSd() {
        String fileName = this.getCheatId() + this.getFilename();
        String fileURL = Konstanten.SCREENSHOT_ROOT_WEBDIR + fileName;

        try {
            URL u = new URL(fileURL);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + Konstanten.APP_PATH_SD_CARD + this.getCheatId());
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

            return true;
        } catch (Exception e) {
            Log.d("Screenshot.saveToSd", e.getMessage());
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
