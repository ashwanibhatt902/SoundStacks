package com.startup.soundstack.utils;

/**
 * Created by harsh on 8/12/2015.
 */
public class Constants {

    public static final String FILENAME = "fileName";
    public static final String INTERNEL_SOUND_DIR_NAME = "myfolder";
    public static final String OPEN_CATEGORY_AFTER_UPLOAD = "openCategory";
    public static final int DUMMY_USER_IMAGE_RESOURCE_ID = 2400;
    public static final int MAX_SOUNDS_CACHE_SIZE = 100;  //MB

    public static final String SOUND_UPLOAD_BROADCAST_ACTION =  "com.startup.soundstack.SOUND_UPLOAD_BROADCAST";
    public static final String SOUND_UPLOAD_STATUS =  "com.startup.soundstack.SOUND_UPLOAD_STATUS";
    public static final String SOUND_UPLOAD_PROGRESS =  "com.startup.soundstack.SOUND_UPLOAD_PROGRESS";

    public interface Preference {
        String SYSTEM_LOGIN_ID = "systemLoginID";
        String CATEGORY_SORT = "categorySort";
        String SOUND_SORT = "categorySort";
        String CATEGORY_IMAGES_RES_DOWNLOADED_ONCE = "catResDownloadedOnce";
        String CATEGORY_IMAGES_LAST_DOWNLOADED_TIME= "catImgLastDownloadedTime";
    }

    public interface PinningLabel {
        String ALL_TAGS = "allTags";
        String USER_CATEGORY = "userCategory";
        String CATEGORY_IMAGE = "categoryImage";
    }

    public interface UserProperty {
        String USER = "User";
        String PROFILE_PIC_URL = "ProfilePicURL";
        String NAME = "name";
        String EMAIL = "email";
        String EMAIL_ADD = "email_add";
        String DISLIKE_SOUND_ID = "dislikeSoundIds";
        String FAV_CATEGORIES_ID = "favCategoriesIds";
        String LIKE_SOUND_ID = "likeSoundIds";
        String FAV_SOUND_ID = "favSoundIds";
        String PROFILE_PIC_FILE = "profilePicFile";
        String PROFILE_PIC_FILE_LQ = "profilePicFileLQ";
        String COVER_PIC_FILE = "coverPicFile";
    }


}
