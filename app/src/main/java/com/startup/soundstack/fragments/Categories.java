package com.startup.soundstack.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.SearchResultsActivity;
import com.startup.soundstack.adapter.CategroyAdapter;
import com.startup.soundstack.customclass.CustomRecycleView;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.MenuColorizer;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;

import java.util.List;


public class Categories extends Fragment {

    private CustomRecycleView recyclerView;
    private CategroyAdapter mAdapter;
    private View mProgressBar;
    private CustomText mInfoMsg;

    private String mQueryKey = null;
    private JSONArray mQueryvalue = null;

    final String Cat_tag = "categories";
    View mParent = null;
    private boolean refreshing = false;
    private CustomRecycleView.OnLastVisible loadMoreCallback;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Boolean mEnableSwipeRefresh = true;
    private Context mContext;
    View view;


    public static Categories newInstance(String param1, String param2) {
        Categories fragment = new Categories();
        return fragment;
    }

    public Categories() {
        // Required empty public constructor
    }

    public void setQuery(String key, JSONArray jsonArray) {
        mQueryKey = key;
        mQueryvalue = jsonArray;
    }

    public void enableSwipeRefreshView(Boolean enable) {
        mEnableSwipeRefresh = enable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UpdateCenter.getEventBus().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = (CustomRecycleView) view.findViewById(R.id.sound_rv);
        mParent = view.findViewById(R.id.parent);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CategroyAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        mProgressBar = view.findViewById(R.id.loading);
        mInfoMsg = (CustomText) view.findViewById(R.id.info_msg);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contentView);
        if (mEnableSwipeRefresh) {
            swipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.color_accent);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    recyclerView.setReachedEnd(false);
                    fetchCategories(false, true, Utility.isConnectedToInternet(getActivity()));
                }
            });
        }
        else {
            swipeRefreshLayout.setEnabled(false);
        }



        loadMoreCallback = new CustomRecycleView.OnLastVisible() {
            @Override
            public void loadMore() {
//                Log.e("error","here i am");
                mAdapter.getCategories().add(null);
                mAdapter.notifyItemInserted(mAdapter.getCategories().size() - 1);
                fetchCategories(true, false, Utility.isConnectedToInternet(getActivity()));
            }
        };
        recyclerView.setCallbacks(loadMoreCallback);

        fetchCategories(false, false, Utility.isConnectedToInternet(getActivity()));
        return view;

    }

    public void onButtonPressed(Uri uri) {

    }

    @Subscribe
    public void updateUI(UpdateCenter.UpdateEvent event) {
        if (mAdapter != null && !event.mSender.equals(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void updateUI(final UpdateCenter.CreateCategoryEvent event) {
        if (mAdapter != null && event.mCategory != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            if (!event.mHandled) {
                try {
                    event.mCategory.pin(Constants.PinningLabel.USER_CATEGORY);

                    mAdapter.getCategories().clear();
                    fetchCategories(false, false, false);

                    ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
                    tagsParseQuery.fromLocalDatastore();
                    tagsParseQuery.getFirstInBackground(new GetCallback<Tags>() {
                        @Override
                        public void done(Tags object, ParseException e) {
                            if (object == null) return;
                            object.setCategoryTags(Utility.jArrayToList(event.mCategory.getTags(), null));
                            object.pinInBackground(Constants.PinningLabel.ALL_TAGS);

                        }
                    });
                    event.mHandled = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            else {
                mAdapter.getCategories().clear();
                //fetch automatically refresh the reccle view
                fetchCategories(false, false, false);
            }
        }
        else {
            Snackbar.make(mParent, R.string.fail_creating_stack, Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
    }

    private void fetchCategories(final boolean isLoadMore, final boolean isRefresh, final boolean isConnected) {

        mInfoMsg.setVisibility(View.GONE);
//         = Utility.isConnectedToInternet(getActivity());
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);

        String col = Utility.getPreference(this.getActivity()).getString(Constants.Preference.CATEGORY_SORT, SoundItem.COLUMN_CREATED_AT);
        if(col == SoundItem.COLUMN_NAME){
            query.addAscendingOrder(col);
        }else{
            query.addDescendingOrder(col);
        }
        if (!isConnected) {
            query.fromLocalDatastore();
        }

        if(isLoadMore){
            query.setSkip(mAdapter.getCategories().size());
        }

        if (mQueryKey != null && (mQueryvalue == null || mQueryvalue.length() < 1)) {
            recyclerView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mInfoMsg.setVisibility(View.VISIBLE);
            mInfoMsg.setText((getActivity().getString(R.string.NO_Item_Prefix) + " " + ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle()));

            return;
        }
        else if (mQueryvalue != null){
            query.whereContainedIn(mQueryKey, Utility.jArrayToList(mQueryvalue, null));
        }
//        query.addAscendingOrder("name");
        query.include("image");
        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(final List<Category> list, ParseException e) {

                if (mContext == null) return;

                if (e == null) {

                    if(isRefresh){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if(!isLoadMore){
                        mAdapter.getCategories().clear();
                    }
                    if(isLoadMore){
                        if(list.size() < 30 && list.size() > 0 && isConnected){
                            recyclerView.setReachedEnd(true);
                        }
                        else {
                            recyclerView.setLastVisibleItem(0);
                        }
                        recyclerView.setLoadMore(false);
                        int pos = mAdapter.getCategories().size();
                        mAdapter.getCategories().remove(pos - 1);
                        mAdapter.notifyItemRemoved(pos - 1);
                    }
                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.getCategories().addAll(list);
                    mAdapter.notifyDataSetChanged();
                    if (isConnected) {
                        // Release any objects previously pinned for this query.
                        ParseObject.unpinAllInBackground(list);
                        ParseObject.unpinAllInBackground(Cat_tag, list, new DeleteCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    // There was some error.
                                    return;
                                }

                                // Add the latest results for this query to the cache.
                                ParseObject.pinAllInBackground(Cat_tag, list);
                            }
                        });
                    }
                    if(refreshing){
                        refreshing = false;
                        getActivity().invalidateOptionsMenu();
                    }

                    if (mAdapter.getCategories().size() == 0) {
                        mInfoMsg.setVisibility(View.VISIBLE);
                        mInfoMsg.setText("No Stack Available under " + ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());
                    }
                } else {
                    e.printStackTrace();
                    if (e.getCode() == ParseException.CONNECTION_FAILED || e.getCode() == ParseException.TIMEOUT) {
                        fetchCategories(isLoadMore, isRefresh, false);
                    }
                    else {
                        if(isRefresh){
                            swipeRefreshLayout.setRefreshing(false);
                            mProgressBar.setVisibility(View.GONE);
                            mInfoMsg.setVisibility(View.VISIBLE);
                            mInfoMsg.setText("Some error has occurred");
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

        //TODO cancel every backkground task here
        mContext = null;
        UpdateCenter.getEventBus().unregister(this);
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cat_frag, menu);

        MenuColorizer.colorMenu((Activity) mContext, menu, getResources().getColor(R.color.color_accent), -1, true);

        final MenuItem refreshItem = menu.findItem(R.id.action_filter);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.prgrs_action);
            } else {
                refreshItem.setActionView(null);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_add:
//                Toast.makeText(this.getActivity(), "add Clicked", Toast.LENGTH_LONG).show();
//                return true;
            case R.id.action_filter:

                return true;
            case R.id.rating_sort:
                refreshing = true;
                getActivity().invalidateOptionsMenu();
                recyclerView.setReachedEnd(false);
                Utility.setPreference(this.getActivity(), Constants.Preference.CATEGORY_SORT, SoundItem.COLUMN_LIKE_COUNT);
                getActivity().setProgressBarIndeterminateVisibility(true);
                fetchCategories(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;
            case R.id.upload_sort:
                refreshing = true;
                getActivity().invalidateOptionsMenu();
                recyclerView.setReachedEnd(false);
                Utility.setPreference(this.getActivity(), Constants.Preference.CATEGORY_SORT, SoundItem.COLUMN_CREATED_AT);
                fetchCategories(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;
            case R.id.alphabetical_sort:
                refreshing = true;
                getActivity().invalidateOptionsMenu();
                recyclerView.setReachedEnd(false);
                Utility.setPreference(this.getActivity(), Constants.Preference.CATEGORY_SORT, SoundItem.COLUMN_NAME);
                fetchCategories(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;

            case R.id.search: {
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                intent.putExtra("type", Categories.class.toString());
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
