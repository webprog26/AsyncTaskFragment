package com.example.webprog26.asyncfragment;

import android.graphics.Bitmap;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.webprog26.asyncfragment.fragments.PhotoDownloadFragment;
import com.example.webprog26.asyncfragment.interfaces.AsyncPhotoTaskListener;

public class MainActivity extends AppCompatActivity implements AsyncPhotoTaskListener{

    private static final String TAG = "MainActivity_TAG";
    private static final String URL = "https://thumbs.dreamstime.com/z/christmas-girl-santa-22520312.jpg";
    private static final String PHOTO_DOWNLOAD_FRAGMENT_TAG = "photo_download_fragment_tag";

    private static final String PERCENTS_DOWNLOADED = "percents_downloaded";

    private PhotoDownloadFragment mPhotoDownloadFragment;

    private TextView mTvDownloadedPercents;
    private ProgressBar mPbDownloadedPercents;
    private ImageView mIvPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPhotoDownloadFragment =(PhotoDownloadFragment) fragmentManager.findFragmentByTag(PHOTO_DOWNLOAD_FRAGMENT_TAG);
        if(mPhotoDownloadFragment == null){
            mPhotoDownloadFragment = PhotoDownloadFragment.newInstance(URL);
            fragmentManager.beginTransaction().add(mPhotoDownloadFragment, PHOTO_DOWNLOAD_FRAGMENT_TAG).commit();
        }

        mTvDownloadedPercents = (TextView) findViewById(R.id.tvDownloadedPercents);
        mPbDownloadedPercents = (ProgressBar) findViewById(R.id.pbDownloadedPercents);
        updateProgressBar(0);
        if(savedInstanceState != null){
            int progress = savedInstanceState.getInt(PERCENTS_DOWNLOADED);
            Log.i(TAG, "progress " + progress);
            mPbDownloadedPercents.setProgress(progress);
            Log.i(TAG, "getProgress() " + mPbDownloadedPercents.getProgress());
            mTvDownloadedPercents.setText(getResources().getString(R.string.percents, progress));
        } else {
            updateProgressBar(0);
            mTvDownloadedPercents.setText(getResources().getString(R.string.percents, 0));
        }

        mIvPhoto = (ImageView) findViewById(R.id.ivPhoto);

        Button btnGetPhoto = (Button) findViewById(R.id.btnGetPhoto);
        btnGetPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProgressBar(0);
                //In real world you should check internet connection
                if(mPhotoDownloadFragment != null){
                    mPhotoDownloadFragment.startDownload();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(PERCENTS_DOWNLOADED, mPbDownloadedPercents.getProgress());
    }

    @Override
    public void onPreExecute() {
        //
    }

    @Override
    public void onProgressUpdate(Integer... progress) {
        mPbDownloadedPercents.setProgress(progress[0]);
        mTvDownloadedPercents.setText(getResources().getString(R.string.percents, progress[0]));
    }

    @Override
    public void onPostExecute(Bitmap result) {
        if(result != null){
            mIvPhoto.setImageBitmap(result);
        }
        if(mPhotoDownloadFragment != null){
            mPhotoDownloadFragment.updateRunningStatus(false);
        }
    }

    @Override
    public void onCancelled(Bitmap result) {
        //
    }

    private void updateProgressBar(int progress){
        if(mPbDownloadedPercents != null){
            mPbDownloadedPercents.post(new Runnable() {
                @Override
                public void run() {
                    mPbDownloadedPercents.setProgress(0);
                }
            });
        }
    }
}
