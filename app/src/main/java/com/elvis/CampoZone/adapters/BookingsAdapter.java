package com.elvis.CampoZone.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.BookingData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {
    private static ArrayList<BookingData> mDataset;
    private boolean isClient;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView clientName, clientEmail, clientDate, clientTime, clientDescription;
        TextView bookingStatus;
        Button declineBtn, acceptBtn;
        LinearLayout responseLayout;
        DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        ViewHolder(View v) {
            super(v);
            //find views
            clientName = v.findViewById(R.id.client_name);
            clientEmail = v.findViewById(R.id.client_email);
            clientDate = v.findViewById(R.id.client_date);
            clientTime = v.findViewById(R.id.client_time);
            clientDescription = v.findViewById(R.id.client_description);
            bookingStatus = v.findViewById(R.id.booking_status);
            declineBtn = v.findViewById(R.id.decline_btn);
            acceptBtn = v.findViewById(R.id.accept_btn);
            responseLayout = v.findViewById(R.id.response_layout);

            //setonclicklisteners
            declineBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Set status as declined
                    int pos = getAdapterPosition();
                    String key = mDataset.get(pos).getKey();
                    mDataBaseReference.child("bookings").child(key).child("status").setValue("Declined");
                }
            });
            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Set status as accepted
                    int pos = getAdapterPosition();
                    String key = mDataset.get(pos).getKey();
                    mDataBaseReference.child("bookings").child(key).child("status").setValue("Accepted");
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public BookingsAdapter(ArrayList<BookingData> myDataset, boolean isClient) {
        mDataset = myDataset;
        this.isClient = isClient;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        // create a new view
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bookings_item, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        BookingData bookingData = mDataset.get(position);
        holder.clientName.setText(bookingData.getName());
        holder.clientEmail.setText(bookingData.getEmail());
        holder.clientDate.setText(bookingData.getDate());
        holder.clientTime.setText(bookingData.getTime());
        holder.clientDescription.setText(bookingData.getDescription());
        holder.bookingStatus.setText(bookingData.getStatus());
        if (isClient) {
            holder.responseLayout.setVisibility(View.GONE);
        } else {
            holder.responseLayout.setVisibility(View.VISIBLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

