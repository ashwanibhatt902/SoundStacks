package com.startup.soundstack.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.customclass.CustomSpinner;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.fragments.ReportSoundDialog;
import com.startup.soundstack.fragments.Sounds;
import com.startup.soundstack.models.AdControl;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.AppRater;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.MenuColorizer;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Dheeraj on 7/22/2015.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.CustomViewHolder> {
    Context mContext;
    List<SoundItem> soundItemList;
    MediaPlayer mp;
    int soundPlayingId = -1;
    int expandLayoutPostion = -1;
    int LOAD_VIEW = 1;
    int NORMAL_VIEW = 2;
    private String activityName = "";
    Task<byte[]> downloadSoundtask = null;

    public HomeAdapter(Context context) {
        this.mContext = context;
        soundItemList = new ArrayList<SoundItem>();
//        soundItemList.add(null);
    }

    public List<SoundItem> getSoundItemList() {
        return soundItemList;
    }

    public void setSoundItemList(ArrayList<SoundItem> soundItemList) {
        this.soundItemList = soundItemList;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CustomViewHolder vh = null;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == LOAD_VIEW) {
            View view = inflater.inflate(R.layout.progresslayout, parent, false);
            vh = new PViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.sound_layout, parent, false);
            vh = new NormalViewHolder(view);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, final int position) {
        final SoundItem soundItem = soundItemList.get(position);
        if (holder.getItemViewType() == LOAD_VIEW) {
            final PViewHolder pViewHolder = (PViewHolder) holder;
        } else {
            final NormalViewHolder nViewHolder = (NormalViewHolder) holder;

            AdControl adControl = SoundStackApplication.getAdControlObject();
            if(adControl.isEnabled() && adControl.isSound_enabled() && position > 0 && position % adControl.getRow_count() == 0){
                showNativeAd(nViewHolder, soundItem);
            }else{
                nViewHolder.adView.setVisibility(View.GONE);
            }

            final String name = soundItem.getName();
            nViewHolder.soundTxt.setText(name);
            nViewHolder.playImg.setVisibility(View.VISIBLE);



            final File file = new File(mContext.getFilesDir(), "myfolder/" + soundItem.getObjectId() + soundItem.getExtension());

            if (position == soundPlayingId) {
                if (!file.exists()) {
                    nViewHolder.playImg.setVisibility(View.INVISIBLE);
                    nViewHolder.prgrss.setVisibility(View.VISIBLE);
                }else{
                    nViewHolder.prgrss.setVisibility(View.INVISIBLE);
                    nViewHolder.playImg.setImageResource(R.drawable.stop);
                }
            } else {
                if (!file.exists()) {
                    nViewHolder.playImg.setImageResource(R.drawable.download1);
                }
                else {
                    nViewHolder.playImg.setImageResource(R.drawable.play);
                }
                //same detail as mention below for like dislike
                nViewHolder.prgrss.setVisibility(View.INVISIBLE);
            }

            if (!file.exists()) {
                nViewHolder.share.setImageResource(R.drawable.share_dis);
                nViewHolder.share.setEnabled(false);
            } else {
                nViewHolder.share.setImageResource(R.drawable.share);
                nViewHolder.share.setEnabled(true);
            }

            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                JSONArray likeIds = currentUser.getJSONArray(Constants.UserProperty.LIKE_SOUND_ID);
                JSONArray dislikeIds = currentUser.getJSONArray(Constants.UserProperty.DISLIKE_SOUND_ID);
                JSONArray favSoundIds = currentUser.getJSONArray(Constants.UserProperty.FAV_SOUND_ID);

                if (Utility.contain(soundItem.getObjectId(), likeIds)) {
                    nViewHolder.likeIcon.setImageResource(R.drawable.ic_thumb_up_black_18dp);
                    nViewHolder.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                }
                else if(Utility.contain(soundItem.getObjectId(), dislikeIds)) {
                    nViewHolder.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_black_18dp);
                    nViewHolder.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                }
                //Should not add this but somehow without this like dislike icon set according to previous expansion
                //i.e if previous like icon is dark but current is grey then current will also set grey without this else
                // some basic mistake is there
                else {
                    nViewHolder.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                    nViewHolder.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                }

                if (Utility.contain(soundItem.getObjectId(), favSoundIds)) {
                    nViewHolder.favIcon.setImageResource(R.drawable.ic_favorite_black_18dp);
                    nViewHolder.favIcon.setTag(R.drawable.ic_favorite_black_18dp);
                }

                else {
                    nViewHolder.favIcon.setImageResource(R.drawable.ic_favorite_outline_black_18dp);
                    nViewHolder.favIcon.setTag(R.drawable.ic_favorite_outline_black_18dp);
                }
            }
            else {
                nViewHolder.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                nViewHolder.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                nViewHolder.favIcon.setImageResource(R.drawable.ic_favorite_outline_black_18dp);
                nViewHolder.favIcon.setTag(R.drawable.ic_favorite_outline_black_18dp);
            }

            nViewHolder.dislikeParent.setOnClickListener(mLLExpand);
            nViewHolder.likeParent.setOnClickListener(mLLExpand);
            nViewHolder.favIcon.setOnClickListener(mLLExpand);
