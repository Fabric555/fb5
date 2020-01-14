package com.elvis.CampoZone.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.utils.ResultContract;
import com.elvis.CampoZone.adapters.SearchCursorAdapter;
import com.elvis.CampoZone.adapters.VenuesAdapter;
import com.elvis.CampoZone.data.UserData;
import com.elvis.CampoZone.data.VenueData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FilterQueryProvider {

    //declare global variables
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public static final String FB_VENUES_PATH = "venues";
    public static final String FB_USERS_PATH = "users";
    public static final ArrayList<VenueData> VENUES_DATA = new ArrayList<>();
    DatabaseReference mDataBaseReference, venuesRef;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter recyclerViewAdapter;
    public static final String TAG = MainActivity.class.getSimpleName();
    NavigationView navigationView;
    View navView;
    ImageView navImg;
    TextView navName, navEmail;
    MenuItem venueItem;
    MatrixCursor cursor;
    SearchCursorAdapter cursorAdapter;
    int i = 0;
    public static DataSnapshot searchDataSnapshot;
    boolean isClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //initialize firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        venuesRef = mDataBaseReference.child(FB_VENUES_PATH);

        //find views
        recyclerView = findViewById(R.id.recycler_view);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        navView = navigationView.getHeaderView(0);
        navImg = navView.findViewById(R.id.nav_img);
        navName = navView.findViewById(R.id.nav_name);
        navEmail = navView.findViewById(R.id.nav_email);
        venueItem = navigationView.getMenu().getItem(2);

        setSupportActionBar(toolbar);

        //setup recyclerview
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);

        //setup drawer layout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        queryVenues();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            finish();
            startActivity(new Intent(MainActivity.this, StartUpActivity.class));
        } else {
            queryUserInfo();
        }
    }

    private void queryVenues() {
        //query venues from firebase database
        venuesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        VENUES_DATA.clear();
                        searchDataSnapshot = dataSnapshot;
                        //Loop through each venue
                        for (DataSnapshot dataSnapShot: dataSnapshot.getChildren()) {
                            VenueData venueData = dataSnapShot.getValue(VenueData.class);
                            //Check if venue exists
                            if (venueData != null) {
                                //Add venue to the arraylist
                                VENUES_DATA.add(venueData);
                            }
                        }
                        //Check if arraylist is not empty, then it passes the list to the recyclerview
                        //adapter
                        if (!VENUES_DATA.isEmpty()) {
                            recyclerViewAdapter = new VenuesAdapter(VENUES_DATA);
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
        } else if (id == R.id.nav_bookings) {
            //Pass boolean isClient to add accept and decline buttons
            Intent intent = new Intent(MainActivity.this, BookingsActivity.class);
            intent.putExtra("isClient", isClient);
            startActivity(intent);
        } else if (id == R.id.nav_venue) {
            startActivity(new Intent(MainActivity.this, EditVenueActivity.class));
        }else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
            finish();
            startActivity(new Intent(MainActivity.this, StartUpActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void queryUserInfo() {
        mDataBaseReference.child(FB_USERS_PATH).child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserData userData = dataSnapshot.getValue(UserData.class);
                        if (userData != null) {
                            if (userData.getImg().equals("") || userData.getImg() == null) {
                                Glide.with(MainActivity.this)
                                        .load(R.drawable.ic_account_circle_white_24dp)
                                        .into(navImg);
                            } else {
                                Glide.with(MainActivity.this)
                                        .load(userData.getImg())
                                        .into(navImg);
                            }
                            navName.setText(userData.getName());
                            navEmail.setText(userData.getEmail());
                            isClient = userData.isClient();
                            if (isClient) {
                                venueItem.setVisible(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                //initialize search feature
                SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                //get search item
                final SearchView searchView = (SearchView) item.getActionView();
                //indicate activity to receive searches
                if(searchManager != null) {
                    searchView.setSearchableInfo(searchManager
                            .getSearchableInfo(new ComponentName(getApplicationContext(), SearchableActivity.class)));
                }

                //adapter to populate list of searches
                cursorAdapter = new SearchCursorAdapter(this, cursor);
                cursorAdapter.setFilterQueryProvider(this);
                searchView.setSuggestionsAdapter(cursorAdapter);
                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionSelect(int position) {
                        return false;
                    }

                    @Override
                    public boolean onSuggestionClick(int position) {
                        if (cursor != null && cursor.moveToPosition(position)) {
                            String key = cursor.getString(cursor.getColumnIndex(
                                    ResultContract.ResultEntry.SUGGEST_COLUMN_INTENT_EXTRA_DATA));
                            searchView.setIconified(true);
                            searchView.clearFocus();
                            item.collapseActionView();

                            Intent intent = new Intent(MainActivity.this, DescriptionActivity.class);
                            intent.putExtra("key", key);
                            startActivity(intent);

                        }
                        return true;
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    public boolean onQueryTextSubmit(String query) {
                        searchView.setIconified(true);
                        searchView.clearFocus();
                        item.collapseActionView();
                        return false;
                    }

                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                searchView.setIconified(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        //Query and filter suggestions
        final String query = constraint.toString().toLowerCase();

        String[] columns = {
                ResultContract.ResultEntry._ID,
                ResultContract.ResultEntry.SUGGEST_COLUMN_TEXT_1,
                ResultContract.ResultEntry.SUGGEST_COLUMN_TEXT_2,
                ResultContract.ResultEntry.SUGGEST_COLUMN_ICON_1,
                ResultContract.ResultEntry.SUGGEST_COLUMN_INTENT_EXTRA_DATA};
        cursor = new MatrixCursor(columns);
        startManagingCursor(cursor);
        if (searchDataSnapshot == null) {
            venuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            searchDataSnapshot = dataSnapshot;
                            getSuggestions(query);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            getSuggestions(query);
        }

        return cursor;
    }

    private void getSuggestions(String query) {

        for (DataSnapshot dataSnapShot: searchDataSnapshot.getChildren()) {
            VenueData value = dataSnapShot.getValue(VenueData.class);
            if(value != null) {
                String location = value.getLocation();
                String name = value.getName();
                String owner = value.getOwner();
                String img = value.getImg();
                String key = value.getKey();

                final String id = String.valueOf(i++);
                if (!query.equals("")) {
                    if (location.toLowerCase().contains(query) || name.toLowerCase().equals(query)
                            || owner.toLowerCase().equals(query)) {
                        cursor.addRow(new Object[]{id, name, location, img, key});
                    }
                }

            }
        }
    }
}
