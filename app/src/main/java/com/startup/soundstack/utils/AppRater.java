package com.startup.soundstack.utils;


import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Created by harsh on 10/18/2015.
 */


public class AppRater {

    private final static String APP_TITLE = "SoundStacks";// App Name

    private final static int DAYS_UNTIL_PROMPT = 1;//Min number of days

    public static void showRateDialog(final Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        final SharedPreferences.Editor editor = prefs.edit();

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);


        float density = mContext.getResources().getDisplayMetrics().density;
        int px =  (int)(10 * density);

        TextView tv = new TextView(mContext);
        tv.setText("If you enjoyed listening sound provided by " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        tv.setPadding(px, px, px, px);
        ll.addView(tv);

        Button rateButton = new Button(mContext);
        rateButton.setText("Rate " + APP_TITLE);
        rateButton.setPadding(px, px, px, px);
        rateButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mContext, " unable to find market app", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                editor.putBoolean("dontshowagain", true);
                editor.commit();
            }
        });
        ll.addView(rateButton);

        Button remindLaterBttn = new Button(mContext);
        remindLaterBttn.setText("Remind me later");
        remindLaterBttn.setPadding(px, px, px, px);
        remindLaterBttn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        remindLaterBttn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(remindLaterBttn);

        Button noButton = new Button(mContext);
        noButton.setText("No, thanks");
        noButton.setPadding(px, px, px, px);
        noButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        noButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
//        ll.addView(noButton);

        dialog.setContentView(ll);
        dialog.show();
    }

    public static boolean shouldDisplayDialog(Context mContext){
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        editor.commit();

        if (System.currentTimeMillis() >= date_firstLaunch +
                (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
            editor.commit();
            return true;
        }else{
            return false;
        }

    }
}