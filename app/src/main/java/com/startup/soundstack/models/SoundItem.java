package com.startup.soundstack.models;

import com.facebook.ads.NativeAd;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Dheeraj on 7/22/2015.
 */
@ParseClassName("Sounds")
public class SoundItem extends ParseObject implements Serializable{

    private String soundExt = null;

    private NativeAd nativeAd;
    public static String COLUMN_NAME = "name";
    public static String COLUMN_CREATED_AT = "createdAt";
    public static String COLUMN_CATEGORY_ID = "categoryId";
    public static String COLUMN_HQ_SOUND = "highqsound";
    public static String COLUMN_LQ_SOUND = "lowqsound";
    public static String COLUMN_LIKE_COUNT = "likeCount";
    public static String COLUMN_DISLIKE_COUNT = "dislikeCOunt";
    public static String COLUMN_UPLOADED_BY = "uploadedBy";
    public static String COLUMN_EXTENSION = "extension";
    public static String COLUMN_UPLOADED_BY_USER = "uploadedByUser";
    public static String COLUMN_CATEGORY = "category";
    public static String COLUMN_TAGS = "tags";
    public static String COLUMN_REPORT_COUNT = "reportCount";


    public NativeAd getNativeAd() {
        return nativeAd;
    }

    public void setNativeAd(NativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    public String getName() {
        return getString(COLUMN_NAME);
    }

    public void setName(String name) {
        put(COLUMN_NAME, name);
    }

    public ParseFile getSoundFile() {
        return getParseFile(COLUMN_HQ_SOUND);
    }

    public void setSoundFile(ParseFile soundFile) {
        put(COLUMN_HQ_SOUND, soundFile);
    }

    public int getLikeCount() {

        return getInt(COLUMN_LIKE_COUNT);
    }

    public void setLikeCount(int likeCount) {
       put(COLUMN_LIKE_COUNT, likeCount);

    }

    public void incrementReportCount() {
        increment(COLUMN_REPORT_COUNT, 1);
    }

    public void incrementLikeCount() {
        increment(COLUMN_LIKE_COUNT, 1);
    }
    public void decrementLikeCount() {
        increment(COLUMN_LIKE_COUNT, -1);
    }

    public int getDisLikeCount() {
        return getInt(COLUMN_DISLIKE_COUNT);
    }

    public void setDisLikeCount(int disLikeCount) {
        put(COLUMN_DISLIKE_COUNT, disLikeCount);
    }

    public void incrementDisLikeCount() {
        increment(COLUMN_DISLIKE_COUNT, 1);
    }
    public void decrementDisLikeCount() {
        increment(COLUMN_DISLIKE_COUNT, -1);
    }

    public String getByText() {
        String uploadedBy = getString(COLUMN_UPLOADED_BY);
        if (uploadedBy == null || uploadedBy.length() == 0) {
            uploadedBy = "Bluecap App";
        }
        return "By: "+uploadedBy;
    }

    public void setByText(String byText) {
        put(COLUMN_UPLOADED_BY, byText);
    }

    public String getExtension() {

        if (soundExt == null) {
            String name = getSoundFile().getName();

            int dotIndex = name.lastIndexOf('.');
            soundExt = ".ogg";
            if (dotIndex > 0) {
                soundExt = name.substring(dotIndex);
            }
        }
        return soundExt;
    }

    public void setExtension(String extension) {
        put(COLUMN_EXTENSION, extension);
    }

    public void setColumnUploadedByUser(String user){put(COLUMN_UPLOADED_BY_USER, user);}


    public String getColumnUploadedByUser( ){
        return getString(COLUMN_UPLOADED_BY_USER);
    }

    public void setCategory(Category category) {
        put(COLUMN_CATEGORY, category);
        addUnique(COLUMN_CATEGORY_ID, category.getObjectId());
    }

    public List<Category> getCategory() {
        return  getList(COLUMN_CATEGORY_ID);
    }

    public void setTags(List<String> tags) {
        if (tags.size() > 0) {
            addAllUnique(COLUMN_TAGS, tags);
        }
    }

    public JSONArray getTags() {
        return   getJSONArray(SoundItem.COLUMN_TAGS);
    }
}
