package com.cheatdatabase.callbacks;

import android.graphics.Bitmap;

import java.util.List;

public interface GalleryLoadingCallback {

    void success(List<Bitmap> bitmapList);

    void fail(Exception e);

}
