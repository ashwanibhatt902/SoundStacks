package com.startup.soundstack.activities;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.adapter.MainActivityAdaptor;
import com.startup.soundstack.fragments.Sounds;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.Constants;

import java.io.File;

//import com.google.android.gms.ads.AdListener;


/**
 * Created by harsh on 10/24/2015.
 */


public class SoundShareActivity extends AppCompatActivity implements Sounds.OnSoundFragmentInteractionListener {


    ViewPager viewPager;
    MenuItem mLoginMenuItem = null;
    Toolbar mToolbar = null;

//    private MediaPlayer mMediaPlayer;
////    private List<SoundItem> mSoundsList;
//    private int mSelectedPosition = -1;
//    private PopupMenu mCategoryPopupMenu = null;
//    String mCategoryName = null;
//    private int mSelectedMenuID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_sound);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("All Sound"));
        tabLayout.addTab(tabLayout.newTab().setText("My Sound"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final MainActivityAdaptor adapter = new MainActivityAdaptor(getSupportFragmentManager(), tabLayout.getTabCount(), SoundShareActivity.class);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }



    @Override
    public void onSoundFragmentInteraction(SoundItem clickedSound) {

        Tracker t = ((SoundStackApplication)this.getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Sound Shared")
                .setAction(String.format("id : %s", clickedSound.getObjectId()))
                .build());

        File imagePath = new File(SoundShareActivity.this.getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
        File newFile = new File(imagePath, clickedSound.getObjectId() + clickedSound.getExtension());
//        Uri contentUri = Uri.fromFile(newFile);
        Uri contentUri = FileProvider.getUriForFile(SoundShareActivity.this, "com.soundstack.soundfileprovider", newFile);

        Intent theIntent = new Intent();
        theIntent.setData(contentUri);
        theIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        setResult(Activity.RESULT_OK, theIntent);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
    }
}

