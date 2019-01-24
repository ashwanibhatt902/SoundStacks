package com.startup.soundstack.customclass;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.startup.soundstack.R;


/**
 * Created by Dheeraj on 7/6/2015.
 */
public class CustomEditText extends EditText{

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
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
