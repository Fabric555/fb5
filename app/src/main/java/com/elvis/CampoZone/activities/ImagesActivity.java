package com.elvis.CampoZone.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.adapters.ImagesAdapter;
import com.elvis.CampoZone.data.ImageData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ImagesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter recyclerViewAdapter;
    public static final String FB_IMAGES_PATH = "images";
    public static final ArrayList<ImageData> IMAGES_DATA = new ArrayList<>();
    DatabaseReference mDataBaseReference;
    String uid, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        //initialize firebase
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        //retrieve venue owner uid and venue name
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uid = extras.getString("uid");
            name = extras.getString("name");
        }

        setTitle(name + " Images");

        //find views
        recyclerView = findViewById(R.id.recycler_view);

        //setup recyclerview
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        queryImages();
    }

    private void queryImages() {
        mDataBaseReference.child(FB_IMAGES_PATH).child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        IMAGES_DATA.clear();
                        for (DataSnapshot dataSnapShot: dataSnapshot.getChildren()) {
                            ImageData imageData = dataSnapShot.getValue(ImageData.class);
                            if (imageData != null) {
                                IMAGES_DATA.add(imageData);
                            }
                        }
                        if (!IMAGES_DATA.isEmpty()) {
                            recyclerViewAdapter = new ImagesAdapter(IMAGES_DATA);
                            recyclerView.setAdapter(recyclerViewAdapter);
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
}
