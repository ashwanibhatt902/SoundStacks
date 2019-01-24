package com.startup.soundstack.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.startup.soundstack.utils.Constants;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class DeleteOldSoundFileService extends IntentService {
    public static final String ACTION_DELETE_OLD_SOUND_FILE = "com.startup.soundstack.services.action.DELETE_FILE";

    public DeleteOldSoundFileService() {
        super("DeleteOldSoundFileService");
    }

    public static void startActionClean(Context context) {
        Intent intent = new Intent(context, DeleteOldSoundFileService.class);
        intent.setAction(ACTION_DELETE_OLD_SOUND_FILE);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DELETE_OLD_SOUND_FILE.equals(action)) {
                handleActionDeleteFiles();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDeleteFiles() {

        File soundDir = new File(getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
        TreeMap<Long, File> modifiedSinceDayFilemap = new TreeMap <>();

        if (soundDir.exists()) {
            long size = 0;
                for (File file : soundDir.listFiles()) {
                    if (!file.isDirectory()) {
                        size = size+file.length();
                        long lastModifiedTime = file.lastModified();
                        modifiedSinceDayFilemap.put(lastModifiedTime, file);
                    }
                }

            deleteFiles(modifiedSinceDayFilemap, size);
        }

    }

    private void deleteFiles(TreeMap <Long, File> modifiedSinceDayFilemap, long currentSize ) {

        Long ref = new Long(currentSize);
        if (currentSize/(1024*1024) > Constants.MAX_SOUNDS_CACHE_SIZE)

            for(Map.Entry<Long, File> entry : modifiedSinceDayFilemap.entrySet()) {
                try {
                    long length = entry.getValue().length();
                    entry.getValue().delete();
                    ref = ref - length;
                    if (ref/(1024*1024) < Constants.MAX_SOUNDS_CACHE_SIZE ) {
                        break;
                    }
                }
                catch (Exception w) {
                }
            }

    }

}
