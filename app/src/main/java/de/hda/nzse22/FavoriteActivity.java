package de.hda.nzse22;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.hda.nzse22.favoritesAdapter.favoritesAdapter;
import de.hda.nzse22.model.NZSEDatabase;
import de.hda.nzse22.model.chargingStation;

public class FavoriteActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS = 1;
    private final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private BottomNavigationView navView;
    private RecyclerView mRecyclerView;
    private favoritesAdapter mAdapter;
    private List<chargingStation> mFavoriteChargingStations = null;
    private NZSEDatabase database;
    private SeekBar distanceBar;
    private TextView distance;
    private int progressValue = 10;
    private SharedPreferences isServicetechniker;


    /**
     * Creates the activity
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle).
     *                           (https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle))
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        database = NZSEDatabase.getDatabase(getApplicationContext());
        initSharedPreferences();
        setList();
        initNavBar();
        initSeekBar();
    }

    /**
     * Initialises the SharedPreferences for this activity
     */
    private void initSharedPreferences() {
        isServicetechniker = getSharedPreferences("NZSE_SS22", MODE_PRIVATE);
    }

    /**
     * Sets the list favorite chargingstations and sorts them by distance
     *
     * @throws InterruptedException Throws exception if thread failed
     */
    void populateFavoriteList() throws InterruptedException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
        }

        mFavoriteChargingStations = new ArrayList<>();

        AtomicReference<List<chargingStation>> dbList = new AtomicReference<>();
        Thread dbListThread;
        if (!isServicetechniker.getBoolean("isServicetechniker", false)) {
            dbListThread = new Thread(() -> dbList.set(database.chargingStationDAO().getFavorites()));
        } else {
            dbListThread = new Thread(() -> dbList.set(database.chargingStationDAO().getNotWorkingChargingStations()));
        }
        dbListThread.start();
        dbListThread.join();

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                for (chargingStation favorite : dbList.get()) {
                    LatLng favoriteCoordination = new LatLng(favorite.getLatitude(), favorite.getLongitude());
                    if (checkIsInDistance(favoriteCoordination, currentLocation, progressValue)) {
                        mFavoriteChargingStations.add(favorite);
                    }
                    mFavoriteChargingStations.sort((o1, o2) -> {
                        LatLng chargingStation1 = new LatLng(o1.getLatitude(), o1.getLongitude());
                        LatLng chargingStation2 = new LatLng(o2.getLatitude(), o2.getLongitude());
                        int distanceStation1 = checkDistance(chargingStation1, currentLocation);
                        int distanceStation2 = checkDistance(chargingStation2, currentLocation);
                        return Integer.compare(distanceStation1, distanceStation2);
                    });
                }
                initRecycler();
            }
        });
    }

    /**
     * When activity is resumed, initialise NavigationBar, RecyclerView and reload favorite chargingstations
     */
    @Override
    protected void onResume() {
        initNavBar();
        setList();
        initRecycler();
        initSharedPreferences();
        super.onResume();
    }

    /**
     * Populates the list of favorite chargingstations
     */
    private void setList() {
        try {
            populateFavoriteList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialises the RecyclerView
     */
    private void initRecycler() {
        mAdapter = new favoritesAdapter(mFavoriteChargingStations);
        mRecyclerView = findViewById(R.id.favoritesRecycler);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    /**
     * Initialises the NavigationBar
     */
    @SuppressLint("NonConstantResourceId")
    private void initNavBar() {
        navView = findViewById(R.id.bottom_navigation_favorites);
        navView.setSelectedItemId(R.id.navigation_favorites);
        navView.getMenu().performIdentifierAction(R.id.navigation_favorites, 0);
        navView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.navigation_favorites:
                    return true;
                case R.id.navigation_map:
                    startActivity(new Intent(getApplicationContext(), MapActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }

    /**
     * Initialises the SeekBar
     */
    private void initSeekBar() {
        distance = findViewById(R.id.distanceFavoriteText);
        distanceBar = findViewById(R.id.seekBarFavorites);
        String distanceText = progressValue + " km";
        distance.setText(distanceText);
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 0 && progress <= seekBar.getMax()) {
                        progressValue = progress / 10;
                        progressValue = progressValue * 10;
                        String distanceText = progressValue + " km";
                        distance.setText(distanceText);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setList();
                initRecycler();
            }
        });
    }

    /**
     * Checks if given Location is in near distance of given coordinates
     *
     * @param favoriteCoordinates Position of the favorite chargingstation
     * @param lastCoordinates     Current position of the user
     * @param maxDistance         Maximum distance
     * @return Return if the location is in distance to the last location
     */
    private boolean checkIsInDistance(LatLng favoriteCoordinates, LatLng lastCoordinates, int maxDistance) {
        return checkDistance(favoriteCoordinates, lastCoordinates) <= maxDistance;
    }

    /**
     * Return the distance between chargingstation and last coordinates of the user
     *
     * @param favoriteCoordinates Coordinate of chargingstation
     * @param lastCoordinates     Coordinate of user
     * @return Distance to last coordinates
     */
    private int checkDistance(@NonNull LatLng favoriteCoordinates, @NonNull LatLng lastCoordinates) {
        Location currentLocation = new Location(LocationManager.GPS_PROVIDER);
        currentLocation.setLatitude(lastCoordinates.latitude);
        currentLocation.setLongitude(lastCoordinates.longitude);

        Location favoriteLocation = new Location(LocationManager.GPS_PROVIDER);
        favoriteLocation.setLatitude(favoriteCoordinates.latitude);
        favoriteLocation.setLongitude(favoriteCoordinates.longitude);

        int distance = (int) currentLocation.distanceTo(favoriteLocation);
        distance /= 1000;
        return distance;
    }


}
