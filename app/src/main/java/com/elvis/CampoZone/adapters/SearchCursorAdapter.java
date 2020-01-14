package com.elvis.CampoZone.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.activities.MainActivity;
import com.elvis.CampoZone.utils.ResultContract;

public class SearchCursorAdapter extends CursorAdapter {
    public SearchCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.search_list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String img = cursor.getString(cursor.getColumnIndex(
                ResultContract.ResultEntry.SUGGEST_COLUMN_ICON_1));
        String location = cursor.getString(cursor.getColumnIndex(
                ResultContract.ResultEntry.SUGGEST_COLUMN_TEXT_2));
        TextView locationView = view.findViewById(R.id.location_view);
        ImageView imageView = view.findViewById(R.id.img_view);
        locationView.setText(location);
        Glide.with(context)
                .load(img)
                .into(imageView);

        String name = cursor.getString(cursor.getColumnIndex(
                ResultContract.ResultEntry.SUGGEST_COLUMN_TEXT_1));
        TextView nameView = view.findViewById(R.id.name_view);
        nameView.setText(name);
    }
}