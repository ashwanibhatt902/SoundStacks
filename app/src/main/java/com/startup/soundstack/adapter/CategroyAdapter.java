package com.startup.soundstack.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.activities.HomeActivity;
import com.startup.soundstack.activities.SoundActivity;
import com.startup.soundstack.customclass.CustomText;
import com.startup.soundstack.models.AdControl;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dheeraj on 7/28/2015.
 */
public class CategroyAdapter extends RecyclerView.Adapter<HomeAdapter.CustomViewHolder> {
    private List<Category> categories;
    private Context mContext;
    private int LOAD_VIEW = 1;
    private int NORMAL_VIEW = 2;


    public CategroyAdapter(Context context){
        mContext = context;
        categories = new ArrayList<>();
    }

    public static class CategoryHV extends HomeAdapter.CustomViewHolder {
        public TextView catName;
        public LinearLayout main;
        public ImageView mfavIcon = null;
        public View mfavIconParent = null;
        public TextView mBytext = null;
        public ImageView mCategoryImage = null;
        public View categoryType = null;
        public LinearLayout adView;

        public CategoryHV(View itemView) {
            super(itemView);
            adView = (LinearLayout) itemView.findViewById(R.id.admain);
            catName = (TextView)itemView.findViewById(R.id.catName);
            mCategoryImage = (ImageView)itemView.findViewById(R.id.iconImage);
            main = (LinearLayout) itemView.findViewById(R.id.main);
            mfavIcon = (ImageView) itemView.findViewById(R.id.favicon);
            mfavIconParent =  itemView.findViewById(R.id.faviconparent);
            mBytext = (TextView)itemView.findViewById(R.id.by);
            categoryType = itemView.findViewById(R.id.category_type);
        }
    }


