package com.startup.soundstack.services;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.startup.soundstack.activities.SoundActivity;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import bolts.Continuation;
import bolts.Task;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SoundUploadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.startup.soundstack.action.FOO";
    private static final String ACTION_UPLOAD = "com.startup.soundstack.action.BAZ";

    // TODO: Rename parameters
    private static final String FILE_OUTPUT_PATH = "com.startup.soundstack.extra.FILE_OUTPUT_PATH";
    private static final String BYTES_TO_UPLOAD = "com.startup.soundstack.extra.BYTES_TO_UPLOAD";
    private static final String SOUNDITEM = "com.startup.soundstack.extra.SOUNDITEM";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, SoundUploadService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(FILE_OUTPUT_PATH, param1);
        intent.putExtra(BYTES_TO_UPLOAD, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionUpload(Context context, String param1, byte[] param2, SoundItem soundItem) {
        Intent intent = new Intent(context, SoundUploadService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(FILE_OUTPUT_PATH, param1);
        intent.putExtra(BYTES_TO_UPLOAD, param2);
        intent.putExtra(SOUNDITEM, soundItem);

        context.startService(intent);
    }

    public SoundUploadService() {
        super("SoundUploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(FILE_OUTPUT_PATH);
                final String param2 = intent.getStringExtra(BYTES_TO_UPLOAD);
                handleActionFoo(param1, param2);
            } else if (ACTION_UPLOAD.equals(action)) {
                final String param1 = intent.getStringExtra(FILE_OUTPUT_PATH);
                final byte[] param2 = intent.getByteArrayExtra(BYTES_TO_UPLOAD);



                SoundItem soundItem = (SoundItem)intent.getSerializableExtra(SOUNDITEM);

                handleActionUpload(param1, param2, soundItem);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(final String finalOutPath, final byte[] bytes, final SoundItem soundItem) {

        File file = new File(finalOutPath);
        final ParseFile parseFile = new ParseFile(file.getName(), bytes);

//        try {
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    soundItem.setSoundFile(parseFile);

                    try {
                        soundItem.save();
                        //TODO do it in proper way
                        try {
                            File path = new File(getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
                            if (!path.exists()) {
                                path.mkdir();
                            }

                            int dotIndex = finalOutPath.lastIndexOf('.');
                            String ext = ".ogg";
                            if (dotIndex > 0) {
                                ext = finalOutPath.substring(dotIndex);
                            }

                            File file = new File(path, String.format("%s" + ext, soundItem.getObjectId()));

                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

                            bos.write(bytes);
                            bos.flush();
                            bos.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        //TODO

                    } catch (ParseException e1) {
                        e1.printStackTrace();

                        Intent localIntent = new Intent(Constants.SOUND_UPLOAD_BROADCAST_ACTION)
                                .putExtra(Constants.SOUND_UPLOAD_STATUS, false);
                        LocalBroadcastManager.getInstance(SoundUploadService.this).sendBroadcast(localIntent);
                    }
                }
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer percentDone) {
                Intent localIntent = new Intent(Constants.SOUND_UPLOAD_BROADCAST_ACTION)
                // Puts the status into the Intent
                .putExtra(Constants.SOUND_UPLOAD_PROGRESS, percentDone);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(SoundUploadService.this).sendBroadcast(localIntent);
            }
        });
    }
}
