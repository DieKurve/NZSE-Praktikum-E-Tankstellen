package com.diekurve.eTankstellen.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Database for the Charging Stations
 */
@Database(entities = {chargingStation.class}, version = 4)
public abstract class chargingStations extends RoomDatabase {
    private static volatile chargingStations INSTANCE;

    /**
     * Returns the instance of the databases
     *
     * @return Instance of the database
     */
    public static chargingStations getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (chargingStations.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    chargingStations.class, "chargingstation_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * chargingStation Data Access Object
     *
     * @return chargingStationDAO
     */
    public abstract ChargingStationDAO chargingStationDAO();
}
