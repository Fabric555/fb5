package com.elvis.CampoZone.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.data.BookingData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {

    ImageView backImg;
    public static String[] monthName = { "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December" };
    CardView dateCardView, timeCardView;
    public static TextView dateTextView, timeTextView;
    EditText descriptionEditText;
    public static String time, date;
    Button bookVenueBtn;
    String uid;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference mDatabaseRef;
    public static final String FB_BOOKINGS_PATH = "bookings";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        //find views
        backImg = findViewById(R.id.back_img);
        dateCardView = findViewById(R.id.date_card_view);
        timeCardView = findViewById(R.id.time_card_view);
        dateTextView = findViewById(R.id.date_text_view);
        timeTextView = findViewById(R.id.time_text_view);
        bookVenueBtn = findViewById(R.id.book_venue_btn);
        descriptionEditText = findViewById(R.id.description_edit_text);
        progressBar = findViewById(R.id.progress_bar);

        //retrieve venue owner uid
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uid = extras.getString("uid");
        }

        //set onclicklisteners
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        timeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
        dateCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        bookVenueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //book venue
                bookVenue();
            }
        });

        setCurrentTimeAndDate();
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hr;
            String min;
            if (hourOfDay < 10) {
                hr = "0" + String.valueOf(hourOfDay);
            } else {
                hr = String.valueOf(hourOfDay);
            }
            if (minute < 10) {
                min = "0" + String.valueOf(minute);
            } else {
                min = String.valueOf(minute);
            }
            // Do something with the time chosen by the user
            time = hr + ":" + min;
            timeTextView.setText(time);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int mon, int dy) {
            // Do something with the date chosen by the user
            date = String.valueOf(dy) + " " + monthName[mon] + " "
                    + String.valueOf(year);
            dateTextView.setText(date);
        }
    }

    private void setCurrentTimeAndDate() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String hr;
        String min;
        if (hour < 10) {
            hr = "0" + String.valueOf(hour);
        } else {
            hr = String.valueOf(hour);
        }
        if (minute < 10) {
            min = "0" + String.valueOf(minute);
        } else {
            min = String.valueOf(minute);
        }
        // Do something with the time chosen by the user
        time = hr + ":" + min;
        timeTextView.setText(time);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Do something with the date chosen by the user
        String date = String.valueOf(day) + " " + monthName[month] + " "
                + String.valueOf(year);
        dateTextView.setText(date);

    }

    private void bookVenue() {
        bookVenueBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        String key = mDatabaseRef.child(FB_BOOKINGS_PATH).push().getKey();
        BookingData bookingData = new BookingData(
                uid, currentUser.getDisplayName(),
                currentUser.getEmail(), date, time,
                key, descriptionEditText.getText().toString(), "Pending"
        );
        if (key != null) {
            mDatabaseRef.child(FB_BOOKINGS_PATH).child(key).setValue(bookingData);
            Toast.makeText(this, "You have successfully requested to book an appointment.",
                    Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "An error occured! Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
