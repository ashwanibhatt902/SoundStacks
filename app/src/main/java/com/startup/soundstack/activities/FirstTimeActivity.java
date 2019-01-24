package com.startup.soundstack.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.startup.soundstack.R;

public class FirstTimeActivity extends AppIntro2 {


    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(AppIntroFragment.newInstance("Listen Sound", "Listen new sounds added by users", R.drawable.sound_first, Color.parseColor("#EF6950")));
        addSlide(AppIntroFragment.newInstance("Record Sound", "Record new sounds and create categories", R.drawable.record_first, Color.parseColor("#EF6950")));
        addSlide(AppIntroFragment.newInstance("Share Sound", "Share sound publicly", R.drawable.zshare, Color.parseColor("#EF6950")));


        setVibrate(true);
        setVibrateIntensity(30);
    }


    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        SharedPreferences pref = getSharedPreferences("SoundStacks", MODE_PRIVATE);
        pref.edit().putBoolean("firstTime", false).commit();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.righttoleft, R.anim.lefttoright);
    }

    @Override
    public void onSlideChanged() {

    }

}
