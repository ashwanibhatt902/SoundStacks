package com.startup.soundstack.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.startup.soundstack.R;
import com.startup.soundstack.SoundStackApplication;
import com.startup.soundstack.utils.Constants;

public class SelectType extends DialogFragment {

    private boolean mOpenCategory = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.activity_select_type, null);

        View recordButton = dialogLayout.findViewById(R.id.record_audio);
        View importButton = dialogLayout.findViewById(R.id.import_audio);

        recordButton.setOnClickListener(mButtonListener);
        importButton.setOnClickListener(mButtonListener);

        builder.setView(dialogLayout);

//        builder.setTitle("Create Category");
        final AlertDialog customDialog =  builder.create();

//        Toolbar toolbar = (Toolbar) dialogLayout.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        getSupportActionBar().setTitle("Select Option");
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return customDialog;

    }



    View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view){
            int id = view.getId();
            switch (id){
                case R.id.record_audio:
                    Intent intent = new Intent(getActivity(), RecordActivity.class);
                    intent.putExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD, mOpenCategory);
                    startActivity(intent);
                    SelectType.this.dismiss();
                    Tracker t = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("Sound Saved")
                            .setAction(String.format("record"))
                            .build());
                    Answers.getInstance().logCustom(new CustomEvent("Add Audio").putCustomAttribute("type","Record"));

                    break;
                case R.id.import_audio:
                    Intent import_intent = new Intent(getActivity(), SelectMusicActivity.class);
                    import_intent.putExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD, mOpenCategory);
                    Tracker tracker = ((SoundStackApplication)getActivity().getApplication()).getTracker();
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Sound Saved")
                            .setAction(String.format("import audio"))
                            .build());
                    Answers.getInstance().logCustom(new CustomEvent("Add Audio").putCustomAttribute("type", "Import"));

                    startActivity(import_intent);
                    SelectType.this.dismiss();

                    break;
            }
        }
    };


    public boolean ismOpenCategory() {
        return mOpenCategory;
    }

    public void setmOpenCategory(boolean mOpenCategory) {
        this.mOpenCategory = mOpenCategory;
    }
}
