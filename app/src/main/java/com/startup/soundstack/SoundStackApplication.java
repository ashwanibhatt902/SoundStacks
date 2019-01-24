package com.startup.soundstack;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.startup.soundstack.activities.HomeActivity;
import com.startup.soundstack.models.AdControl;
import com.startup.soundstack.models.AppExtensibleResource;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.Report;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.services.DeleteOldSoundFileService;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Dheeraj on 7/22/2015.
 */
public class SoundStackApplication extends Application{
    private Tracker mTracker;
    private static AdControl adControl;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Answers.getInstance().logCustom(new CustomEvent("App opened"));

        ParseObject.registerSubclass(Category.class);
        ParseObject.registerSubclass(SoundItem.class);
        ParseObject.registerSubclass(Report.class);
        ParseObject.registerSubclass(Tags.class);
        ParseObject.registerSubclass(AppExtensibleResource.class);

        initializeTracker();

        String gh = BuildConfig.BUILD_TYPE;
        System.out.print(gh);

        Parse.Configuration configuration = null;
        //for any debug build
        if (BuildConfig.DEBUG) {
            configuration = new Parse.Configuration.Builder(this)
                    .applicationId(BuildConfig.PARSE_APP_ID)
                    .clientKey(null)
                    .server(BuildConfig.PARSE_API_URL)
                    .enableLocalDataStore().build();
        }

        //release testing build
        else if (BuildConfig.TESTING_BUILD){
            configuration = new Parse.Configuration.Builder(this)
                    .applicationId(BuildConfig.PARSE_APP_ID)
                    .clientKey(null)
                    .server(BuildConfig.PARSE_API_URL)
                    .enableLocalDataStore().build();
        }

        //for production release build
        else {
            configuration = new Parse.Configuration.Builder(this)
                    .applicationId(BuildConfig.PARSE_APP_ID)
                    .clientKey(null)
                    .server(BuildConfig.PARSE_API_URL)
                    .enableLocalDataStore().build();
        }

        Parse.initialize(configuration);
        adControl = new AdControl();

        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this);

        if (Utility.getPreference(this).getString(Constants.Preference.SYSTEM_LOGIN_ID, "undefined").equalsIgnoreCase("undefined")) {
            AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
            Account[] list = manager.getAccounts();
            String gmailID = null;

            for (Account account : list) {
                if (account.type.equalsIgnoreCase("com.google")) {
                    gmailID = account.name;
                    break;
                }
            }
            if (gmailID != null) {
                Utility.setPreference(this, Constants.Preference.SYSTEM_LOGIN_ID, gmailID);
            }
        }
        initImageLoader(10);
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> adsQuery = ParseQuery.getQuery("Ads");
        adsQuery.getFirstInBackground(new GetCallback <ParseObject> () {
            @Override
            public void done (final ParseObject object, ParseException e){
                if (e == null) {
                    adControl.setEnabled(object.getBoolean("enabled"));
                    adControl.setCategory_enabled(object.getBoolean("category_enabled"));
                    adControl.setSound_enabled(object.getBoolean("sound_enabled"));
                    adControl.setRow_count(object.getInt("rowcount"));
                }
            }
        });

        ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
        tagsParseQuery.getFirstInBackground(new GetCallback < Tags > () {
            @Override
            public void done (final Tags object, ParseException e){
                if (e == null) {
                    object.unpinInBackground(Constants.PinningLabel.ALL_TAGS, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                object.pinInBackground(Constants.PinningLabel.ALL_TAGS);
                                HomeActivity.sAllTags = object;
                            }
                        }
                    });
                }
            }
        });

        if (currentUser != null) {
            currentUser.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    UpdateCenter.postUpdateEvent(this, UpdateCenter.EventType.Session);
                }
            });
        }

        Utility.downloadCategoryImagesAsync(this);
        DeleteOldSoundFileService.startActionClean(this);
    }



    public void initImageLoader(int discCacheSize) {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(discCacheSize>0).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        discCacheSize = discCacheSize < 1 ? 1 : discCacheSize;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).
                diskCacheSize(discCacheSize*1024*1024).
                memoryCacheSize(1024 * 1024).
                defaultDisplayImageOptions(defaultOptions).
                build();
        ImageLoader.getInstance().init(config);
        //end config
    }

    public void initializeTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(30);

            mTracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_ID);

        }
    }

    synchronized public Tracker getTracker(){
        return mTracker;
    }

    public static AdControl getAdControlObject(){
        return adControl;
    }
}
