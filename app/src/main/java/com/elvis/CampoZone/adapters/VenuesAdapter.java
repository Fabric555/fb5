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

public class VenuesAdapter extends RecyclerView.Adapter<VenuesAdapter.ViewHolder> {
    private static ArrayList<VenueData> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView img;
        TextView name, owner, location;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            //find views
            img = v.findViewById(R.id.img);
            name = v.findViewById(R.id.name);
            owner = v.findViewById(R.id.owner);
            location = v.findViewById(R.id.location);
            cardView = v.findViewById(R.id.card_view);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context ctx = v.getContext();
                    String key = mDataset.get(getAdapterPosition()).getKey();
                    Intent intent = new Intent(ctx, DescriptionActivity.class);
                    intent.putExtra("key", key);
                    ctx.startActivity(intent);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public VenuesAdapter(ArrayList<VenueData> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public VenuesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        // create a new view
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.venue_item, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Context ctx = holder.itemView.getContext();
        Glide.with(ctx)
                .load(mDataset.get(position).getImg())
                .into(holder.img);
        holder.name.setText(mDataset.get(position).getName());
        holder.owner.setText(mDataset.get(position).getOwner());
        holder.location.setText(mDataset.get(position).getLocation());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