//            nViewHolder.report.setOnClickListener(mLLExpand);
            nViewHolder.disLikeCount.setText(Integer.toString(soundItem.getDisLikeCount()));
            nViewHolder.likeCount.setText(Integer.toString(soundItem.getLikeCount()));
            nViewHolder.byText.setText(soundItem.getByText());
            nViewHolder.llExpandArea.setTag(R.id.likeCount, new Integer(position));
            nViewHolder.llExpandArea.setTag(nViewHolder);

            nViewHolder.clickedParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext instanceof Sounds.OnSoundFragmentInteractionListener) {

                        if (!file.exists()) {
                            return;
                        }
                        Sounds.OnSoundFragmentInteractionListener listener = (Sounds.OnSoundFragmentInteractionListener) (mContext);
                        listener.onSoundFragmentInteraction(soundItem);
                    }
                }
            });



            nViewHolder.clickedParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


//                    new MenuInflater(mContext).inflate(R.menu.sound_long_lclick, menu);
//
//                    MenuColorizer.colorMenu((Activity) mContext, menu, mContext.getResources().getColor(R.color.color_accent), -1, false);

                    BottomSheet.Builder builder = new BottomSheet.Builder((Activity) mContext).
//                            icon(getRoundedBitmap()).
                            title(soundItem.getName()).sheet(R.menu.sound_long_lclick);

                    //kind of hack TODO remove in future version
                    Field field = null;
                    try {
                        field = BottomSheet.Builder.class.getDeclaredField("menu");
                        field.setAccessible(true);
                        Menu value = (Menu)field.get(builder);
                        MenuColorizer.colorMenu((Activity) mContext, value, mContext.getResources().getColor(R.color.color_accent), -1, false);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }



                    builder.listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            String pdMessage = new String("Setting Ringtone/Notification...");
                            switch (which) {
                                case R.id.share: {
                                    share(soundItem);
                                    break;
                                }
                                case R.id.ringtone:
                                    pdMessage = "Ringtone...";
                                    setRingtoneAsync(which, file, soundItem, pdMessage, position);
                                    break;
                                case R.id.notification:
                                    pdMessage = "Notification...";
                                    setRingtoneAsync(which, file, soundItem, pdMessage, position);
                                    break;
                                case R.id.alarm:
                                    pdMessage = "Alarm...";
                                    setRingtoneAsync(which, file, soundItem, pdMessage, position);
                                    break;

                                case R.id.report: {
                                    reportSound(soundItem);
                                    break;
                                        }
                                case R.id.add: {
//                                    soundItem.addUnique(SoundItem.COLUMN_CATEGORY_ID, "JW4CCJcQ3O");
//                                    soundItem.saveInBackground();
                                    addSound(soundItem);
                                    break;
                                        }
                                    }
                                }
                            });
                    builder.show();
