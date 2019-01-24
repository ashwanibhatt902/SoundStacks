package com.startup.soundstack.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.startup.soundstack.R;
import com.startup.soundstack.adapter.SoundsAdapter;
import com.startup.soundstack.soundfile.SoundFile;
import com.startup.soundstack.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;


public class SelectMusicActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

//    private SimpleCursorAdapter mAdapter;
    private boolean mShowAll;
    private Cursor mInternalCursor;
    private Cursor mExternalCursor;
    private RecyclerView recyclerView;

    // Result codes
    private static final int REQUEST_CODE_EDIT = 1;
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 2;

    SoundsAdapter mAdapter;

    public SelectMusicActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Inflate our UI from its XML layout description.
        setContentView(R.layout.activity_select_music);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Select Sound");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.select_music);

        mAdapter = new SoundsAdapter(this, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        try {

            mInternalCursor = null;
            mExternalCursor = null;
            getLoaderManager().initLoader(INTERNAL_CURSOR_ID,  null, this);
            getLoaderManager().initLoader(EXTERNAL_CURSOR_ID,  null, this);

        } catch (SecurityException e) {
            Log.e("Ringdroid", e.toString());
        } catch (IllegalArgumentException e) {
            Log.e("Ringdroid", e.toString());
        }

        Boolean bundle = getIntent().getBooleanExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD, true);
        mAdapter.showCategory = bundle;
    }

    private int getUriIndex(Cursor c) {
        int uriIndex;
        String[] columnNames = {
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString()
        };

        for (String columnName : Arrays.asList(columnNames)) {
            uriIndex = c.getColumnIndex(columnName);
            if (uriIndex >= 0) {
                return uriIndex;
            }
            // On some phones and/or Android versions, the column name includes the double quotes.
            uriIndex = c.getColumnIndex("\"" + columnName + "\"");
            if (uriIndex >= 0) {
                return uriIndex;
            }
        }
        return -1;
    }

    private Uri getUri(){
        //Get the uri of the item that is in the row
        Cursor c = mAdapter.getCursor();
        int uriIndex = getUriIndex(c);
        if (uriIndex == -1) {
            return null;
        }
        String itemUri = c.getString(uriIndex) + "/" +
                c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        return (Uri.parse(itemUri));
    }

    private void refreshListView() {
        mInternalCursor = null;
        mExternalCursor = null;
        Bundle args = new Bundle();
        getLoaderManager().restartLoader(INTERNAL_CURSOR_ID,  args, this);
        getLoaderManager().restartLoader(EXTERNAL_CURSOR_ID,  args, this);
    }

    private static final String[] INTERNAL_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\""
    };

    private static final String[] EXTERNAL_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\""
    };

    private static final int INTERNAL_CURSOR_ID = 0;
    private static final int EXTERNAL_CURSOR_ID = 1;

    /* Implementation of LoaderCallbacks.onCreateLoader */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        ArrayList<String> selectionArgsList = new ArrayList<String>();
        String selection;
        Uri baseUri;
        String[] projection;

        switch (id) {
            case INTERNAL_CURSOR_ID:
                baseUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
                projection = INTERNAL_COLUMNS;
                break;
            case EXTERNAL_CURSOR_ID:
                baseUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                projection = EXTERNAL_COLUMNS;
                break;
            default:
                return null;
        }

        if (mShowAll) {
            selection = "(_DATA LIKE ?)";
            selectionArgsList.add("%");
        } else {
            selection = "(";
            for (String extension : SoundFile.getSupportedExtensions()) {
                selectionArgsList.add("%." + extension);
                if (selection.length() > 1) {
                    selection += " OR ";
                }
                selection += "(_DATA LIKE ?)";
            }
            selection += ")";

            selection = "(" + selection + ") AND (_DATA NOT LIKE ?)";
            selectionArgsList.add("%espeak-data/scratch%");
        }

        String filter = args != null ? args.getString("filter") : null;
        if (filter != null && filter.length() > 0) {
            filter = "%" + filter + "%";
            selection =
                    "(" + selection + " AND " +
                            "((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))";
            selectionArgsList.add(filter);
            selectionArgsList.add(filter);
            selectionArgsList.add(filter);
        }

        String[] selectionArgs =
                selectionArgsList.toArray(new String[selectionArgsList.size()]);
        return new CursorLoader(
                this,
                baseUri,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
    }

    /* Implementation of LoaderCallbacks.onLoadFinished */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case INTERNAL_CURSOR_ID:
                mInternalCursor = data;
                break;
            case EXTERNAL_CURSOR_ID:
                mExternalCursor = data;
                break;
            default:
                return;
        }
        // TODO: should I use a mutex/synchronized block here?
        if (mInternalCursor != null && mExternalCursor != null) {
            Cursor mergeCursor = new MergeCursor(new Cursor[] {mInternalCursor, mExternalCursor});
            mAdapter.swapCursor(mergeCursor);
        }
    }

    /* Implementation of LoaderCallbacks.onLoaderReset */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
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
