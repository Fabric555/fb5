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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button signUpBtn, loginBtn;
    FirebaseAuth mAuth;
    private static final String TAG = LoginActivity.class.getSimpleName();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();

        //find views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        signUpBtn = findViewById(R.id.sign_up_btn);
        loginBtn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_bar);

        //set onclicklisteners
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nullFields()) {
                    popSnackBar("Please fill out all the sections");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
                    popSnackBar("Invalid Email Address!");
                } else {
                    //login user
                    login();
                }
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start login activity
                Intent intent = new Intent(LoginActivity.this, StartUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    //check to see if all the sections have been filled
    private boolean nullFields() {
        return TextUtils.isEmpty(emailEditText.getText())
                || TextUtils.isEmpty(passwordEditText.getText());
    }

    private void popSnackBar(String message) {
        Snackbar.make(findViewById(R.id.login_layout),
                message, Snackbar.LENGTH_SHORT).show();
    }

    private void login() {
        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);
        signUpBtn.setEnabled(false);
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            popSnackBar("Authentication failed.");
                            progressBar.setVisibility(View.GONE);
                            loginBtn.setEnabled(true);
                            signUpBtn.setEnabled(true);
                        }
                    }
                });
    }

}
