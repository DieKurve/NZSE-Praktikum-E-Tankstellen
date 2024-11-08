package com.diekurve.eTankstellen.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

class ChargingStationRepository {
    private ChargingStationDAO mChargingStationDao;
    private LiveData<List<chargingStation>> mAllChargingStations;

    ChargingStationRepository(Application application){
        ChargingStationDatabase db = ChargingStationDatabase.getDatabase(application);
        mChargingStationDao = db.charingstationDao();
        mAllChargingStations = (LiveData<List<chargingStation>>) mChargingStationDao.getAll();
    }

    LiveData<List<chargingStation>> getmAllChargingStations(){
        return mAllChargingStations;
    }

    void insert(chargingStation chargingStation){
        ChargingStationDatabase.databaseWriterExectutor.execute(()->{
            mChargingStationDao.insertStation(chargingStation);
        });
    }
}
