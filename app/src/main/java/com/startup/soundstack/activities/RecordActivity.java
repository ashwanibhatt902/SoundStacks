package com.startup.soundstack.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.parse.ParseUser;
import com.startup.soundstack.R;
import com.startup.soundstack.soundfile.SoundFile;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.MenuColorizer;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener{

    final int RECORD_SOUND_PERMISSION_CODE = 1231;
    ImageView micImage;
    boolean isRecording;
    private static final int RECORDINGLIMIT = 90;
    int secondCount = 10;
    TextView numText, messageText;
    MediaRecorder mRecorder;
    Handler mHandler;
    private String mFilename = "/sdcard/temp.amr";
    private Toast mToast = null;
    private boolean mRecordingKeepGoing;
    private RecordAudioThread mRecordAudioThread;
    private SoundFile mSoundFile;
    private long mRecordingLastUpdateTime;
    private double mRecordingTime;
    View  mParentView;
    ImageButton mPauseButton;
    SoundFile.ProgressListener listener ;

    private Bundle mPreviousBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Answers.getInstance().logCustom(new CustomEvent("Record Activity"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AlphaAnimation blinkanimation= new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        blinkanimation.setDuration(800); // duration - half a second
        blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        blinkanimation.setRepeatCount(-1); // Repeat animation infinitely
        blinkanimation.setRepeatMode(Animation.REVERSE);

        micImage = (ImageView) findViewById(R.id.micImg);
        micImage.setAnimation(blinkanimation);

        numText = (TextView) findViewById(R.id.num);
        messageText = (TextView) findViewById(R.id.messagge);
        mPauseButton = (ImageButton) findViewById(R.id.pauseButton);

        micImage.setOnClickListener(this);
        messageText.setText(getString(R.string.taptostart));

        getSupportActionBar().setTitle("Record Audio");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler =  new Handler();

        mPreviousBundle = getIntent().getExtras();
        mParentView = findViewById(R.id.parentRL);

    }

    @Override
    public void onClick(View v) {
        if(mRecordingKeepGoing){

            if (mRecordAudioThread.mPaused ) {
                mRecordAudioThread.onResume();
            }
            if (mRecordingTime > 2) {
//                mHandler.removeCallbacks(runnable);
//                stopRecording();
                mRecordingKeepGoing = false;
            }
            else if (mToast == null || mToast.getView() == null ||(mToast.getView() != null && !mToast.getView().isShown())) {
               mToast =  Toast.makeText(RecordActivity.this, "Wait for 2 secs", Toast.LENGTH_SHORT);
                mToast.show();
            }
        }else {

            recordAudioWithPermission();
//            recordAudio();
        }
    }

    public void recordAudioWithPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.RECORD_AUDIO)) {
                if (false) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            RECORD_SOUND_PERMISSION_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        else {
            recordAudio();
        }
        }


    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

        protected void recordAudio() {

            micImage.setImageResource(R.drawable.record_play);
            messageText.setText(getString(R.string.taptosop));
            mRecordingLastUpdateTime = getCurrentTime();
            mRecordingKeepGoing = true;

        listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double elapsedTime) {
                        long now = getCurrentTime();
                        if (now - mRecordingLastUpdateTime > 5) {
                            mRecordingTime = elapsedTime;
                            // Only UI thread can update Views such as TextViews.
                            if  (elapsedTime >= RECORDINGLIMIT) mRecordingKeepGoing = false;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    int min = (int)(mRecordingTime/60);
                                    float sec = (float)(mRecordingTime - 60 * min);
                                    numText.setText(String.format("%d:%05.2f", min, sec));
                                }
                            });
                            mRecordingLastUpdateTime = now;
                        }

                        if (mRecordAudioThread.mPaused) {
                            synchronized (mRecordAudioThread.mPauseLock) {
                                try {
                                    mRecordAudioThread.mPauseLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    mRecordAudioThread.interrupt();
                                    return false;
                                }
                            }
                        }
                        return mRecordingKeepGoing;
                    }
                };

        // Record the audio stream in a background thread
        mRecordAudioThread = new RecordAudioThread() ;
        mRecordAudioThread.start();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_record_activity, menu);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                ActivityCompat.finishAfterTransition(this);
//                return true;
//            case R.id.action_reset:
//                reset();
//                return true;
//            case R.id.action_pause: {
//                if (mRecordAudioThread != null) {
//                    if (mRecordAudioThread.mPaused) {
//                        mRecordAudioThread.onResume();
//                    }
//                    else {
//                        mRecordAudioThread.onPause();
//                    }
//                }
//            }
//
//
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
    }

    @Override
    protected void onDestroy() {
        if (mRecordAudioThread != null) {
            reset();
        }
        super.onDestroy();
    }

    private void reset() {


        if (mRecordAudioThread != null) {
            mRecordAudioThread.interrupt();
        }
        mRecordingKeepGoing = false;
        micImage.setImageResource(R.drawable.record_normal);
        numText.setText("0");
        SoundFile mSoundFile = null;
        long mRecordingLastUpdateTime = 0;
        messageText.setText(getString(R.string.taptostart));
        mRecordingTime = 0;
    }

    public void recordinAction(View view) {
        switch (view.getId()) {
            case R.id.stopButton: {
                onClick(view);
                break;
            }
            case R.id.pauseButton: {
                if (mRecordAudioThread != null) {
                    if (mRecordAudioThread.mPaused) {
                        mPauseButton.setImageResource(R.drawable.ic_pause_white_48dp);
                        mRecordAudioThread.onResume();
                    }
                    else {
                        mPauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                        mRecordAudioThread.onPause();
                    }
                }
                break;
            }
            case R.id.resetButton: {
                reset();
                break;
            }
        }
    }

    class RecordAudioThread extends Thread {

        public Object mPauseLock;
        public boolean mPaused;
        private boolean mFinished;


        public RecordAudioThread() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
        }

        /**
         * Call this on pause.
         */
        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        /**
         * Call this on resume.
         */
        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }


        public void run() {
            try {
                mSoundFile = SoundFile.record(listener);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                if (mSoundFile == null) {

                    return;
                }
                Intent intent = new Intent(RecordActivity.this, MainActivity.class);
                intent.putExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD, mPreviousBundle);
                HomeActivity.sSoundFile = mSoundFile;
//                    intent.putExtra(Constants.FILENAME, mSoundFile.getInputFile().getPath());

                startActivity(intent);
                RecordActivity.this.finish();
            } catch (InterruptedException ie) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        numText.setText("0");
                    }
                });
                return;
            } catch (final Exception e) {

                return;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_SOUND_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    recordAudio();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(mParentView, "You denied the permission required for recording sound", Snackbar.LENGTH_INDEFINITE).setAction("Grant Again", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recordAudioWithPermission();
                        }
                    }).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
