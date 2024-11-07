package com.diekurve.eTankstellen.mapAdapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.diekurve.eTankstellen.R;
import com.diekurve.eTankstellen.model.ChargingStationDAO;
import com.diekurve.eTankstellen.model.chargingStations;
import com.diekurve.eTankstellen.model.chargingStation;

public class mapAdapter extends RecyclerView.Adapter<mapAdapter.ViewHolder> {

    private final List<chargingStation> localDataSet;
    private Context mContext;


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public mapAdapter(List<chargingStation> dataSet) {
        localDataSet = dataSet;
    }


    /**
     * Create new views (invoked by the layout manager)
     *
     * @param viewGroup Group in which new View is added
     * @param viewType  type of the new View
     * @return ViewHolder of a View of given viewType
     */
    @NonNull
    @Override
    public mapAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_map, viewGroup, false);
        mContext = viewGroup.getContext();
        return new mapAdapter.ViewHolder(view);
    }


    /**
     * Initializes the Elements of the ViewHolder of the given Position
     *
     * @param viewHolder Represents the content of the item
     * @param position   Position of the item in the adapter data set
     */
    @Override
    public void onBindViewHolder(@NonNull mapAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.operatorTextView.setText(localDataSet.get(position).getOperator());
        viewHolder.addressTextView.setText(localDataSet.get(position).getAddress());
        viewHolder.cityTextView.setText(localDataSet.get(position).getCity());

        if (localDataSet.get(position).getFavorite()) {
            viewHolder.favoriteButton.setBackgroundResource(android.R.drawable.star_big_on);
        } else {
            viewHolder.favoriteButton.setBackgroundResource(android.R.drawable.star_big_off);
        }
        if (localDataSet.get(position).isWorking()) {
            viewHolder.workingView.setImageResource(android.R.drawable.presence_online);
        } else {
            viewHolder.workingView.setImageResource(android.R.drawable.presence_busy);
        }


        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.favoriteButton.setOnClickListener(v -> {
            localDataSet.get(position).setFavorite(!localDataSet.get(position).getFavorite());
            updateChargingStation(position);
            notifyItemChanged(position);
        });


        viewHolder.navigationButton.setOnClickListener(l -> {
            String geoLocation = "google.navigation:q=" + localDataSet.get(position).getLatitude()
                    + "," + localDataSet.get(position).getLongitude();
            Uri gmmIntentUri = Uri.parse(geoLocation);
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            navigationIntent.setPackage("com.google.android.apps.maps");
            mContext.startActivity(navigationIntent);

        });


    }

    /**
     * @return size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    /**
     * Updates the element on position in the database
     *
     * @param position Position of Element which will be updated
     */
    private void updateChargingStation(int position) {
        chargingStations db = chargingStations.getDatabase(mContext);
        ChargingStationDAO dao = db.chargingStationDAO();
        Thread updateThread = new Thread(() -> dao.update(localDataSet.get(position)));
        updateThread.start();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            Log.e("error", e.toString());
        }
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView addressTextView;
        private final TextView operatorTextView;
        private final ToggleButton favoriteButton;
        private final ImageButton navigationButton;
        private final TextView cityTextView;
        private final ImageView workingView;


        /**
         * Finds and sets the elements of the View
         *
         * @param view View of the ViewHolder
         */
        public ViewHolder(View view) {
            super(view);
            operatorTextView = view.findViewById(R.id.operatorMap);
            addressTextView = view.findViewById(R.id.addresMap);
            favoriteButton = view.findViewById(R.id.favoriteMap);
            cityTextView = view.findViewById(R.id.cityMap);
            workingView = view.findViewById(R.id.workingView);
            navigationButton = view.findViewById(R.id.navigationMap);

        }

    }
}
