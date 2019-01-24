package com.startup.soundstack.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.startup.soundstack.R;
import com.startup.soundstack.customclass.CustomSpinner;
import com.startup.soundstack.models.Report;
import com.startup.soundstack.models.SoundItem;
import com.startup.soundstack.utils.UpdateCenter;
import com.startup.soundstack.utils.Utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by harsh on 10/16/2015.
 */
public class ReportSoundDialog extends DialogFragment {

    SoundItem mItem = null;
    private TextInputLayout mTextInputReason;
    private CustomSpinner mReasonSpinner;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_report_sound, null);

        mTextInputReason = (TextInputLayout)dialogLayout.findViewById(R.id.textInput_reason);
        mReasonSpinner = (CustomSpinner) dialogLayout.findViewById(R.id.reason_spinner);

        String soundID = null;
        if (getArguments() != null) {
            soundID = getArguments().getString("soundID");
        }

        ParseQuery<SoundItem> query = ParseQuery.getQuery(SoundItem.class);
        query.whereEqualTo("objectId", soundID);
        query.fromLocalDatastore();
        List<SoundItem> items = null;

        try {
            items = query.find();
            mItem = items.get(0);
        } catch (ParseException e) {
            e.printStackTrace();
        }






        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogLayout)
                // Add action buttons
                .setPositiveButton(R.string.report, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReportSoundDialog.this.getDialog().cancel();
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

                        if (mItem != null) {

                            final ProgressDialog pd = new ProgressDialog(getActivity());
                            pd.setTitle("Submitting Report");
                            pd.setCancelable(false);

                            pd.setMessage("Submitting...");
                            customDialog.dismiss();
                            pd.show();

                            mItem.incrementReportCount();
                            final Report report = new Report();

                            report.setByUser();
                            report.setSoundID(mItem);
                            String reason = mReasonSpinner.getSelectedItem().toString();

                            report.setColumnReasonDetail(mTextInputReason.getEditText().getText().toString());
                            //                                ParseObject.saveAll(Arrays.asList(mItem, report));
                            report.setColumnReason(reason);

                            ParseObject.saveAllInBackground(Arrays.asList(mItem, report), new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    Context context = ReportSoundDialog.this.getActivity();
                                    if (e == null) {
                                        UpdateCenter.postReportEvent(null, report);
                                    }

                                    else {
                                        UpdateCenter.postReportEvent(null, null);
                                        Utility.revertAll(Arrays.asList(mItem, report));
                                    }

                                    pd.dismiss();
                                }

                            });


                        }

                    }
                });
            }
        });



        return customDialog;

    }
}
