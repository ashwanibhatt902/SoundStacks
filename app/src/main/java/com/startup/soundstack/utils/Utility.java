package com.startup.soundstack.utils;

import android.animation.Animator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.Login;
import com.startup.soundstack.models.AppExtensibleResource;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import bolts.Task;

/**
 * Created by Dheeraj on 8/1/2015.
 */
public class Utility {
    private static String FOLDER_NAME = ".SoundStack";
    public static final String Sound_tag = "sounds";

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static void setPreference(Context context, String key, Object value){

        SharedPreferences pref = context.getSharedPreferences("soundstack", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if(value instanceof Long){
            editor.putLong(key, (Long)value);
        }else if(value instanceof Float){
            editor.putFloat(key, (Float)value);
            editor.putFloat(key, (Float)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(key, (Boolean)value);
        }else if(value instanceof Integer){
            editor.putInt(key, (Integer)value);
        }else if(value instanceof String){
            editor.putString(key, (String)value);
        }

        editor.commit();
    }

    public static SharedPreferences getPreference(Context context){

        return context.getSharedPreferences("soundstack", Context.MODE_PRIVATE);

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static boolean isValidPassword(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return true; //TODO we could write our own password validation
        }
    }

    public static void saveEventuallyAll(List<ParseObject> allObjects) {
        for (ParseObject parseObject: allObjects) {
            parseObject.saveEventually();
        }
    }

    public static void revertAll(List<ParseObject> allObjects) {
        for (ParseObject parseObject: allObjects) {
            parseObject.revert();
        }
    }

    public static void openLoginActivity(Context context) {
        Intent intent = new Intent(context, Login.class);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.righttoleft, R.anim.lefttoright);
    }

    public static void logoutCurrentUser() {
        if (ParseUser.getCurrentUser() != null) ParseUser.logOut();
    }

    public static JSONObject createJsonQuery(String activityTitle, String queryKey, JSONArray queryValue, String className, int iconResID) {
        JSONObject jsonObject = new JSONObject();

        if (TextUtils.isEmpty(activityTitle)) activityTitle = "Sound Items";
        try {
            jsonObject.put("class", className);
            jsonObject.putOpt("title", activityTitle);
            jsonObject.putOpt("queryKey", queryKey);
            jsonObject.putOpt("queryValue", queryValue);
            jsonObject.putOpt("iconResID", iconResID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static Task<ParseObject> uploadUserImageAsync(final Context context, final String sURL, final String propertyName) {

        final Task<ParseObject>.TaskCompletionSource tcs = Task.create();

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                InputStream oin = null;
                String ext = "jpeg";
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                try {
                    Uri uri = Uri.parse(params[0]);
                    if (uri != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                        ContentResolver contentResolver = context.getContentResolver();
                        oin = contentResolver.openInputStream(uri);
                        ext = mime.getExtensionFromMimeType(contentResolver.getType(uri));
                    }
                    else {
                        URL url = new URL(params[0]);
                        URLConnection connection = url.openConnection();
                        oin = connection.getInputStream();
                        String type = connection.getContentType();
                        if (!TextUtils.isEmpty(type)) {
                            ext = mime.getExtensionFromMimeType(connection.getContentType());
                        }
                    }
                    byte[] contents = new byte[16384];

                    int bytesRead=0;
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    while( (bytesRead = oin.read(contents)) != -1){
                        buffer.write(contents, 0, bytesRead);
                    }

                    final byte[] data = buffer.toByteArray();
                    final ParseUser  user = ParseUser.getCurrentUser();
                    String filename = propertyName+"."+ext;
                    ParseFile file = new ParseFile(filename, data);
                    user.put(propertyName, file);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                tcs.setError(e);
                            }
                            else {
                                if (Constants.UserProperty.PROFILE_PIC_FILE.equals(propertyName)  && (data.length)/1024 > 300 ) {
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    params.put("userID", user.getObjectId());
                                    ParseCloud.callFunctionInBackground("scaleProfileImage", params, new FunctionCallback<Object>() {
                                        @Override
                                        public void done(Object object, ParseException e) {
                                        try {
                                            user.fetch();
                                            tcs.setResult(user);
                                        } catch (ParseException e1) {
                                            e1.printStackTrace();
                                            tcs.setError(e1);
                                        }

//                                        PersonalFragment.this.updateUI(UpdateCenter.generateUpdateEvent(PersonalFragment.this, UpdateCenter.EventType.Session));
//                                        profilePB.setVisibility(View.GONE);
//                                        Snackbar.make(mBaseView, finalMsg, Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else {
                                    try {
                                        user.fetch();
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                    tcs.setResult(user);
                                }
                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    tcs.setError(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    tcs.setError(e);
                }  finally {
                    try {
                        if (oin != null) oin.close();
                    } catch (Exception e) {
                        Log.v("UploadUserImage", "Fail to close stream");
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }.execute(sURL);

        return tcs.getTask();
    }

    public static Task<ParseObject> downloadCategoryImagesAsync(final Context context) {
        final Task<ParseObject>.TaskCompletionSource tcs = Task.create();

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                Long lastSuccessGetTime = Utility.getPreference(context).getLong(Constants.Preference.CATEGORY_IMAGES_LAST_DOWNLOADED_TIME, 0);
                boolean downOnce = Utility.getPreference(context).getBoolean(Constants.Preference.CATEGORY_IMAGES_RES_DOWNLOADED_ONCE, false);
                final SharedPreferences.Editor editor = Utility.getPreference(context).edit();

                if (!downOnce) {

                    editor.commit();

                    ParseQuery<AppExtensibleResource> appCatImagesQuery = ParseQuery.getQuery(AppExtensibleResource.class);
                    appCatImagesQuery.whereGreaterThanOrEqualTo(AppExtensibleResource.RESOURCE_ID, 2400);

                    appCatImagesQuery.findInBackground(new FindCallback<AppExtensibleResource>() {
                        @Override
                        public void done(List<AppExtensibleResource> objects, ParseException e) {
                            if (e == null) {
                                if (objects != null && objects.size() > 0) {
                                    try {
                                        ParseObject.pinAll(Constants.PinningLabel.CATEGORY_IMAGE, objects);

                                        editor.putBoolean(Constants.Preference.CATEGORY_IMAGES_RES_DOWNLOADED_ONCE, true);
                                        editor.putLong(Constants.Preference.CATEGORY_IMAGES_LAST_DOWNLOADED_TIME, System.currentTimeMillis());
                                    } catch (ParseException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }
                    });

                }

                else {
                    Date lastLaunchDate = new Date(lastSuccessGetTime);
                    ParseQuery<AppExtensibleResource> AppCatImagesQuery = ParseQuery.getQuery(AppExtensibleResource.class);
                    AppCatImagesQuery.whereGreaterThanOrEqualTo(AppExtensibleResource.RESOURCE_ID, 2400);

                    DateFormat gmtFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
                    TimeZone gmtTime = TimeZone.getTimeZone("GMT");
                    gmtFormat.setTimeZone(gmtTime);
                    try {
                        lastLaunchDate = gmtFormat.parse(gmtFormat.format(lastLaunchDate));
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }

                    AppCatImagesQuery.whereGreaterThan("updatedAt", lastLaunchDate);
                    AppCatImagesQuery.findInBackground(new FindCallback<AppExtensibleResource>() {
                        @Override
                        public void done(final List<AppExtensibleResource> objects, ParseException e) {
                            if (e == null) {
                                if (objects != null && objects.size() > 0) {
                                    ParseObject.unpinAllInBackground(Constants.PinningLabel.CATEGORY_IMAGE, objects, new DeleteCallback() {
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                // There was some error.
                                                return;
                                            }

                                            // Add the latest results for this query to the cache.
                                            try {
                                                ParseObject.pinAll(Constants.PinningLabel.CATEGORY_IMAGE, objects);
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                                }
                                editor.putLong(Constants.Preference.CATEGORY_IMAGES_LAST_DOWNLOADED_TIME, System.currentTimeMillis());
                            }
                        }
                    });

                }
                editor.putLong("date_firstlaunch", System.currentTimeMillis());
                return true;
            }
        }.execute();

        return tcs.getTask();

    }
    public static boolean contain(String SoundId, JSONArray allIds) {

        boolean contain = false;

        if (allIds != null && allIds.length() > 0) {
            for (int i = 0; i < allIds.length(); i++) {
                try {
                    String id = allIds.getString(i);
                    if (id.equals(SoundId)) {
                        contain = true;
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return contain;
    }

    public static void applyCircularRevealAnimation(View view, boolean elseApplyLinear) {

        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        // create the animator for this view (the start radius is zero)

        Animator anim = null;
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){
            anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            anim.start();
        } else{
            AlphaAnimation blinkanimation= new AlphaAnimation(0, 1); // Change alpha from fully visible to invisible
            blinkanimation.setDuration(800); // duration - half a second
            blinkanimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
//            blinkanimation.setRepeatCount(1); // Repeat animation infinitely
            blinkanimation.setRepeatMode(Animation.REVERSE);

            view.setAnimation(blinkanimation);
        }

    }

    public  static  List<String> jArrayToList(JSONArray jArray, String stringToAdd ) {
        List<String> soundIds = new ArrayList<>();
        if (stringToAdd == null) {
            for (int i = 0; i < jArray.length(); i++) {
                soundIds.add(jArray.optString(i));
            }
        }

        else {
            for (int i = 0; i < jArray.length(); i++) {
                soundIds.add(jArray.optString(i)+stringToAdd);
            }
        }
        return soundIds;
    }

    public static void pinUserCategoryInBG() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) return;

        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
        query.whereEqualTo(SoundItem.COLUMN_UPLOADED_BY_USER, currentUser.getObjectId());

        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(final List<Category> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    ParseObject.unpinAllInBackground(Constants.PinningLabel.USER_CATEGORY, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseObject.pinAllInBackground(Constants.PinningLabel.USER_CATEGORY, objects);
                            }
                        }
                    });
                }
            }
        });
    }

    public static String getSoundPath(CharSequence title, String extension) {
        String subdir = ".soundStack/temp/";

        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }

        String parentdir = externalRootDir + subdir;

        // Create the parent directory
        File parentDirFile = new File(parentdir);
        parentDirFile.mkdirs();

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory()) {
            parentdir = externalRootDir;
        }

        // Turn the title into a filename
        String filename = "";
        for (int i = 0; i < title.length(); i++) {
            if (Character.isLetterOrDigit(title.charAt(i))) {
                filename += title.charAt(i);
            }
        }

        // Try to make the filename unique
        String path = parentdir + filename + extension;
        File file = new File(path);

        if (file.exists()) {
            file.delete();
        }

        return path;
    }


    public static Bitmap getBitmapFromUri(Context context,Uri selectedImage, int requiredSize){
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT < 19) {
            String selectedImagePath = getRealPathFromURI(context,selectedImage);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            bitmap = BitmapFactory.decodeFile(selectedImagePath, decodeFile(options, requiredSize));
        } else {
            ParcelFileDescriptor parcelFileDescriptor;
            try {
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(selectedImage, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, decodeFile(options, requiredSize));
                parcelFileDescriptor.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    private static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            result = cursor.getString(0);
            cursor.close();
        }
        return result;
    }


    private static BitmapFactory.Options decodeFile(BitmapFactory.Options options, int requiredSize) {
        // Find the correct scale value. It should be the power of 2.
        int width_tmp = options.outWidth, height_tmp = options.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp < requiredSize && height_tmp < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize

        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;

        return options;
    }
}