//                    getRoundedBitmap(builder);
//                    builder.icon(getRoundedBitmap());
                    return true;
                }
            });

            nViewHolder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   share(soundItem);
                }
            });

            nViewHolder.playRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tracker tracker = ((SoundStackApplication)((Activity)mContext).getApplication()).getTracker();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Sound Played")
                            .setAction(String.format("id : %s",soundPlayingId))
                            .build());
                    if (soundPlayingId != -1) {
                        notifyItemChanged(soundPlayingId);
                        if (mp != null) {
                            mp.reset();
                            mp.release();
                            mp = null;
                        }

                        if (soundPlayingId == position ) {
                            if (downloadSoundtask != null) {
                                soundItem.getSoundFile().cancel();
                                downloadSoundtask = null;
                            }

                            soundPlayingId = -1;
                            return;
                        }

                        if (downloadSoundtask != null) {

                            SoundItem previousSoundItem = soundItemList.get(soundPlayingId);
                            previousSoundItem.getSoundFile().cancel();
                            downloadSoundtask = null;

                        }
                    }
                    soundPlayingId = position;
                    File file = new File(mContext.getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME + "/" + soundItem.getObjectId() + soundItem.getExtension());

                    if (file.exists()) {
                        nViewHolder.playImg.setImageResource(R.drawable.stop);
                        playSound(file.getPath());
                    } else {
                        if (!Utility.isConnectedToInternet(mContext)) {
                            showSnackbar(R.string.No_Internet_msg);
                            soundPlayingId = -1;
                            return;
                        }
                        nViewHolder.playImg.setVisibility(View.GONE);
                        nViewHolder.prgrss.setVisibility(View.VISIBLE);


                        downloadSoundtask =  soundItem.getSoundFile().getDataInBackground();

                        downloadSoundtask.continueWith(new Continuation<byte[], Object>() {
                            @Override
                            public Object then(Task<byte[]> task) throws Exception {

                                if (task.isCancelled()) {
                                    return  null;
                                }

                                if (task.isFaulted()) {
                                    showSnackbar(R.string.Download_Fail);

                                }
                               else if (task.isCompleted()) {

                                    try {
                                        File path = new File(mContext.getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
                                        if (!path.exists()) {
                                            path.mkdir();
                                        }
                                        File file = new File(path, String.format("%s"+ soundItem.getExtension(), soundItem.getObjectId()));
                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                        bos.write(task.getResult());
                                        bos.flush();
                                        bos.close();
                                        nViewHolder.playImg.setImageResource(R.drawable.stop);
                                        playSound(file.getPath());
                                        UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                    } catch (IOException exp) {
                                        exp.printStackTrace();
                                    }
                                } else {
                                    showSnackbar(R.string.Download_Fail);
                                }
                                nViewHolder.prgrss.setVisibility(View.GONE);
                                nViewHolder.playImg.setVisibility(View.VISIBLE);

                                downloadSoundtask = null;
                                return null;
                            }
                        },  Task.UI_THREAD_EXECUTOR);


//                        soundItem.getSoundFile().getDataInBackground(new GetDataCallback() {
//                            @Override
//                            public void done(byte[] bytes, ParseException e) {
//                                if (e == null) {
//                                }
//                            }
//                        });
                    }

                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return soundItemList.size();
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public static class NormalViewHolder extends CustomViewHolder {
        private final View llExpandArea;
        public TextView soundTxt;
        public View prgrss;
        public ImageView playImg;
        public RelativeLayout playRelative;
        public ImageView share;
        public ImageView likeIcon;
        public ImageView disLikeIcon;
        public TextView likeCount;
        public TextView disLikeCount;
        public ImageView favIcon;
        public TextView byText;
        public View likeParent;
        public View dislikeParent;
//        public View report;
        public View clickedParent;
        public RelativeLayout adView;

        public NormalViewHolder(View v) {
            super(v);
            adView = (RelativeLayout) itemView.findViewById(R.id.admain);
            soundTxt = (TextView) v.findViewById(R.id.soundname);
            prgrss =  v.findViewById(R.id.prgrss);
            playImg = (ImageView) v.findViewById(R.id.iconImage);
            playRelative = (RelativeLayout) v.findViewById(R.id.playParent);
            share = (ImageView) v.findViewById(R.id.share);
            llExpandArea = v.findViewById(R.id.llExpandArea);
            likeCount = (TextView) v.findViewById(R.id.likeCount);
            disLikeCount = (TextView) v.findViewById(R.id.dislikeCount);
            likeIcon = (ImageView) v.findViewById(R.id.likeIcon);
            disLikeIcon = (ImageView) v.findViewById(R.id.dislikeIcon);
            favIcon = (ImageView) v.findViewById(R.id.favIcon);
            byText  = (TextView) v.findViewById(R.id.by);
            likeParent = v.findViewById(R.id.likeParent);
            dislikeParent = v.findViewById(R.id.dislikeParent);
//            report = v.findViewById(R.id.report);
            clickedParent = v.findViewById((R.id.main));
        }
    }


    public static class PViewHolder extends CustomViewHolder {
        public PViewHolder(View v) {
            super(v);
        }
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        public CustomViewHolder(View v) {
            super(v);
        }
    }

    private void playSound(String name) {
        if (mp != null) {
            mp.reset();
            mp.release();
        }
        try {
            mp = new MediaPlayer();
            File file = new File(name);
            mp.setDataSource(name);
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp1) {
                    mp.reset();
                    mp.release();
                    notifyItemChanged(soundPlayingId);
                    soundPlayingId = -1;
                    mp = null;
                    if(AppRater.shouldDisplayDialog(mContext)){
                        AppRater.showRateDialog(mContext);
                    }
                }
            });
            mp.prepare();
            mp.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    View.OnClickListener mLLExpand;

    {
        mLLExpand = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ParseUser user = ParseUser.getCurrentUser();
                if (user == null) {
//                    Utility.openLoginActivity(mContext);
                    Snackbar.make(((Activity)mContext).findViewById(R.id.main), R.string.login_request, Snackbar.LENGTH_LONG)
                            .setAction("Login", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Utility.openLoginActivity(mContext);
                                }
                            }).show();
                    return;
                }else{
                    if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && !ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                        ParseUser.getCurrentUser().fetchInBackground();
                        Snackbar.make(((Activity)mContext).findViewById(R.id.main), R.string.verify_mail_request, Snackbar.LENGTH_LONG)
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

                if (!Utility.isConnectedToInternet(mContext)) {
                    showSnackbar(R.string.No_Internet_msg);
                    return;
                }

                ViewParent parent = v.getParent();
                if (parent instanceof ViewGroup) {
                    LinearLayout ll = (LinearLayout) parent;
                    final NormalViewHolder vh = (NormalViewHolder) ll.getTag();
                    final int position = (int) ll.getTag(R.id.likeCount);
                    final SoundItem item = soundItemList.get(position);

                    if (item == null) return;

                    if (v.getId() == R.id.report) {
                       reportSound(item);
                    }
                    final JSONArray dislikeIds = user.getJSONArray("dislikeSoundIds");
                    final JSONArray likeIds = user.getJSONArray("likeSoundIds");

                    if (v.getId() == R.id.likeParent) {
                        vh.dislikeParent.setOnClickListener(null);
                        vh.likeParent.setOnClickListener(null);
                        if (!Utility.contain(item.getObjectId(), likeIds)) {

                            vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_black_18dp);
                            Utility.applyCircularRevealAnimation(vh.likeIcon, false);
                            vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
    //                        Utility.applyCircularRevealAnimation(vh.disLikeIcon, false);


                            user.addAllUnique(Constants.UserProperty.LIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            item.incrementLikeCount();

                            if (Utility.contain(item.getObjectId(), dislikeIds)) {
                                item.decrementDisLikeCount();
                                user.removeAll(Constants.UserProperty.DISLIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            }

                            vh.likeCount.setText(Integer.toString(item.getLikeCount()));
                            vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));

                            ParseObject.saveAllInBackground(Arrays.asList(item, user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Utility.revertAll(Arrays.asList(item, user));
                                        vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                                        vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                                        if (Utility.contain(item.getObjectId(), dislikeIds)) {
                                            vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_black_18dp);
                                        }
                                    }

                                    vh.likeCount.setText(Integer.toString(item.getLikeCount()));
                                    vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                    vh.likeParent.setOnClickListener(mLLExpand);
                                    vh.dislikeParent.setOnClickListener(mLLExpand);
                                }
                            });
                        }


                        else {
                            vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                            Utility.applyCircularRevealAnimation(vh.likeIcon, false);

                            user.removeAll(Constants.UserProperty.LIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            item.decrementLikeCount();

                            vh.likeCount.setText(Integer.toString(item.getLikeCount()));

                            ParseObject.saveAllInBackground(Arrays.asList(item, user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Utility.revertAll(Arrays.asList(item, user));
                                        vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_black_18dp);
                                    }

                                    vh.likeCount.setText(Integer.toString(item.getLikeCount()));
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                    vh.dislikeParent.setOnClickListener(mLLExpand);
                                    vh.likeParent.setOnClickListener(mLLExpand);
                                }
                            });

                        }
                    } else if (v.getId() == R.id.dislikeParent ) {


                        vh.dislikeParent.setOnClickListener(null);
                        vh.likeParent.setOnClickListener(null);

                        if (!Utility.contain(item.getObjectId(), dislikeIds)) {


                            vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_black_18dp);
                            Utility.applyCircularRevealAnimation(vh.disLikeIcon, false);
                            vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
//                        Utility.applyCircularRevealAnimation(vh.likeIcon, false);
                            vh.likeParent.setOnClickListener(null);

                            user.addAllUnique(Constants.UserProperty.DISLIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            item.incrementDisLikeCount();

                            if (Utility.contain(item.getObjectId(), likeIds)) {
                                item.decrementLikeCount();
                                user.removeAll(Constants.UserProperty.LIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            }

                            vh.likeCount.setText(Integer.toString(item.getLikeCount()));
                            vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));

                            ParseObject.saveAllInBackground(Arrays.asList(item, user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {

                                        Utility.revertAll(Arrays.asList(item, user));
                                        vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_grey600_18dp);
                                        vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                                        if (Utility.contain(item.getObjectId(), likeIds)) {
                                            vh.likeIcon.setImageResource(R.drawable.ic_thumb_up_black_18dp);
                                        }
//                                if (getActivityName().equals(mContext.getString(R.string.activity_Like_sounds))) {
//                                    getSoundItemList().remove(position);
//                                    notifyItemRemoved(position);
//                                    notifyItemRangeChanged(1, getSoundItemList().size());
//                                }

                                    }
                                    vh.likeCount.setText(Integer.toString(item.getLikeCount()));
                                    vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));
                                    vh.likeParent.setOnClickListener(mLLExpand);
                                    vh.dislikeParent.setOnClickListener(mLLExpand);
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                }
                            });
                        }

                        else {
                            vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_grey600_18dp);
                            Utility.applyCircularRevealAnimation(vh.disLikeIcon, false);

                            user.removeAll(Constants.UserProperty.DISLIKE_SOUND_ID, Arrays.asList(item.getObjectId()));
                            item.decrementDisLikeCount();

                            vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));

                            ParseObject.saveAllInBackground(Arrays.asList(item, user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Utility.revertAll(Arrays.asList(item, user));
                                        vh.disLikeIcon.setImageResource(R.drawable.ic_thumb_down_black_18dp);
                                    }

                                    vh.disLikeCount.setText(Integer.toString(item.getDisLikeCount()));
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                    vh.dislikeParent.setOnClickListener(mLLExpand);
                                    vh.likeParent.setOnClickListener(mLLExpand);
                                }
                            });
                        }

                    } else if (v.getId() == R.id.favIcon) {
                        Object tag = vh.favIcon.getTag();
                        vh.favIcon.setOnClickListener(null);
                        if (tag == null || (int) tag == R.drawable.ic_favorite_outline_black_18dp) {
                            vh.favIcon.setImageResource(R.drawable.ic_favorite_black_18dp);
                            Utility.applyCircularRevealAnimation(vh.favIcon, false);
                            vh.favIcon.setTag(R.drawable.ic_favorite_black_18dp);

                            user.addAllUnique(Constants.UserProperty.FAV_SOUND_ID, Arrays.asList(item.getObjectId()));

                            ParseObject.saveAllInBackground(Arrays.asList(user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
//                                    handleError(e, Arrays.asList((ParseObject) user));
                                    if (e!=null) {
                                        Utility.revertAll(Arrays.asList((ParseObject)user));
                                        vh.favIcon.setImageResource(R.drawable.ic_favorite_outline_black_18dp);
                                        vh.favIcon.setTag(R.drawable.ic_favorite_outline_black_18dp);
                                    }
                                    vh.favIcon.setOnClickListener(mLLExpand);
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                }
                            });
                        } else {
                            vh.favIcon.setImageResource(R.drawable.ic_favorite_outline_black_18dp);
                            Utility.applyCircularRevealAnimation(vh.favIcon, false);
                            vh.favIcon.setTag(R.drawable.ic_favorite_outline_black_18dp);

                            user.removeAll(Constants.UserProperty.FAV_SOUND_ID, Arrays.asList(item.getObjectId()));

                            ParseObject.saveAllInBackground(Arrays.asList(user), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
//                                    if (e == null || handleError(e, Arrays.asList((ParseObject) user))) {
////                                    if (getActivityName().equals(mContext.getString(R.string.activity_fav_sounds))) {
////                                        getSoundItemList().remove(position);
////                                        notifyItemRemoved(position);
////                                        notifyItemRangeChanged(1, getSoundItemList().size());
////                                    }
//
//                                    }
                                    if (e != null) {
                                        Utility.revertAll(Arrays.asList((ParseObject)user));
                                        vh.favIcon.setImageResource(R.drawable.ic_favorite_black_18dp);
                                        vh.favIcon.setTag(R.drawable.ic_favorite_black_18dp);
                                    }
                                    UpdateCenter.postUpdateEvent(HomeAdapter.this, UpdateCenter.EventType.LikeDislike);
                                    vh.favIcon.setOnClickListener(mLLExpand);
                                }
                            });
                        }
                    }
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;
        if (soundItemList.get(position) == null) {
            viewType = LOAD_VIEW;
        } else {
            viewType = NORMAL_VIEW;
        }
        return viewType;
    }


    private boolean handleError(ParseException e, List<ParseObject> parseObjects) {
        boolean calledSaveEventually = false;

        if (e == null) return calledSaveEventually;

        int errCode = e.getCode();
        switch (errCode) {
            case ParseException.CONNECTION_FAILED :
            case ParseException.TIMEOUT: {
                Utility.saveEventuallyAll(parseObjects);
                calledSaveEventually = true;
                break;
            }
            case ParseException.INVALID_SESSION_TOKEN:
            case ParseException.INVALID_LINKED_SESSION: {
                ParseUser.getCurrentUser().logOut();
                Utility.openLoginActivity(mContext);
                break;
            }
            case ParseException.OTHER_CAUSE: {
                //if we are saving multiple object in background and exception occur then we need to handle it like this
                //so check parseObjects.size() > 1 may be useful but i am not sure
               Throwable throwable  =  e.getCause();
                if (throwable != null &&  throwable.getCause() instanceof ParseException )  {
                    calledSaveEventually = handleError(((ParseException) throwable.getCause()), parseObjects);
                }
            }

        }
        return calledSaveEventually;
    }

    private void showSnackbar(int resID) {
        if (mContext != null) {
            Snackbar.make(((Activity) mContext).findViewById(R.id.main), resID, Snackbar.LENGTH_LONG).show();
        }
    }
    private void showSnackbar(String resID) {
        if (mContext != null) {
            Snackbar.make(((Activity) mContext).findViewById(R.id.main), resID, Snackbar.LENGTH_LONG).show();
        }
    }

    public void onPause(){
        if(mp != null){
            mp.pause();
        }
    }

    public void onResume(){
        if(mp != null){
            mp.start();
        }
    }

    public void onDetach(){

        mContext = null;
        if (downloadSoundtask != null && soundPlayingId != 1) {
            final SoundItem soundItem = soundItemList.get(soundPlayingId);
            soundItem.getSoundFile().cancel();
            downloadSoundtask = null;
        }

        if (mp != null) {
            mp.reset();
            mp.release();
            mp = null;
        }
        soundPlayingId = -1;
    }

    private void share(SoundItem soundItem) {
        Tracker tracker = ((SoundStackApplication)((Activity)mContext).getApplication()).getTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Sound Share")
                .setAction(String.format("id : %s", soundItem.getObjectId()))
                .build());

        File imagePath = new File(mContext.getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
        File newFile = new File(imagePath, soundItem.getObjectId() + soundItem.getExtension());
        Uri contentUri = FileProvider.getUriForFile(mContext, "com.soundstack.soundfileprovider", newFile);
        Intent theIntent = new Intent(Intent.ACTION_SEND);
        theIntent.setType("audio/*");
        theIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        Intent chooser = Intent.createChooser(theIntent, "Share Sound..");

        if (theIntent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(chooser);
        }
    }

    private boolean setRingtone(int menuID, SoundItem item, File file) {

        boolean pass = true;
        String  title = item.getName(),extension = item.getExtension();
        boolean notification = false, alarm = false, ringtone = false;

        String subdir = "media/audio/" ;
        String externalRootDir = Environment.getExternalStorageDirectory().getPath();
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/";
        }
        switch(menuID) {
            case R.id.alarm:
                subdir = "media/audio/alarms/";
                alarm = true;
                break;
            case R.id.notification:
                subdir = "media/audio/notifications/";
                notification = true;
                break;
            case R.id.ringtone:
                subdir = "media/audio/ringtones/";
                ringtone = true;
                break;
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
        String path = null;
        for (int i = 0; i < 100; i++) {
            String testPath;
            if (i > 0)
                testPath = parentdir + filename + i + extension;
            else
                testPath = parentdir + filename + extension;

            try {
                RandomAccessFile f = new RandomAccessFile(new File(testPath), "r");
                f.close();
            } catch (Exception e) {
                // Good, the file didn't exist
                path = testPath;
                break;
            }
        }

//        return path;
        if (copyFile(file, path)) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            values.put(MediaStore.MediaColumns.TITLE, title);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
            values.put(MediaStore.Audio.Media.IS_RINGTONE, ringtone);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, notification);
            values.put(MediaStore.Audio.Media.IS_ALARM, alarm);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);

            mContext.getApplicationContext().getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + path + "\"", null);

            Uri newUri = mContext.getApplicationContext().getContentResolver().insert(uri, values);

            if (ringtone) {
                RingtoneManager.setActualDefaultRingtoneUri(mContext.getApplicationContext(),
                        RingtoneManager.TYPE_RINGTONE, newUri);
            }
            else if (notification){
                RingtoneManager.setActualDefaultRingtoneUri(mContext.getApplicationContext(),
                        RingtoneManager.TYPE_NOTIFICATION, newUri);
            }

            else if (alarm) {
                RingtoneManager.setActualDefaultRingtoneUri(mContext.getApplicationContext(),
                        RingtoneManager.TYPE_ALARM, newUri);
            }
        }

        else pass = false;

        return  pass;
    }

    private void reportSound(SoundItem item) {
        if (ParseUser.getCurrentUser() == null) {
            Snackbar.make(((Activity)mContext).findViewById(R.id.main), R.string.login_request, Snackbar.LENGTH_LONG)
                    .setAction("Login", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utility.openLoginActivity(mContext);
                        }
                    }).show();
            return;
        }else{
            if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && !ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                ParseUser.getCurrentUser().fetchInBackground();
                Snackbar.make(((Activity)mContext).findViewById(R.id.main), R.string.verify_mail_request, Snackbar.LENGTH_LONG)
                        .setAction("Resend", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ParseUser user = ParseUser.getCurrentUser();
                                user.setEmail(user.getEmail());
                                user.saveInBackground();
                            }
                        }).show();
                return;
            }
        }
        ReportSoundDialog reportSoundDialog = new ReportSoundDialog();
        Bundle bundle = new Bundle();
        bundle.putString("soundID", item.getObjectId());
        reportSoundDialog.setArguments(bundle);
        reportSoundDialog.show(((FragmentActivity)mContext).getSupportFragmentManager(), "cat");
    }

    private boolean copyFile (File fileToCopy, String outputPath) {
        boolean pass = true;
        try {
            //create output directory if it doesn't exist
//            File dir = new File (outputPath);
//            if (!dir.exists())
//            {
//                dir.mkdirs();
//            }


            FileInputStream in = new FileInputStream(fileToCopy);
            FileOutputStream out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
            pass =  false;
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
            pass = false;
        }

        return  pass;
    }

    private void addSound(final SoundItem soundItem) {

        if (ParseUser.getCurrentUser() == null) {
            showSnackbar(R.string.login_request);
            return;
        }else{
            if (!ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && !ParseUser.getCurrentUser().getBoolean("emailVerified")) {
                ParseUser.getCurrentUser().fetchInBackground();
                Snackbar.make(((Activity)mContext).findViewById(R.id.main), R.string.verify_mail_request, Snackbar.LENGTH_LONG)
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
        // Get the layout inflater
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_add_sound, null);

        final CustomSpinner spinner = (CustomSpinner)dialogLayout.findViewById(R.id.category_spinner);

        final CustomSpinnerAdapter SpinnerAdapter = new CustomSpinnerAdapter(mContext, R.layout.spinner_row, R.id.txtname);
        SpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(SpinnerAdapter);
        spinner.setSelection(0, false);


        final String Cat_tag = "categories";
        final boolean[] stackAvailable = {true};
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
        query.whereEqualTo(SoundItem.COLUMN_UPLOADED_BY_USER, ParseUser.getCurrentUser().getObjectId());
        query.whereNotContainedIn("objectId", soundItem.getCategory());
        query.fromLocalDatastore();
        query.addAscendingOrder("name");
        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(final List<Category> list, ParseException e) {
                if (mContext == null) return;

                if (e == null) {

                    if (list.size() == 0) {
                        stackAvailable[0] = false;
                        Category category = new Category();
                        category.setName("No stack available");
                        list.add(category);
                    }
                    SpinnerAdapter.getCategories().addAll(list);
                    SpinnerAdapter.notifyDataSetChanged();
                    spinner.setSelection(0, false);

                } else {
                    stackAvailable[0] = false;
                }
            }
        });







        builder.setView(dialogLayout);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                dialog.dismiss();
                if (stackAvailable[0] == false) {
                    return;
                }
                int position = spinner.getSelectedItemPosition();
                if (position >= 0) {
                    final Category selectedCategory = SpinnerAdapter.getItem(position);
                    if (selectedCategory.getByText() == null) return;
                    soundItem.setCategory(selectedCategory);
                    //TODO need discussion on this
//                    ParseACL catACL = selectedCategory.getACL();
//                    if (catACL != null && !catACL.getPublicReadAccess()) {
//                        soundItem.setACL(catACL);
//                    }
                    soundItem.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                showSnackbar("Sound \""+soundItem.getName()+ "\" Successfully added into stack \""+selectedCategory.getName()+" \"");
                            }
                            else {
                                soundItem.revert();
                                showSnackbar("Fail to add into stack");
                            }
                        }
                    });
                }
            }
        });

        final AlertDialog customDialog = builder.create();

        customDialog.show();
    }

    private void  setRingtoneAsync(final int which, final File file, final SoundItem soundItem, final String pdMessage, final int position ) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogCustom);
        // Get the layout inflater
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_confirm_ringtone, null);

        CustomText message = (CustomText)dialogLayout.findViewById(R.id.messagge);

        message.setText("Set \"" + soundItem.getName() + "\" as " + pdMessage);

        builder.setView(dialogLayout);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which1) {
                dialog.dismiss();

                new AsyncTask<String, Void, Boolean>() {

                    ProgressDialog pd = new ProgressDialog(mContext);

                    @Override
                    protected void onPreExecute() {


                        pd.setTitle("Setting " + pdMessage);
                        pd.show();

                    }

                    @Override
                    protected Boolean doInBackground(String... params) {
                        if (!file.exists()) {
//                                                        if file not exist  then download it now instead of using same code i am writing it again
                            ParseFile soundFile = soundItem.getSoundFile();
                            if (soundFile != null) {
                                try {
                                    byte[] bytes = soundFile.getData();
                                    if (bytes != null && mContext != null) {

                                        File path = new File(mContext.getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
                                        if (!path.exists()) {
                                            path.mkdir();
                                        }
                                        File file = new File(path, String.format("%s" + soundItem.getExtension(), soundItem.getObjectId()));
                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                        bos.write(bytes);
                                        bos.flush();
                                        bos.close();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return setRingtone(which, soundItem, file);
                    }

                    @Override
                    protected void onPostExecute(Boolean pass) {
                        pd.dismiss();
                        if (!pass) {
                            showSnackbar(R.string.Fail_to_set_ringtone);
                        } else {
                            notifyItemChanged(position);
                            String gh = mContext.getString(R.string.success_to_set_ringtone) + " ";

                            showSnackbar(gh + pdMessage);
                        }
                    }
                }.execute(pdMessage);
            }
        });

        final AlertDialog customDialog = builder.create();

        customDialog.show();
    }

    private void showNativeAd(final NormalViewHolder viewH,final SoundItem soundItem) {
        final View view = viewH.adView;
        if (soundItem.getNativeAd() == null) {
            final NativeAd nativeAd = new NativeAd(mContext, mContext.getString(R.string.sounds_native_id));
            nativeAd.setAdListener(new AdListener() {

                @Override
                public void onError(Ad ad, AdError error) {
                    Log.e("error", error.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    view.setVisibility(View.VISIBLE);
                    soundItem.setNativeAd(nativeAd);

                    String titleForAd = nativeAd.getAdTitle();
                    NativeAd.Image coverImage = nativeAd.getAdCoverImage();
                    NativeAd.Image iconForAd = nativeAd.getAdIcon();
                    String socialContextForAd = nativeAd.getAdSocialContext();
                    String titleForAdButton = nativeAd.getAdCallToAction();
                    String textForAdBody = nativeAd.getAdBody();
                    NativeAd.Rating appRatingForAd = nativeAd.getAdStarRating();


                    TextView txtName = (TextView) view.findViewById(R.id.appName);
                    txtName.setText(titleForAd);

                    ImageView imgView = (ImageView) view.findViewById(R.id.adIcon);
                    ImageLoader.getInstance().displayImage(iconForAd.getUrl(), imgView);

                    if (titleForAd != null && !titleForAd.isEmpty()) {
                        ((CustomText) view.findViewById(R.id.ctaBttn)).setText(titleForAdButton);
                    }

                    if (appRatingForAd != null) {
                        ((CustomText) view.findViewById(R.id.star)).setText(String.valueOf(appRatingForAd.getValue()));
                    }

                    nativeAd.registerViewForInteraction(view);
                }

                @Override
                public void onAdClicked(Ad ad) {
                }
            });
            view.setVisibility(View.GONE);
            nativeAd.loadAd();

        } else {
            view.setVisibility(View.VISIBLE);
            soundItem.getNativeAd().unregisterView();

            String titleForAd = soundItem.getNativeAd().getAdTitle();
            NativeAd.Image coverImage = soundItem.getNativeAd().getAdCoverImage();
            NativeAd.Image iconForAd = soundItem.getNativeAd().getAdIcon();
            String socialContextForAd = soundItem.getNativeAd().getAdSocialContext();
            String titleForAdButton = soundItem.getNativeAd().getAdCallToAction();
            String textForAdBody = soundItem.getNativeAd().getAdBody();
            NativeAd.Rating appRatingForAd = soundItem.getNativeAd().getAdStarRating();


            TextView txtName = (TextView) view.findViewById(R.id.appName);
            txtName.setText(titleForAd);

            ImageView imgView = (ImageView) view.findViewById(R.id.adIcon);
            ImageLoader.getInstance().displayImage(iconForAd.getUrl(), imgView);

            if (titleForAd != null && !titleForAd.isEmpty()) {
                ((CustomText) view.findViewById(R.id.ctaBttn)).setText(titleForAdButton);
            }

            if (appRatingForAd != null) {
                ((CustomText) view.findViewById(R.id.star)).setText(String.valueOf(appRatingForAd.getValue()));
            }

            soundItem.getNativeAd().registerViewForInteraction(view);
        }
    }
//    private Drawable getRoundedBitmap(BottomSheet.Builder builder, SoundItem item) {
//        final Bitmap[] src = {null};
//
//        ParseQuery<ParseUser> query = ParseUser.getQuery();
//        query.getInBackground(item.getColumnUploadedByUser(), new GetCallback<ParseUser>() {
//            public void done(ParseUser object, ParseException e) {
//                object.setUsername("another_username");
//
//                // This will throw an exception, since the ParseUser is not authenticated
//                object.saveInBackground();
//            }
//        });
////        ParseUser currentUser = new ParseUser();
////        currentUser.setO
//        ParseFile profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE_LQ);
//        if (profileParseFile == null) {
//            profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE);
//        }
//
//        byte[] bytes = null;
//
//        if (profileParseFile != null) {
//            try {
//                bytes = profileParseFile.getData();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//
//        src[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        Bitmap dst;
//        if (src[0].getWidth() >= src[0].getHeight()) {
//            dst = Bitmap.createBitmap(src[0], src[0].getWidth() / 2 - src[0].getHeight() / 2, 0, src[0].getHeight(), src[0].getHeight());
//        } else {
//            dst = Bitmap.createBitmap(src[0], 0, src[0].getHeight() / 2 - src[0].getWidth() / 2, src[0].getWidth(), src[0].getWidth());
//        }
//        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), dst);
//        roundedBitmapDrawable.setCornerRadius(dst.getWidth() / 2);
//        roundedBitmapDrawable.setAntiAlias(true);
//
//        return roundedBitmapDrawable;
//    }
}
