package com.atom.bluetoothfitnessapplication.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.atom.bluetoothfitnessapplication.data.models.AccelerometerData;
import com.atom.bluetoothfitnessapplication.data.repositories.TestRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class TestViewModel extends AndroidViewModel {
    private final TestRepository testRepository;

    public TestViewModel(@NonNull Application application) {
        super(application);
        this.testRepository = new TestRepository(application);

    }

    public Observable<List<AccelerometerData>> getAllAccelerometerData(){
        return testRepository.getAllAccelerometersData();
    }

    public Completable insertAccelerometer(AccelerometerData accelerometer){
        return testRepository.insertAccelerometer(accelerometer);
    }
}
