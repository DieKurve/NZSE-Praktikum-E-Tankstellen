package com.diekurve.eTankstellen;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diekurve.eTankstellen.download.Download;
import com.diekurve.eTankstellen.model.ChargingStationDatabase;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import com.diekurve.eTankstellen.favoritesAdapter.favoritesAdapter;
import com.diekurve.eTankstellen.model.ChargingStationDAO;
import com.diekurve.eTankstellen.model.chargingStation;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS = 1;
    private final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET};
    List<chargingStation> chargingStations = null;
    List<chargingStation> mNearChargingStations = null;
    private ChargingStationDatabase database;
    private ChargingStationDAO chargingStationDAO;
    private BottomNavigationView navView;
    private favoritesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SharedPreferences sharedPreferences;


    /**
     * Creates the activity and creates the database if not exists
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     *                           down then this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle).
     *                           (<a href="https://developer.android.com/reference/android/app/
     *                           Activity#onCreate(android.os.Bundle)">...</a>)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
        createDatabase();
        setList();
        initNavView();

        try {
            List<chargingStation> stations = Download.csvRead();
            database.charingstationDao().insertAll(stations);
        } catch (InterruptedException e) {
            Log.e("Error", e.toString());
            throw new RuntimeException(e);
        }
//        deleteDatabase();

//        if (getDatabaseSize() < amountofChargingStations) {
//            startActivity(new Intent(getApplicationContext(), GetChargingStationsActivity.class));
//        } else if (getDatabaseSize() > amountofChargingStations) {
//            deleteDatabase();
//            startActivity(new Intent(getApplicationContext(), GetChargingStationsActivity.class));
//        }

    }

    /**
     * Initializes the NavigationView
     */
    @SuppressLint("NonConstantResourceId")
    private void initNavView() {
        navView = findViewById(R.id.bottom_navigation_main);

        navView.setSelectedItemId(R.id.navigation_home);
        navView.getMenu().performIdentifierAction(R.id.navigation_home, 0);

        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                return true;
            }
            if (item.getItemId() == R.id.navigation_favorites) {

                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.navigation_map) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    /**
     * Initialises the RecyclerView
     */
    private void initRecycler() {
        mAdapter = new favoritesAdapter(mNearChargingStations);
        mRecyclerView = findViewById(R.id.recyclerViewMain);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    /**
     * Populates the list of charging stations
     */
    private void setList() {
        try {
            populateChargingStation();
        } catch (InterruptedException e) {
            Log.e("error", e.toString());
        }
    }

    /**
     * Sets the list favorite charging stations and sorts them by distance
     *
     * @throws InterruptedException Throws exception if thread failed
     */
    /*private void populateStationList() throws InterruptedException {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
        }

        mNearChargingStations = new ArrayList<>();

            chargingStations = dbList.get();
        }*/

//        FusedLocationProviderClient fusedLocationClient =
//                LocationServices.getFusedLocationProviderClient(this);
//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//            if (location != null) {
//                LatLng currentLocation = new LatLng(location.getLatitude(),
//                        location.getLongitude());
//                for (chargingStation favorite : chargingStations) {
//                    LatLng favoriteCoordination = new LatLng(favorite.getLatitude(),
//                            favorite.getLongitude());
//                    if (checkIsInDistance(favoriteCoordination, currentLocation, 25)) {
//                        mNearChargingStations.add(favorite);
//                    }
//                    mNearChargingStations.sort((o1, o2) -> {
//                        LatLng chargingStation1 = new LatLng(o1.getLatitude(), o1.getLongitude());
//                        LatLng chargingStation2 = new LatLng(o2.getLatitude(), o2.getLongitude());
//                        int distanceStation1 = checkDistance(chargingStation1, currentLocation);
//                        int distanceStation2 = checkDistance(chargingStation2, currentLocation);
//                        return Integer.compare(distanceStation1, distanceStation2);
//                    });
//                }
//
//            }
//
//        });
//        initRecycler();
//    }

    /**
     * Checks if given Location is in near distance of given coordinates
     *
     * @param favoriteCoordinates Position of the favorite chargingstation
     * @param lastCoordinates     Current position of the user
     * @param maxDistance         Maximum distance
     * @return Return if the location is in distance to the last location
     */
    private boolean checkIsInDistance(LatLng favoriteCoordinates, LatLng lastCoordinates,
                                      int maxDistance) {
        return checkDistance(favoriteCoordinates, lastCoordinates) <= maxDistance;
    }

    /**
     * Return the distance between Charging Stations and last coordinates of the user
     *
     * @param favoriteCoordinates Coordinate of Charging Station
     * @param lastCoordinates     Coordinate of user
     * @return Distance to last coordinates
     */
    private int checkDistance(@NonNull LatLng favoriteCoordinates,
                              @NonNull LatLng lastCoordinates) {
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

    /**
     * @throws InterruptedException Throws exception if thread fails
     */
    private void populateChargingStation() throws InterruptedException {
        new Thread(() -> chargingStations = chargingStationDAO.getAll()).start();

    }

    /**
     * Creates the database
     */
    private void createDatabase() {
        return;
        /*database = com.diekurve.eTankstellen.model.chargingStations.
                getDatabase(getApplicationContext());
        chargingStationDAO = database.chargingStationDAO();*/
    }

//    /**
//     * @return Amount of entities in database
//     */
//    private Integer getDatabaseSize() {
//        AtomicReference<Integer> sizeDB = new AtomicReference<>(0);
//        Thread checkThread = new Thread(() -> {
//            sizeDB.set(chargingStationDAO.getAll().size());
//        });
//        checkThread.start();
//        try {
//            checkThread.join();
//        } catch (InterruptedException e) {
//            Log.e("error", e.toString());
//        }
//        return sizeDB.get();
//    }

    /**
     * Deletes the database
     */
    private void deleteDatabase() {
        //Runnable runnable = () -> chargingStationDAO.deleteAll();
        //runnable.run();
    }

    /**
     * When activity is resumed reload favorite charging stations
     */
    @Override
    public void onResume() {
        navView.setSelectedItemId(R.id.navigation_home);
        sharedPreferences = getSharedPreferences("E_Tankstellen", MODE_PRIVATE);
        super.onResume();
        try {
            populateChargingStation();
        } catch (InterruptedException e) {
            Log.e("error", e.toString());
        }
        System.out.println(chargingStations.size());
    }

    /**
     * When activity is destroyed close the  database
     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
