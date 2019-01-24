package com.startup.soundstack.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.dpizarro.autolabel.library.AutoLabelUI;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.startup.soundstack.R;
import com.startup.soundstack.adapter.CustomSpinnerAdapter;
import com.startup.soundstack.customclass.CustomSpinner;
import com.startup.soundstack.fragments.CreateCategoryDialog;
import com.startup.soundstack.fragments.WaveformFragment;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.models.Tags;
import com.startup.soundstack.soundfile.SamplePlayer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Continuation;
import bolts.Task;


public class MainActivity extends AppCompatActivity {
    static String filename = null;
    static Boolean showCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Answers.getInstance().logCustom(new CustomEvent("Crop Activity"));

        if (savedInstanceState != null) {
            filename = savedInstanceState.getString(Constants.FILENAME);
        }

        else {
            Bundle extra = getIntent().getExtras();
            if (extra != null) {
                filename = extra.getString(Constants.FILENAME);
            }
            else {
                filename = null;
            }
        }
        if(filename == null && HomeActivity.sSoundFile == null){
            finish();
            return;

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Crop Audio");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CustomWaveformFragment())
                    .commit();
        }

        Bundle bundle = getIntent().getBundleExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD);
        showCategory = true;
        if (bundle != null) {
            showCategory  = bundle.getBoolean(Constants.OPEN_CATEGORY_AFTER_UPLOAD);
        }
    }

    public static class CustomWaveformFragment extends WaveformFragment {

        private FragmentActivity mContext;

        public CustomWaveformFragment() {
            UpdateCenter.getEventBus().register(this);
        }


        @Override
        protected String getFileName() {
                return filename;
        }

        @Override
        protected void showAlert(String message) {
            if (mContext != null) {
                Snackbar.make(mParent, message, Snackbar.LENGTH_LONG).show();
            }
        }

        private void afterFileUploaded(SoundItem soundItem) {

        }

        private void showTryAginSnackbar(String message) {
            if (getActivity() != null) {
                Snackbar.make(mParent, message, Snackbar.LENGTH_SHORT)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomWaveformFragment.this.saveSound();
                            }
                        })
                        .show();
            }
        }


        @Override
        protected void afterSaveSound(String title, final String finalOutPath, int duration, final byte [] bytes) {

            if (bytes == null) {
                //Show error
                Snackbar.make(mParent, "Fail to prepare sound", Snackbar.LENGTH_SHORT)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomWaveformFragment.this.saveSound();
                            }
                        })
                        .show();
            }

            File file =  new File(finalOutPath);
            final ParseFile parseFile = new ParseFile(file.getName(), bytes);

            final Task<Void> fileUploadTask [] = new Task[1];
            final ProgressDialog pd = new ProgressDialog(getActivity());
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("Uploading File");
            pd.setCancelable(false);

            pd.setMessage("Uploading...");


            pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pd.dismiss();

                    if (fileUploadTask[0] != null) {

                        parseFile.cancel();
                    }
                }
            });

            pd.setButton(DialogInterface.BUTTON_NEUTRAL, "Run in background", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pd.dismiss();
                }
                });

            pd.show();

            // save sound.
            final SoundItem soundItem = new SoundItem();
            soundItem.setName(mName.getText().toString());
            soundItem.setByText(ParseUser.getCurrentUser().getString("name"));
            soundItem.setColumnUploadedByUser(ParseUser.getCurrentUser().getObjectId());
            Category selectedCategory = null;
            if (!(mCatSpinner.getSelectedItemPosition() == 0)) {
                selectedCategory = mSpinnerAdapter.getItem(mCatSpinner.getSelectedItemPosition());
                soundItem.setCategory(mSpinnerAdapter.getItem(mCatSpinner.getSelectedItemPosition()));
                ParseACL catACL = selectedCategory.getACL();
                if (catACL != null && !catACL.getPublicReadAccess()) {
                    soundItem.setACL(catACL);
                }
            }
            soundItem.setTags(mTagList);

            try {
                final Category finalSelectedCategory = selectedCategory;

                fileUploadTask[0] = parseFile.saveInBackground(new ProgressCallback() {
                    @Override
                    public void done(Integer percentDone) {
                        pd.setProgress(percentDone);
                    }
                });

                fileUploadTask[0].continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        if (task.isCancelled()) {
                            showTryAginSnackbar("Task was canceled by user.");
//                            throw new RuntimeException("Task was canceled by user.");
                        }
                        if (task.isFaulted() ) {
                            pd.dismiss();
                            showTryAginSnackbar(task.getError().getMessage());

                        }
                        if (task.isCompleted()) {

                            soundItem.setSoundFile(parseFile);

                            soundItem.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e == null) {
                                        UpdateCenter.postNewSoundEvent(null, soundItem);


                                        if (getActivity() != null) {
                                            getActivity().finish();

                                            //TODO do it in proper way
                                            try {
                                                File path = new File(getActivity().getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
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

                                            if (finalSelectedCategory != null && showCategory) {
                                                Intent intent = new Intent(mContext, SoundActivity.class);
                                                JSONObject jsonObject = Utility.createJsonQuery(finalSelectedCategory.getName(), SoundItem.COLUMN_CATEGORY_ID, new JSONArray(Arrays.asList(finalSelectedCategory.getObjectId())), SoundItem.class.toString(), -1);
                                                try {
                                                    jsonObject.put("enableSwipe", true);
                                                } catch (JSONException e2) {
                                                    e2.printStackTrace();
                                                }
                                                intent.putExtra("obj", jsonObject.toString());
                                                mContext.startActivity(intent);
                                            }
                                        }
                                    }

                                    else {
                                        showTryAginSnackbar("Fail to upload audio");
                                    }

                                    pd.dismiss();
                                }
                            });
                        }
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);

