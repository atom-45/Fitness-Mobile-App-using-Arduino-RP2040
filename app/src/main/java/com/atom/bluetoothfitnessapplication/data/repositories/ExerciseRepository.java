package com.atom.bluetoothfitnessapplication.data.repositories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Query;

import com.atom.bluetoothfitnessapplication.data.local.daos.ExerciseDAO;
import com.atom.bluetoothfitnessapplication.data.local.database.FitnessExerciseDatabase;
import com.atom.bluetoothfitnessapplication.data.models.Backs;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseDescription;
import com.atom.bluetoothfitnessapplication.data.models.Flapjacks;
import com.atom.bluetoothfitnessapplication.data.models.MountainClimbers;
import com.atom.bluetoothfitnessapplication.data.models.Plank;
import com.atom.bluetoothfitnessapplication.data.models.PushUp;
import com.atom.bluetoothfitnessapplication.data.models.SitUps;
import com.atom.bluetoothfitnessapplication.data.models.Skipping;
import com.atom.bluetoothfitnessapplication.data.models.Walking;
import com.atom.bluetoothfitnessapplication.data.models.Weights;
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class ExerciseRepository {

    private final ExerciseDAO exerciseDAO;

    @Inject
    public ExerciseRepository(@NonNull Application application) {
        this.exerciseDAO = FitnessExerciseDatabase
                .getInstance(application.getApplicationContext())
                .exerciseDAO();
    }

    public Observable<List<Walking>> getAllWalkingData() {
        return exerciseDAO.getAllWalkingData();
    }

    public  Observable<List<Weights>> getAllWeightsData() {
        return exerciseDAO.getAllWeightsData();
    }

    public Observable<List<SitUps>> getAllSitUpData() {
        return exerciseDAO.getAllSitUpData();
    }

    public Observable<List<MountainClimbers>> getAllMountainClimberData() {
        return exerciseDAO.getAllMountainClimberData();
    }

    public Observable<List<Backs>> getAllBackData() {
        return exerciseDAO.getAllBackData();
    }

    public Observable<List<Flapjacks>> getAllFlapJackData() {
        return exerciseDAO.getAllFlapJackData();
    }

    public Observable<List<PushUp>> getAllPushUpData() {
        return exerciseDAO.getAllPushUpData();
    }

    public Observable<List<Skipping>> getAllSkippingData() {
        return exerciseDAO.getAllSkippingData();
    }

    public Observable<List<Plank>> getAllPlankData() {
        return exerciseDAO.getAllPlankData();
    }



    public Single<List<Walking>> getWalkingDataByDate(String date)
    {
        return exerciseDAO.getWalkingDataByDate(date);
    }


    public Single<List<Weights>> getWeightsDataByDate(String date)
    {
        return exerciseDAO.getWeightsDataByDate(date);
    }


    public Single<List<SitUps>> getSitUpDataByDate(String date)
    {
        return exerciseDAO.getSitUpDataByDate(date);
    }


    public Single<List<MountainClimbers>> getMountainClimberDataByDate(String date)
    {
        return exerciseDAO.getMountainClimberDataByDate(date);
    }


    public Single<List<Backs>> getBackDataByDate(String date)
    {
        return exerciseDAO.getBackDataByDate(date);
    }


    public Single<List<PushUp>> getPushUpDataByDate(String date)
    {
        return exerciseDAO.getPushUpDataByDate(date);
    }


    public Single<List<Skipping>> getSkippingDataByDate(String date)
    {
        return exerciseDAO.getSkippingDataByDate(date);
    }


    public Single<List<Flapjacks>> getFlapJackDataByDate(String date)
    {
        return exerciseDAO.getFlapJackDataByDate(date);
    }


    public Single<List<Plank>> getPlankDataByDate(String date)
    {
        return exerciseDAO.getPlankDataByDate(date);
    }

    public Observable<List<ExerciseDescription>> getAllExerciseDescriptions() {
        return exerciseDAO.getAllExerciseDescriptions();
    }

    public Completable insertWalking(Walking walking) {
        return exerciseDAO.insertWalking(walking);
    }

    public Completable insertSitUps(SitUps sitUps) {
        return exerciseDAO.insertSitUps(sitUps);
    }

    public Completable insertMountainClimbers(MountainClimbers mountainClimbers) {
        return exerciseDAO.insertMountainClimbers(mountainClimbers);
    }

    public Completable insertBacks(Backs backs) {
        return exerciseDAO.insertBacks(backs);
    }

    public Completable insertFlapJacks(Flapjacks flapjacks) {
        return exerciseDAO.insertFlapJacks(flapjacks);
    }

    public Completable insertWeights(Weights weights) {
        return exerciseDAO.insertWeights(weights);
    }

    public Completable insertPushUp(PushUp pushUp) {
        return exerciseDAO.insertPushUp(pushUp);
    }

    public Completable insertSkipping(Skipping skipping) {
        return exerciseDAO.insertSkipping(skipping);
    }

    public Completable insertPlank(Plank plank){
        return exerciseDAO.insertPlank(plank);
    }

    public Completable insertExerciseDescription(ExerciseDescription exerciseDescription) {
        return exerciseDAO.insertExerciseDescription(exerciseDescription);
    }

    public Single<List<WorkoutSummary>> getAllRecentSummaries(int limit) {
        return exerciseDAO.getAllRecentSummaries(limit);
    }

    public Single<List<WorkoutSummary>> getPastSummaries(String exerciseType, int limit) {
        return exerciseDAO.getPastSummaries(exerciseType, limit);
    }

    public Completable insertWorkoutSummary(WorkoutSummary workoutSummary) {
        return exerciseDAO.insertWorkoutSummary(workoutSummary);
    }

}
