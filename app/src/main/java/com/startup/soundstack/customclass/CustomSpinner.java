package com.startup.soundstack.customclass;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/**
 * Created by HKurra on 10/19/2015.
 */

public class CustomSpinner extends Spinner {
    OnItemSelectedListener listener;

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
        if (listener != null)
            listener.onItemSelected(null, null, position, 0);
    }

    public void setOnItemSelectedEvenIfUnchangedListener(
            OnItemSelectedListener listener) {
        this.listener = listener;
    }
}