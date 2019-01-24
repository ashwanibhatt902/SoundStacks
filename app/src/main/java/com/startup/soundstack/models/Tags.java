package com.startup.soundstack.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.startup.soundstack.utils.Utility;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by HKurra on 10/16/2015.
 */
@ParseClassName("Tags")
public class Tags extends ParseObject {

    public JSONArray getSoundTags() {
        return getJSONArray("soundTags");
    }

    public void setSoundTags(List<String> array) {
        addAllUnique("soundTags", array);
    }

    public JSONArray getCategoryTags() {
        return getJSONArray("categoryTags");
    }

    public void setCategoryTags(List<String> array) {
        addAllUnique("categoryTags", array);
    }
}
