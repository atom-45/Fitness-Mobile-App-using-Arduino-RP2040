package com.atom.bluetoothfitnessapplication.data.repositories;

import android.app.Application;

import androidx.annotation.NonNull;

import com.atom.bluetoothfitnessapplication.data.local.daos.TestDAO;
import com.atom.bluetoothfitnessapplication.data.local.database.FitnessExerciseDatabase;
import com.atom.bluetoothfitnessapplication.data.models.AccelerometerData;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class TestRepository {

    private final TestDAO testDAO;

    public TestRepository(@NonNull Application application) {
        this.testDAO = FitnessExerciseDatabase
                .getInstance(application.getApplicationContext())
                .testDAO();
    }

    public Observable<List<AccelerometerData>> getAllAccelerometersData() {
        return testDAO.getAllAccelerometerData();
    }

    public Completable insertAccelerometer(AccelerometerData accelerometer) {
        return testDAO.insertAccelerometer(accelerometer);
    }

}
