package com.atom.bluetoothfitnessapplication.data.local.daos;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ExerciseDAO {

    @Query("SELECT * FROM walking")
    Observable<List<Walking>> getAllWalkingData();

    @Query("SELECT * FROM weights")
    Observable<List<Weights>> getAllWeightsData();

    @Query("SELECT * FROM situps")
    Observable<List<SitUps>> getAllSitUpData();

    @Query("SELECT * FROM mountain_climbers")
    Observable<List<MountainClimbers>> getAllMountainClimberData();

    @Query("SELECT * FROM backs")
    Observable<List<Backs>> getAllBackData();

    @Query("SELECT * FROM pushups")
    Observable<List<PushUp>> getAllPushUpData();

    @Query("SELECT * FROM skipping")
    Observable<List<Skipping>> getAllSkippingData();

    @Query("SELECT * FROM flapjacks")
    Observable<List<Flapjacks>> getAllFlapJackData();

    @Query("SELECT * FROM planks")
    Observable<List<Plank>> getAllPlankData();

    /**
     *
     * Below Exercise Date Queries.
     */

    @Query("SELECT * FROM walking WHERE date_of_exercise = :date")
    Single<List<Walking>> getWalkingDataByDate(String date);

    @Query("SELECT * FROM weights WHERE date_of_exercise = :date")
    Single<List<Weights>> getWeightsDataByDate(String date);

    @Query("SELECT * FROM situps WHERE date_of_exercise = :date")
    Single<List<SitUps>> getSitUpDataByDate(String date);

    @Query("SELECT * FROM mountain_climbers WHERE date_of_exercise = :date")
    Single<List<MountainClimbers>> getMountainClimberDataByDate(String date);

    @Query("SELECT * FROM backs WHERE date_of_exercise = :date")
    Single<List<Backs>> getBackDataByDate(String date);

    @Query("SELECT * FROM pushups WHERE date_of_exercise = :date")
    Single<List<PushUp>> getPushUpDataByDate(String date);

    @Query("SELECT * FROM skipping WHERE date_of_exercise = :date")
    Single<List<Skipping>> getSkippingDataByDate(String date);

    @Query("SELECT * FROM flapjacks WHERE date_of_exercise = :date")
    Single<List<Flapjacks>> getFlapJackDataByDate(String date);

    @Query("SELECT * FROM planks WHERE date_of_exercise = :date")
    Single<List<Plank>> getPlankDataByDate(String date);




    @Query("SELECT * FROM exercise_description")
    Observable<List<ExerciseDescription>> getAllExerciseDescriptions();



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWalking(Walking walking);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWalkingList(List<Walking> walking);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertSitUps(SitUps sitUps);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertSitUpsList(List<SitUps> sitUps);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMountainClimbers(MountainClimbers mountainClimbers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertMountainClimbersList(List<MountainClimbers> mountainClimbers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertBacks(Backs backs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertBacksList(List<Backs> backs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFlapJacks(Flapjacks flapjacks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFlapJacksList(List<Flapjacks> flapjacks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWeights(Weights weights);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWeightsList(List<Weights> weights);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPushUp(PushUp pushUp);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPushUpList(List<PushUp> pushUp);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertSkipping(Skipping skipping);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertSkippingList(List<Skipping> skipping);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPlank(Plank plank);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPlankList(List<Plank> plank);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertExerciseDescription(ExerciseDescription exerciseDescription);

    @Query("SELECT * FROM workout_summaries ORDER BY id DESC LIMIT :limit")
    Single<List<WorkoutSummary>> getAllRecentSummaries(int limit);

    @Query("SELECT * FROM workout_summaries WHERE exercise_type = :exerciseType ORDER BY id DESC LIMIT :limit")
    Single<List<WorkoutSummary>> getPastSummaries(String exerciseType, int limit);

    @Query("SELECT exercise_type, COUNT(*) as total_sessions, " +
           "SUM(CASE WHEN timestamp >= date('now', '-7 days') THEN 1 ELSE 0 END) as weekly_sessions, " +
           "MAX(timestamp) as last_date FROM workout_summaries GROUP BY exercise_type")
    Single<List<ExerciseStats>> getExerciseFrequencyStats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertWorkoutSummary(WorkoutSummary workoutSummary);

    @Delete
    Completable deleteWorkoutSummary(WorkoutSummary workoutSummary);
}
