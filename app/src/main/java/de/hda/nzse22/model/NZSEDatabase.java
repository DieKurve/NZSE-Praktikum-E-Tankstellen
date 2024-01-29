package de.hda.nzse22.model;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Database for the chargingstations
 */
@Database(entities = {chargingStation.class}, version = 4)
public abstract class NZSEDatabase extends RoomDatabase {
    private static volatile NZSEDatabase INSTANCE;

    /**
     * Returns the instance of the databases
     *
     * @return Instance of the database
     */
    public static NZSEDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NZSEDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NZSEDatabase.class, "chargingstation_database")
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
