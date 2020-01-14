package com.elvis.CampoZone.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elvis.CampoZone.R;
import com.elvis.CampoZone.utils.ResultContract;
import com.elvis.CampoZone.loaders.SearchLoader;
import com.elvis.CampoZone.adapters.SearchCursorAdapter;
import com.elvis.CampoZone.adapters.SearchResultAdapter;
import com.elvis.CampoZone.data.VenueData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends AppCompatActivity
        implements FilterQueryProvider, LoaderManager.LoaderCallbacks<List<VenueData>> {

    ArrayList<VenueData> searchResultList = new ArrayList<>();
    RelativeLayout emptySearchView;
    SearchResultAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView searchRecyclerView;
    TextView searchText;
    DatabaseReference mDataBaseReference, venuesRef;
    public static final String FB_VENUES_PATH = "venues";
    ProgressBar searchProgressBar;
    String query;

    MatrixCursor cursor;
    SearchCursorAdapter cursorAdapter;
    int i = 0;
    int searchLoaderId = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        //initialize firebase
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();
        venuesRef = mDataBaseReference.child(FB_VENUES_PATH);

        //find views
        searchRecyclerView = findViewById(R.id.search_recycler_view);
        emptySearchView = findViewById(R.id.empty_search_view);
        searchText = findViewById(R.id.search_query_txt);
        searchRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        searchRecyclerView.setLayoutManager(mLayoutManager);
        searchProgressBar = findViewById(R.id.search_progress_bar);

        setTitle("Search Results");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //handle intent
        handleIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("query", query);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        query = savedInstanceState.getString("query");
        if (query != null && !query.equals("")) {
            searchRecyclerView.setVisibility(View.GONE);
            searchProgressBar.setVisibility(View.VISIBLE);
            emptySearchView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            String queryTxt = "Showing Results for: " + query;
            searchText.setText(queryTxt);
            if (searchResultList.isEmpty()) {
                doMySearch();
            } else {
                searchText.setVisibility(View.VISIBLE);
                searchProgressBar.setVisibility(View.GONE);
                mAdapter = new SearchResultAdapter(searchResultList);
                searchRecyclerView.setAdapter(mAdapter);
                searchRecyclerView.setVisibility(View.VISIBLE);
                emptySearchView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchRecyclerView.setVisibility(View.GONE);
            searchProgressBar.setVisibility(View.VISIBLE);
            emptySearchView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);

            query = intent.getStringExtra(SearchManager.QUERY);
            String queryTxt = "Showing Results for: " + query;
            searchText.setText(queryTxt);
            searchResultList.clear();
            doMySearch();
        }
    }


    private void doMySearch(){
        searchLoaderId++;
        if (MainActivity.searchDataSnapshot == null) {
            venuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            MainActivity.searchDataSnapshot = dataSnapshot;
                            // Get a reference to the LoaderManager, in order to interact with loaders.
                            LoaderManager loaderManager = getLoaderManager();

                            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                            // because this activity implements the LoaderCallbacks interface).
                            loaderManager.initLoader(searchLoaderId, null, SearchableActivity.this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(searchLoaderId, null, this);
        }

    }

    @Override
    public Loader<List<VenueData>> onCreateLoader(int i, Bundle bundle) {

        return new SearchLoader(this, query.toLowerCase());
    }

    @Override
    public void onLoadFinished(Loader<List<VenueData>> loader, List<VenueData> venuesData) {
        searchText.setVisibility(View.VISIBLE);
        searchProgressBar.setVisibility(View.GONE);

        if (venuesData != null && !venuesData.isEmpty()) {
            searchResultList.addAll(venuesData);
            mAdapter = new SearchResultAdapter(searchResultList);
            searchRecyclerView.setAdapter(mAdapter);
            searchRecyclerView.setVisibility(View.VISIBLE);
            emptySearchView.setVisibility(View.GONE);
        }
        else {
            searchRecyclerView.setVisibility(View.GONE);
            emptySearchView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<VenueData>> loader) {

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
                SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                final SearchView searchView = (SearchView) item.getActionView();
                if(searchManager != null) {
                    searchView.setSearchableInfo(searchManager
                            .getSearchableInfo(new ComponentName(getApplicationContext(), SearchableActivity.class)));
                }

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

                            Intent intent = new Intent(SearchableActivity.this, DescriptionActivity.class);
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
        if (MainActivity.searchDataSnapshot == null) {
            venuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    MainActivity.searchDataSnapshot = dataSnapshot;
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

        for (DataSnapshot dataSnapShot: MainActivity.searchDataSnapshot.getChildren()) {
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
