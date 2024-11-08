package com.diekurve.eTankstellen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.diekurve.eTankstellen.mapAdapter.mapAdapter;
import com.diekurve.eTankstellen.model.ChargingStationDatabase;
import com.diekurve.eTankstellen.model.chargingStation;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


public class MapActivity extends AppCompatActivity {


    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView map = null;
    //    final int MY_PERMISSIONS = 1;
    //    private GoogleMap mGoogleMap;
    private ChargingStationDatabase database;
    private List<chargingStation> chargingStationList = new ArrayList<>();
    private RecyclerView mRecyclerViewMap;
    private mapAdapter mAdapter;
    private LatLng lastLocation;
    private IMapController mapController = map.getController();

    /**
     * Creates the activity
     *
     * @param savedInstance If the activity is being re-initialized after previously being shut down
     *                      then this Bundle contains the data it most recently supplied in
     *                      onSaveInstanceState(Bundle).
     *                      (https://developer.android.com/reference/android/app/
     *                      Activity#onCreate(android.os.Bundle))
     */
    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        requestPermissionsIfNecessary(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}
        );

        setContentView(R.layout.activity_map);
        //database = chargingStations.getDatabase(getApplicationContext());
        try {
            chargingStationList = loadChargingStations();
        } catch (InterruptedException e) {
            Log.e("error", e.toString());
        }

        Log.i("ChargingStationList", "Size:" + chargingStationList.size());

        // Get Location from GPS
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation_map);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        assert mapFragment != null;
//        mapFragment.getMapAsync(this);
        initNavView(navView);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);


        IMapController mapController = map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);
    }

    /**
     * Initializes the NavigationView
     *
     * @param navView NavigationView which will be initialized
     */
    private void initNavView(BottomNavigationView navView) {
        navView.setSelectedItemId(R.id.navigation_map);
        navView.getMenu().performIdentifierAction(R.id.navigation_map, 0);

        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.navigation_favorites) {
                startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else return item.getItemId() == R.id.navigation_map;
        });

        map.setVisibility(View.VISIBLE);
        map.setMultiTouchControls(true);
        addMarkers();
    }

//    /**
//     * Initializes the map, marks the current location on the map and all Charging Stations
//     *
//     * @param googleMap GoogleMap for use with method
//     */
//    @Override
//    public void onMapReady(@NonNull GoogleMap googleMap) {
//        mGoogleMap = googleMap;
//        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
//            return;
//        }
//        mGoogleMap.setMyLocationEnabled(true);
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//            if (location != null) {
//                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                mGoogleMap.addMarker(new MarkerOptions().position(lastLocation).title("Marker on Last Location"));
//                float zoom = 12.0f;
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, zoom));
//                addMarker();
//            }
//        });
//    }

    /**
     * Adds a all markers on the map
     */
    private void addMarkers() {
        for (chargingStation chargingStationsObject : chargingStationList) {
            Location chargingStationLocation = new Location(LocationManager.GPS_PROVIDER);
            chargingStationLocation.setLongitude(chargingStationsObject.getLongitude());
            chargingStationLocation.setLatitude(chargingStationsObject.getLatitude());
            if (checkIsInDistance(chargingStationLocation, lastLocation, 25)) {
                Marker newMarker = new Marker(map);
                newMarker.setTextIcon(chargingStationsObject.getOperator());
                newMarker.setPosition(new GeoPoint(chargingStationsObject.getLatitude(),
                        chargingStationsObject.getLongitude()));
                map.getOverlays().add(newMarker);
            }
        }
    }

    /**
     * Return List of all chargingstations in the database
     *
     * @return List of all chargingstations
     * @throws InterruptedException Throws exception if an error with the thread occurs
     */
    private List<chargingStation> loadChargingStations() throws InterruptedException {
        AtomicReference<List<chargingStation>> chargingStations = new AtomicReference<>();
        /*Thread dbListThread = new Thread(() ->
                chargingStations.set(database.chargingStationDAO().getAll()));
        dbListThread.start();
        dbListThread.join();*/
        return chargingStations.get();
    }

    /**
     * Gets a list of chargingstation in the database at the given coordinates
     *
     * @param longitude Longitude of chargingstation
     * @param latitude  Latitude of chargingstation
     * @return List of chargingstations at the given coordinates
     * @throws InterruptedException Throws exception if an error with the thread occurs
     */
    private List<chargingStation> loadChargingStationWithCoordinates(double longitude,
                                                                     double latitude)
            throws InterruptedException {
        AtomicReference<List<chargingStation>> chargingStations = new AtomicReference<>();
        /*Thread dbListThread = new Thread(() ->
                chargingStations.set(database.chargingStationDAO().
                        getChargingStationsWithCoordinates(latitude, longitude)));
        dbListThread.start();
        dbListThread.join();*/
        return chargingStations.get();
    }

    /**
     * Checks if given Location is in near distance of given coordinates
     *
     * @param checkLocation Location of the chargingstation
     * @param lastLocation  Current position of the user
     * @param maxDistance   Maximum distance
     * @return Return if the location is in distance to the last location
     */
    private boolean checkIsInDistance(Location checkLocation, LatLng lastLocation,
                                      int maxDistance) {
        Location currentLocation = new Location(LocationManager.GPS_PROVIDER);
        currentLocation.setLatitude(lastLocation.latitude);
        currentLocation.setLongitude(lastLocation.longitude);
        int distance = (int) currentLocation.distanceTo(checkLocation);
        distance /= 1000;
                return distance <= maxDistance;
    }

    /**
     * Called if activity is resumed
     */
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

}
