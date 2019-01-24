package com.startup.soundstack.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.fragments.Categories;
import com.startup.soundstack.fragments.CreateCategoryDialog;
import com.startup.soundstack.fragments.Sounds;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SoundActivity extends AppCompatActivity  {
    private String mTitle;
    private FloatingActionButton fab = null;

    JSONObject obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        String strObj = null;

        if (savedInstanceState != null) {
            strObj = savedInstanceState.getString("obj");
        }

        //if no data in saved instance then it might be new intent fire
        if (strObj == null) {
            strObj = getIntent().getStringExtra("obj");
        }
        try {
            obj = new JSONObject(strObj);
        } catch (JSONException e) {
            e.printStackTrace();
            obj = new JSONObject();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTitle = obj.optString("title");
        getSupportActionBar().setTitle(mTitle);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int iconResID = obj.optInt("iconResID");
        if (iconResID != -1) {
            getSupportActionBar().setIcon(iconResID);
        }



        fab = (FloatingActionButton)findViewById(R.id.fab);

        if (obj.optString("class").equals(Category.class.toString())) {

            JSONArray jArray = obj.optJSONArray("queryValue");
            String queryKey = obj.optString("queryKey");


            if (obj.optBoolean("showFAB")) {
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_playlist_add_white_24dp);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ParseUser.getCurrentUser() == null) {
                            Snackbar.make(view, R.string.login_request, Snackbar.LENGTH_LONG)
                                    .setAction("Login", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Utility.openLoginActivity(SoundActivity.this);
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
                        CreateCategoryDialog createCategoryDialog = new CreateCategoryDialog();
                        createCategoryDialog.show(getSupportFragmentManager(), "cat");
                    }
                });
            }

            Categories categories = (Categories)getSupportFragmentManager().findFragmentByTag(Categories.class.toString());

            if (categories == null) {

                categories  = new Categories();

                categories.setQuery(queryKey, jArray);
                categories.enableSwipeRefreshView(obj.optBoolean("enableSwipe"));

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, categories, Categories.class.toString())
                        .commit();
            }

            else {
                categories.setQuery(queryKey, jArray);
                categories.enableSwipeRefreshView(obj.optBoolean("enableSwipe"));
            }
        }

        else {

            if (obj.optBoolean("showFAB")) {
                fab.setVisibility(View.VISIBLE);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ParseUser.getCurrentUser() == null) {
                            Snackbar.make(view, R.string.login_request, Snackbar.LENGTH_LONG)
                                    .setAction("Login", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Utility.openLoginActivity(SoundActivity.this);
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
                        SelectType selectTypeDialog = new SelectType();
                        selectTypeDialog.setmOpenCategory(false);
                        selectTypeDialog.show(getSupportFragmentManager(), "select");
                    }
                });
            }


            JSONArray jArray = obj.optJSONArray("queryValue");
            String queryKey = obj.optString("queryKey");

            Sounds sounds = (Sounds)getSupportFragmentManager().findFragmentByTag(Sounds.class.toString());

            if (sounds == null) {
                sounds = new Sounds();

                sounds.setQuery(queryKey, jArray);
                sounds.enableSwipeRefreshView(obj.optBoolean("enableSwipe"));

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, sounds, Sounds.class.toString())
                        .commit();
            } else  {
                sounds.setQuery(queryKey, jArray);
                sounds.enableSwipeRefreshView(obj.optBoolean("enableSwipe"));
            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("obj", obj.toString());
        // etc.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
                overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
    }
}
