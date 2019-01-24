package com.startup.soundstack.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.dpizarro.autolabel.library.AutoLabelUI;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.activities.HomeActivity;
import com.startup.soundstack.models.AppExtensibleResource;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.utils.Constants;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harsh on 10/4/2015.
 */

public class CreateCategoryDialog extends DialogFragment {

    private TextInputLayout mTextInputName = null;
    private TextInputLayout mTextInputTag = null;
    private AutoLabelUI mAutoLabelUI;
    private List<String> mTagList;
    private Spinner mSpinner;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mTags;
    private IconListAdapter mAdapter;
    private View mStackWarning;

    enum AccessLevel {
        Public,
        Private
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_new_category, null);

        mTextInputName = (TextInputLayout)dialogLayout.findViewById(R.id.textInput_catname);
        mTextInputTag = (TextInputLayout)dialogLayout.findViewById(R.id.textInput_cat_tag1);
        mAutoLabelUI = (AutoLabelUI)dialogLayout.findViewById(R.id.cat_label_view);
        mStackWarning = dialogLayout.findViewById(R.id.access_warning);
        mTagList = new ArrayList<>();
        mSpinner = (Spinner) dialogLayout.findViewById(R.id.access_spinner);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   mStackWarning.setVisibility(View.GONE);
               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {

               }
           });

                mRecyclerView = (RecyclerView) dialogLayout.findViewById(R.id.iconSelector);
        mTags = (AutoCompleteTextView) dialogLayout.findViewById(R.id.cat_tag);

//        dialogLayout.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
        tagsParseQuery.fromLocalDatastore();
        tagsParseQuery.getFirstInBackground(new GetCallback<Tags>() {
            @Override
            public void done(Tags object, ParseException e) {
                if (object == null) return;
                List<String> list = Utility.jArrayToList(object.getCategoryTags(), " ");
                String[] arr = new String[list.size()];
                list.toArray(arr);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CreateCategoryDialog.this.getActivity(),
                        android.R.layout.simple_dropdown_item_1line, arr);
                mTags.setAdapter(adapter);
            }
        });

        mTags.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (mTagList.size() == 3) {
                    mTextInputTag.setError("Only 3 tag allowed");
                    return;
                }
                if (s.length() >= 2) {
                    mTextInputTag.setError("");
                    String value = s.toString();
                    if (s.charAt(s.length()-1) == ' ') {
                        mAutoLabelUI.addLabel(value.substring(0, s.length()-1));
                        mTagList.add(value.substring(0, s.length()-1));
                        s.clear();
                    }

                }
            }
        });

        mAutoLabelUI.setOnRemoveLabelListener(new AutoLabelUI.OnRemoveLabelListener() {
            @Override
            public void onRemoveLabel(View view, int position) {
                mTagList.remove(view.getTag());
                mTextInputTag.setErrorEnabled(false);
            }
        });

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new IconListAdapter();
        mRecyclerView.setAdapter(mAdapter);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogLayout)
                // Add action buttons
                .setPositiveButton(R.string.create, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateCategoryDialog.this.getDialog().cancel();
                    }
                });

