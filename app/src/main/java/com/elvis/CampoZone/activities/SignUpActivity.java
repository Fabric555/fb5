package com.elvis.CampoZone.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity {

    boolean isClient;
    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpBtn, loginBtn;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase, usersRef;
    private static final String TAG = SignUpActivity.class.getSimpleName();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //get intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isClient = extras.getBoolean("isClient");
        }

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabase.child("users");
        //find views
        nameEditText = findViewById(R.id.name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        signUpBtn = findViewById(R.id.sign_up_btn);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);

        //set onclicklisteners
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nullFields()) {
                    popSnackBar("Please fill out all the sections");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
                    popSnackBar("Invalid Email Address!");
                } else if (passwordEditText.getText().toString().length() < 6
                        || confirmPasswordEditText.getText().toString().length() < 6) {
                    popSnackBar("Password should be atleast six characters");
                } else if (!passwordEditText.getText().toString()
                        .equals(confirmPasswordEditText.getText().toString())) {
                    popSnackBar("The passwords do not match");
                } else {
                    String email = emailEditText.getText().toString();
                    //check if user already exists in the database
                    usersRef.orderByChild("email").equalTo(email)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        popSnackBar("Email already exists!");
                                    } else {
                                        //sign up the user
                                        signUp();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getMessage());
                                }
                            });

                }
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start login activity
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //check to see if all the sections have been filled
    private boolean nullFields() {
        return TextUtils.isEmpty(nameEditText.getText()) || TextUtils.isEmpty(emailEditText.getText())
                || TextUtils.isEmpty(passwordEditText.getText())
                || TextUtils.isEmpty(confirmPasswordEditText.getText());
    }

    private void popSnackBar(String message) {
        Snackbar.make(findViewById(R.id.sign_up_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void signUp() {
        progressBar.setVisibility(View.VISIBLE);
        signUpBtn.setEnabled(false);
        loginBtn.setEnabled(false);
        String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveToFB(user, password);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            popSnackBar("Authentication failed.");
                            progressBar.setVisibility(View.GONE);
                            signUpBtn.setEnabled(true);
                            loginBtn.setEnabled(true);
                        }
                    }
                });
    }

    private void saveToFB(FirebaseUser user, String password) {
        //update the username to firebase
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEditText.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });

        String img = "";
        if (user.getPhotoUrl() != null) {
            img = user.getPhotoUrl().toString();
        }
        UserData userData = new UserData(
                user.getUid(), nameEditText.getText().toString(), user.getEmail(), password, img, isClient
        );
        //saves user info to firebase database
        usersRef.child(user.getUid()).setValue(userData);
        if (isClient) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            //open venue activity to input venue info
            Intent i = new Intent(this, VenueActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

    }
}
