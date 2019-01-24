package com.startup.soundstack.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

/**
 * Created by HKurra on 10/28/2015.
 */

@ParseClassName("AppExtensibleResource")
public class AppExtensibleResource extends ParseObject {

    public static String RESOURCE_ID = "ResourceID";
    public static String RESOURCE_FILE = "ResourceFile";
    public static String COLOR = "Color";

    public int getResourceID() {
        return getInt("ResourceID");
    }

    public ParseFile getResourceFile() {
        return getParseFile("ResourceFile");
    }

    public String getColor() {
        return getString(COLOR);
    }
}
