package de.hda.nzse22.favoritesAdapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hda.nzse22.R;
import de.hda.nzse22.model.ChargingStationDAO;
import de.hda.nzse22.model.NZSEDatabase;
import de.hda.nzse22.model.chargingStation;

public class favoritesAdapter extends RecyclerView.Adapter<favoritesAdapter.ViewHolder> {

    private final List<chargingStation> localDataSet;
    private Context mContext;
    private SharedPreferences isWorking;


    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public favoritesAdapter(List<chargingStation> dataSet) {
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
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_favorites, viewGroup, false);
        mContext = viewGroup.getContext();
        isWorking = mContext.getSharedPreferences("NZSE_SS22", MODE_PRIVATE);
        return new ViewHolder(view);
    }


    /**
     * Initializes the Elements of the ViewHolder of the given Position
     *
     * @param viewHolder Represents the content of the item
     * @param position   Position of the item in the adapter data set
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        viewHolder.operatorTextView.setText(localDataSet.get(position).getOperator());
        viewHolder.addressTextView.setText(localDataSet.get(position).getAddress());
        viewHolder.cityTextView.setText(localDataSet.get(position).getCity());

        viewHolder.plugType.setText(localDataSet.get(position).getPlugType1());

        if (isWorking.getBoolean("isServicetechniker", false)) {
            viewHolder.favoriteButton.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.favoriteButton.setVisibility(View.VISIBLE);
            if (localDataSet.get(position).getFavorite()) {
                viewHolder.favoriteButton.setBackgroundResource(android.R.drawable.star_big_on);
            } else {
                viewHolder.favoriteButton.setBackgroundResource(android.R.drawable.star_big_off);
            }
        }
        if (localDataSet.get(position).isWorking()) {
            viewHolder.workingView.setImageResource(android.R.drawable.presence_online);
        } else {
            viewHolder.workingView.setImageResource(android.R.drawable.presence_busy);
        }

        if (localDataSet.get(position).isExpand()) {
            viewHolder.expandButton.setBackgroundResource(android.R.drawable.arrow_up_float);
            viewHolder.detailsView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.expandButton.setBackgroundResource(android.R.drawable.arrow_down_float);
            viewHolder.detailsView.setVisibility(View.GONE);
        }

        if (isWorking.getBoolean("isServicetechniker", false)) {
            viewHolder.reportButton.setText(R.string.repariert);
        }

        viewHolder.favoriteButton.setOnClickListener(v -> {
            localDataSet.get(position).setFavorite(!localDataSet.get(position).getFavorite());
            updateChargingStation(position);
            notifyItemChanged(position);
        });

        viewHolder.expandButton.setOnClickListener(l -> {
            localDataSet.get(position).setExpand(!localDataSet.get(position).isExpand());
            updateChargingStation(position);
            notifyItemChanged(position);

        });

        viewHolder.reportButton.setOnClickListener(l -> {
            localDataSet.get(position).setWorking(isWorking.getBoolean("isServicetechniker", false));
            updateChargingStation(position);
            notifyItemChanged(position);
        });

        viewHolder.navigationButton.setOnClickListener(l -> {
            String geoLocation = "google.navigation:q=" + localDataSet.get(position).getLatitude() + "," +
                    localDataSet.get(position).getLongitude();
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
        NZSEDatabase db = NZSEDatabase.getDatabase(mContext);
        ChargingStationDAO dao = db.chargingStationDAO();
        Thread updateThread = new Thread(() -> dao.update(localDataSet.get(position)));
        updateThread.start();
        try {
            updateThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        private final ToggleButton expandButton;
        private final TextView cityTextView;
        private final ImageView workingView;
        private final TextView plugType;
        private final View detailsView;

        private final Button reportButton;
        private final Button navigationButton;


        /**
         * Finds and sets the elements of the View
         *
         * @param view View of the ViewHolder
         */
        public ViewHolder(View view) {
            super(view);
            operatorTextView = view.findViewById(R.id.operatorRecycler);
            addressTextView = view.findViewById(R.id.addressRecycler);
            favoriteButton = view.findViewById(R.id.favoriteRecycler);
            cityTextView = view.findViewById(R.id.cityRecycler);
            workingView = view.findViewById(R.id.workingView);
            expandButton = view.findViewById(R.id.expandButton);

            detailsView = view.findViewById(R.id.detailsLayout);
            plugType = view.findViewById(R.id.plugtypeText);
            reportButton = view.findViewById(R.id.reportButton);
            navigationButton = view.findViewById(R.id.routeButton);
        }

    }
}