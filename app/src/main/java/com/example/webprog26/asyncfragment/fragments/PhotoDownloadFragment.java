package com.example.webprog26.asyncfragment.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.webprog26.asyncfragment.interfaces.AsyncPhotoTaskListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by webprog26 on 16.12.2016.
 */

public class PhotoDownloadFragment extends Fragment {

    private static final String TAG = "PhotoDownloadFragment";

    private static final String PHOTO_URL = "photo_url";
    private AsyncPhotoTaskListener listener;
    private PhotoDownloadAsyncTask mTask;
    private URL mPhotoUrl;
    private boolean isTaskRunning = false;

    public static PhotoDownloadFragment newInstance(String photoUrl){
        Bundle args = new Bundle();
        PhotoDownloadFragment fragment = new PhotoDownloadFragment();
        args.putString(PHOTO_URL, photoUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        try {
            mPhotoUrl = new URL(getArguments().getString(PHOTO_URL));
        } catch (MalformedURLException e){
            e.printStackTrace();
        }
        Log.i(TAG, "onCreate() isTaskRunning " + isTaskRunning);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof AsyncPhotoTaskListener){
            listener = (AsyncPhotoTaskListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private class PhotoDownloadAsyncTask extends AsyncTask<Void, Integer, Bitmap>{

        int downloadedBytes = 0;
        int totalBytes = 0;

        private WeakReference<URL> mUrlWeakReference;

        public PhotoDownloadAsyncTask(URL photoUrl) {
            this.mUrlWeakReference = new WeakReference<URL>(photoUrl);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            URL photoUrl = mUrlWeakReference.get();
            if(photoUrl == null){
                return null;
            }
            Log.i(TAG, "In async url is " + photoUrl.toString());
            Bitmap bitmap =null;
            InputStream is = null;
            try {
                if (isCancelled()) {
                    return null;
                }
                publishProgress(0);
                HttpURLConnection conn = (HttpURLConnection) photoUrl.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int responseCode = conn.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK){
                    throw new Exception("Unsucesfull Result code");
                }
                totalBytes = conn.getContentLength();
                downloadedBytes = 0;

                is = conn.getInputStream();
                BufferedInputStream bif = new BufferedInputStream(is) {

                    int progress = 0;

                    public int read(byte[] buffer, int byteOffset, int byteCount)
                            throws IOException {
                        int readBytes = super.read(buffer, byteOffset, byteCount);
                        if ( isCancelled() ){
                            // Returning -1 means that there is no more data because the
                            // end of the stream has been reached.
                            return -1;
                        }
                        if (readBytes > 0) {
                            downloadedBytes += readBytes;
                            int percent = (int) ((downloadedBytes * 100f) / totalBytes);
                            if (percent > progress) {
                                publishProgress(percent);
                                progress = percent;
                            }
                        }
                        return readBytes;
                    }
                };
                Bitmap downloaded = BitmapFactory.decodeStream(bif);
                if ( !isCancelled() ){
                    bitmap = downloaded;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            listener.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            listener.onPostExecute(bitmap);
            Log.i(TAG, "onPostExecute(Bitmap bitmap) isTaskRunning " + isTaskRunning);
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            listener.onCancelled(bitmap);
        }
    }

    public void startDownload(){
        if(!isTaskRunning){
            mTask = new PhotoDownloadAsyncTask(mPhotoUrl);
            mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            isTaskRunning = true;
        }
        Log.i(TAG, "startDownload() isTaskRunning " + isTaskRunning);
    }

    public void updateRunningStatus(boolean status){
        this.isTaskRunning = status;
        Log.i(TAG, "updateRunningStatus(boolean status) " + isTaskRunning);
    }
}
