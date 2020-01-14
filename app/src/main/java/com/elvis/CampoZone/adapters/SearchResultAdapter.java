package com.elvis.CampoZone.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.activities.DescriptionActivity;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.VenueData;

import java.util.ArrayList;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private static ArrayList<VenueData> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {
        Context ctx;
        TextView mLocationView;
        CardView mCardView;
        ImageView mImageView;
        TextView mNameView;
        int pos;

        ViewHolder(View v) {
            super(v);
            mNameView = v.findViewById(R.id.name_view_vertical);
            mLocationView = v.findViewById(R.id.location_view_vertical);
            mImageView = v.findViewById(R.id.img_view_vertical);
            mCardView = v.findViewById(R.id.list_card_chosen);
            ctx = v.getContext();
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pos = getAdapterPosition();
                    String key = mDataset.get(pos).getKey();
                    Intent intent = new Intent(ctx, DescriptionActivity.class);
                    intent.putExtra("key", key);
                    ctx.startActivity(intent);
                }
            });
        }
    }

    public SearchResultAdapter(ArrayList<VenueData> myDataset) {
        mDataset = myDataset;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load((mDataset.get(position)).getImg())
                .into(holder.mImageView);
        holder.mNameView.setText((mDataset.get(position)).getName());
        holder.mLocationView.setText((mDataset.get(position)).getLocation());
    }

    public int getItemCount() {
        return mDataset.size();
    }
}