//                fileUploadTask[0].continueWithTask(new Continuation<Void, Task<Void>>() {
//                    @Override
//                    public Task<Void> then(Task<Void> task) throws Exception {
//
//                        if (task.isCancelled()) {
//                            throw new RuntimeException("Task was canceled by user.");
//                        }
//                        if (task.isFaulted()) {
//                            throw new RuntimeException("There was an error.");
//                        }
//                        if (task.isCompleted()) {
//
//                            soundItem.setSoundFile(parseFile);
//
//                            return soundItem.saveInBackground();
//                        }
//                        return null;
//                    }
//                }).continueWith(new Continuation<Void, Object>() {
//                    @Override
//                    public Object then(Task<Void> task) throws Exception {
//
//                        if (task.isCancelled()) {
//                            throw new RuntimeException("Task was canceled by user.");
//                        }
//
//                        if (task.isFaulted()) {
//                            Snackbar.make(mParent, task.getError().getMessage(), Snackbar.LENGTH_SHORT)
//                                    .setAction("Retry", new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            CustomWaveformFragment.this.saveSound();
//                                        }
//                                    })
//                                    .show();
//                            return null;
//                        }
//
//                        if (task.isCompleted()) {
//
////                            mTagsInfo.setSoundTags(mTagList);
////                            mTagsInfo.pinInBackground();
//
//                            UpdateCenter.postNewSoundEvent(null, soundItem);
//                            pd.dismiss();
//
//                            if (getActivity() != null) {
//                                getActivity().finish();
//
//
//                            //TODO do it in proper way
//                            try {
//                                File path = new File(getActivity().getFilesDir(), Constants.INTERNEL_SOUND_DIR_NAME);
//                                if (!path.exists()) {
//                                    path.mkdir();
//                                }
//
//                                int dotIndex = finalOutPath.lastIndexOf('.');
//                                String ext = ".ogg";
//                                if (dotIndex > 0) {
//                                    ext = finalOutPath.substring(dotIndex);
//                                }
//
//                                File file = new File(path, String.format("%s" + ext, soundItem.getObjectId()));
//
//                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//
//                                bos.write(bytes);
//                                bos.flush();
//                                bos.close();
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                            }
//                            //TODO
//
//                            if (finalSelectedCategory != null && showCategory) {
//                                Intent intent = new Intent(mContext, SoundActivity.class);
//                                JSONObject jsonObject = Utility.createJsonQuery(finalSelectedCategory.getName(), SoundItem.COLUMN_CATEGORY_ID, new JSONArray(Arrays.asList(finalSelectedCategory.getObjectId())), SoundItem.class.toString(), -1);
//                                try {
//                                    jsonObject.put("enableSwipe", true);
//                                } catch (JSONException e2) {
//                                    e2.printStackTrace();
//                                }
//                                intent.putExtra("obj", jsonObject.toString());
//                                mContext.startActivity(intent);
//                            }
//                        }
//                        }
//                        pd.dismiss();
//                        return null;
//                    }
//                }, Task.UI_THREAD_EXECUTOR);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.fragment_waveform, container, false);
        mParent = view.findViewById(R.id.parent);
        mTags = (AutoCompleteTextView)view.findViewById(R.id.tag1);
        mName = (EditText)view.findViewById(R.id.soundName);
        mCatSpinner = (CustomSpinner)view.findViewById(R.id.category_spinner);
        mAutoLabelUI = (AutoLabelUI)view.findViewById(R.id.label_view);
        mtextInputName = (TextInputLayout) view.findViewById(R.id.textInput_name);
        mTextInputTag = (TextInputLayout) view.findViewById(R.id.textInput_tag1);

        mSpinnerAdapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_row, R.id.txtname);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCatSpinner.setAdapter(mSpinnerAdapter);
        mCatSpinner.setSelection(0, false);


        mTagList = new ArrayList<String>();
        mAutoLabelUI.setOnRemoveLabelListener(new AutoLabelUI.OnRemoveLabelListener() {
            @Override
            public void onRemoveLabel(View view, int position) {
                mTagList.remove(view.getTag());
                mTextInputTag.setError("");
            }
        });

        ParseQuery<Tags> tagsParseQuery = new ParseQuery<Tags>(Tags.class);
        tagsParseQuery.fromLocalDatastore();
        tagsParseQuery.getFirstInBackground(new GetCallback<Tags>() {
            @Override
            public void done(Tags object, ParseException e) {
                if (object == null) return;
                List<String> list = Utility.jArrayToList(object.getSoundTags(), " ");
                String[] arr = new String[list.size()];
                list.toArray(arr);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CustomWaveformFragment.this.getActivity(),
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
                    if (s.charAt(s.length() - 1) == ' ') {
                        mAutoLabelUI.addLabel(value.substring(0, s.length() - 1));
                        mTagList.add(value.substring(0, s.length() - 1));
                        s.clear();
                    }

                }
            }
        });

            loadGui(view);
            if (mSoundFile == null) {
                loadFromFile();
            } else {
                mPlayer = new SamplePlayer(mSoundFile);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finishOpeningSoundFile();
                    }
                });
            }
            setHasOptionsMenu(true);

            fetchCategories();

            view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSound();
                }
            });
        return  view;

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = getActivity();
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mContext = null;
            UpdateCenter.getEventBus().unregister(this);
        }