//        builder.setTitle("Create Category");
        final AlertDialog customDialog =  builder.create();

        customDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button positiveButton = customDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = customDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                positiveButton.setTextColor(getActivity().getResources().getColor(R.color.color_accent));
                negativeButton.setTextColor(getActivity().getResources().getColor(R.color.color_accent));

                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        //Dismiss once everything is OK.
                        if (saveCategory()) {
                            Answers.getInstance().logCustom(new CustomEvent("Category Added"));

                            customDialog.dismiss();
                        }
                    }
                });
            }
        });



        return customDialog;

    }

    private boolean saveCategory() {

        String name, tag1;
        name = mTextInputName.getEditText().getText().toString();
        tag1 = mTextInputTag.getEditText().getText().toString();
        AccessLevel accessLevel = mSpinner.getSelectedItemPosition() == 0 ? AccessLevel.Public : AccessLevel.Private;
        ParseACL parseACL = new ParseACL();

        switch (accessLevel) {
            case Public:
                parseACL.setPublicReadAccess(true);
                parseACL.setWriteAccess(ParseUser.getCurrentUser(), true);
                break;
            case Private:
                parseACL.setReadAccess(ParseUser.getCurrentUser(), true);
                parseACL.setWriteAccess(ParseUser.getCurrentUser(), true);
        }
        boolean val = true;

        if (name.isEmpty()) {
            mTextInputName.setError("Name can't be empty.");
            val = false;
        } else {
            mTextInputName.setErrorEnabled(false);
        }

        if (mAutoLabelUI.getLabelsCounter() == 0 ) {
            if (tag1.isEmpty()) {
                mTextInputTag.setError("Tag required.");
                val = false;
            }
            else {
                mTagList.add(tag1);
                mTextInputTag.setErrorEnabled(false);
            }
        } else {
            mTextInputTag.setErrorEnabled(false);
        }

        if (parseACL.getPublicReadAccess()) {
            ParseUser currentUser = ParseUser.getCurrentUser();

            ParseQuery<Category> appCatImagesQuery = ParseQuery.getQuery(Category.class);
            appCatImagesQuery.whereEqualTo(SoundItem.COLUMN_UPLOADED_BY_USER, currentUser.getObjectId());
            appCatImagesQuery.fromLocalDatastore();
            List<Category> result = null;
            try {
                result = appCatImagesQuery.find();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int publicCatCount = 0;
            for (Category cat : result) {
                if (cat.getACL().getPublicReadAccess()) {
                    publicCatCount++;
                }
            }
            if (publicCatCount >= 10) {
                val = false;
                mStackWarning.setVisibility(View.VISIBLE);
            }
        }

        if (val) {
            final ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setTitle(R.string.creating_stack);
            pd.setCancelable(false);

            pd.setMessage("Creating...");
            pd.show();

                // save sound.
                final Category category = new Category();
                category.setName(name);
                category.setByText(ParseUser.getCurrentUser().getString("name"));
                category.setColumnUploadedByUser(ParseUser.getCurrentUser().getObjectId());
//            category.setCategory(mSpinnerAdapter.getItem(mCatSpinner.getSelectedItemPosition()));
                category.setTags(mTagList);
                category.setACL(parseACL);
                category.setImage(mAdapter.getSelectedImageResource());

                Tracker tracker = ((SoundStackApplication) ((Activity) getActivity()).getApplication()).getTracker();
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Category Create")
                        .setAction(String.format("category : %s", name))
                        .build());

                try {
                    category.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Log.e("error", "hejhejr");
                                UpdateCenter.postNewCategoryEvent(CreateCategoryDialog.this, category);
                                pd.dismiss();

                            } else {
                                Log.e("error", "hejhej2r");
                                UpdateCenter.postNewCategoryEvent(CreateCategoryDialog.this, null);
                                pd.dismiss();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return val;
        }


    public class IconListAdapter extends RecyclerView.Adapter<IconListAdapter.ViewHolder> {
//        private TypedArray mIcons;
        private int mCurrentSelection = 0;
        private List<AppExtensibleResource> mImageResource;

        public int getCurrentSelection() {
            return mCurrentSelection;
        }

        public  AppExtensibleResource getSelectedImageResource() {
            AppExtensibleResource resource = null;
            try {
                resource =  mImageResource.get(mCurrentSelection);
            }
            catch (IndexOutOfBoundsException e) {

            }
            return resource;
        }
        public void setmCurrentSelection(int mCurrentSelection) {
            this.mCurrentSelection = mCurrentSelection;
        }

        public List<AppExtensibleResource> getImageResource() {
            return mImageResource;
        }

        public void setmImageResource(List<AppExtensibleResource> mImageResource) {
            this.mImageResource = mImageResource;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView icon;

            public ViewHolder(View v) {
                super(v);
                icon = (ImageView) v.findViewById(R.id.icon);
            }
        }

        public IconListAdapter() {
//            mIcons = getActivity().getResources().obtainTypedArray(R.array.icons_selector);

            mImageResource = new ArrayList<>();
            ParseQuery<AppExtensibleResource> appCatImagesQuery = ParseQuery.getQuery(AppExtensibleResource.class);
            appCatImagesQuery.whereGreaterThanOrEqualTo(AppExtensibleResource.RESOURCE_ID, 2400);
            appCatImagesQuery.addAscendingOrder(AppExtensibleResource.RESOURCE_ID);
            appCatImagesQuery.fromLocalDatastore();
            try {
                setmImageResource(appCatImagesQuery.find());
                if (mImageResource == null || mImageResource.size() < 1 ) {
                    //if not found in local DB
                    Utility.downloadCategoryImagesAsync(getActivity());
                }
            } catch (ParseException e) {
                e.printStackTrace();
                //if not found in local DB
                Utility.downloadCategoryImagesAsync(getActivity());
            }
        }
        @Override
        public IconListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                   int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_selector_row, parent, false);

            ViewHolder vh = new ViewHolder(v);

            return vh;
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
//            ParseObject resource = null;
//            resource = cat.getParseObject("image");
//

            ImageLoader imageLoader = ImageLoader.getInstance();

            //download and display image from url

            String imgURL = null;
            AppExtensibleResource appExtensibleResource = getImageResource().get(position);
            ParseFile imageFile = appExtensibleResource.getParseFile(AppExtensibleResource.RESOURCE_FILE);
            if (imageFile != null) {
                imgURL = imageFile.getUrl();

                String colorName = getImageResource().get(position).getString(AppExtensibleResource.COLOR);
                if (colorName == null){
                    colorName = "#00bcd4";
                }
                try {
                    holder.icon.setColorFilter(Color.parseColor(colorName));
                }
                catch(Exception e) {
                    holder.icon.setColorFilter(Color.parseColor("#00bcd4"));
                }
            }

            else {
                if (appExtensibleResource.getResourceID() == Constants.DUMMY_USER_IMAGE_RESOURCE_ID) {

                    ParseFile profileParseFile = ParseUser.getCurrentUser().getParseFile(Constants.UserProperty.PROFILE_PIC_FILE_LQ);
                    if (profileParseFile == null) {
                        profileParseFile = ParseUser.getCurrentUser().getParseFile(Constants.UserProperty.PROFILE_PIC_FILE);
                    }

                    if (profileParseFile != null) {
                        imgURL = profileParseFile.getUrl();

                    }

                    else {
                        imgURL = "drawable://" + R.drawable.com_facebook_profile_picture_blank_portrait;;
                    }
                    holder.icon.clearColorFilter();

                }
            }
            imageLoader.displayImage(imgURL,
                    holder.icon, HomeActivity.sDisplayImageOptions);

//            RequestCreator imageRequestCallback = Picasso.with(getActivity()).load(getImageResource().get(position).getParseFile(AppExtensibleResource.RESOURCE_FILE).getUrl());
//            imageRequestCallback.into(holder.icon);

            if (position == getCurrentSelection()) {
                holder.icon.setBackgroundResource(R.color.color_accent);
            }
            else {
                holder.icon.setBackgroundResource(R.drawable.ripple_default);
            }


            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundResource(R.color.color_accent);
                    notifyItemChanged(getCurrentSelection());
                    setmCurrentSelection(position);
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return getImageResource().size();
        }

    }
}
