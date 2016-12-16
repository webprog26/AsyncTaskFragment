package com.example.webprog26.asyncfragment.interfaces;

import android.graphics.Bitmap;

/**
 * Created by webprog26 on 16.12.2016.
 */

public interface AsyncPhotoTaskListener {
    void onPreExecute();
    void onProgressUpdate(Integer... progress);
    void onPostExecute(Bitmap result);
    void onCancelled(Bitmap result);
}
