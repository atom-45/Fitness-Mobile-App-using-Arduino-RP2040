package com.atom.bluetoothfitnessapplication.di.application;

import android.app.Application;

import com.atom.bluetoothfitnessapplication.di.components.ApplicationComponent;
import com.atom.bluetoothfitnessapplication.di.components.DaggerApplicationComponent;

public class FitnessApplication extends Application {
    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
      super.onCreate();

      applicationComponent = DaggerApplicationComponent
              .builder()
              .application(this)
              .build();

    }

    public ApplicationComponent getApplicationComponent(){
        return applicationComponent;
    }

}
