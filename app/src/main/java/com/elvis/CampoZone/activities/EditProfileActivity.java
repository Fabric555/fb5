package com.elvis.CampoZone.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.UserData;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageView backImg, profileImg;
    Button changeImgBtn, saveChangesBtn;
    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference mDataBaseReference, usersRef;
    StorageReference mStorageRef;
    public static final String FB_USERS_PATH = "users";
    public static final String FB_IMAGE_PATH = "images/";
    private static final int REQUEST_CODE = 1;
    private static final int MY_PERMISSION_REQUEST = 1;
    Uri newImgUri;
    private static final String TAG = EditProfileActivity.class.getSimpleName();
    Uri downloadUri;
    String oldEmail, oldPassword, img;
    boolean isClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        usersRef = mDataBaseReference.child("users").child(currentUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //find views
        backImg = findViewById(R.id.back_img);
        profileImg = findViewById(R.id.profile_img);
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        changeImgBtn = findViewById(R.id.change_img_btn);
        saveChangesBtn = findViewById(R.id.save_changes_btn);
        progressBar = findViewById(R.id.progress_bar);

        //retrieve userinfo
        queryUserInfo();

        //setonclicklisteners
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permissions
                String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"};
                if (!hasPermissions(EditProfileActivity.this, permissions)) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this, permissions,
                            MY_PERMISSION_REQUEST);
                } else {
                    chooseImg();
                }
            }
        });
        changeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check permissions
                String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE"};
                if (!hasPermissions(EditProfileActivity.this, permissions)) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this, permissions,
                            MY_PERMISSION_REQUEST);
                } else {
                    chooseImg();
                }
            }
        });
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nullFields()) {
                    popSnackBar("Please fill out all the sections");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
                    popSnackBar("Invalid Email Address!");
                } else if (!passwordEditText.getText().toString()
                        .equals(confirmPasswordEditText.getText().toString())) {
                    popSnackBar("The passwords do not match");
                } else if (newImgUri != null) {
                    // Create a reference to the file to delete
                    StorageReference desertRef = mStorageRef.child("images/").child(currentUser.getUid());

                // Delete previous file
                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                    uploadImg();
                } else {
                    saveChanges();
                }
            }
        });

    }

    private void queryUserInfo() {
        mDataBaseReference.child(FB_USERS_PATH).child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            if (userData.getImg().equals("") || userData.getImg() == null) {
                                Glide.with(EditProfileActivity.this)
                                        .load(R.drawable.ic_add_a_photo_black_24dp)
                                        .into(profileImg);
                            } else {
                                Glide.with(EditProfileActivity.this)
                                        .load(userData.getImg())
                                        .into(profileImg);
                            }
                            oldEmail = userData.getEmail();
                            oldPassword = userData.getPassword();
                            nameEditText.setText(userData.getName());
                            emailEditText.setText(oldEmail);
                            passwordEditText.setText(oldPassword);
                            confirmPasswordEditText.setText(oldPassword);
                            isClient = userData.isClient();
                            img = userData.getImg();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            newImgUri = data.getData();
            new BitmapAsyncTask().execute(newImgUri);
        }
    }

    //background task to display the image selected
    private class BitmapAsyncTask extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Uri... uris) {
            Uri imgUri = uris[0];
            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver()
                        .openFileDescriptor(imgUri, "r");
                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    Bitmap image = decodeSampledBitmapFromFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();
                    return image;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Glide.with(EditProfileActivity.this)
                        .load(bitmap)
                        .into(profileImg);
            }
        }
    }

    //reduce size of image selected
    private static int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > 120 || width > 120) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= 120
                    && (halfWidth / inSampleSize) >= 120) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //obtain bitmap for the image selected
    private static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
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
        return TextUtils.isEmpty(nameEditText.getText()) || TextUtils.isEmpty(emailEditText.getText())
                || TextUtils.isEmpty(passwordEditText.getText())
                || TextUtils.isEmpty(confirmPasswordEditText.getText());
    }

    private void popSnackBar(String message) {
        Snackbar.make(findViewById(R.id.edit_profile_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void uploadImg() {
        progressBar.setVisibility(View.VISIBLE);
        changeImgBtn.setEnabled(false);
        profileImg.setEnabled(false);
        saveChangesBtn.setEnabled(false);
        //upload image to firebase storage
        final UploadTask uploadTask = mStorageRef.child(FB_IMAGE_PATH)
                .child(currentUser.getUid()).putFile(newImgUri);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.upload_image_dialog, null);
        final ProgressBar uploadPicProgressBar = v.findViewById(R.id.upload_pic_progress_bar);
        final TextView uploadPicProgressPercentage = v.findViewById(R.id.upload_pic_progress_percentage);
        builder.setView(v);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                uploadTask.cancel();
                dialogInterface.cancel();
                progressBar.setVisibility(View.GONE);
                changeImgBtn.setEnabled(true);
                profileImg.setEnabled(true);
                saveChangesBtn.setEnabled(true);
            }
        });
        builder.show();

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final StorageReference ref = mStorageRef.child(FB_IMAGE_PATH).child(currentUser.getUid());

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
                            downloadUri = task.getResult();
                            saveChanges();
                        } else if (task.getException() != null){
                            // Handle failures
                            Log.e(TAG, task.getException().getMessage());
                            progressBar.setVisibility(View.GONE);
                            changeImgBtn.setEnabled(true);
                            profileImg.setEnabled(true);
                            saveChangesBtn.setEnabled(true);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) { popSnackBar(e.getMessage()); }
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

    private void saveChanges(){
        progressBar.setVisibility(View.VISIBLE);
        changeImgBtn.setEnabled(false);
        profileImg.setEnabled(false);
        saveChangesBtn.setEnabled(false);
        //update user profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEditText.getText().toString())
                .build();

        if (downloadUri != null) {
            //update user profile
            profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nameEditText.getText().toString())
                    .setPhotoUri(downloadUri)
                    .build();
        }

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            // Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(oldEmail, oldPassword);

// Prompt the user to re-provide their sign-in credentials
                            currentUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "User re-authenticated.");
                                            currentUser.updateEmail(emailEditText.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "User email address updated.");
                                                                currentUser.updatePassword(passwordEditText.getText().toString())
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Log.d(TAG, "User password updated.");
                                                                                    if (downloadUri != null) {
                                                                                        img = downloadUri.toString();
                                                                                    }
                                                                                    //upload user data to firebase database
                                                                                    UserData userData = new UserData(
                                                                                            currentUser.getUid(), nameEditText.getText().toString(),
                                                                                            emailEditText.getText().toString(), passwordEditText.getText().toString(),
                                                                                            img, isClient
                                                                                    );

                                                                                    usersRef.setValue(userData);
                                                                                    Toast.makeText(EditProfileActivity.this, "Changes successfully made",
                                                                                            Toast.LENGTH_LONG).show();
                                                                                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(intent);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }
}
