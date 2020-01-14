package com.elvis.CampoZone.loaders;


import android.content.AsyncTaskLoader;
import android.content.Context;

import com.elvis.CampoZone.activities.MainActivity;
import com.elvis.CampoZone.data.VenueData;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchLoader extends AsyncTaskLoader<List<VenueData>> {

    /** Query URL */
    private String mQuery;
    private List<VenueData> mData;

    public SearchLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Use cached data
            deliverResult(mData);
        } else {
            // We have no data, so kick off loading it
            forceLoad();
        }
    }

    @Override
    public List<VenueData> loadInBackground() {

        if (mQuery == null) {
            return null;
        }

        List<VenueData> songsData = new ArrayList<>();
        for (DataSnapshot dataSnapShot: MainActivity.searchDataSnapshot.getChildren()) {
            VenueData value = dataSnapShot.getValue(VenueData.class);
            if(value != null) {
                String location = value.getLocation();
                String name = value.getName();
                String owner = value.getOwner();
                if (!mQuery.equals("")) {
                    if (location.toLowerCase().contains(mQuery) || name.toLowerCase().equals(mQuery)
                            || owner.toLowerCase().equals(mQuery)) {
                        songsData.add(value);
                    }
                }

            }
        }

        mData = songsData;

        return mData;
    }

    @Override
    public void deliverResult(List<VenueData> data) {
        // We’ll save the data for later retrieval
        mData = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }
}
