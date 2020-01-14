package com.elvis.CampoZone.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.adapters.BookingsAdapter;
import com.elvis.CampoZone.data.BookingData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class BookingsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter recyclerViewAdapter;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference mDataBaseReference, bookingsRef;
    public static final ArrayList<BookingData> BOOKINGS_DATA = new ArrayList<>();
    boolean isClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        setTitle("Bookings");


        //retrieve isClient boolean
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isClient = extras.getBoolean("isClient");
        }

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        bookingsRef = mDataBaseReference.child("bookings");

        //find views
        recyclerView = findViewById(R.id.recycler_view);

        //setup recyclerview
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (isClient) {
            //If client match email and current user email
            queryClientBookings();
        } else {
            //If venue owner match uid with their uid
            queryVenueOwnerBookings();
        }
    }

    private void queryVenueOwnerBookings() {
        BOOKINGS_DATA.clear();
        bookingsRef.orderByChild("uid").equalTo(currentUser.getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        BookingData bookingData = dataSnapshot.getValue(BookingData.class);
                        if (bookingData != null) {
                            BOOKINGS_DATA.add(bookingData);
                            recyclerViewAdapter = new BookingsAdapter(BOOKINGS_DATA, isClient);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        BookingData bookingData = dataSnapshot.getValue(BookingData.class);
                        for (int i = 0; i < BOOKINGS_DATA.size(); i++) {
                            if (BOOKINGS_DATA.get(i).getKey().equals(dataSnapshot.getKey())) {
                                BOOKINGS_DATA.remove(i);
                                BOOKINGS_DATA.add(i, bookingData);
                                recyclerViewAdapter.notifyItemChanged(i);
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void queryClientBookings() {
        BOOKINGS_DATA.clear();
        bookingsRef.orderByChild("email").equalTo(currentUser.getEmail())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        BookingData bookingData = dataSnapshot.getValue(BookingData.class);
                        if (bookingData != null) {
                            BOOKINGS_DATA.add(bookingData);
                            recyclerViewAdapter = new BookingsAdapter(BOOKINGS_DATA, isClient);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        BookingData bookingData = dataSnapshot.getValue(BookingData.class);
                        for (int i = 0; i < BOOKINGS_DATA.size(); i++) {
                            if (BOOKINGS_DATA.get(i).getKey().equals(dataSnapshot.getKey())) {
                                BOOKINGS_DATA.remove(i);
                                BOOKINGS_DATA.add(i, bookingData);
                                recyclerViewAdapter.notifyItemChanged(i);
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
