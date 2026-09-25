package com.atom.bluetoothfitnessapplication.dsp;

import android.util.Log;

import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;
import com.atom.bluetoothfitnessapplication.utilities.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Advanced DSP Analytics Utility incorporating dynamic sample rate derivation,
 * dynamic minimum sample distance calculation, adaptive peak detection,
 * zero-phase Butterworth signal analysis, and normalized exercise classification.
 */
public class DynamicAnalyzerUtils {

    private static final String TAG = "DynamicAnalyzerUtils";

    /**
     * Unified, locale-resilient exercise category classifier.
     */
    public enum ExerciseCategory {
        SIT_UP,
        PUSH_UP,
        LEG_RAISES,   // Handles Leg Raises & Leg Hip Raises
        SKIPPING,
        BACKS,
        GENERIC;

        public static ExerciseCategory fromString(String label) {
            if (label == null || label.trim().isEmpty()) return GENERIC;
            String s = label.trim().toLowerCase(Locale.ENGLISH);

            if (s.contains("sit")) return SIT_UP;
            if (s.contains("push")) return PUSH_UP;
            if (s.contains("leg") || s.contains("raise") || s.contains("hip")) return LEG_RAISES;
            if (s.contains("skip") || s.contains("jump")) return SKIPPING;
            if (s.contains("back")) return BACKS;
            return GENERIC;
        }
    }

    /**
     * Dynamically derives session sample rate (in Hz) from total sample count and session duration.
     *
     * @param totalSamples Number of data samples collected during the session.
     * @param durationInSeconds Duration of the exercise session in seconds.
     * @return Calculated sample rate in Hz, or fallback (50.0f Hz) if inputs are invalid.
     */
    public static float calculateSampleRate(int totalSamples, float durationInSeconds) {
        if (totalSamples <= 0 || durationInSeconds <= 0) {
            return 50.0f; // Default fallback sample rate in Hz
        }
        return (float) totalSamples / durationInSeconds;
    }

    /**
     * Overloaded helper to calculate sample rate from a data list and session duration in seconds.
     */
    public static float calculateSampleRate(List<?> dataList, float durationInSeconds) {
        int length = (dataList != null) ? dataList.size() : 0;
        return calculateSampleRate(length, durationInSeconds);
    }

    /**
     * Dynamically calculates minimum sample distance (debounce window in samples) for peak detection
     * based on session sample rate (fs) and expected minimum physical repetition duration.
     *
     * @param sampleRate Session sample rate in Hz.
     * @param minRepDurationSec Minimum physical duration of a single repetition in seconds.
     * @param safetyFactor Safety fraction (e.g. 0.75) to prevent double-counting split crests.
     * @return Dynamic minimum sample distance in samples.
     */
    public static int calculateDynamicMinSampleDistance(float sampleRate, float minRepDurationSec, float safetyFactor) {
        if (sampleRate <= 0 || minRepDurationSec <= 0) {
            return 130; // Default fallback distance
        }
        int minDistance = Math.round(safetyFactor * sampleRate * minRepDurationSec);
        return Math.max(15, minDistance); // Absolute floor of 15 samples
    }

    /**
     * Dynamically calculates minimum repetition duration in seconds (minRepDurationSec)
     * from detected preliminary peak intervals, with a biological safety floor of 0.4s and default fallback.
     */
    public static float calculateDynamicRepDuration(List<Integer> peakIndices, float sampleRate, float defaultDurationSec) {
        if (peakIndices == null || peakIndices.size() < 2 || sampleRate <= 0) {
            return defaultDurationSec;
        }

        float sumIntervals = 0;
        for (int i = 1; i < peakIndices.size(); i++) {
            sumIntervals += (peakIndices.get(i) - peakIndices.get(i - 1));
        }
        float avgSampleInterval = sumIntervals / (peakIndices.size() - 1);
        float measuredRepDurationSec = avgSampleInterval / sampleRate;

        return Math.max(0.4f, 0.60f * measuredRepDurationSec);
    }