    @Override
    public HomeAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        HomeAdapter.CustomViewHolder vh = null;
        if (viewType == LOAD_VIEW) {
            View view = inflater.inflate(R.layout.progresslayout, parent, false);
            vh = new HomeAdapter.PViewHolder(view);
        }
        else {
            View view = inflater.inflate(R.layout.category_layout, parent, false);
            vh = new CategoryHV(view);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(final HomeAdapter.CustomViewHolder holder1, int position) {

        if (holder1.getItemViewType() == LOAD_VIEW) {
            final HomeAdapter.PViewHolder pViewHolder = (HomeAdapter.PViewHolder) holder1;
        }
        else {
            final Category cat = categories.get(position);

            final CategoryHV holder = (CategoryHV) holder1;

            AdControl adControl = SoundStackApplication.getAdControlObject();
            if(adControl.isEnabled() && adControl.isCategory_enabled() && position > 0 && position % adControl.getRow_count() == 0){
                showNativeAd(holder, cat);
            }else{
                holder.adView.setVisibility(View.GONE);
            }

            holder.catName.setText(cat.getName());
            holder.main.setOnClickListener(mLayout);
            holder.mfavIconParent.setOnClickListener(mLayout);
            holder.mfavIconParent.setTag(R.id.main, holder);
            holder.main.setTag(R.id.main, holder);
            holder.main.setTag(R.id.favicon, position);
            holder.mfavIconParent.setTag(R.id.favicon, position);

            String imgURL = cat.getImageURL();

            final ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(imgURL,
                    holder.mCategoryImage, HomeActivity.sDisplayImageOptions);

            int imageColor = cat.getImageColor();

            if (imageColor == -1) {
                holder.mCategoryImage.clearColorFilter();
            }
            else {
                holder.mCategoryImage.setColorFilter(imageColor);
            }

            ParseACL catACl = cat.getACL();
            if (catACl != null) {
                if (catACl.getPublicReadAccess()) {
                    holder.categoryType.setVisibility(View.GONE);
                } else {
                    holder.categoryType.setVisibility(View.VISIBLE);
                }
            }

            holder.mBytext.setText(cat.getByText());

            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                JSONArray favCategoriesIds = currentUser.getJSONArray(Constants.UserProperty.FAV_CATEGORIES_ID);

                if (Utility.contain(cat.getObjectId(), favCategoriesIds)) {
                    holder.mfavIcon.setImageResource(R.drawable.heart_color_36dp);
                    holder.mfavIcon.setTag(R.drawable.heart_color_36dp);

                } else {
                    holder.mfavIcon.setImageResource(R.drawable.heart_light);
                    holder.mfavIcon.setTag(R.drawable.heart_light);
                }

            } else {
                holder.mfavIcon.setImageResource(R.drawable.heart_light);
            }
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;
        if (categories.get(position) == null) {
            viewType = LOAD_VIEW;
        } else {
            viewType = NORMAL_VIEW;
        }
        return viewType;
    }


    View.OnClickListener mLayout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final CategoryHV holder = (CategoryHV)v.getTag(R.id.main);
            Category cat  = (Category) categories.get((int) v.getTag(R.id.favicon));
            final ParseUser curretUser = ParseUser.getCurrentUser();

            if (v.getId() == R.id.main) {
                Intent intent = new Intent(mContext, SoundActivity.class);
                JSONObject jsonObject = Utility.createJsonQuery(cat.getName(), SoundItem.COLUMN_CATEGORY_ID, new JSONArray(Arrays.asList(cat.getObjectId())), SoundItem.class.toString(),-1);
                try {
                    jsonObject.put("enableSwipe", true);
                    String uploadedByUserID = cat.getColumnUploadedByUser();

                    if (curretUser != null  && uploadedByUserID != null && uploadedByUserID.equals(curretUser.getObjectId())) {
                        jsonObject.put("showFAB", true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("obj", jsonObject.toString());
                mContext.startActivity(intent);
                Tracker tracker = ((SoundStackApplication)((Activity)mContext).getApplication()).getTracker();
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Category Opened")
                        .setAction(String.format("category : %s",cat.getName()))
                        .build());
                Answers.getInstance().logCustom(new CustomEvent("Category Opened").putCustomAttribute("Category", cat.getName()));

                ((Activity)mContext).overridePendingTransition(R.anim.righttoleft,
                        R.anim.lefttoright);
            }

            else if (v.getId() == R.id.faviconparent){

                if (!Utility.isConnectedToInternet(mContext)) {
                    Snackbar.make(((Activity) mContext).findViewById(R.id.main), R.string.No_Internet_msg, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (curretUser == null) {
                    Utility.openLoginActivity(mContext);
                    return;
                }
                if (((int)holder.mfavIcon.getTag()) == R.drawable.heart_light) {

                    holder.mfavIcon.setImageResource(R.drawable.heart_color_36dp);
                    Utility.applyCircularRevealAnimation(holder.mfavIcon, false);
//                    anim.start();

                    holder.mfavIcon.setTag(R.drawable.heart_color_36dp);
                    holder.mfavIconParent.setOnClickListener(null);

                    curretUser.addAllUnique(Constants.UserProperty.FAV_CATEGORIES_ID, Arrays.asList(cat.getObjectId()));

                    ParseObject.saveAllInBackground(Arrays.asList(curretUser), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
//                                    handleError(e, Arrays.asList((ParseObject) user));
                            if (e != null) {
                                Utility.revertAll(Arrays.asList((ParseObject) curretUser));
                                holder.mfavIcon.setImageResource(R.drawable.heart_light);
                                holder.mfavIcon.setTag(R.drawable.heart_light);
                            }
                            holder.mfavIconParent.setOnClickListener(mLayout);
                        }
                    });
                }
                else {
                    holder.mfavIcon.setImageResource(R.drawable.heart_light);
                    Utility.applyCircularRevealAnimation(holder.mfavIcon, false);
                    holder.mfavIcon.setTag(R.drawable.heart_light);

                    holder.mfavIconParent.setOnClickListener(null);

                    curretUser.removeAll(Constants.UserProperty.FAV_CATEGORIES_ID, Arrays.asList(cat.getObjectId()));

                    ParseObject.saveAllInBackground(Arrays.asList(curretUser), new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
//                                    handleError(e, Arrays.asList((ParseObject) user));
                            if (e != null) {
                                Utility.revertAll(Arrays.asList((ParseObject) curretUser));
                                holder.mfavIcon.setImageResource(R.drawable.heart_color_36dp);
                                holder.mfavIcon.setTag(R.drawable.heart_color_36dp);
                            }
                            holder.mfavIconParent.setOnClickListener(mLayout);
                        }
                    });
                }
            }
        }
    };

    private void showNativeAd(final CategoryHV viewH,final Category category) {
        final View view = viewH.adView;
        if (category.getNativeAd() == null) {
            final NativeAd nativeAd = new NativeAd(mContext, mContext.getString(R.string.category_native_id));
            nativeAd.setAdListener(new AdListener() {

                @Override
                public void onError(Ad ad, AdError error) {
                    Log.e("error", error.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    view.setVisibility(View.VISIBLE);
                    category.setNativeAd(nativeAd);

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
            category.getNativeAd().unregisterView();

            view.setVisibility(View.VISIBLE);

            String titleForAd = category.getNativeAd().getAdTitle();
            NativeAd.Image coverImage = category.getNativeAd().getAdCoverImage();
            NativeAd.Image iconForAd = category.getNativeAd().getAdIcon();
            String socialContextForAd = category.getNativeAd().getAdSocialContext();
            String titleForAdButton = category.getNativeAd().getAdCallToAction();
            String textForAdBody = category.getNativeAd().getAdBody();
            NativeAd.Rating appRatingForAd = category.getNativeAd().getAdStarRating();


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

            category.getNativeAd().registerViewForInteraction(view);
        }
    }
}
