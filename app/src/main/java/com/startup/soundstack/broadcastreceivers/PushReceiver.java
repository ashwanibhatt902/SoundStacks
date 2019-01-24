package com.startup.soundstack.broadcastreceivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.parse.ParsePushBroadcastReceiver;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Created by Dheeraj on 11/11/2015.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String message = "", title = "";
        String ss = intent.getExtras().toString();
        Set<String> qq = intent.getExtras().keySet();
        String data = intent.getStringExtra("com.parse.Data");
        try{
            JSONObject jsonData = new JSONObject(data);
            if(jsonData.has("message")){
                message = jsonData.getString("message");
            }
            if(jsonData.has("title")){
                title = jsonData.getString("title");
            }
            if(jsonData.has("bigImage")){
                new sendNotification(context)
                        .execute(jsonData.getString("bigImage"), message, title);
            }else{
                showNotification(context, title, message, null);
            }
        }catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    class sendNotification extends AsyncTask<String, Void, Bitmap> {

        Context ctx;
        String message;
        String title;

        public sendNotification(Context context) {
            super();
            this.ctx = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {

                in = new URL(params[0]).openStream();
                message = params[1];
                title = params[2];
                Bitmap bmp = BitmapFactory.decodeStream(in);
                return bmp;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            try {
                showNotification(ctx, title, message, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showNotification(Context context, String title, String message,Bitmap bitmap){
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notBuilder =  new NotificationCompat.Builder(context)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.icon)
                .setSound(uri)
                .setContentIntent(pIntent);

        if(bitmap != null) {
            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle().bigPicture(bitmap);
            if (message != null) {
                style.setSummaryText(message);
            }
            notBuilder.setStyle(style);
        }else{
            notBuilder.setContentText(message);
        }

        if(title != null){
            notBuilder.setContentTitle(title);
        }

        Notification notification = notBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }
}