    /**
     * Dynamically adapts minimum sample distance based on a moving average of recent peak intervals.
     *
     * @param peakIndices List of detected peak sample indices.
     * @param defaultMinDistance Default fallback minimum sample distance.
     * @return Adaptive minimum sample distance for subsequent peak detection.
     */
    public static int calculateAdaptiveMinDistance(List<Integer> peakIndices, int defaultMinDistance) {
        if (peakIndices == null || peakIndices.size() < 2) {
            return defaultMinDistance;
        }
        float sumIntervals = 0;
        for (int i = 1; i < peakIndices.size(); i++) {
            sumIntervals += (peakIndices.get(i) - peakIndices.get(i - 1));
        }
        float avgInterval = sumIntervals / (peakIndices.size() - 1);
        int adaptiveDistance = Math.round(0.70f * avgInterval);
        return Math.max(20, adaptiveDistance);
    }

    /**
     * Calculates exercise stats (Reps, ROM) depending on the exercise type, using dynamic sample rate
     * and dynamic minimum sample distance without parameter clutter.
     */
    public static Pair<Integer, Float> calculateExerciseStats(
            String exercise,
            List<Float> accData,
            List<Float> gyroData,
            float sampleRate
    ) {
        if (accData == null || accData.isEmpty()) {
            return new Pair<>(0, 0f);
        }

        float samplingRate = sampleRate > 0 ? sampleRate : 71.61f;
        ExerciseCategory category = ExerciseCategory.fromString(exercise);

        switch (category) {
            case SIT_UP: {
                if (gyroData == null || gyroData.size() < 20) {
                    return new Pair<>(0, 0f);
                }
                float cutoffFreq = 2.5f;

                int minDistance = calculateDynamicMinSampleDistance(samplingRate, 1.2f, 0.75f);
                if (samplingRate > 150.0f) {
                    minDistance = 180;
                }

                float[] gyroXArr = new float[gyroData.size()];
                for (int i = 0; i < gyroData.size(); i++) {
                    gyroXArr[i] = gyroData.get(i);
                }

                float[] filteredGyroX = ButterworthFilter.filterSignal(gyroXArr, cutoffFreq, samplingRate);
                List<Integer> gyroPeaks = IMURepDetector.findPeaks(filteredGyroX, 15.0f, minDistance);
                int repCount = gyroPeaks.size();
                Log.d(TAG, "Sit-Up Counting: Reps=" + repCount + " (Threshold=15.0, minDistance=" + minDistance + ")");

                List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                        filteredGyroX,
                        gyroPeaks,
                        samplingRate,
                        true
                );

                float avgRom = 0f;
                if (!romValues.isEmpty()) {
                    float sum = 0f;
                    for (float r : romValues) sum += r;
                    avgRom = sum / romValues.size();
                } else if (repCount > 0) {
                    List<Float> filteredGyroXAbs = new ArrayList<>();
                    for (float f : filteredGyroX) filteredGyroXAbs.add(Math.abs(f));
                    avgRom = calculateRangeOfMotion(filteredGyroXAbs, repCount, samplingRate);
                }
                return new Pair<>(repCount, avgRom);
            }

            case PUSH_UP: {
                if (accData.size() < 20) {
                    return new Pair<>(0, 0f);
                }
                float cutoffFreq = 2.0f;

                int minDistance = calculateDynamicMinSampleDistance(samplingRate, 0.8f, 0.81f);
                if (samplingRate > 150.0f) {
                    minDistance = 130;
                }

                float[] accZArr = new float[accData.size()];
                for (int i = 0; i < accData.size(); i++) {
                    accZArr[i] = accData.get(i);
                }

                float[] filteredAccZ = ButterworthFilter.filterSignal(accZArr, cutoffFreq, samplingRate);

                float[] filteredGyroX = null;
                List<Integer> gyroXPeaks = new ArrayList<>();
                if (gyroData != null && gyroData.size() >= 20) {
                    float[] gyroXArr = new float[gyroData.size()];
                    for (int i = 0; i < gyroData.size(); i++) {
                        gyroXArr[i] = gyroData.get(i);
                    }
                    filteredGyroX = ButterworthFilter.filterSignal(gyroXArr, 2.5f, samplingRate);
                    gyroXPeaks = IMURepDetector.findPeaks(filteredGyroX, 15.0f, minDistance);
                }

                List<Integer> accZPeaks = IMURepDetector.findPeaks(filteredAccZ, 0.5f, minDistance);
                List<Integer> repPeaks = !gyroXPeaks.isEmpty() ? gyroXPeaks : accZPeaks;
                int repCount = repPeaks.size();
                Log.d(TAG, "Push-Up Counting: Reps=" + repCount + " (AccZPeaks=" + accZPeaks.size() + ", GyroPeaks=" + gyroXPeaks.size() + ", minDistance=" + minDistance + ")");

                float avgRom = 0f;
                if (filteredGyroX != null && !repPeaks.isEmpty()) {
                    List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                            filteredGyroX,
                            repPeaks,
                            samplingRate,
                            true
                    );

                    if (!romValues.isEmpty()) {
                        float sum = 0f;
                        for (float r : romValues) sum += r;
                        avgRom = sum / romValues.size();
                    } else if (repCount > 0) {
                        List<Float> filteredGyroXAbs = new ArrayList<>();
                        for (float f : filteredGyroX) filteredGyroXAbs.add(Math.abs(f));
                        avgRom = calculateRangeOfMotion(filteredGyroXAbs, repCount, samplingRate);
                    }
                }

                return new Pair<>(repCount, avgRom);
            }

