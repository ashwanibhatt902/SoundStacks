package com.startup.soundstack.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.parse.Parse;
import com.parse.ParseUser;
import com.startup.soundstack.activities.HomeActivity;
import com.startup.soundstack.fragments.Categories;
import com.startup.soundstack.fragments.PersonalFragment;
import com.startup.soundstack.fragments.Sounds;
import com.startup.soundstack.models.SoundItem;

import org.json.JSONArray;

import java.util.Arrays;

/**
 * Created by Dheeraj on 7/27/2015.
 */
public class MainActivityAdaptor extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    Class aClass;

    public MainActivityAdaptor(FragmentManager fm, int NumOfTabs, Class activityClass ) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.aClass = activityClass;
    }

    @Override
    public Fragment getItem(int position) {

        if (aClass.equals(HomeActivity.class)) {
            switch (position) {
                case 0:
                    Categories tab1 = new Categories();
                    return tab1;
                case 1:
                    Sounds tab2 = new Sounds();
                    return tab2;
                case 2:
                    PersonalFragment tab3 = new PersonalFragment();
                    return tab3;
                default:
                    return null;
            }
        } else {
            switch (position) {
                case 0:
                    Sounds tab1 = new Sounds();
                    return tab1;
                case 1:
                    Sounds tab2 = new Sounds();

                    if (ParseUser.getCurrentUser() != null) {
                        tab2.setQuery(SoundItem.COLUMN_UPLOADED_BY_USER, new JSONArray(Arrays.asList(ParseUser.getCurrentUser().getObjectId())));
                    }

                    else {
                        tab2.setQuery(SoundItem.COLUMN_UPLOADED_BY_USER, null);
                    }
                    return tab2;
                case 2:
                    PersonalFragment tab3 = new PersonalFragment();
                    return tab3;
                default:
                    return null;
            }
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