//        @Override
//        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
//            menuInflater.inflate(R.menu.menu_save_sound, menu);
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            int id = item.getItemId();
//
//            if (id == R.id.action_settings) {
//                return true;
//            } else if (id == R.id.action_save) {
//                saveSound();
//                return true;
//            }
//
//            return super.onOptionsItemSelected(item);
//        }

        private void saveSound() {

            if (mCatSpinner.getSelectedItemPosition() == 0) {
                Snackbar.make(mParent, R.string.create_new_category_or_create, Snackbar.LENGTH_LONG)
                        .setAction("Create", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openCreateCategoryDialog();
                            }
                        })
                        .show();
                return;
            }
            String name, tag1, cropSound = "/sdcard/temp_crop.";
            name = mName.getText().toString();

            tag1 = mTags.getText().toString();

            boolean val = true;
//            try {
//                int startFrame = mWaveformView.secondsToFrames(mWaveformView.pixelsToSeconds(mStartPos));
//                int frameCount = mWaveformView.secondsToFrames(mWaveformView.pixelsToSeconds(mEndPos)) - startFrame;
//                cropSound = cropSound+mSoundFile.getFiletype().toLowerCase();
//                mSoundFile.WriteFile(new File(cropSound), startFrame, frameCount);
//
//            }catch (IOException e){
//                e.printStackTrace();
//            }


            if (name.isEmpty()) {
                mtextInputName.setError("Name can't be empty.");
                val = false;
            } else {
                mtextInputName.setError("");
            }

            if (mAutoLabelUI.getLabelsCounter() == 0 ) {
                if (tag1.isEmpty()) {
                    mTextInputTag.setError("Tag required.");
                    val = false;
                }
                else {
                    mTagList.add(tag1);
                    mTextInputTag.setError("");
                }
            } else {
                mTextInputTag.setError("");
            }

            if (val) {

                if (mIsPlaying) {
                    handlePause();
                }
               saveRingtone("Tempsound");

            }
        }

        private void fetchCategories() {
            final String Cat_tag = "categories";
//        final boolean isConnected = Utility.isConnectedToInternet(getActivity());
            ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
            query.whereEqualTo(SoundItem.COLUMN_UPLOADED_BY_USER, ParseUser.getCurrentUser().getObjectId());
            query.fromLocalDatastore();
            query.addAscendingOrder("name");
            query.findInBackground(new FindCallback<Category>() {
                @Override
                public void done(final List<Category> list, ParseException e) {
                    if (e == null) {
//                        mProgressDialog.dismiss();
                        Category category = new Category();
                        category.setName(getString(R.string.create_new_category));
                        list.add(0, category);

                        mSpinnerAdapter.getCategories().addAll(list);
                        mSpinnerAdapter.notifyDataSetChanged();
                        mCatSpinner.setSelection(0, false);

                    } else {
                        e.printStackTrace();
                        if (e.getCode() == ParseException.CONNECTION_FAILED || e.getCode() == ParseException.TIMEOUT) {
                            ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
                            query.fromLocalDatastore();
                            query.addAscendingOrder("name");
                            query.findInBackground(new FindCallback<Category>() {

                                @Override
                                public void done(List<Category> list, ParseException e) {
//                                    mProgressDialog.dismiss();
                                    mSpinnerAdapter.getCategories().addAll(list);
                                    mSpinnerAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                    mCatSpinner.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (0 == position) {
                                openCreateCategoryDialog();
                            } else {

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }
            });
        }

        private void openCreateCategoryDialog() {
            CreateCategoryDialog createCategoryDialog = new CreateCategoryDialog();
            createCategoryDialog.show(getActivity().getSupportFragmentManager(), "cat");
        }

        @Subscribe
        public void updateUI(UpdateCenter.CreateCategoryEvent event) {
            if (mSpinnerAdapter != null && event.mCategory != null) {

                mSpinnerAdapter.getCategories().add(1, event.mCategory);

                mSpinnerAdapter.notifyDataSetChanged();
                mCatSpinner.setSelection(1);
            }

            else {
                showAlert(getString(R.string.fail_creating_stack));
            }
        }

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);

            if (bundle != null) {
                filename = bundle.getString(Constants.FILENAME);
                mFilename = filename;
            }
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            // Save UI state changes to the savedInstanceState.
            // This bundle will be passed to onCreate if the process is
            // killed and restarted.
            savedInstanceState.putString(Constants.FILENAME, filename);
            // etc.
        }

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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString(Constants.FILENAME, filename);
        // etc.
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nagative100to0, R.anim.oto100);
    }
}
