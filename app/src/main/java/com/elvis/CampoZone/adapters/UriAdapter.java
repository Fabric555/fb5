package com.elvis.CampoZone.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elvis.CampoZone.R;
import com.elvis.CampoZone.activities.VenueActivity;
import com.elvis.CampoZone.data.ImageData;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class UriAdapter extends RecyclerView.Adapter<UriAdapter.ViewHolder> {
    private static ArrayList<ImageData> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView uriImg, removeImgBtn;

        ViewHolder(View v) {
            super(v);
            //find views
            uriImg = v.findViewById(R.id.uri_img);
            removeImgBtn = v.findViewById(R.id.remove_img_btn);

            //setonclicklistener for removeimgbtn
            removeImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VenueActivity.URI_DATA.remove(getAdapterPosition());
                    VenueActivity.recyclerViewAdapter.notifyItemRemoved(getAdapterPosition());
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UriAdapter(ArrayList<ImageData> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        // create a new view
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.uri_item, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Context ctx = holder.itemView.getContext();
        final Uri uri = Uri.parse(mDataset.get(position).getImg());
        final ImageView uriImgFinal = holder.uriImg;
        //background task to display the image selected
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    ParcelFileDescriptor parcelFileDescriptor = ctx.getContentResolver()
                            .openFileDescriptor(uri, "r");
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
                    Glide.with(ctx)
                            .load(bitmap)
                            .into(uriImgFinal);
                }
            }
        }.execute();

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
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

}
