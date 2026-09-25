package com.atom.bluetoothfitnessapplication.presentation.viewmodels;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

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
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSession;
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;
import com.atom.bluetoothfitnessapplication.data.repositories.ExerciseRepository;
import com.atom.bluetoothfitnessapplication.dsp.DynamicAnalyzerUtils;
import com.atom.bluetoothfitnessapplication.presentation.screens.MainUiState;
import com.atom.bluetoothfitnessapplication.utilities.Pair;
import com.atom.bluetoothfitnessapplication.utilities.TimerUtils;
import com.atom.bluetoothfitnessapplication.utilities.Triple;
import com.atom.bluetoothfitnessapplication.utilities.WidgetHelper;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;

public class ExerciseViewModel extends ViewModel {

    private static final String TAG = "ExerciseViewModel";

    private final ExerciseRepository exerciseRepository;
    private final WorkoutSession activeSession;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Inject
    public ExerciseViewModel(@NonNull ExerciseRepository exerciseRepository, @NonNull WorkoutSession workoutSession) {
        this.exerciseRepository = exerciseRepository;
        this.activeSession = workoutSession;
    }

    public Observable<List<Walking>> getAllWalkingData() {
        return exerciseRepository.getAllWalkingData();
    }

    public Observable<List<Weights>> getAllWeightsData() {
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

    public Single<List<Walking>> getWalkingDataByDate(String date) {
        return exerciseRepository.getWalkingDataByDate(date);
    }

    public Single<List<Weights>> getWeightsDataByDate(String date) {
        return exerciseRepository.getWeightsDataByDate(date);
    }

    public Single<List<SitUps>> getSitUpDataByDate(String date) {
        return exerciseRepository.getSitUpDataByDate(date);
    }

    public Single<List<MountainClimbers>> getMountainClimberDataByDate(String date) {
        return exerciseRepository.getMountainClimberDataByDate(date);
    }

    public Single<List<Backs>> getBackDataByDate(String date) {
        return exerciseRepository.getBackDataByDate(date);
    }

    public Single<List<PushUp>> getPushUpDataByDate(String date) {
        return exerciseRepository.getPushUpDataByDate(date);
    }

    public Single<List<Skipping>> getSkippingDataByDate(String date) {
        return exerciseRepository.getSkippingDataByDate(date);
    }

    public Single<List<Flapjacks>> getFlapJackDataByDate(String date) {
        return exerciseRepository.getFlapJackDataByDate(date);
    }

    public Single<List<Plank>> getPlankDataByDate(String date) {
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

    public Completable insertPlank(Plank plank) {
        return exerciseRepository.insertPlank(plank);
    }

    public Observable<List<LegRaises>> getAllLegRaisesData() {
        return exerciseRepository.getAllLegRaisesData();
    }

    public Single<List<LegRaises>> getLegRaisesDataByDate(String date) {
        return exerciseRepository.getLegRaisesDataByDate(date);
    }

    public Completable insertLegRaises(LegRaises legRaises) {
        return exerciseRepository.insertLegRaises(legRaises);
    }

    public Completable insertExerciseDescription(ExerciseDescription exerciseDescription) {
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

    public void deleteWorkoutSummary(WorkoutSummary summary, MainUiState uiState) {
        Disposable disposable = exerciseRepository.deleteWorkoutSummary(summary)
                .andThen(getAllRecentSummaries(20))
                .flatMap(recent -> getExerciseFrequencyStats().map(stats -> new Pair<>(recent, stats)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    uiState.setAllRecentSummaries(pair.first());
                    uiState.setExerciseFrequencyStats(pair.second());
                }, throwable -> Log.e(TAG, "Delete Error", throwable));
        compositeDisposable.add(disposable);
    }

    // Business Logic Methods

    public void fetchTrendData(String exerciseType, boolean isPrimaryMetric, MainUiState uiState, String plankName, String skippingName, String mtClimbersName, String sitUpName) {
        Disposable disposable = getPastSummaries(exerciseType, 10)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(summaries -> {
                    if (summaries.isEmpty()) {
                        uiState.setBarData(null);
                        return;
                    }
                    boolean isPlank = exerciseType.equals(plankName);
                    boolean isCardio = exerciseType.equals(skippingName) || exerciseType.equals(mtClimbersName);
                    boolean isSitUp = exerciseType.equals(sitUpName);
                    
                    List<BarEntry> entries = new ArrayList<>();
                    List<WorkoutSummary> reversed = new ArrayList<>(summaries);
                    Collections.reverse(reversed);
                    for (int i = 0; i < reversed.size(); i++) {
                        WorkoutSummary s = reversed.get(i);
                        float value;
                        if (isPlank) {
                            value = isPrimaryMetric ? (float) s.getDuration() : s.getStabilityScore();
                        } else if (isCardio) {
                            value = isPrimaryMetric ? (s.getCadence() > 0 ? 1f / s.getCadence() : 0f) : (float) s.getRepCount();
                        } else if (isSitUp) {
                            value = isPrimaryMetric ? (float) s.getRepCount() : s.getRangeOfMotion();
                        } else {
                            value = isPrimaryMetric ? (float) s.getRepCount() : s.getMaxPower();
                        }
                        entries.add(new BarEntry((float) i, value));
                    }
                    String label;
                    if (isPlank) {
                        label = isPrimaryMetric ? "Hold Time (s)" : "Stability (%)";
                    } else if (isCardio) {
                        label = isPrimaryMetric ? "Cadence (reps/s)" : "Total Reps";
                    } else if (isSitUp) {
                        label = isPrimaryMetric ? "Repetitions" : "Range of Motion (deg)";
                    } else {
                        label = isPrimaryMetric ? "Repetitions" : "Peak Power (G)";
                    }
                    BarDataSet dataSet = new BarDataSet(entries, label);
                    dataSet.setColor(android.graphics.Color.parseColor("#00C853"));
                    dataSet.setValueTextColor(android.graphics.Color.GRAY);
                    dataSet.setDrawValues(true);
                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.6f);
                    uiState.setBarData(barData);
                }, throwable -> Log.e(TAG, "Trend Error", throwable));
        compositeDisposable.add(disposable);
    }

    public void saveExerciseData(String[] exerciseData, String dateOfExercise, String pushUpLabel, String sitUpLabel, String skippingLabel, String walkingLabel, String flapJackLabel, String weightsLabel, String backsLabel, String mtClimbersLabel, String plankLabel, String legRaisesLabel, String legHipRaisesLabel) {
        String selected = activeSession.getSelectedExercise();
        if (selected == null || !activeSession.isRunning()) return;
        String time = LocalTime.now().toString();

        Object dataPoint = null;
        String s = selected.trim();
        if (s.equalsIgnoreCase(pushUpLabel.trim())) dataPoint = new PushUp(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(sitUpLabel.trim())) dataPoint = new SitUps(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(skippingLabel.trim())) dataPoint = new Skipping(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(walkingLabel.trim())) dataPoint = new Walking(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(flapJackLabel.trim())) dataPoint = new Flapjacks(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(weightsLabel.trim())) dataPoint = new Weights(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(backsLabel.trim())) dataPoint = new Backs(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(mtClimbersLabel.trim())) dataPoint = new MountainClimbers(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(plankLabel.trim())) dataPoint = new Plank(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(legRaisesLabel.trim())) dataPoint = new LegRaises(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);
        else if (s.equalsIgnoreCase(legHipRaisesLabel.trim())) dataPoint = new LegHipRaises(exerciseData[0], exerciseData[1], exerciseData[2], exerciseData[3], exerciseData[4], exerciseData[5], time, dateOfExercise);

        if (dataPoint != null) {
            activeSession.getAccumulatedDataPoints().add(dataPoint);
        } else {
            Log.w(TAG, "saveExerciseData: No label match for '" + selected + "'");
        }
    }

    public void saveDescriptionData(String description, Runnable onSuccess) {
        if (description.isEmpty()) return;
        Disposable disposable = insertExerciseDescription(new ExerciseDescription(LocalDateTime.now().toString(), description))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::run, throwable -> Log.e(TAG, "Error saving description", throwable));
        compositeDisposable.add(disposable);
    }

    public void fetchAllRecentSummaries(MainUiState uiState) {
        Disposable disposable = getAllRecentSummaries(20)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(uiState::setAllRecentSummaries, throwable -> Log.e(TAG, "Error fetching recent summaries", throwable));
        compositeDisposable.add(disposable);
    }

    public void fetchExerciseStats(MainUiState uiState) {
        Disposable disposable = getExerciseFrequencyStats()
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(uiState::setExerciseFrequencyStats, throwable -> Log.e(TAG, "Error fetching exercise stats", throwable));
        compositeDisposable.add(disposable) ;
    }

    public void fetchDrillDownSummaries(String exerciseType, MainUiState uiState) {
        Disposable disposable = getPastSummaries(exerciseType, 20)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(summaries -> {
                    uiState.setDrillDownSummaries(summaries);
                    uiState.setDrillDownExerciseName(exerciseType);
                }, throwable -> Log.e(TAG, "Error fetching drill down", throwable));
        compositeDisposable.add(disposable);
    }

    public void selectTimelineSummary(WorkoutSummary summary, MainUiState uiState) {
        if (summary == null) return;
        uiState.setCurrentWorkoutSummary(summary);

        Disposable disposable = getPastSummaries(summary.getExerciseType(), 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pastList -> {
                    List<WorkoutSummary> filteredPast = new ArrayList<>();
                    for (WorkoutSummary s : pastList) {
                        if (s.getId() != summary.getId()) {
                            filteredPast.add(s);
                        }
                    }
                    uiState.setPastSummaries(filteredPast);
                }, throwable -> Log.e(TAG, "Error selecting timeline summary", throwable));
        compositeDisposable.add(disposable);
    }

    @SuppressWarnings("unchecked")
    public void stopExerciseSession(Context context, MainUiState uiState, 
                                    String pushUpLabel, String sitUpLabel, String skippingLabel, 
                                    String walkingLabel, String flapJackLabel, String weightsLabel, 
                                    String backsLabel, String mtClimbersLabel, String plankLabel, String legRaisesLabel, String legHipRaisesLabel) {
        String selected = activeSession.getSelectedExercise();
        if (selected == null) return;

        int durationSec = activeSession.getSeconds();
        List<Float> magnitudesCopy = new ArrayList<>(activeSession.getMagnitudes());
        List<Float> gyroMagnitudesCopy = new ArrayList<>(activeSession.getGyroMagnitudes());
        List<Float> gyroXCopy = new ArrayList<>(activeSession.getGyroX());
        List<Float> gyroYCopy = new ArrayList<>(activeSession.getGyroY());
        List<Float> gyroZCopy = new ArrayList<>(activeSession.getGyroZ());
        List<Float> accYCopy = new ArrayList<>(activeSession.getAccY());
        List<Float> accZCopy = new ArrayList<>(activeSession.getAccZ());

        activeSession.setRunning(false);
        activeSession.setSelectedExercise(null);
        uiState.setSelectedExercise(null);
        uiState.setLiveRepCount(0);

        uiState.setTimerText(TimerUtils.formatElapsedTime(durationSec));

        String s = selected.trim();

        final Completable batchInsert;
        if (!activeSession.getAccumulatedDataPoints().isEmpty()) {
            List<Object> points = new ArrayList<>(activeSession.getAccumulatedDataPoints());
            Log.d(TAG, "stopExerciseSession: Saving " + points.size() + " data points for " + selected);
            if (s.equalsIgnoreCase(pushUpLabel.trim())) batchInsert = exerciseRepository.insertPushUpList((List<PushUp>)(List)points);
            else if (s.equalsIgnoreCase(sitUpLabel.trim())) batchInsert = exerciseRepository.insertSitUpsList((List<SitUps>)(List)points);
            else if (s.equalsIgnoreCase(skippingLabel.trim())) batchInsert = exerciseRepository.insertSkippingList((List<Skipping>)(List)points);
            else if (s.equalsIgnoreCase(walkingLabel.trim())) batchInsert = exerciseRepository.insertWalkingList((List<Walking>)(List)points);
            else if (s.equalsIgnoreCase(flapJackLabel.trim())) batchInsert = exerciseRepository.insertFlapJacksList((List<Flapjacks>)(List)points);
            else if (s.equalsIgnoreCase(weightsLabel.trim())) batchInsert = exerciseRepository.insertWeightsList((List<Weights>)(List)points);
            else if (s.equalsIgnoreCase(backsLabel.trim())) batchInsert = exerciseRepository.insertBacksList((List<Backs>)(List)points);
            else if (s.equalsIgnoreCase(mtClimbersLabel.trim())) batchInsert = exerciseRepository.insertMountainClimbersList((List<MountainClimbers>)(List)points);
            else if (s.equalsIgnoreCase(plankLabel.trim())) batchInsert = exerciseRepository.insertPlankList((List<Plank>)(List)points);
            else if (s.equalsIgnoreCase(legRaisesLabel.trim())) batchInsert = exerciseRepository.insertLegRaisesList((List<LegRaises>)(List)points);
            else if (s.equalsIgnoreCase(legHipRaisesLabel.trim())) batchInsert = exerciseRepository.insertLegHipRaisesList((List<LegHipRaises>)(List)points);
            else batchInsert = Completable.complete();
        } else {
            Log.w(TAG, "stopExerciseSession: No data points accumulated");
            batchInsert = Completable.complete();
        }

        Disposable disposable = DynamicAnalyzerUtils.processWorkoutSummaryReactive(
                selected,
                durationSec,
                magnitudesCopy,
                gyroMagnitudesCopy,
                gyroXCopy,
                gyroYCopy,
                gyroZCopy,
                accYCopy,
                accZCopy
        )
        .flatMap(summary -> batchInsert.andThen(getPastSummaries(selected, 6))
                .flatMap(past -> insertWorkoutSummary(summary)
                        .toSingleDefault(true)
                        .flatMap(ignored -> getAllRecentSummaries(20))
                        .map(recent -> new Pair<>(past, recent)))
                .map(pair -> new Pair<>(summary, pair)))
        .flatMap(pair -> getExerciseFrequencyStats().map(stats -> new Triple<>(pair.first(), pair.second().first(), new Pair<>(pair.second().second(), stats))))
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(triple -> {
            WorkoutSummary summary = triple.first();
            List<WorkoutSummary> past = triple.second();
            List<WorkoutSummary> recent = triple.third().first();
            List<ExerciseStats> stats = triple.third().second();

            uiState.setPastSummaries(past);
            uiState.setCurrentWorkoutSummary(summary);
            uiState.setAllRecentSummaries(recent);
            uiState.setExerciseFrequencyStats(stats);

                    float value;
                    if (summary.getExerciseType().equals(plankLabel)) {
                        value = (float) summary.getDuration();
                    } else if (summary.getExerciseType().equals(skippingLabel) || summary.getExerciseType().equals(mtClimbersLabel)) {
                        value = summary.getCadence() > 0 ? 1f / summary.getCadence() : 0f;
                    } else {
                        value = (float) summary.getRepCount();
                    }

                    String unit;
                    if (summary.getExerciseType().equals(plankLabel)) {
                        unit = "sec";
                    } else if (summary.getExerciseType().equals(skippingLabel) || summary.getExerciseType().equals(mtClimbersLabel)) {
                        unit = "reps/s";
                    } else {
                        unit = "reps";
                    }

                    String secondary;
                    if (summary.getExerciseType().equals(plankLabel)) {
                        secondary = String.format(Locale.getDefault(), "%.0f%% Stab", summary.getStabilityScore());
                    } else if (summary.getExerciseType().equals(sitUpLabel)) {
                        secondary = String.format(Locale.getDefault(), "%.2f° ROM", summary.getRangeOfMotion());
                    } else {
                        secondary = String.format(Locale.getDefault(), "%.1fG Max", summary.getMaxPower());
                    }

                    WidgetHelper.INSTANCE.saveLastExercise(context, summary.getExerciseType(), value, unit, secondary);
                }, throwable -> Log.e(TAG, "Finalize Error", throwable));
        compositeDisposable.add(disposable);
        activeSession.reset();
    }

    public void onStartTimer(MainUiState uiState) {
        if (!activeSession.isRunning()) {
            String currentExercise = activeSession.getSelectedExercise();
            activeSession.reset();
            activeSession.setSelectedExercise(currentExercise);
            uiState.setCurrentWorkoutSummary(null);
        }
        activeSession.setRunning(true);
    }

    public void onResetTimer(MainUiState uiState) {
        activeSession.reset();
        uiState.setSelectedExercise(null);
        uiState.setLiveRepCount(0);
        uiState.setTimerText(TimerUtils.formatElapsedTime(0));
    }

    public void startTimerLoop(MainUiState uiState) {
        if (timerRunnable != null) return;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeSession.isRunning()) {
                    activeSession.incrementSeconds();
                    uiState.setTimerText(TimerUtils.formatElapsedTime(activeSession.getSeconds()));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}
