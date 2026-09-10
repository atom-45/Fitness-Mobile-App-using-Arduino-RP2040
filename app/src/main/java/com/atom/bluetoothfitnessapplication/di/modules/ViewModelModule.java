package com.atom.bluetoothfitnessapplication.di.modules;

import androidx.lifecycle.ViewModel;

import com.atom.bluetoothfitnessapplication.factories.ViewModelKey;
import com.atom.bluetoothfitnessapplication.presentation.viewmodels.ExerciseViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(ExerciseViewModel.class)
    public abstract ViewModel bindExerciseViewModel(ExerciseViewModel exerciseViewModel);


}
