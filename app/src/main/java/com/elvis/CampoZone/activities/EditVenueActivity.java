package com.elvis.CampoZone.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.adapters.EditUriAdapter;
import com.elvis.CampoZone.data.ImageData;
import com.elvis.CampoZone.data.VenueData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class EditVenueActivity extends AppCompatActivity {

    ImageView backImg;
    Button selectImgsBtn, saveBtn;
    EditText nameEditText, locationEditText, numberEditText, priceEditText, descriptionEditText;
    private static final int REQUEST_CODE = 1;
    private static final int MY_PERMISSION_REQUEST = 1;
    DatabaseReference mDatabaseRef, venuesRef, imagesRef;
    StorageReference mStorageRef;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public static final String FB_IMAGE_PATH = "images/";
    public static final String FB_IMG_PATH = "images";
    private static final String TAG = VenueActivity.class.getSimpleName();
    ProgressBar progressBar;
    ImageView emptyImg;
    public static final ArrayList<ImageData> URI_DATA = new ArrayList<>();
    public static final ArrayList<ImageData> DELETE_URLS_LIST = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    public static RecyclerView.Adapter recyclerViewAdapter;
    public static String firstImg;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_venue);

        //initialize firebase
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        venuesRef = mDatabaseRef.child("venues");
        mStorageRef = FirebaseStorage.getInstance().getReference().child(FB_IMAGE_PATH);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        imagesRef = mDatabaseRef.child(FB_IMG_PATH).child(currentUser.getUid());

        //find views
        backImg = findViewById(R.id.back_img);
        selectImgsBtn = findViewById(R.id.select_imgs_btn);
        saveBtn = findViewById(R.id.save_btn);
        nameEditText = findViewById(R.id.name_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);
        numberEditText = findViewById(R.id.number_edit_text);
        priceEditText = findViewById(R.id.price_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        progressBar = findViewById(R.id.progress_bar);
        emptyImg = findViewById(R.id.empty_img);
        recyclerView = findViewById(R.id.recycler_view);

        //setup recyclerview
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(layoutManager);

        //clear delete urls list and uri list incase there are any previously stored data
        DELETE_URLS_LIST.clear();
        URI_DATA.clear();

        //set onclicklisteners
        selectImgsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permissions
                String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"};
                if (!hasPermissions(EditVenueActivity.this, permissions)) {
                    ActivityCompat.requestPermissions(EditVenueActivity.this, permissions,
                            MY_PERMISSION_REQUEST);
                } else {
                    chooseImg();
                }
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nullFields()) {
                    popSnackBar("Please fill out all the sections");
                } else if (URI_DATA.isEmpty()){
                    popSnackBar("Please select an image for the venue");
                } else {
                    uploadImg(0);
                }
            }
        });
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        queryVenueDetails();
        queryVenueImages();
    }

    private void queryVenueDetails() {
        venuesRef.orderByChild("uid").equalTo(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapShot: dataSnapshot.getChildren()) {
                            VenueData venueData = dataSnapShot.getValue(VenueData.class);
                            if (venueData != null) {
                                nameEditText.setText(venueData.getName());
                                locationEditText.setText(venueData.getLocation());
                                numberEditText.setText(venueData.getNumber());
                                priceEditText.setText(venueData.getPrice());
                                descriptionEditText.setText(venueData.getDescription());
                                key = venueData.getKey();
                                firstImg = venueData.getImg();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void queryVenueImages() {
        URI_DATA.clear();
        imagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapShot: dataSnapshot.getChildren()) {
                    ImageData imageData = dataSnapShot.getValue(ImageData.class);
                    if (imageData != null) {
                        URI_DATA.add(imageData);
                    }
                }
                if (!URI_DATA.isEmpty()) {
                    emptyImg.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerViewAdapter = new EditUriAdapter(URI_DATA);
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

    private void chooseImg(){
        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            /*
             * Get the file's content URI from the incoming Intent,
             * then query the server app to get the file's display name
             * and size.
             */
            Uri returnUri = data.getData();
            Cursor returnCursor =
                    getContentResolver().query(returnUri, null, null,
                            null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            if (returnCursor != null) {
                returnCursor.moveToFirst();
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String name = returnCursor.getString(nameIndex);
                String img = returnUri.toString();
                ImageData imageData = new ImageData(name, img);
                URI_DATA.add(imageData);
                returnCursor.close();
                //update recyclerview with the new image selected
                emptyImg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewAdapter = new EditUriAdapter(URI_DATA);
                recyclerView.setAdapter(recyclerViewAdapter);
            }
        }
    }

    //check if the app has been granted permission to access photos
    public boolean hasPermissions(Context context, String... permissions) {
        if (!(Build.VERSION.SDK_INT < 23 || context == null || permissions == null)) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    //check to see if all the sections have been filled
    private boolean nullFields() {
        return TextUtils.isEmpty(nameEditText.getText()) || TextUtils.isEmpty(locationEditText.getText())
                || TextUtils.isEmpty(numberEditText.getText())
                || TextUtils.isEmpty(priceEditText.getText())
                || TextUtils.isEmpty(descriptionEditText.getText());
    }

    private void popSnackBar(String message) {
        Snackbar.make(findViewById(R.id.venue_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void uploadImg(int i) {
        final int pos = i;
        String img = URI_DATA.get(pos).getImg();
        //check to see if it starts with https then skip but if its the last item move to savevenue
        if (img.startsWith("https")) {
            //Check to see if pos is the last img count in the list then savevenue
            if (pos < URI_DATA.size() - 1) {
                uploadImg(pos + 1);
            } else {
                saveVenue();
            }
        } else {
            Uri uri = Uri.parse(img);
            final String name = URI_DATA.get(pos).getName();
            progressBar.setVisibility(View.VISIBLE);
            selectImgsBtn.setEnabled(false);
            saveBtn.setEnabled(false);
            //upload image to firebase storage
            final UploadTask uploadTask = mStorageRef.child(name).putFile(uri);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams")
            View v = inflater.inflate(R.layout.upload_image_dialog, null);
            final ProgressBar uploadPicProgressBar = v.findViewById(R.id.upload_pic_progress_bar);
            final TextView uploadPicProgressPercentage = v.findViewById(R.id.upload_pic_progress_percentage);
            final TextView uploadPicTitle = v.findViewById(R.id.upload_pic_title);
            int posCount = pos + 1;
            uploadPicTitle.setText("Uploading image " + String.valueOf(posCount) + " of "
                    + String.valueOf(URI_DATA.size()));
            builder.setView(v);

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    uploadTask.cancel();
                    dialogInterface.cancel();
                    progressBar.setVisibility(View.GONE);
                    selectImgsBtn.setEnabled(true);
                    saveBtn.setEnabled(true);
                }
            });
            builder.setCancelable(false);
            builder.show();

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final StorageReference ref = mStorageRef.child(name);

                    popSnackBar("Please wait...");

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful() && task.getException() != null) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                String key = imagesRef.push().getKey();
                                if (downloadUri != null && key != null) {
                                    //Save image to firebase under images node
                                    ImageData imageData = new ImageData(name, downloadUri.toString());
                                    imagesRef.child(key).setValue(imageData);
                                    //If firstimage is empty, set uploaded image as first image
                                    //Save the first image to be displayed in the venue list
                                    if (firstImg.equals("")) {
                                        firstImg = downloadUri.toString();
                                    }
                                    //Check to see if pos is the last img count in the list then savevenue
                                    if (pos < URI_DATA.size() - 1) {
                                        uploadImg(pos + 1);
                                    } else {
                                        saveVenue();
                                    }
                                }

                            } else if (task.getException() != null){
                                // Handle failures
                                Log.e(TAG, task.getException().getMessage());
                                popSnackBar("An error occured");
                                progressBar.setVisibility(View.GONE);
                                selectImgsBtn.setEnabled(true);
                                saveBtn.setEnabled(true);
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            popSnackBar(e.getMessage());
                                            progressBar.setVisibility(View.GONE);
                                            selectImgsBtn.setEnabled(true);
                                            saveBtn.setEnabled(true);
                                        }
                                    }
            ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    long currentProgress = (100 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();
                    int progress = (int) currentProgress;
                    String progressPercentage = String.valueOf(progress) + "%";
                    uploadPicProgressBar.setProgress(progress);
                    uploadPicProgressPercentage.setText(progressPercentage);
                }
            });
        }
    }

    private void saveVenue(){
        if (!DELETE_URLS_LIST.isEmpty()) {
            //Delete urls first then save data
            for (final ImageData imageData: DELETE_URLS_LIST) {
                final String img = imageData.getImg();
                String name = imageData.getName();
                mStorageRef.child(name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        imagesRef.orderByChild("img").equalTo(img)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapShot: dataSnapshot.getChildren()) {
                                            String key = dataSnapShot.getKey();
                                            if (key != null) {
                                                imagesRef.child(key).removeValue();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                    }
                });
            }
        }

        //upload venue data to firebase database
        VenueData venueData = new VenueData(
                currentUser.getUid(), nameEditText.getText().toString(),
                currentUser.getDisplayName(), locationEditText.getText().toString(),
                priceEditText.getText().toString(), currentUser.getEmail(),
                numberEditText.getText().toString(), firstImg, key,
                descriptionEditText.getText().toString()
        );
        venuesRef.child(key).setValue(venueData);
        finish();
    }

}
