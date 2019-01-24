package com.startup.soundstack.activities;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.widget.ArrayAdapter;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.startup.soundstack.R;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class RecentSearch extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "com.startup.soundstack.recentsearch" ;
    public static final int MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;

    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            SearchManager.SUGGEST_COLUMN_FORMAT,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            BaseColumns._ID };

    public RecentSearch() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor recentCursor = super.query(uri, projection, selection, selectionArgs,
                sortOrder);
        final Cursor customResultsCursor = queryCache(recentCursor, selectionArgs[0]);
        Cursor[] cursors = new Cursor[] { recentCursor, customResultsCursor};
        return new MergeCursor(cursors);
//        return recentCursor;
    }


    private Cursor queryCache(Cursor recentsCursor, String userQuery) {
        final MatrixCursor arrayCursor = new MatrixCursor(recentsCursor.getColumnNames());

        final int formatColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_FORMAT);
        final int iconColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1);
        final int displayColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
        final int queryColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY);
        final int idIndex = recentsCursor.getColumnIndex("_id");

        final int columnCount = recentsCursor.getColumnCount();

//        ParseQuery<SoundItem> tagsParseQuery = new ParseQuery<SoundItem>(SoundItem.class);
//        tagsParseQuery.whereEqualTo(SoundItem.COLUMN_TAGS, userQuery);
//        tagsParseQuery.whereContains(SoundItem.COLUMN_NAME, userQuery);
//        tagsParseQuery.wher

        JSONArray jArray = null;
        if (HomeActivity.sCurrentTabPosition == 0) {
            jArray = HomeActivity.sAllTags.getCategoryTags();
        }
        else {
            jArray = HomeActivity.sAllTags.getSoundTags();
        }

        List<String> soundIds = new ArrayList<>();
        for (int i=0;i<jArray.length();i++){
            String string = jArray.optString(i).toLowerCase();
            if (string.contains(userQuery.toLowerCase())) {
                soundIds.add(string);
            }

        }

        //            List<SoundItem> customSearchResults  = tagsParseQuery.find();

        int startId = Integer.MAX_VALUE;

        for (String customSearchResult : soundIds) {
            final Object[] newRow = new Object[columnCount];
            if (formatColumnIndex >= 0) newRow[formatColumnIndex] = 0;
            if (iconColumnIndex >= 0) newRow[iconColumnIndex] = R.drawable.abc_ic_clear_mtrl_alpha;//R.drawable.invisible;
            if (displayColumnIndex >= 0) newRow[displayColumnIndex] = customSearchResult;
            if (queryColumnIndex >= 0) newRow[queryColumnIndex] = customSearchResult;
            newRow[idIndex] = startId--;
            arrayCursor.addRow(newRow);
        }
        //        tagsParseQuery.findInBackground(new FindCallback<SoundItem>() {
//            @Override
//            public void done(List<SoundItem> objects, ParseException e) {
//
//            }
//        });
//        tagsParseQuery.fromLocalDatastore();
//        tagsParseQuery.findInBackground(new GetCallback<Tags>() {
//            @Override
//            public void done(Tags object, ParseException e) {
//                if (object == null) return;
//                List<String> list = Utility.jArrayToList(object.getCategoryTags());
//                String[] arr = new String[list.size()];
//                list.toArray(arr);
//
//                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateCategoryDialog.this.getActivity(),
//                        android.R.layout.simple_dropdown_item_1line, arr);
//                mTags.setAdapter(adapter);
//            }
//        });

        // Populate data here


        return arrayCursor;
    }
}