            case LEG_RAISES:
            case BACKS: {
                if (accData == null || accData.size() < 20) {
                    return new Pair<>(0, 0f);
                }
                float cutoffFreq = 2.5f;

                float baselineDuration = 1.0f;
                int minDistance = calculateDynamicMinSampleDistance(samplingRate, baselineDuration, 0.75f);
                if (samplingRate > 150.0f) {
                    minDistance = 150;
                }

                float[] accZArr = new float[accData.size()];
                for (int i = 0; i < accData.size(); i++) {
                    accZArr[i] = accData.get(i);
                }

                float[] filteredAccZ = ButterworthFilter.filterSignal(accZArr, cutoffFreq, samplingRate);

                float[] filteredGyroX = null;
                List<Integer> gyroXPeaks = new ArrayList<>();
                if (gyroData != null && gyroData.size() >= 20) {
                    float[] gyroXArr = new float[gyroData.size()];
                    for (int i = 0; i < gyroData.size(); i++) {
                        gyroXArr[i] = gyroData.get(i);
                    }
                    filteredGyroX = ButterworthFilter.filterSignal(gyroXArr, cutoffFreq, samplingRate);
                    gyroXPeaks = IMURepDetector.findPeaks(filteredGyroX, 12.0f, minDistance);
                }

                List<Integer> accZPeaks = IMURepDetector.findPeaks(filteredAccZ, 0.4f, minDistance);
                List<Integer> repPeaks = !gyroXPeaks.isEmpty() ? gyroXPeaks : accZPeaks;

                if (repPeaks.size() >= 2) {
                    float dynamicRepDuration = calculateDynamicRepDuration(repPeaks, samplingRate, baselineDuration);
                    minDistance = calculateDynamicMinSampleDistance(samplingRate, dynamicRepDuration, 0.75f);
                    if (filteredGyroX != null) {
                        gyroXPeaks = IMURepDetector.findPeaks(filteredGyroX, 12.0f, minDistance);
                    }
                    accZPeaks = IMURepDetector.findPeaks(filteredAccZ, 0.4f, minDistance);
                    repPeaks = !gyroXPeaks.isEmpty() ? gyroXPeaks : accZPeaks;
                }

                int repCount = repPeaks.size();
                Log.d(TAG, category.name() + " Counting: Reps=" + repCount + " (AccZPeaks=" + accZPeaks.size() + ", GyroPeaks=" + gyroXPeaks.size() + ", minDistance=" + minDistance + ")");

                float avgRom = 0f;
                if (filteredGyroX != null && !repPeaks.isEmpty()) {
                    List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                            filteredGyroX,
                            repPeaks,
                            samplingRate,
                            true
                    );

                    if (!romValues.isEmpty()) {
                        float sum = 0f;
                        for (float r : romValues) sum += r;
                        avgRom = sum / romValues.size();
                    } else if (repCount > 0) {
                        List<Float> filteredGyroXAbs = new ArrayList<>();
                        for (float f : filteredGyroX) filteredGyroXAbs.add(Math.abs(f));
                        avgRom = calculateRangeOfMotion(filteredGyroXAbs, repCount, samplingRate);
                    }
                }

