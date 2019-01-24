package com.startup.soundstack.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.startup.soundstack.R;
import com.startup.soundstack.adapter.CategroyAdapter;
import com.startup.soundstack.adapter.HomeAdapter;
import com.startup.soundstack.fragments.Categories;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harsh on 10/17/2015.
 */


public class SearchResultsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private View mProgressbar;
    private TextView mTextView = null;
    private int mType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextView = (TextView) findViewById(R.id.not_found);

        //Refresh all tags in memory
        ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
//        if (HomeActivity.sAllTags != null) {
            tagsParseQuery.fromLocalDatastore();
//        }
        tagsParseQuery.getFirstInBackground(new GetCallback<Tags>() {
            @Override
            public void done(Tags object, ParseException e) {
                if (object == null) return;
                else {
                    HomeActivity.sAllTags = object;
                }
            }
        });

        String type = getIntent().getStringExtra("type");
        if (type != null && type.equals(Categories.class.toString())) {
            mAdapter = new CategroyAdapter(this);
            mType = 1;
        }

        else {
            mAdapter = new HomeAdapter(this);
            ((HomeAdapter)mAdapter).getSoundItemList().clear();
            mType = 2;
        }
        recyclerView = (RecyclerView) findViewById(R.id.search_list);
        mProgressbar = findViewById(R.id.progressBar);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(mAdapter);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_results, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setSubmitButtonEnabled(true);
//        searchView.setSubmitButtonEnabled(true);

//TODO following code not working so set "app:showAsAction = always" for search menu
        searchView.onActionViewExpanded();
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.requestFocusFromTouch();


        return true;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            mTextView.setVisibility(View.GONE);
            mTextView.setText(getString(R.string.Not_Found));

            if (mType == 2) {
                ((HomeAdapter) mAdapter).getSoundItemList().clear();
            }
            else {
                ((CategroyAdapter) mAdapter).getCategories().clear();
            }

            mAdapter.notifyDataSetChanged();

            mProgressbar.setVisibility(View.VISIBLE);
            String queryString = intent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    RecentSearch.AUTHORITY, RecentSearch.MODE);
            suggestions.saveRecentQuery(queryString, null);

            queryString = queryString.toLowerCase();

            String[] queryWords = queryString.split(" ");


//            tagsParseQuery.con(SoundItem.COLUMN_TAGS, userQuery);

            List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();

            ParseQuery<ParseObject> tagsParseQuery = null;

            for (String name : queryWords) {
                if (mType == 2) {
                    tagsParseQuery = new ParseQuery(SoundItem.class);
                }
                else {
                    tagsParseQuery = new ParseQuery(Category.class);
                }
            tagsParseQuery.whereContains("tagsAsString", name);
            queries.add(tagsParseQuery);
        }

            final ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
//            ParseQuery<ParseObject> mainQuery = new ParseQuery(SoundItem.class);
//            mainQuery.whereContainedIn(SoundItem.COLUMN_TAGS, Arrays.asList(queryWords));

            if (!Utility.isConnectedToInternet(SearchResultsActivity.this)) {
                mainQuery.fromLocalDatastore();
            }

            mainQuery.setLimit(50);

            mainQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {
                        handleQueryComplete(objects);
                    }

                    else {
                        if (e.getCode() == ParseException.TIMEOUT || e.getCode() == ParseException.CONNECTION_FAILED) {
                            mainQuery.fromLocalDatastore();
                            mainQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    if (e == null) {
                                        handleQueryComplete(objects);
                                    }
                                }
                            });
                        }

                        else {
                            mTextView.setVisibility(View.VISIBLE);
                            mTextView.setText("Fail to search, try again");
                            mProgressbar.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    void handleQueryComplete(List<ParseObject> objects) {
        mProgressbar.setVisibility(View.GONE);

        View imageViewLayout = findViewById(R.id.search_image_layout);

        if (objects != null && objects.size() > 0) {
            final boolean b = mType == 2 ? ((HomeAdapter) mAdapter).getSoundItemList().addAll((List) objects) :
                    ((CategroyAdapter) mAdapter).getCategories().addAll((List) objects);
            mAdapter.notifyDataSetChanged();
            mTextView.setVisibility(View.GONE);
            imageViewLayout.setVisibility(View.GONE);

        } else {
            mTextView.setVisibility(View.VISIBLE);
            imageViewLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mType == 2) {
            ((HomeAdapter) mAdapter).onDetach();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mType == 2) {
            ((HomeAdapter) mAdapter).onPause();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mType == 2) {
            ((HomeAdapter) mAdapter).onResume();
        }
    }
}

