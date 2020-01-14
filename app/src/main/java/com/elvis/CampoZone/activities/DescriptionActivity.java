package com.elvis.CampoZone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.UserData;
import com.elvis.CampoZone.data.VenueData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DescriptionActivity extends AppCompatActivity {

    ImageView img;
    TextView name, owner, price, email, phone, location, description;
    DatabaseReference mDataBaseReference;
    public static final String FB_VENUES_PATH = "venues";
    Toolbar toolbar;
    String uid, venueName, key;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    boolean isClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        //initialize firebase
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //find views
        img = findViewById(R.id.img);
        name = findViewById(R.id.name);
        owner = findViewById(R.id.owner);
        price = findViewById(R.id.price);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        toolbar = findViewById(R.id.toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //enable up navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //obtain key from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            key = extras.getString("key");
        }

        queryClientStatus();
        queryVenueDetails();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isClient) {
                    popSnackBar("Lectures are prohibited from making appointments");
                } else {
                    //open booking activity
                    Intent intent = new Intent(DescriptionActivity.this, BookingActivity.class);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                }
            }
        });
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open images activity tto view all of the venue images
                Intent intent = new Intent(DescriptionActivity.this, ImagesActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("name", venueName);
                startActivity(intent);
            }
        });
    }

    private void queryClientStatus() {
        mDataBaseReference.child("users").child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            isClient = userData.isClient();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void queryVenueDetails(){
        mDataBaseReference.child(FB_VENUES_PATH).child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        VenueData venueData = dataSnapshot.getValue(VenueData.class);
                        if (venueData != null) {
                            Glide.with(DescriptionActivity.this)
                                    .load(venueData.getImg())
                                    .into(img);
                            venueName = venueData.getName();
                            name.setText(venueName);
                            owner.setText(venueData.getOwner());
                            price.setText(venueData.getPrice());
                            email.setText(venueData.getEmail());
                            phone.setText(venueData.getNumber());
                            location.setText(venueData.getLocation());
                            description.setText(venueData.getDescription());
                            uid = venueData.getUid();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void popSnackBar(String message) {
        Snackbar.make(findViewById(R.id.coordinator),
                message, Snackbar.LENGTH_SHORT).show();
    }

}
