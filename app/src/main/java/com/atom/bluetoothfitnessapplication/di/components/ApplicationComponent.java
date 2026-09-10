package com.atom.bluetoothfitnessapplication.di.components;

import android.app.Application;

import com.atom.bluetoothfitnessapplication.di.modules.ViewModelModule;
import com.atom.bluetoothfitnessapplication.presentation.activities.MainActivity;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
@Singleton
@Component(modules = {ViewModelModule.class})
public interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        ApplicationComponent build();
    }

    void inject(MainActivity mainActivity);



}
