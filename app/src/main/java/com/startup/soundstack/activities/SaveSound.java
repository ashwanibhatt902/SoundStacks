package com.startup.soundstack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.adapter.CustomSpinnerAdapter;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class SaveSound extends AppCompatActivity {

    EditText edtName, edtTag1, edtTag2, edtTag3;
    TextInputLayout txtInputName, txtInputtag1, txtInputtag2, txtInputtag3;
    Spinner spinnerCategory;
    CustomSpinnerAdapter spinnerAdapter;
    ProgressDialog pd;
    String filename;
    String fileExt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if (!intent.hasExtra("filename")) {
            finish();
        } else {
            filename = intent.getExtras().getString("filename");
            fileExt = intent.getExtras().getString("ext");
        }


        setContentView(R.layout.activity_save_sound);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Save Sound");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        edtName = (EditText) findViewById(R.id.soundName);
        edtTag1 = (EditText) findViewById(R.id.tag1);
        edtTag2 = (EditText) findViewById(R.id.tag2);
        edtTag3 = (EditText) findViewById(R.id.tag3);

        txtInputtag1 = (TextInputLayout) findViewById(R.id.textInput_tag1);
        txtInputtag2 = (TextInputLayout) findViewById(R.id.textInput_tag2);
        txtInputtag3 = (TextInputLayout) findViewById(R.id.textInput_tag3);
        txtInputName = (TextInputLayout) findViewById(R.id.textInput_name);

        spinnerCategory = (Spinner) findViewById(R.id.category_spinner);
        spinnerAdapter = new CustomSpinnerAdapter(this, R.layout.spinner_row, R.id.txtname);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        fetchCategories();
    }


    private void fetchCategories() {
        final String Cat_tag = "categories";
        final boolean isConnected = Utility.isConnectedToInternet(this);
        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
        if (!isConnected) {
            query.fromLocalDatastore();
        }
        query.addAscendingOrder("name");
        query.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(final List<Category> list, ParseException e) {
                if (e == null) {
                    pd.dismiss();
                    spinnerAdapter.getCategories().addAll(list);
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                    if (e.getCode() == ParseException.CONNECTION_FAILED || e.getCode() == ParseException.TIMEOUT) {
                        ParseQuery<Category> query = ParseQuery.getQuery(Category.class);
                        query.fromLocalDatastore();
                        query.addAscendingOrder("name");
                        query.findInBackground(new FindCallback<Category>() {

                            @Override
                            public void done(List<Category> list, ParseException e) {
                                pd.dismiss();
                                spinnerAdapter.getCategories().addAll(list);
                                spinnerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save_sound, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_save) {
            saveSound();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveSound() {
        String name, tag1, tag2, tag3;
        name = edtName.getText().toString();
        tag1 = edtTag1.getText().toString();
        tag2 = edtTag2.getText().toString();
        tag3 = edtTag3.getText().toString();

        boolean val = true;

        if (name.isEmpty()) {
            txtInputName.setError("Name can't be empty.");
            val = false;
        } else {
            txtInputName.setError("");
        }
        if (tag1.isEmpty()) {
            txtInputtag1.setError("Tag required.");
            val = false;
        } else {
            txtInputtag1.setError("");
        }
        if (tag2.isEmpty()) {
            txtInputtag2.setError("Tag required.");
            val = false;
        } else {
            txtInputtag2.setError("");
        }
        if (tag3.isEmpty()) {
            txtInputtag3.setError("Tag required.");
            val = false;
        } else {
            txtInputtag3.setError("");
        }

        if (val) {
            Tracker t = ((SoundStackApplication)this.getApplication()).getTracker();
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("Sound Saved")
                    .setAction(String.format("category : %s", spinnerAdapter.getItem(spinnerCategory.getSelectedItemPosition())))
                    .build());

            // save sound.
            SoundItem soundItem = new SoundItem();
            soundItem.setName(name);
            soundItem.setByText(ParseUser.getCurrentUser().getString("name"));
            soundItem.setColumnUploadedByUser(ParseUser.getCurrentUser().getObjectId());
//            soundItem.setExtension(fileExt);
            soundItem.setCategory(spinnerAdapter.getItem(spinnerCategory.getSelectedItemPosition()));

            Answers.getInstance().logCustom(new CustomEvent("Sound Activity").putCustomAttribute("Category Added to",spinnerAdapter.getItem(spinnerCategory.getSelectedItemPosition()).getName()));

            File file = new File("/sdcard/" + filename);
            byte[] bFile = new byte[(int) file.length()];

            try {
                //convert file into array of bytes
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(bFile);
                fileInputStream.close();
                ParseFile parseFile = new ParseFile(filename, bFile);

                soundItem.setSoundFile(parseFile);
                soundItem.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.e("error","hejhejr");

                        } else {
                            Log.e("error","hejhej2r");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

