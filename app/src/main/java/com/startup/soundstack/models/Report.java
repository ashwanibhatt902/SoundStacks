package com.startup.soundstack.models;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by harsh on 10/16/2015.
 */

@ParseClassName("Report")
public class Report extends ParseObject{

    public static String COLUMN_REPORTED_SOUND= "reportedSound";
    public static String COLUMN_REASON_DETAIL = "Reason_detail";
    public static String COLUMN_REASON = "reason";
    public static String COLUMN_REPORT_BY_USER = "byUser";

    public void setByUser() {
        put(COLUMN_REPORT_BY_USER, ParseUser.getCurrentUser());
    }
    public void setSoundID(SoundItem item) {
        put(COLUMN_REPORTED_SOUND, item);
    }

    public void setColumnReasonDetail(String reasonDetail) {
        put(COLUMN_REASON_DETAIL, reasonDetail);
    }

    public void setColumnReason(String reasonDetail) {
        put(COLUMN_REASON, reasonDetail);
    }





}
