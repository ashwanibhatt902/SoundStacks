package com.startup.soundstack.customclass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.startup.soundstack.R;


/**
 * Created by Dheeraj on 7/6/2015.
 */
public class CustomButton extends Button {
    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs)
    {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        String customFont = a.getString(R.styleable.TextViewPlus_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset)
    {
        Typeface tf = null;
        try
        {
            tf = Typeface.createFromAsset(ctx.getAssets(), "ProximaNovaSoftMedium.ttf");
        }
        catch (Exception e)
        {
            return false;
        }

        setTypeface(tf);
        return true;
    }
}
