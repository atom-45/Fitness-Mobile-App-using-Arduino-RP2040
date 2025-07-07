package com.atom.bluetoothfitnessapplication.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.atom.bluetoothfitnessapplication.data.models.AccelerometerData;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

@Dao
public interface TestDAO {
    @Query("SELECT * FROM test_table")
    Observable<List<AccelerometerData>> getAllAccelerometerData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAccelerometer(AccelerometerData accelerometer);
}
