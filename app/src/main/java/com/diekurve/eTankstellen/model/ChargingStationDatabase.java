package com.diekurve.eTankstellen.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database for the Charging Stations
 */
@Database(entities = {chargingStation.class}, version = 1, exportSchema = false)
public abstract class ChargingStationDatabase extends RoomDatabase {

    public abstract ChargingStationDAO charingstationDao();

    private static volatile ChargingStationDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriterExectutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Returns the instance of the databases
     *
     * @return Instance of the database
     */
    public static ChargingStationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChargingStationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ChargingStationDatabase.class, "chargingstation_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriterExectutor.execute(() -> {
                ChargingStationDAO dao = INSTANCE.charingstationDao();
                dao.deleteAll();

                chargingStation station = new chargingStation();
                dao.insertStation(station);
                station = new chargingStation();
                dao.insertStation(station);
            });
        }

    };
}
