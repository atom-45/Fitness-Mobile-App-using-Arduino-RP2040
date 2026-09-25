package com.atom.bluetoothfitnessapplication.data.repositories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Query;

import com.atom.bluetoothfitnessapplication.data.local.daos.ExerciseDAO;
import com.atom.bluetoothfitnessapplication.data.local.database.FitnessExerciseDatabase;
import com.atom.bluetoothfitnessapplication.data.models.Backs;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseDescription;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseStats;
import com.atom.bluetoothfitnessapplication.data.models.Flapjacks;
import com.atom.bluetoothfitnessapplication.data.models.LegHipRaises;
import com.atom.bluetoothfitnessapplication.data.models.LegRaises;
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

    public Observable<List<LegRaises>> getAllLegRaisesData() {
        return exerciseDAO.getAllLegRaisesData();
    }

    public Observable<List<LegHipRaises>> getAllLegHipRaisesData() {
        return exerciseDAO.getAllLegHipRaisesData();
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

    public Single<List<LegRaises>> getLegRaisesDataByDate(String date) {
        return exerciseDAO.getLegRaisesDataByDate(date);
    }

    public Single<List<LegHipRaises>> getLegHipRaisesDataByDate(String date) {
        return exerciseDAO.getLegHipRaisesDataByDate(date);
    }

    public Observable<List<ExerciseDescription>> getAllExerciseDescriptions() {
        return exerciseDAO.getAllExerciseDescriptions();
    }

    public Completable insertWalking(Walking walking) {
        return exerciseDAO.insertWalking(walking);
    }

    public Completable insertWalkingList(List<Walking> walking) {
        return exerciseDAO.insertWalkingList(walking);
    }

    public Completable insertSitUps(SitUps sitUps) {
        return exerciseDAO.insertSitUps(sitUps);
    }

    public Completable insertSitUpsList(List<SitUps> sitUps) {
        return exerciseDAO.insertSitUpsList(sitUps);
    }

    public Completable insertMountainClimbers(MountainClimbers mountainClimbers) {
        return exerciseDAO.insertMountainClimbers(mountainClimbers);
    }

    public Completable insertMountainClimbersList(List<MountainClimbers> mountainClimbers) {
        return exerciseDAO.insertMountainClimbersList(mountainClimbers);
    }

    public Completable insertBacks(Backs backs) {
        return exerciseDAO.insertBacks(backs);
    }

    public Completable insertBacksList(List<Backs> backs) {
        return exerciseDAO.insertBacksList(backs);
    }

    public Completable insertFlapJacks(Flapjacks flapjacks) {
        return exerciseDAO.insertFlapJacks(flapjacks);
    }

    public Completable insertFlapJacksList(List<Flapjacks> flapjacks) {
        return exerciseDAO.insertFlapJacksList(flapjacks);
    }

    public Completable insertWeights(Weights weights) {
        return exerciseDAO.insertWeights(weights);
    }

    public Completable insertWeightsList(List<Weights> weights) {
        return exerciseDAO.insertWeightsList(weights);
    }

    public Completable insertPushUp(PushUp pushUp) {
        return exerciseDAO.insertPushUp(pushUp);
    }

    public Completable insertPushUpList(List<PushUp> pushUp) {
        return exerciseDAO.insertPushUpList(pushUp);
    }

    public Completable insertSkipping(Skipping skipping) {
        return exerciseDAO.insertSkipping(skipping);
    }

    public Completable insertSkippingList(List<Skipping> skipping) {
        return exerciseDAO.insertSkippingList(skipping);
    }

    public Completable insertPlank(Plank plank){
        return exerciseDAO.insertPlank(plank);
    }

    public Completable insertPlankList(List<Plank> plank) {
        return exerciseDAO.insertPlankList(plank);
    }

    public Completable insertLegRaises(LegRaises legRaises) {
        return exerciseDAO.insertLegRaises(legRaises);
    }

    public Completable insertLegRaisesList(List<LegRaises> legRaisesList) {
        return exerciseDAO.insertLegRaisesList(legRaisesList);
    }

    public Completable insertLegHipRaises(LegHipRaises legHipRaises) {
        return exerciseDAO.insertLegHipRaises(legHipRaises);
    }

    public Completable insertLegHipRaisesList(List<LegHipRaises> legHipRaisesList) {
        return exerciseDAO.insertLegHipRaisesList(legHipRaisesList);
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

    public Single<List<ExerciseStats>> getExerciseFrequencyStats() {
        return exerciseDAO.getExerciseFrequencyStats();
    }

    public Completable insertWorkoutSummary(WorkoutSummary workoutSummary) {
        return exerciseDAO.insertWorkoutSummary(workoutSummary);
    }

    public Completable deleteWorkoutSummary(WorkoutSummary workoutSummary) {
        return exerciseDAO.deleteWorkoutSummary(workoutSummary);
    }

}
