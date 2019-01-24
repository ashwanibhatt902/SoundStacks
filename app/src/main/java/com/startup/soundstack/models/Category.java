package com.startup.soundstack.models;

import android.graphics.Color;

import com.facebook.ads.NativeAd;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.startup.soundstack.R;
import com.startup.soundstack.utils.Constants;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by Dheeraj on 7/28/2015.
 */
@ParseClassName("Categories")
public class Category extends ParseObject{
    public String mName = "";
    private NativeAd nativeAd;

    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    public String getName() {
        String name = getString("name");
        if (name == null || name.isEmpty()) {
            return mName;
        }
        return getString("name");
    }

    public void setName(String name) {
        put("name", name);
        this.mName = name;
    }

    public void setByText(String byText) {
        put(SoundItem.COLUMN_UPLOADED_BY, byText);
    }
    public String getByText() {
        String uploadedBy = getString(SoundItem.COLUMN_UPLOADED_BY);
        if (uploadedBy == null || uploadedBy.length() == 0) {
            uploadedBy = "Bluecap App";
        }
        return "By: "+uploadedBy;
    }

    public void setColumnUploadedByUser(String user) {
        put(SoundItem.COLUMN_UPLOADED_BY_USER, user);
    }

    public String getColumnUploadedByUser( ) {
        return  getString(SoundItem.COLUMN_UPLOADED_BY_USER);
    }

    public void setTags(List<String> tagList) {
        if (tagList.size() > 0) {
            addAllUnique(SoundItem.COLUMN_TAGS, tagList);
        }
    }

    public void setImage(AppExtensibleResource appExtensibleResource) {

        if (appExtensibleResource == null) return;

        put("image", appExtensibleResource);

        if (appExtensibleResource.getResourceID() == Constants.DUMMY_USER_IMAGE_RESOURCE_ID) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                ParseFile profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE_LQ);
                if (profileParseFile == null) {
                    profileParseFile = currentUser.getParseFile(Constants.UserProperty.PROFILE_PIC_FILE);
                }
                if (profileParseFile != null) {
                    put("imageAsURL", profileParseFile.getUrl());
                }
            }
        }
    }


    public JSONArray getTags() {
        return   getJSONArray(SoundItem.COLUMN_TAGS);
    }

    public String getImageURL() {

        String imageURL = null;
        ParseObject appExtensibleResource = getParseObject("image");

        if (appExtensibleResource != null) {
            ParseFile imageFile = appExtensibleResource.getParseFile(AppExtensibleResource.RESOURCE_FILE);
            if (imageFile != null) {
                imageURL = imageFile.getUrl();
            }

            else if (appExtensibleResource.getInt(AppExtensibleResource.RESOURCE_ID) == Constants.DUMMY_USER_IMAGE_RESOURCE_ID) {
                imageURL = getString("imageAsURL");
                if (imageURL == null) {
                    imageURL = "drawable://" + R.drawable.com_facebook_profile_picture_blank_portrait;
                }
            }
        }

        return imageURL;
    }

    public int getImageColor() {

        int colorCode = -1;
        ParseObject appExtensibleResource = getParseObject("image");

        if (appExtensibleResource != null) {
            String colorName = appExtensibleResource.getString(AppExtensibleResource.COLOR);
            if (colorName == null) {
                 if (appExtensibleResource.getInt(AppExtensibleResource.RESOURCE_ID) == Constants.DUMMY_USER_IMAGE_RESOURCE_ID) {
                     return colorCode;
                 }
                 else {
                     colorName = "#00bcd4";
                 }
            }

            try {
                colorCode = Color.parseColor(colorName);
            } catch (Exception e) {
                colorCode = Color.parseColor("#00bcd4");
            }

        }

        return colorCode;
    }
}
