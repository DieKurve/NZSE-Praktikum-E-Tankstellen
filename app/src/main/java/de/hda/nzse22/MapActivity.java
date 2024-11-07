package de.hda.nzse22;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import de.hda.nzse22.mapAdapter.mapAdapter;
import de.hda.nzse22.model.NZSEDatabase;
import de.hda.nzse22.model.chargingStation;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    final int MY_PERMISSIONS = 1;
    private final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private GoogleMap mGoogleMap;
    private NZSEDatabase database;
    private List<chargingStation> chargingStationList = new ArrayList<>();
    private RecyclerView mRecyclerViewMap;
    private mapAdapter mAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng lastLocation;

    /**
     * Creates the activity
     *
     * @param savedInstance If the activity is being re-initialized after previously being shut down
     *                      then this Bundle contains the data it most recently supplied in
     *                      onSaveInstanceState(Bundle).
     *                      (https://developer.android.com/reference/android/app/Activity#onCreate(android.os.Bundle))
     */
    @Override
    protected void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_map);
        database = NZSEDatabase.getDatabase(getApplicationContext());
        try {
            chargingStationList = loadChargingStations();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(chargingStationList.size());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation_map);

        initNavView(navView);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

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
    }

    /**
     * Initializes the map, marks the current location on the map and all chargingstations
     *
     * @param googleMap GoogleMap for use with method
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS);
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(lastLocation).title("Marker on Last Location"));
                float zoom = 12.0f;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, zoom));
                addMarker();
            }
        });
    }

    /**
     * Adds a new marker on the map
     */
    private void addMarker() {
        for (chargingStation chargingStatonsObject : chargingStationList) {
            Location chargingStationLocation = new Location(LocationManager.GPS_PROVIDER);
            chargingStationLocation.setLongitude(chargingStatonsObject.getLongitude());
            chargingStationLocation.setLatitude(chargingStatonsObject.getLatitude());
            if (checkIsInDistance(chargingStationLocation, lastLocation, 25)) {
                LatLng chargingStationCoordinates = new LatLng(chargingStatonsObject.getLatitude(), chargingStatonsObject.getLongitude());
                mGoogleMap.addMarker(new MarkerOptions().position(chargingStationCoordinates).title(chargingStatonsObject.getOperator()));
                mGoogleMap.setOnMarkerClickListener(marker -> {
                    mRecyclerViewMap = findViewById(R.id.recyclerViewMapMarker);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    mRecyclerViewMap.setLayoutManager(linearLayoutManager);
                    double longitude = marker.getPosition().longitude;
                    double latitude = marker.getPosition().latitude;

                    try {
                        mAdapter = new mapAdapter(loadChargingStationWithCoordinates(longitude, latitude));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mRecyclerViewMap.setAdapter(mAdapter);
                    return false;
                });
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
        Thread dbListThread = new Thread(() -> chargingStations.set(database.chargingStationDAO().getAll()));
        dbListThread.start();
        dbListThread.join();
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
    private List<chargingStation> loadChargingStationWithCoordinates(double longitude, double latitude) throws InterruptedException {
        AtomicReference<List<chargingStation>> chargingStations = new AtomicReference<>();
        Thread dbListThread = new Thread(() -> chargingStations.set(database.chargingStationDAO().getChargingStationsWithCoordinates(latitude, longitude)));
        dbListThread.start();
        dbListThread.join();
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
    private boolean checkIsInDistance(Location checkLocation, LatLng lastLocation, int maxDistance) {

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
    }

}
