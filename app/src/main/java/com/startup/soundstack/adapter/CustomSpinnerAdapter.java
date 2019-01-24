package com.startup.soundstack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseACL;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.HomeActivity;
import com.startup.soundstack.models.Category;

import java.util.ArrayList;

/**
 * Created by Dheeraj on 9/21/2015.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<Category> {
    ArrayList<Category> categories;
    LayoutInflater inflater;
    int layoutId, textViewId;

    public CustomSpinnerAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        categories = new ArrayList<Category>();
        layoutId = resource;
        this.textViewId = textViewResourceId;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = null;
        if(convertView == null){
            row = inflater.inflate(layoutId, parent, false);
        }else{
            row = convertView;
        }

        TextView txtView = (TextView) row.findViewById(textViewId);
        ImageView imgView = (ImageView) row.findViewById(R.id.catIcon);
        View catType = row.findViewById(R.id.category_type);

        Category category = categories.get(position);
        txtView.setText(category.getName());

        int imageColor = category.getImageColor();

        if (imageColor == -1) {
            imgView.clearColorFilter();
        }
        else {
            imgView.setColorFilter(imageColor);
        }

        ParseACL catACl = category.getACL();
        if (catACl != null) {
            if (catACl.getPublicReadAccess()) {
                catType.setVisibility(View.GONE);
            } else {
                catType.setVisibility(View.VISIBLE);
            }
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(category.getImageURL(),
                imgView, HomeActivity.sDisplayImageOptions);


        return row;
    }
}
