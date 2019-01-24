package com.startup.soundstack.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.startup.soundstack.BuildConfig;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.adapter.MainActivityAdaptor;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.fragments.CreateCategoryDialog;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.soundfile.SoundFile;
import com.startup.soundstack.utils.MenuColorizer;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.UpdateCenter.EventType;
import com.startup.soundstack.utils.Utility;

import de.psdev.licensesdialog.LicensesDialog;


public class HomeActivity extends AppCompatActivity {

    ViewPager viewPager;
    MenuItem mLoginMenuItem = null;
    Toolbar mToolbar = null;
    int mPreviousTab = 0;


    public static Tags sAllTags;
    public static int sCurrentTabPosition;
    //TODO remove below static variaable in future
    public static SoundFile sSoundFile;

    public static DisplayImageOptions sDisplayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(false)
            .cacheOnDisk(true).resetViewBeforeLoading(true)
            .showImageForEmptyUri(R.color.color_primary)
            .showImageOnFail(R.color.color_primary)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Answers.getInstance().logCustom(new CustomEvent("Home Activity"));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Tracker t = ((SoundStackApplication)this.getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Screen Opened")
                .setAction("Home")
                .build());

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.category_Tab)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.allSound_Tab)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.Personal_Tab)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final MainActivityAdaptor adapter = new MainActivityAdaptor(getSupportFragmentManager(), tabLayout.getTabCount(), HomeActivity.class);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() == null) {
                    Snackbar.make(view, R.string.login_request, Snackbar.LENGTH_LONG)
                            .setAction("Login", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Utility.openLoginActivity(HomeActivity.this);
                                }
                            }).show();
                    return;
                }else{
                    if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && !ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                        ParseUser.getCurrentUser().fetchInBackground();
                        Snackbar.make(view, R.string.verify_mail_request, Snackbar.LENGTH_LONG)
                                .setAction("Resend", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final ParseUser user = ParseUser.getCurrentUser();
                                        final String email = user.getEmail();
                                        user.setEmail("");
                                        user.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                user.setEmail(email);
                                                user.saveEventually();
                                            }
                                        });
                                    }
                                }).show();
                        return;
                    }
                }

                if (viewPager.getCurrentItem() == 1) {
                    SelectType selectTypeDialog = new SelectType();
                    selectTypeDialog.show(getSupportFragmentManager(), "select");
                } else {
                    CreateCategoryDialog createCategoryDialog = new CreateCategoryDialog();
                    createCategoryDialog.show(getSupportFragmentManager(), "cat");
                }

            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                sCurrentTabPosition = tab.getPosition();
                if (tab.getPosition() == 2) {
                    fab.setVisibility(View.GONE);
                    tabLayout.bringToFront();

                } else {
                    if (tab.getPosition() == 1) {
                        fab.setImageResource(R.drawable.record_normal);
                    } else {
                        fab.setImageResource(R.drawable.ic_playlist_add_white_24dp);
                    }
                    fab.setVisibility(View.VISIBLE);
                    if (mPreviousTab == 2) {
                        Utility.applyCircularRevealAnimation(fab, false);
                    }
                }

                mPreviousTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        UpdateCenter.getEventBus().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);

        MenuColorizer.colorMenu(this, menu, getResources().getColor(R.color.color_accent), -1, true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
       mLoginMenuItem = menu.findItem(R.id.login);
        if (ParseUser.getCurrentUser() != null) {
            mLoginMenuItem.setTitle("Logout");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login: {
                if (item.getTitle().equals("Login")) {
                    Utility.openLoginActivity(HomeActivity.this);
                }
                else {
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            UpdateCenter.postUpdateEvent(HomeActivity.this, EventType.Session);
                        }
                    });
//                    item.setTitle("Login");
                }
                return true;
            }

            case R.id.share: {
                String shareBody = "Listen & Share. https://play.google.com/store/apps/details?id="+this.getPackageName();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sound Stack");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(sharingIntent, "Share App..."));
                }

                return true;
            }

            case R.id.about: {
                final Dialog alertDialog = new Dialog(HomeActivity.this);

                alertDialog.setTitle("About");
                alertDialog.setContentView(R.layout.dialog_about);

                CustomText version = (CustomText)alertDialog.findViewById(R.id.version);
                version.setText(getString(R.string.Version_SoundStacks)+" "+BuildConfig.VERSION_NAME+"."+BuildConfig.VERSION_CODE);

                View v = alertDialog.findViewById(R.id.license);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        new LicensesDialog.Builder(HomeActivity.this)
                                .setNotices(R.raw.soundstacknotices)
                                .setIncludeOwnLicense(true)
                                .setThemeResourceId(R.style.AppTheme)
                                .setDividerColorId(R.color.accent_material_dark)
                                .build()
                                .show();
                    }
                });
                alertDialog.show();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() != 0){
            viewPager.setCurrentItem(0);
        }else{
            super.onBackPressed();
            overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
        }
    }

    @Subscribe
    public void updateUI(UpdateCenter.UpdateEvent event) {

        if (mLoginMenuItem != null && event.mType.equals(EventType.Session)) {
            if (ParseUser.getCurrentUser() != null) {
                mLoginMenuItem.setTitle("Logout");
                //fetch user all category in BG if he is login
                Utility.pinUserCategoryInBG();
            }
            else {
                mLoginMenuItem.setTitle("Login");

            }
        }
    }

//    @Subscribe
//    public void updateUI(final UpdateCenter.CreateSoundEvent event) {
//        if (event.mSound != null && selectTypeDialog != null) {
//            selectTypeDialog.dismiss();
//            selectTypeDialog = null;
//        }
//    }


}
