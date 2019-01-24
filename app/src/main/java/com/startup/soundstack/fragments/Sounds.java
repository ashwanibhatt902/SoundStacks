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
import android.util.Log;
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
import com.squareup.otto.Subscribe;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.SearchResultsActivity;
import com.startup.soundstack.adapter.HomeAdapter;
import com.startup.soundstack.customclass.CustomRecycleView;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.MenuColorizer;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;

import java.util.List;


public class Sounds extends Fragment  {
    HomeAdapter mAdapter;
    CustomRecycleView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    CustomRecycleView.OnLastVisible loadMoreCallback;
    boolean refreshing;
    private String mQueryKey;
    private JSONArray mQueryvalue;
    private Boolean mEnableSwipeRefresh = true;
    private CustomText mInfoMsg;
    private View mProgressBar;
    private Context mContext;
    View mParent = null;

    public static Sounds newInstance(String param1, String param2) {
        Sounds fragment = new Sounds();
        return fragment;
    }

    public Sounds() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sounds, container, false);
        recyclerView = (CustomRecycleView) view.findViewById(R.id.sound_rv);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new HomeAdapter(getActivity());

        mParent = view.findViewById(R.id.parent);

        recyclerView.setAdapter(mAdapter);

        mInfoMsg = (CustomText) view.findViewById(R.id.info_msg);
        mProgressBar = view.findViewById(R.id.loading);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contentView);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_primary, R.color.color_accent);


        if (mEnableSwipeRefresh) {
           swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setReachedEnd(false);
                fetchSounds(false, true, Utility.isConnectedToInternet(getActivity()));
            }
        });
        }
        else {
            swipeRefreshLayout.setEnabled(false);
        }

        loadMoreCallback = new CustomRecycleView.OnLastVisible() {
            @Override
            public void loadMore() {
                Log.e("error","here i am");
                mAdapter.getSoundItemList().add(null);
                mAdapter.notifyItemInserted(mAdapter.getSoundItemList().size() - 1);
                fetchSounds(true, false, Utility.isConnectedToInternet(getActivity()));
            }
        };
        recyclerView.setCallbacks(loadMoreCallback);

        fetchSounds(false, false, Utility.isConnectedToInternet(getActivity()));
        return view;
    }

    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
        UpdateCenter.getEventBus().register(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        mAdapter.onDetach();
        mContext = null;
        UpdateCenter.getEventBus().unregister(this);
    }

    @Subscribe
    public void updateUI(UpdateCenter.UpdateEvent event) {
        if (mAdapter != null && !event.mSender.equals(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void updateUI(UpdateCenter.ReportEvent event) {
        if (event.mReport == null) {
            Snackbar.make(mParent, "Fail to submit report", Snackbar.LENGTH_SHORT).show();
        }
        else {
            Snackbar.make(mParent, "Report submitted successfully", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void updateUI(final UpdateCenter.CreateSoundEvent event) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mAdapter != null && event.mSound != null) {
            if (!event.mHandled) {
                try {
                    event.mSound.pin(Utility.Sound_tag);

                    mAdapter.getSoundItemList().clear();
                    fetchSounds(false, false, false);

                    ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
                    tagsParseQuery.fromLocalDatastore();
                    tagsParseQuery.getFirstInBackground(new GetCallback<Tags>() {
                        @Override
                        public void done(Tags object, ParseException e) {
                            if (object == null) return;
                            object.setSoundTags(Utility.jArrayToList(event.mSound.getTags(), null));
                            object.pinInBackground(Constants.PinningLabel.ALL_TAGS);
                        }
                    });
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                event.mHandled = true;
            }
            else {
                mAdapter.getSoundItemList().clear();
                fetchSounds(false, false, false);
            }
        }
        else {
        }
    }

    public void setQuery(String queryKey, JSONArray jArray) {
        mQueryKey = queryKey;
        mQueryvalue = jArray;
    }

    public void enableSwipeRefreshView(Boolean enable) {
        mEnableSwipeRefresh = enable;
    }


    public interface OnSoundFragmentInteractionListener {
        public void onSoundFragmentInteraction(SoundItem clickedSound);
    }

    private void fetchSounds(final boolean isLoadMore, final boolean isRefresh, final boolean isConnected) {

        mInfoMsg.setVisibility(View.GONE);
        ParseQuery<SoundItem> query = ParseQuery.getQuery(SoundItem.class);
        String col = Utility.getPreference(this.getActivity()).getString(Constants.Preference.SOUND_SORT, SoundItem.COLUMN_CREATED_AT);
        if(col == SoundItem.COLUMN_NAME){
            query.addAscendingOrder(col);
        }else{
            query.addDescendingOrder(col);
        }
        if(isLoadMore){
            query.setSkip(mAdapter.getSoundItemList().size());
        }

        if (mQueryKey != null) {

            if (mQueryvalue != null && mQueryvalue.length() > 0) {
                query.whereContainedIn(mQueryKey, Utility.jArrayToList(mQueryvalue, null));
            }
            else {
                mInfoMsg.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mAdapter.getSoundItemList().clear();
                mInfoMsg.setText((getActivity().getString(R.string.NO_Item_Prefix)+" "+ ((AppCompatActivity)getActivity()).getSupportActionBar().getTitle()));

                return;
            }
        }

        query.setLimit(30);
        if (!isConnected) {
            query.fromLocalDatastore();
        }
        query.findInBackground(new FindCallback<SoundItem>() {
            @Override
            public void done(final List<SoundItem> list, ParseException e) {

                if (mContext == null) return;

                if (e == null) {
                    if(isRefresh){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    if(!isLoadMore){
                        mAdapter.getSoundItemList().clear();
                    }
                    if(isLoadMore){
                        if(list.size() < 30 && list.size() > 0 && isConnected){
                            recyclerView.setReachedEnd(true);
                        }
                        else {
                            recyclerView.setLastVisibleItem(0);
                        }
                        recyclerView.setLoadMore(false);
                        int pos = mAdapter.getSoundItemList().size();
                        mAdapter.getSoundItemList().remove(pos - 1);
                        mAdapter.notifyItemRemoved(pos - 1);
                    }

                    mProgressBar.setVisibility(View.GONE);
                    mAdapter.getSoundItemList().addAll(list);
                    mAdapter.notifyDataSetChanged();
                    if (isConnected) {
                        // Release any objects previously pinned for this query.
                        ParseObject.unpinAllInBackground(Utility.Sound_tag, list, new DeleteCallback() {
                            public void done(ParseException e) {
                                if (e != null) {
                                    return;
                                }
                                ParseObject.pinAllInBackground(Utility.Sound_tag, mAdapter.getSoundItemList());
                            }
                        });
                    }
                    if(refreshing){
                        refreshing = false;
                        getActivity().invalidateOptionsMenu();
                    }

                    if (mAdapter.getSoundItemList().size() == 0) {
                        mInfoMsg.setVisibility(View.VISIBLE);
                        if (mQueryKey != null && mQueryKey.equals(SoundItem.COLUMN_CATEGORY_ID)) {
                            mInfoMsg.setText("No sound Available under " + ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());
                        }
                        else {
                            mInfoMsg.setText((getActivity().getString(R.string.NO_Item_Prefix)+" "+ ((AppCompatActivity)getActivity()).getSupportActionBar().getTitle()));
                        }
                    }
                } else {

                    e.printStackTrace();
                    if (e.getCode() == ParseException.CONNECTION_FAILED || e.getCode() == ParseException.TIMEOUT) {
                        fetchSounds(isLoadMore, isRefresh, false);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sound_frag, menu);

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
                Utility.setPreference(this.getActivity(), Constants.Preference.SOUND_SORT, SoundItem.COLUMN_LIKE_COUNT);
                getActivity().setProgressBarIndeterminateVisibility(true);
                fetchSounds(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;
            case R.id.upload_sort:
                refreshing = true;
                getActivity().invalidateOptionsMenu();
                recyclerView.setReachedEnd(false);
                Utility.setPreference(this.getActivity(), Constants.Preference.SOUND_SORT, SoundItem.COLUMN_CREATED_AT);
                fetchSounds(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;
            case R.id.alphabetical_sort:
                refreshing = true;
                getActivity().invalidateOptionsMenu();
                recyclerView.setReachedEnd(false);
                Utility.setPreference(this.getActivity(), Constants.Preference.SOUND_SORT, SoundItem.COLUMN_NAME);
                fetchSounds(false, false, Utility.isConnectedToInternet(getActivity()));
                return true;
            case R.id.search: {
                Intent intent = new Intent(getActivity(), SearchResultsActivity.class);
                intent.putExtra("type", SoundItem.class.toString());
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAdapter != null){
            mAdapter.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null){
            mAdapter.onResume();
        }
    }
}