                return new Pair<>(repCount, avgRom);
            }

            case SKIPPING: {
                if (accData == null || accData.size() < 20) {
                    return new Pair<>(0, 0f);
                }
                float cutoffFreq = 3.0f;

                float baselineDuration = 0.35f;
                int minDistance = calculateDynamicMinSampleDistance(samplingRate, baselineDuration, 0.75f);
                if (samplingRate > 150.0f) {
                    minDistance = 50;
                }

                float[] accArr = new float[accData.size()];
                for (int i = 0; i < accData.size(); i++) {
                    accArr[i] = accData.get(i);
                }

                float[] filteredAcc = ButterworthFilter.filterSignal(accArr, cutoffFreq, samplingRate);

                float[] filteredGyroMag = null;
                List<Integer> gyroMagPeaks = new ArrayList<>();
                if (gyroData != null && gyroData.size() >= 20) {
                    float[] gyroMagArr = new float[gyroData.size()];
                    for (int i = 0; i < gyroData.size(); i++) {
                        gyroMagArr[i] = gyroData.get(i);
                    }
                    filteredGyroMag = ButterworthFilter.filterSignal(gyroMagArr, cutoffFreq, samplingRate);
                    gyroMagPeaks = IMURepDetector.findPeaks(filteredGyroMag, 12.0f, minDistance);
                }

                List<Integer> accPeaks = IMURepDetector.findPeaks(filteredAcc, 1.2f, minDistance);
                List<Integer> repPeaks = !gyroMagPeaks.isEmpty() ? gyroMagPeaks : accPeaks;

                if (repPeaks.size() >= 2) {
                    float dynamicRepDuration = calculateDynamicRepDuration(repPeaks, samplingRate, baselineDuration);
                    minDistance = calculateDynamicMinSampleDistance(samplingRate, dynamicRepDuration, 0.75f);
                    if (filteredGyroMag != null) {
                        gyroMagPeaks = IMURepDetector.findPeaks(filteredGyroMag, 12.0f, minDistance);
                    }
                    accPeaks = IMURepDetector.findPeaks(filteredAcc, 1.2f, minDistance);
                    repPeaks = !gyroMagPeaks.isEmpty() ? gyroMagPeaks : accPeaks;
                }

                int repCount = repPeaks.size();
                Log.d(TAG, "Skipping Counting: Reps=" + repCount + " (AccPeaks=" + accPeaks.size() + ", GyroPeaks=" + gyroMagPeaks.size() + ", adaptiveMinDistance=" + minDistance + ")");

                float avgRom = 0f;
                if (filteredGyroMag != null && !repPeaks.isEmpty()) {
                    List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                            filteredGyroMag,
                            repPeaks,
                            samplingRate,
                            true
                    );

                    if (!romValues.isEmpty()) {
                        float sum = 0f;
                        for (float r : romValues) sum += r;
                        avgRom = sum / romValues.size();
                    } else if (repCount > 0) {
                        List<Float> filteredGyroMagAbs = new ArrayList<>();
                        for (float f : filteredGyroMag) filteredGyroMagAbs.add(Math.abs(f));
                        avgRom = calculateRangeOfMotion(filteredGyroMagAbs, repCount, samplingRate);
                    }
                }

                return new Pair<>(repCount, avgRom);
            }

            default:
                int reps = countReps(accData, gyroData);
                float rom = (gyroData != null) ? calculateRangeOfMotion(gyroData, reps, samplingRate) : 0f;
                return new Pair<>(reps, rom);
        }
    }

    /**
     * Reactively calculates exercise stats (Reps, ROM) on Schedulers.computation().
     */
    public static Single<Pair<Integer, Float>> calculateExerciseStatsReactive(
            String exercise,
            List<Float> accData,
            List<Float> gyroData,
            float sampleRate
    ) {
        return Single.fromCallable(() -> calculateExerciseStats(exercise, accData, gyroData, sampleRate))
                .subscribeOn(Schedulers.computation());
    }

    /**
     * Reactively processes complete workout session summary on Schedulers.computation().
     * Offloads CPU-intensive DSP filtering, peak detection, and numerical integration off the main UI thread.
     */
    public static Single<WorkoutSummary> processWorkoutSummaryReactive(
            String exerciseType,
            int durationSec,
            List<Float> magnitudes,
            List<Float> gyroMagnitudes,
            List<Float> gyroX,
            List<Float> gyroY,
            List<Float> gyroZ,
            List<Float> accY,
            List<Float> accZ
    ) {
        return Single.fromCallable(() -> {
            float sampleRate = calculateSampleRate(accY, (float) durationSec);

            int reps;
            float rom = 0f;
            ExerciseCategory category = ExerciseCategory.fromString(exerciseType);

            switch (category) {
                case SIT_UP: {
                    Pair<Integer, Float> stats = calculateExerciseStats(exerciseType, accY, gyroX, sampleRate);
                    reps = stats.first();
                    rom = stats.second();
                    break;
                }
                case PUSH_UP:
                case LEG_RAISES:
                case BACKS: {
                    Pair<Integer, Float> stats = calculateExerciseStats(exerciseType, accZ, gyroX, sampleRate);
                    reps = stats.first();
                    rom = stats.second();
                    break;
                }
                case SKIPPING: {
                    Pair<Integer, Float> stats = calculateExerciseStats(exerciseType, magnitudes, gyroMagnitudes, sampleRate);
                    reps = stats.first();
                    rom = stats.second();
                    break;
                }
                default: {
                    reps = countReps(magnitudes, gyroMagnitudes);
                    rom = calculateRangeOfMotion(gyroMagnitudes, reps, sampleRate);
                    break;
                }
            }

            float mean = calculateMean(magnitudes);
            float max = calculateMax(magnitudes);
            float stdDev = calculateStdDev(magnitudes, mean);
            float cadence = reps > 0 ? (float) durationSec / reps : 0f;
            float stability = calculateStabilityScore(gyroMagnitudes);
            float symmetry = calculateSymmetryScore(gyroX, gyroY, gyroZ);

            WorkoutSummary summary = new WorkoutSummary(exerciseType, reps, max, mean, stdDev, cadence, durationSec, LocalDateTime.now().toString(), stability, symmetry);
            summary.setRangeOfMotion(rom);
            return summary;
        }).subscribeOn(Schedulers.computation());
    }

    public static int countReps(List<Float> accMagnitudes, List<Float> gyroMagnitudes) {
        if (accMagnitudes.size() < 15 || gyroMagnitudes.size() < 15) return 0;

        float accThreshold = 1.1f;
        float gyroThreshold = 0.4f;

        int count = 0;
        int lastRepIndex = -20;
        boolean motionDetected = false;

        for (int i = 1; i < accMagnitudes.size() - 1; i++) {
            float aVal = accMagnitudes.get(i);
            float gVal = gyroMagnitudes.get(i);

            if (gVal > gyroThreshold) {
                motionDetected = true;
            }

            if (motionDetected) {
                if (aVal > accThreshold && aVal > accMagnitudes.get(i - 1) && aVal > accMagnitudes.get(i + 1)) {
                    if (i - lastRepIndex > 15) {
                        count++;
                        lastRepIndex = i;
                        motionDetected = false;
                    }
                }
            }

            if (gVal < 0.1f) {
                motionDetected = false;
            }
        }
        return count;
    }

    public static float calculateMean(List<Float> data) {
        if (data == null || data.isEmpty()) return 0;
        float sum = 0;
        for (float f : data) sum += f;
        return sum / data.size();
    }

    public static float calculateMax(List<Float> data) {
        if (data == null || data.isEmpty()) return 0;
        return Collections.max(data);
    }

    public static float calculateStdDev(List<Float> data, float mean) {
        if (data == null || data.isEmpty()) return 0;
        float sum = 0;
        for (float f : data) {
            sum += Math.pow(f - mean, 2);
        }
        return (float) Math.sqrt(sum / data.size());
    }

    public static List<Float> calculateMagnitudes(List<Float> x, List<Float> y, List<Float> z) {
        List<Float> magnitudes = new ArrayList<>();
        if (x == null || y == null || z == null) return magnitudes;
        for (int i = 0; i < x.size(); i++) {
            magnitudes.add((float) Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2)));
        }
        return magnitudes;
    }

    public static float calculateStabilityScore(List<Float> gyroMagnitudes) {
        if (gyroMagnitudes == null || gyroMagnitudes.isEmpty()) return 0;

        float mean = calculateMean(gyroMagnitudes);
        float variance = 0;
        for (float f : gyroMagnitudes) variance += Math.pow(f - mean, 2);
        variance /= gyroMagnitudes.size();

        double k = 8.0;
        float score = (float) (Math.exp(-k * variance) * 100f);

        return Math.max(0f, Math.min(100f, score));
    }

    public static float calculateSymmetryScore(List<Float> gx, List<Float> gy, List<Float> gz) {
        if (gx == null || gy == null || gz == null || gx.isEmpty() || gy.isEmpty() || gz.isEmpty()) return 0;

        float driftX = 0, driftY = 0, driftZ = 0;
        float totalAbsoluteMotion = 0;

        for (int i = 0; i < gx.size(); i++) {
            float x = gx.get(i);
            float y = gy.get(i);
            float z = gz.get(i);

            driftX += x;
            driftY += y;
            driftZ += z;

            totalAbsoluteMotion += (Math.abs(x) + Math.abs(y) + Math.abs(z));
        }

        if (totalAbsoluteMotion == 0) return 100f;

        float totalAbsDrift = Math.abs(driftX) + Math.abs(driftY) + Math.abs(driftZ);
        float biasRatio = totalAbsDrift / totalAbsoluteMotion;
        float score = (1.0f - (biasRatio * 2.0f)) * 100f;

        return Math.max(0f, Math.min(100f, score));
    }

    public static float calculateRangeOfMotion(List<Float> gyroMagnitudes, int repCount, float sampleRate) {
        if (gyroMagnitudes == null || gyroMagnitudes.isEmpty() || repCount <= 0 || sampleRate <= 0) return 0f;

        float dt = 1.0f / sampleRate;
        float totalActiveRotationRadPerSec = 0f;
        int activeSamples = 0;

        for (float f : gyroMagnitudes) {
            totalActiveRotationRadPerSec += f;
            activeSamples++;
        }

        if (activeSamples == 0) return 0f;

        float totalDegrees = totalActiveRotationRadPerSec * dt;
        return totalDegrees / (repCount * 2);
    }

    public static float calculateRangeOfMotion(List<Float> gyroMagnitudes, int repCount) {
        return calculateRangeOfMotion(gyroMagnitudes, repCount, 10.0f);
    }

    public static String getImprovementMessage(float current, List<WorkoutSummary> past, String metric) {
        if (past == null || past.isEmpty()) return "";

        float sum = 0;
        for (WorkoutSummary s : past) {
            switch (metric) {
                case "reps": sum += s.getRepCount(); break;
                case "power": sum += s.getMaxPower(); break;
                case "consistency": sum += s.getConsistency(); break;
                case "stability": sum += s.getStabilityScore(); break;
                case "symmetry": sum += s.getSymmetryScore(); break;
                case "duration": sum += s.getDuration(); break;
                case "rom": sum += s.getRangeOfMotion(); break;
            }
        }
        float avg = sum / past.size();
        if (avg == 0) return "";
        float delta = ((current - avg) / avg) * 100;

        if (Math.abs(delta) < 1) return "No change";
        return String.format(Locale.ENGLISH, "%s %.1f%% vs last %d", delta > 0 ? "↑" : "↓", Math.abs(delta), past.size());
    }
}
