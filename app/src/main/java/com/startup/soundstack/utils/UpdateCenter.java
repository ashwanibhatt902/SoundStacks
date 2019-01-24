package com.startup.soundstack.utils;

import com.squareup.otto.Bus;
import com.startup.soundstack.models.Category;
import com.startup.soundstack.models.Report;
import com.startup.soundstack.models.SoundItem;

/**
 * Created by harsh on 8/23/2015.
 */
public class UpdateCenter {

    private static Bus sEventBus = null;

    static  {
        sEventBus = new Bus();
    }

    public static Bus getEventBus () {
        return sEventBus;
    }

    public static  UpdateEvent generateUpdateEvent(Object sender, EventType type) {
        return new UpdateEvent(sender, type);
    }

    public static  CreateCategoryEvent generateCategoryEvent (Object sender, Category type) {
        return new CreateCategoryEvent(sender, type);
    }

    public static void postUpdateEvent(Object sender, EventType type) {
        sEventBus.post(generateUpdateEvent(sender, type));
    }
    public static void postNewCategoryEvent(Object sender, Category type) {
        sEventBus.post(generateCategoryEvent(sender, type));
    }

    public static void postNewSoundEvent(Object sender, SoundItem type) {
        sEventBus.post(new CreateSoundEvent(sender, type));
    }

    public static void postReportEvent(Object sender, Report report) {
        sEventBus.post(new ReportEvent(sender, report));
    }
    public static class UpdateEvent {

        UpdateEvent(Object sender, EventType type) {
            mSender = sender;
            mType = type;
        }
       public  Object mSender;
       public EventType mType;
    }

    public static class CreateCategoryEvent {
        private final Object mSender;
        public final Category mCategory;
        public boolean mHandled = false;

        CreateCategoryEvent(Object sender, Category category) {
            mSender = sender;
            mCategory = category;
        }
    }
    public static class CreateSoundEvent {
        private final Object mSender;
        public final SoundItem mSound;
        public boolean mHandled = false;

        CreateSoundEvent(Object sender, SoundItem category) {
            mSender = sender;
            mSound = category;
        }
    }

    public static class ReportEvent {
        private final Object mSender;
        public Report mReport = null;

        ReportEvent(Object sender, Report report) {
            mSender = sender;
            mReport = report;
        }
    }


    public enum  EventType{
        Session,
        LikeDislike
    }
}
