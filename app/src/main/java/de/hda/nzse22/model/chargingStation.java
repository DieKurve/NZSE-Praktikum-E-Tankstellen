package de.hda.nzse22.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;


@Entity(tableName = "chargingstations")
public class chargingStation {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "Operator")
    public String mOperator = "";

    @ColumnInfo(name = "Address")
    public String mAddress = "";

    @ColumnInfo(name = "City")
    public String mCity = "";

    @ColumnInfo(name = "County")
    public String mCounty = "";

    @ColumnInfo(name = "Latitude")
    public float mLatitude = 0;

    @ColumnInfo(name = "Longitude")
    public float mLongitude = 0;

    @ColumnInfo(name = "MaxPower")
    public float mMaxPower = 0;

    @ColumnInfo(name = "AmountOfChargingPoints")
    public Integer mAmountOfChargingPoints = 0;

    @ColumnInfo(name = "KindofChargingStation")
    public String mKindofChargingStation = "";

    @ColumnInfo(name = "Working")
    public boolean mWorking = true;

    @ColumnInfo(name = "PlugType1")
    public String mPlugType1 = " ";

    @ColumnInfo(name = "PlugType2")
    public String mPlugType2 = " ";

    @ColumnInfo(name = "PlugType3")
    public String mPlugType3 = " ";

    @ColumnInfo(name = "PlugType4")
    public String mPlugType4 = " ";

    @ColumnInfo(name = "Favorite")
    public boolean isFavorite = false;

    public boolean isExpand = false;

    public chargingStation(@NonNull List<String> ladestation) {
        this.mOperator = ladestation.get(0);
        this.mAddress = ladestation.get(1) + " " + ladestation.get(2);
        this.mCity = ladestation.get(4) + " " + ladestation.get(5);
        this.mCounty = ladestation.get(6);
        this.mLatitude = Float.parseFloat(ladestation.get(8).replace(',', '.'));
        this.mLongitude = Float.parseFloat(ladestation.get(9).replace(',', '.'));
        this.mMaxPower = Float.parseFloat(ladestation.get(11).replace(',', '.'));
        this.mKindofChargingStation = ladestation.get(12);
        this.mAmountOfChargingPoints = Integer.parseInt(ladestation.get(13));
        this.mPlugType1 = ladestation.get(14);
        if (ladestation.size() > 17) {
            this.mPlugType2 = ladestation.get(17);
        }
        if (ladestation.size() > 20) {
            this.mPlugType3 = ladestation.get(20);
        }
        if (ladestation.size() > 23) {
            this.mPlugType4 = ladestation.get(23);
        }
    }

    public chargingStation() {

    }

    public String getOperator() {
        return mOperator;
    }

    public String getAddress() {
        return mAddress;
    }

    public boolean isWorking() {
        return mWorking;
    }

    public void setWorking(boolean working) {
        this.mWorking = working;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getCity() {
        return mCity;
    }

    public boolean getFavorite() {
        return isFavorite;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public String getPlugType1() {
        return mPlugType1;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getmOperator() {
        return mOperator;
    }

    public String getmAddress() {
        return mAddress;
    }

    public String getmCity() {
        return mCity;
    }

    public String getmCounty() {
        return mCounty;
    }

    public float getmLatitude() {
        return mLatitude;
    }

    public float getmLongitude() {
        return mLongitude;
    }

    public float getmMaxPower() {
        return mMaxPower;
    }

    public Integer getmAmountOfChargingPoints() {
        return mAmountOfChargingPoints;
    }

    public String getmKindofChargingStation() {
        return mKindofChargingStation;
    }

    public boolean ismWorking() {
        return mWorking;
    }

    public void setmWorking(boolean mWorking) {
        this.mWorking = mWorking;
    }

    public String getmPlugType1() {
        return mPlugType1;
    }

    public void setmPlugType1(String mPlugType1) {
        this.mPlugType1 = mPlugType1;
    }

    public String getmPlugType2() {
        return mPlugType2;
    }

    public void setmPlugType2(String mPlugType2) {
        this.mPlugType2 = mPlugType2;
    }

    public String getmPlugType3() {
        return mPlugType3;
    }

    public void setmPlugType3(String mPlugType3) {
        this.mPlugType3 = mPlugType3;
    }

    public String getmPlugType4() {
        return mPlugType4;
    }

    public void setmPlugType4(String mPlugType4) {
        this.mPlugType4 = mPlugType4;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }


}
