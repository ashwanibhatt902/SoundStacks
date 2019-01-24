package com.startup.soundstack.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.startup.soundstack.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;
    Handler handler = new Handler();
    Runnable splashRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = null;
            SharedPreferences pref = getSharedPreferences("SoundStacks", MODE_PRIVATE);
            if(pref.getBoolean("firstTime", true)){
                intent = new Intent(SplashActivity.this, FirstTimeActivity.class);
            }else{
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.righttoleft, R.anim.lefttoright);
            finish();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(splashRunnable, SPLASH_TIME_OUT);


    }
}
