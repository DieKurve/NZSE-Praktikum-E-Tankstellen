package com.diekurve.eTankstellen.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChargingStationDAO {
    /**
     * Returns a list of all chargingstations in the database
     *
     * @return List of all chargingstations
     */
    @Query("SELECT * FROM chargingstations")
    List<chargingStation> getAll();

    /**
     * Inserts a list of chargingstations into the database
     *
     * @param chargingStationList List of Chargingstations which are inserted into the database
     */
    @Insert
    void insertAll(List<chargingStation> chargingStationList);

    /**
     * Updates given chargingstation object in the database
     *
     * @param chargingStation Chargingstation which will be updated in the databe
     */
    @Update
    void update(chargingStation chargingStation);


    /**
     * Returns a list of all favorite chargingstations
     *
     * @return List of all favorite chargingstations
     */
    @Query("SELECT * FROM chargingstations WHERE Favorite=1")
    List<chargingStation> getFavorites();

    /**
     * Deletes all entities of the database
     */
    @Query("DELETE FROM chargingstations")
    void deleteAll();

    @Query("SELECT * FROM chargingstations WHERE Working=0")
    List<chargingStation> getNotWorkingChargingStations();

    /**
     * Returns a list of chargingstations with the given coordinates
     *
     * @return List of chargingstations with the given coordinates
     */
    @Query("SELECT * FROM chargingstations WHERE Latitude=:latitude AND Longitude=:longitude")
    List<chargingStation> getChargingStationsWithCoordinates(double latitude, double longitude);


}
