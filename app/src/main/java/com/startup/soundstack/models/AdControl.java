package com.startup.soundstack.models;

/**
 * Created by Dheeraj on 12/17/2015.
 */
public class AdControl{
    boolean enabled = false;
    boolean category_enabled = false;
    boolean sound_enabled = false;
    int row_count = 4;

    public int getRow_count() {
        return row_count;
    }

    public void setRow_count(int row_count) {
        this.row_count = row_count;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCategory_enabled() {
        return category_enabled;
    }

    public void setCategory_enabled(boolean category_enabled) {
        this.category_enabled = category_enabled;
    }

    public boolean isSound_enabled() {
        return sound_enabled;
    }

    public void setSound_enabled(boolean sound_enabled) {
        this.sound_enabled = sound_enabled;
    }
}
