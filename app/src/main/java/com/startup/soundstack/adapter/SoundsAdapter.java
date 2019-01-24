package com.startup.soundstack.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.internal.BoltsMeasurementEventListener;
import com.startup.soundstack.R;
import com.startup.soundstack.activities.MainActivity;
import com.startup.soundstack.customclass.CustomCursorAdapter;
import com.startup.soundstack.utils.Constants;

/**
 * Created by Dheeraj on 9/1/2015.
 */
public class SoundsAdapter extends CustomCursorAdapter<SoundsAdapter.MyHolder> {
    Context mContext;
    public boolean showCategory = true;

    static class MyHolder extends RecyclerView.ViewHolder{
        TextView txtTitle;
        TextView txtArtist;
        CardView parentLayout;
        public MyHolder(View itemView) {
            super(itemView);
            txtTitle = (TextView)itemView.findViewById(R.id.row_title);
            txtArtist = (TextView) itemView.findViewById(R.id.row_artist);
            parentLayout = (CardView) itemView.findViewById(R.id.music_item);
        }
    }

    public SoundsAdapter(Context context,Cursor cursor){
        super(cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolderCursor(SoundsAdapter.MyHolder holder,final Cursor cursor) {
        int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        holder.txtArtist.setText(cursor.getString(artistIndex));
        holder.txtTitle.setText(cursor.getString(titleIndex));
        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        final String filename = cursor.getString(dataIndex);
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCropActivity(filename);
            }
        });
    }

    @Override
    public SoundsAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.media_select_row, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    private void startCropActivity(String filename) {
        try {
            Intent intent = new Intent(mContext, MainActivity.class);
            Uri uri = Uri.parse(filename);
            intent.putExtra(Constants.FILENAME, uri.toString());
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.OPEN_CATEGORY_AFTER_UPLOAD, showCategory);
            intent.putExtra(Constants.OPEN_CATEGORY_AFTER_UPLOAD, bundle);

            mContext.startActivity(intent);

            ((Activity)mContext).finish();
        } catch (Exception e) {
            Log.e("Ringdroid", "Couldn't start editor");
        }
    }

}
