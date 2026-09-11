package com.atom.bluetoothfitnessapplication.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.atom.bluetoothfitnessapplication.data.models.Backs;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseDescription;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseStats;
import com.atom.bluetoothfitnessapplication.data.models.Flapjacks;
import com.atom.bluetoothfitnessapplication.data.models.MountainClimbers;
import com.atom.bluetoothfitnessapplication.data.models.Plank;
import com.atom.bluetoothfitnessapplication.data.models.PushUp;
import com.atom.bluetoothfitnessapplication.data.models.SitUps;
import com.atom.bluetoothfitnessapplication.data.models.Skipping;
import com.atom.bluetoothfitnessapplication.data.models.Walking;
import com.atom.bluetoothfitnessapplication.data.models.Weights;
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSession;
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;
import com.atom.bluetoothfitnessapplication.data.repositories.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import javax.inject.Inject;

public class ExerciseViewModel extends ViewModel {

    private final ExerciseRepository exerciseRepository;

    private final WorkoutSession activeSession;

    @Inject
    public ExerciseViewModel(@NonNull ExerciseRepository exerciseRepository, @NonNull WorkoutSession workoutSession) {
        this.exerciseRepository = exerciseRepository;
        this.activeSession = workoutSession;
    }

    public Observable<List<Walking>> getAllWalkingData() {
        return exerciseRepository.getAllWalkingData();
    }

    public  Observable<List<Weights>> getAllWeightsData() {
        return exerciseRepository.getAllWeightsData();
    }

    public Observable<List<SitUps>> getAllSitUpData() {
        return exerciseRepository.getAllSitUpData();
    }

    public Observable<List<MountainClimbers>> getAllMountainClimberData() {
        return exerciseRepository.getAllMountainClimberData();
    }

    public Observable<List<Backs>> getAllBackData() {
        return exerciseRepository.getAllBackData();
    }

    public Observable<List<Flapjacks>> getAllFlapJackData() {
        return exerciseRepository.getAllFlapJackData();
    }

    public Observable<List<Skipping>> getAllSkippingData() {
        return exerciseRepository.getAllSkippingData();
    }

    public Observable<List<PushUp>> getAllPushUpData() {
        return exerciseRepository.getAllPushUpData();
    }

    public Observable<List<Plank>> getAllPlankData() {
        return exerciseRepository.getAllPlankData();
    }

    public Observable<List<ExerciseDescription>> getAllExerciseDescriptions() {
        return exerciseRepository.getAllExerciseDescriptions();
    }

    public Single<List<Walking>> getWalkingDataByDate(String date)
    {
        return exerciseRepository.getWalkingDataByDate(date);
    }

    public Single<List<Weights>> getWeightsDataByDate(String date)
    {
        return exerciseRepository.getWeightsDataByDate(date);
    }


    public Single<List<SitUps>> getSitUpDataByDate(String date)
    {
        return exerciseRepository.getSitUpDataByDate(date);
    }


    public Single<List<MountainClimbers>> getMountainClimberDataByDate(String date)
    {
        return exerciseRepository.getMountainClimberDataByDate(date);
    }


    public Single<List<Backs>> getBackDataByDate(String date)
    {
        return exerciseRepository.getBackDataByDate(date);
    }


    public Single<List<PushUp>> getPushUpDataByDate(String date)
    {
        return exerciseRepository.getPushUpDataByDate(date);
    }


    public Single<List<Skipping>> getSkippingDataByDate(String date)
    {
        return exerciseRepository.getSkippingDataByDate(date);
    }


    public Single<List<Flapjacks>> getFlapJackDataByDate(String date)
    {
        return exerciseRepository.getFlapJackDataByDate(date);
    }


    public Single<List<Plank>> getPlankDataByDate(String date)
    {
        return exerciseRepository.getPlankDataByDate(date);
    }


    public Completable insertWalking(Walking walking) {
        return exerciseRepository.insertWalking(walking);
    }

    public Completable insertSitUps(SitUps sitUps) {
        return exerciseRepository.insertSitUps(sitUps);
    }

    public Completable insertMountainClimbers(MountainClimbers mountainClimbers) {
        return exerciseRepository.insertMountainClimbers(mountainClimbers);
    }

    public Completable insertBacks(Backs backs) {
        return exerciseRepository.insertBacks(backs);
    }

    public Completable insertFlapJacks(Flapjacks flapjacks) {
        return exerciseRepository.insertFlapJacks(flapjacks);
    }

    public Completable insertWeights(Weights weights) {
        return exerciseRepository.insertWeights(weights);
    }

    public Completable insertPushUp(PushUp pushUp) {
        return exerciseRepository.insertPushUp(pushUp);
    }

    public Completable insertSkipping(Skipping skipping) {
        return exerciseRepository.insertSkipping(skipping);
    }

    public Completable insertPlank(Plank plank){
        return exerciseRepository.insertPlank(plank);
    }



    public Completable insertExerciseDescription(ExerciseDescription exerciseDescription){
        return exerciseRepository.insertExerciseDescription(exerciseDescription);
    }


    public WorkoutSession getActiveSession() {
        return activeSession;
    }

    public Single<List<WorkoutSummary>> getAllRecentSummaries(int limit) {
        return exerciseRepository.getAllRecentSummaries(limit);
    }

    public Single<List<WorkoutSummary>> getPastSummaries(String exerciseType, int limit) {
        return exerciseRepository.getPastSummaries(exerciseType, limit);
    }

    public Single<List<ExerciseStats>> getExerciseFrequencyStats() {
        return exerciseRepository.getExerciseFrequencyStats();
    }

    public Completable insertWorkoutSummary(WorkoutSummary workoutSummary) {
        return exerciseRepository.insertWorkoutSummary(workoutSummary);
    }
}
